package dev.slne.surf.jumppad.listeners

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.expireAfterWrite
import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.service.JumpPadBoostService
import dev.slne.surf.jumppad.pad.service.JumpPadService
import dev.slne.surf.jumppad.particles.AnimationService
import dev.slne.surf.jumppad.sounds.SoundService
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.seconds

/**
 * Handles player movement over jump pads.
 *
 * The listener only reacts to actual block changes to avoid running jump pad
 * checks when the player only rotates their camera. Pad lookup is delegated to
 * [JumpPadService], which should use a block-based index for fast access.
 */
object PlayerMoveListener : Listener {
    private const val COOLDOWN_SECONDS = 3
    private const val BLOCK_CENTER_OFFSET = 0.5
    private const val DEFAULT_STATIC_TARGET_HEIGHT = 5.0
    private const val MIN_ARC_PEAK_HEIGHT = 3.0
    private const val ARC_DISTANCE_DIVISOR = 3.0
    private const val BLOCK_ALIGNMENT_EPSILON = 1e-6
    private const val MINECRAFT_GRAVITY = 0.08

    private val cooldowns = Caffeine.newBuilder()
        .expireAfterWrite(COOLDOWN_SECONDS.seconds)
        .build<UUID, Unit>()

    /**
     * Updates the cooldown for a player.
     *
     * @param playerId the unique id of the player
     * @return `true` if the player can use a jump pad, otherwise `false`
     */
    private fun updateCooldown(playerId: UUID): Boolean {
        return cooldowns.asMap().putIfAbsent(playerId, Unit) == null
    }

    /**
     * Handles player movement and starts a jump pad boost when the player enters
     * a registered jump pad area.
     *
     * @param event the player movement event
     */
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!event.hasExplicitlyChangedBlock()) return

        val player = event.player
        if (player.gameMode == GameMode.SPECTATOR) return
        if (JumpPadBoostService.isBoosting(player)) return

        val pad = JumpPadService.getPadAt(event.to) ?: return
        if (!updateCooldown(player.uniqueId)) return

        handleJumpPad(player, pad)
    }

    /**
     * Applies the behavior of the given jump pad to the player.
     *
     * @param player the player using the jump pad
     * @param pad the jump pad that was triggered
     */
    private fun handleJumpPad(player: Player, pad: JumpPad) {
        if (pad.type == JumpPadType.VERTICAL) {
            launchVertically(player, pad)
            return
        }

        val startLocation = pad.origin.clone()
            .add(BLOCK_CENTER_OFFSET, 0.0, BLOCK_CENTER_OFFSET)

        val targetLocation = calculateTargetLocation(player, pad).centerXZIfBlockAligned()
        val actualDistance = startLocation.distance(targetLocation)
        val peakHeight = (actualDistance / ARC_DISTANCE_DIVISOR).coerceAtLeast(MIN_ARC_PEAK_HEIGHT)

        JumpPadBoostService.startBoost(
            player = player,
            start = startLocation,
            target = targetLocation,
            peakHeight = peakHeight,
            padType = pad.type
        )

        playFeedback(player, pad.type)
    }

    /**
     * Launches the player vertically using the configured pad distance as height.
     *
     * @param player the player to launch
     * @param pad the vertical jump pad
     */
    private fun launchVertically(player: Player, pad: JumpPad) {
        val height = pad.distance.toDouble()
        val velocity = Vector(0.0, sqrt(2.0 * MINECRAFT_GRAVITY * height), 0.0)

        player.velocity = velocity

        playFeedback(player, pad.type)
    }

    /**
     * Calculates the target location for a non-vertical jump pad.
     *
     * Static pads use their configured target location if present. Directional
     * pads use their type-specific direction and configured distance.
     *
     * @param player the player using the jump pad
     * @param pad the jump pad to calculate the target for
     * @return the calculated target location
     */
    private fun calculateTargetLocation(player: Player, pad: JumpPad): Location {
        return when (pad.type) {
            JumpPadType.STATIC -> {
                pad.targetLocation ?: pad.origin.clone().add(0.0, DEFAULT_STATIC_TARGET_HEIGHT, 0.0)
            }

            else -> {
                val direction = pad.type.getDirection(player)
                val offset = direction.mul(pad.distance.toDouble())

                pad.origin.clone().add(offset.x(), offset.y(), offset.z())
            }
        }
    }

    /**
     * Plays the visual and audio feedback for a used jump pad.
     *
     * @param player the player who used the jump pad
     * @param type the jump pad type defining the feedback effects
     */
    private fun playFeedback(player: Player, type: JumpPadType) {
        AnimationService.playStartAnimation(player, type)
        SoundService.playSound(player, type)
    }

    /**
     * Centers the X and Z coordinates if they are aligned to block coordinates.
     *
     * This keeps explicitly block-aligned target locations centered on the block
     * while preserving custom decimal coordinates.
     *
     * @param eps the floating-point tolerance used for block alignment checks
     * @return a cloned and optionally centered location
     */
    private fun Location.centerXZIfBlockAligned(eps: Double = BLOCK_ALIGNMENT_EPSILON): Location {
        val loc = clone()

        val blockX = loc.blockX.toDouble()
        val blockZ = loc.blockZ.toDouble()

        if (abs(loc.x - blockX) < eps) {
            loc.x = blockX + BLOCK_CENTER_OFFSET
        }

        if (abs(loc.z - blockZ) < eps) {
            loc.z = blockZ + BLOCK_CENTER_OFFSET
        }

        return loc
    }
}