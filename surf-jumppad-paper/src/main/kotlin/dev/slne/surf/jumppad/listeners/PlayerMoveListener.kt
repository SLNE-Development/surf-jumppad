package dev.slne.surf.jumppad.listeners

import dev.slne.surf.jumppad.core.client.pad.JumpPad
import dev.slne.surf.jumppad.core.client.pad.JumpPadType
import dev.slne.surf.jumppad.core.client.pad.boost.JumpPadLaunch
import dev.slne.surf.jumppad.core.client.pad.effect.playJumpPadSound
import dev.slne.surf.jumppad.core.client.pad.service.JumpPadCooldownService
import dev.slne.surf.jumppad.core.client.pad.service.JumpPadService
import dev.slne.surf.jumppad.pad.service.JumpPadBoostService
import dev.slne.surf.jumppad.pad.toJumpPadPosition
import dev.slne.surf.jumppad.particles.AnimationService
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.util.Vector
import org.spongepowered.math.vector.Vector3d

/**
 * Handles player movement over jump pads.
 *
 * The listener only reacts to actual block changes to avoid running jump pad
 * checks when the player only rotates their camera. Pad lookup is delegated to
 * [JumpPadService], which should use a block-based index for fast access.
 */
object PlayerMoveListener : Listener {

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

        val to = event.to.toJumpPadPosition() ?: return
        val pad = JumpPadService.getPadAt(to) ?: return
        if (!JumpPadCooldownService.tryUse(player.uniqueId)) return

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

        val startPosition = JumpPadLaunch.startPosition(pad)

        val playerDirection = player.location.direction
        val targetPosition = JumpPadLaunch.centerXZIfBlockAligned(
            JumpPadLaunch.targetPosition(
                pad,
                Vector3d(playerDirection.x, playerDirection.y, playerDirection.z)
            )
        )

        val actualDistance = startPosition.distance(targetPosition)

        JumpPadBoostService.startBoost(
            player = player,
            start = startPosition,
            target = targetPosition,
            peakHeight = JumpPadLaunch.peakHeight(actualDistance),
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
        val velocity = Vector(0.0, JumpPadLaunch.verticalLaunchVelocity(height), 0.0)

        player.velocity = velocity

        playFeedback(player, pad.type)
    }

    /**
     * Plays the visual and audio feedback for a used jump pad.
     *
     * @param player the player who used the jump pad
     * @param type the jump pad type defining the feedback effects
     */
    private fun playFeedback(player: Player, type: JumpPadType) {
        AnimationService.playStartAnimation(player, type)
        player.playJumpPadSound(type)
    }
}
