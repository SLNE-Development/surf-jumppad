package dev.slne.surf.jumppad.pad.service

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.ticks
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.particles.AnimationService
import dev.slne.surf.jumppad.plugin
import kotlinx.coroutines.*
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.NumberConversions
import org.bukkit.util.Vector
import org.spongepowered.math.vector.Vector3d
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

/**
 * Handles active jump pad boost movements.
 *
 * Each player can only have one active boost at a time. Starting a new boost
 * cancels the previous one and replaces it with a new coroutine running on the
 * player's Folia entity dispatcher.
 */
object JumpPadBoostService {
    private const val MIN_HORIZONTAL_DISTANCE = 0.1
    private const val MIN_PEAK_HEIGHT = 1.0
    private const val PEAK_REDUCTION_FACTOR = 0.8
    private const val VELOCITY_FACTOR = 0.25
    private const val PLAYER_COLLISION_HEIGHT = 1.8
    private const val FINAL_SNAP_DISTANCE_SQUARED = 1.0

    private val activeBoosts = ConcurrentHashMap<UUID, Job>()

    /**
     * Checks whether the given player currently has an active boost.
     *
     * @param player the player to check
     * @return `true` if the player is currently boosted, otherwise `false`
     */
    fun isBoosting(player: Player): Boolean {
        return activeBoosts[player.uniqueId]?.isActive == true
    }

    /**
     * Starts a new boost for the given player.
     *
     * The boost follows a curved trajectory from [start] to [target]. The curve
     * height is controlled by [peakHeight] and may be reduced automatically if
     * the initial trajectory would collide with solid blocks.
     *
     * @param player the player to boost
     * @param start the start location of the boost
     * @param target the target location of the boost
     * @param peakHeight the initial peak height of the boost arc
     * @param padType the jump pad type used for boost particles
     */
    fun startBoost(
        player: Player,
        start: Location,
        target: Location,
        peakHeight: Double,
        padType: JumpPadType
    ) {
        val world = start.world ?: return
        if (target.world?.uid != world.uid) return
        if (player.gameMode == GameMode.SPECTATOR) return

        val playerUuid = player.uniqueId
        activeBoosts[playerUuid]?.cancel("New boost started")

        val startVector = start.toVector3d()
        val targetVector = target.toVector3d()
        val diff = targetVector.sub(startVector)

        val horizontalDiff = Vector3d(diff.x(), 0.0, diff.z())
        val totalDistance = horizontalDiff.length()

        if (totalDistance < MIN_HORIZONTAL_DISTANCE) return

        val direction = horizontalDiff.normalize()
        val yOffset = diff.y()

        var job: Job? = null
        job = plugin.launch(plugin.entityDispatcher(player), start = CoroutineStart.UNDISPATCHED) {
            try {
                runBoost(
                    player = player,
                    world = world,
                    start = startVector,
                    target = targetVector,
                    direction = direction,
                    targetDistance = totalDistance,
                    yOffset = yOffset,
                    initialPeak = peakHeight,
                    padType = padType
                )
            } finally {
                job?.let { activeBoosts.remove(playerUuid, it) }
            }
        }

        activeBoosts[playerUuid] = job
        job.start()
    }

    /**
     * Stops the active boost of the given player, if one exists.
     *
     * @param player the player whose boost should be stopped
     */
    fun stopBoost(player: Player) {
        activeBoosts.remove(player.uniqueId)?.cancel("Boost stopped")
    }

    private suspend fun runBoost(
        player: Player,
        world: World,
        start: Vector3d,
        target: Vector3d,
        direction: Vector3d,
        targetDistance: Double,
        yOffset: Double,
        initialPeak: Double,
        padType: JumpPadType
    ) {
        var peak = initialPeak

        while (peak > MIN_PEAK_HEIGHT) {
            if (collides(world, start, direction, targetDistance, yOffset, peak)) {
                peak *= PEAK_REDUCTION_FACTOR
            } else {
                break
            }
        }

        val totalTicks = (targetDistance * 1.5 + 15).toInt().coerceIn(20, 100)

        var tick = 0
        while (
            currentCoroutineContext().isActive &&
            tick < totalTicks &&
            player.isOnline &&
            !player.isDead &&
            player.gameMode != GameMode.SPECTATOR
        ) {
            val progress = (tick + 1).toDouble() / totalTicks
            val desiredPosition = calculateTrajectoryPosition(
                start = start,
                direction = direction,
                distance = targetDistance,
                yOffset = yOffset,
                peak = peak,
                progress = progress
            )

            val playerLocation = player.location
            val velocity = Vector(
                (desiredPosition.x() - playerLocation.x) * VELOCITY_FACTOR,
                (desiredPosition.y() - playerLocation.y) * VELOCITY_FACTOR,
                (desiredPosition.z() - playerLocation.z) * VELOCITY_FACTOR
            )

            player.velocity = velocity
            player.fallDistance = 0f

            AnimationService.playBoostAnimation(player, padType, tick)

            tick++
            delay(1.ticks.milliseconds)
        }

        if (player.isOnline && !player.isDead && player.gameMode != GameMode.SPECTATOR) {
            val playerLocation = player.location
            val remaining = Vector(
                target.x() - playerLocation.x,
                target.y() - playerLocation.y,
                target.z() - playerLocation.z
            )

            if (remaining.lengthSquared() < FINAL_SNAP_DISTANCE_SQUARED) {
                player.velocity = remaining
                player.fallDistance = 0f
            }
        }
    }

    private fun collides(
        world: World,
        start: Vector3d,
        direction: Vector3d,
        distance: Double,
        yOffset: Double,
        peak: Double
    ): Boolean {
        val steps = (distance * 2).toInt().coerceAtLeast(5)

        for (i in 1..steps) {
            val progress = i.toDouble() / steps
            val footPosition = calculateTrajectoryPosition(
                start = start,
                direction = direction,
                distance = distance,
                yOffset = yOffset,
                peak = peak,
                progress = progress
            )

            val headPosition = footPosition.add(0.0, PLAYER_COLLISION_HEIGHT, 0.0)

            if (world.isSolidBlockAt(footPosition) || world.isSolidBlockAt(headPosition)) {
                return true
            }
        }

        return false
    }

    private fun calculateTrajectoryPosition(
        start: Vector3d,
        direction: Vector3d,
        distance: Double,
        yOffset: Double,
        peak: Double,
        progress: Double
    ): Vector3d {
        val horizontalPosition = distance * progress
        val verticalPosition = sin(progress * PI) * peak + progress * yOffset

        return Vector3d(
            start.x() + direction.x() * horizontalPosition,
            start.y() + verticalPosition,
            start.z() + direction.z() * horizontalPosition
        )
    }

    private fun Location.toVector3d(): Vector3d {
        return Vector3d(x, y, z)
    }

    private fun World.isSolidBlockAt(position: Vector3d): Boolean {
        return getBlockAt(
            NumberConversions.floor(position.x()),
            NumberConversions.floor(position.y()),
            NumberConversions.floor(position.z())
        ).type.isSolid
    }
}