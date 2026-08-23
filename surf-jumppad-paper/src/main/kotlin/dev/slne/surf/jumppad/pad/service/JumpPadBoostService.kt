package dev.slne.surf.jumppad.pad.service

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.slne.surf.jumppad.core.client.pad.JumpPadPosition
import dev.slne.surf.jumppad.core.client.pad.JumpPadType
import dev.slne.surf.jumppad.core.client.pad.boost.JumpPadBoostPath
import dev.slne.surf.jumppad.core.client.pad.boost.JumpPadBoostTarget
import dev.slne.surf.jumppad.core.client.pad.boost.runBoost
import dev.slne.surf.jumppad.pad.world
import dev.slne.surf.jumppad.particles.AnimationService
import dev.slne.surf.jumppad.plugin
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import org.bukkit.GameMode
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Handles active jump pad boost movements.
 *
 * Each player can only have one active boost at a time. Starting a new boost
 * cancels the previous one and replaces it with a new coroutine running on the
 * player's Folia entity dispatcher.
 */
object JumpPadBoostService {
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
        start: JumpPadPosition,
        target: JumpPadPosition,
        peakHeight: Double,
        padType: JumpPadType
    ) {
        val world = start.world() ?: return
        if (target.worldKey != start.worldKey) return
        if (player.gameMode == GameMode.SPECTATOR) return

        val path = JumpPadBoostPath.between(start, target) ?: return

        val playerUuid = player.uniqueId
        activeBoosts[playerUuid]?.cancel("New boost started")

        val boostTarget = PlayerBoostTarget(player, world)

        val job = plugin.launch(
            plugin.entityDispatcher(player),
            start = CoroutineStart.UNDISPATCHED
        ) {
            runBoost(
                target = boostTarget,
                path = path,
                targetPosition = target,
                initialPeak = peakHeight,
                padType = padType
            )
        }

        activeBoosts[playerUuid] = job

        job.invokeOnCompletion { activeBoosts.remove(playerUuid, job) }
    }

    /**
     * Stops the active boost of the given player, if one exists.
     *
     * @param player the player whose boost should be stopped
     */
    fun stopBoost(player: Player) {
        val playerUuid = player.uniqueId

        if (!activeBoosts.containsKey(playerUuid)) return
        activeBoosts.remove(playerUuid)?.cancel("Boost stopped")
    }

    private class PlayerBoostTarget(
        private val player: Player,
        private val world: World
    ) : JumpPadBoostTarget {
        override val positionX get() = player.x
        override val positionY get() = player.y
        override val positionZ get() = player.z

        override val isBoostable
            get() = player.isOnline && !player.isDead && player.gameMode != GameMode.SPECTATOR

        override fun applyVelocity(x: Double, y: Double, z: Double) {
            player.velocity = Vector(x, y, z)
        }

        override fun resetFallDistance() {
            player.fallDistance = 0f
        }

        override fun playBoostAnimation(type: JumpPadType, tick: Int) {
            AnimationService.playBoostAnimation(player, type, tick)
        }

        override fun isSolidBlockAt(x: Int, y: Int, z: Int) = world.getType(x, y, z).isSolid
    }
}
