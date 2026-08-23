package dev.slne.surf.jumppad.minestom.listener

import com.google.inject.Singleton
import dev.slne.minestom.lobby.api.event.EventRegistrar
import dev.slne.minestom.lobby.api.extension.addListener
import dev.slne.minestom.lobby.api.instance.worldKey
import dev.slne.minestom.lobby.api.player.requireLobbyPlayer
import dev.slne.surf.jumppad.core.client.message.JumpPadMessages
import dev.slne.surf.jumppad.core.client.pad.JumpPad
import dev.slne.surf.jumppad.core.client.pad.JumpPadType
import dev.slne.surf.jumppad.core.client.pad.boost.JumpPadLaunch
import dev.slne.surf.jumppad.core.client.pad.effect.playJumpPadSound
import dev.slne.surf.jumppad.core.client.pad.service.JumpPadCooldownService
import dev.slne.surf.jumppad.core.client.pad.service.JumpPadService
import dev.slne.surf.jumppad.core.client.permission.JumpPadPermissions
import dev.slne.surf.jumppad.minestom.dialog.view.JumpPadInfoDialog
import dev.slne.surf.jumppad.minestom.pad.service.JumpPadBoostService
import dev.slne.surf.jumppad.minestom.particles.AnimationService
import net.minestom.server.ServerFlag
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.event.Event
import net.minestom.server.event.EventNode
import net.minestom.server.event.entity.EntityTeleportEvent
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.event.player.PlayerMoveEvent
import org.spongepowered.math.vector.Vector3d

/**
 * Handles everything players do to jump pads: stepping onto them, touching them, and leaving them
 * behind.
 */
@Singleton
class JumpPadListener : EventRegistrar {

    override fun register(node: EventNode<Event>) {
        node.addListener(::onPlayerMove)
        node.addListener(::onBlockInteract)
        node.addListener(::onEntityTeleport)
        node.addListener<PlayerDisconnectEvent> { event -> JumpPadBoostService.stopBoost(event.player) }
    }

    private fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        val to = event.newPosition
        if (!hasChangedBlock(player.position, to)) return
        if (player.gameMode == GameMode.SPECTATOR) return
        if (JumpPadBoostService.isBoosting(player)) return

        val worldKey = (player.instance ?: return).worldKey ?: return

        val pad = JumpPadService.getPadAt(worldKey, to.blockX(), to.blockY(), to.blockZ()) ?: return
        if (!JumpPadCooldownService.tryUse(player.uuid)) return

        handleJumpPad(player, pad)
    }

    private fun onBlockInteract(event: PlayerBlockInteractEvent) {
        val worldKey = event.instance.worldKey ?: return
        val block = event.blockPosition

        val pad = JumpPadService.getPadAt(worldKey, block.blockX(), block.blockY(), block.blockZ())
            ?: JumpPadService.getPadAt(
                worldKey,
                block.blockX(),
                block.blockY() + 1,
                block.blockZ()
            )
            ?: return

        val player = event.player
        if (!player.requireLobbyPlayer()
                .hasPermission(JumpPadPermissions.COMMAND_JUMP_PAD_GENERIC)
        ) {
            return
        }

        player.sendMessage(JumpPadMessages.padAtBlock(JumpPadInfoDialog.showDialog(pad)))

        event.isCancelled = true
    }

    private fun onEntityTeleport(event: EntityTeleportEvent) {
        val player = event.entity as? Player ?: return

        JumpPadBoostService.stopBoost(player)
    }

    private fun handleJumpPad(player: Player, pad: JumpPad) {
        if (pad.type == JumpPadType.VERTICAL) {
            launchVertically(player, pad)
            return
        }

        val startPosition = JumpPadLaunch.startPosition(pad)

        val playerDirection = player.position.direction()
        val targetPosition = JumpPadLaunch.centerXZIfBlockAligned(
            JumpPadLaunch.targetPosition(
                pad,
                Vector3d(playerDirection.x(), playerDirection.y(), playerDirection.z())
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

    private fun launchVertically(player: Player, pad: JumpPad) {
        val height = pad.distance.toDouble()

        player.velocity = Vec(
            0.0,
            JumpPadLaunch.verticalLaunchVelocity(height) * ServerFlag.SERVER_TICKS_PER_SECOND,
            0.0
        )

        playFeedback(player, pad.type)
    }

    private fun playFeedback(player: Player, type: JumpPadType) {
        AnimationService.playStartAnimation(player, type)
        player.playJumpPadSound(type)
    }

    private fun hasChangedBlock(from: Point, to: Point) =
        from.blockX() != to.blockX() || from.blockY() != to.blockY() || from.blockZ() != to.blockZ()
}
