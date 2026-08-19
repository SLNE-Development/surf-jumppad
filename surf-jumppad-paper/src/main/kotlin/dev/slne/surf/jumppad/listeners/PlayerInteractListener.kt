package dev.slne.surf.jumppad.listeners

import dev.slne.surf.jumppad.core.client.message.JumpPadMessages
import dev.slne.surf.jumppad.core.client.pad.service.JumpPadService
import dev.slne.surf.jumppad.dialogs.view.JumpPadInfoDialog
import dev.slne.surf.jumppad.pad.toJumpPadPosition
import dev.slne.surf.jumppad.permissions.Permissions
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

object PlayerInteractListener : Listener {
    @EventHandler
    fun onBlockInteract(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        val location = block.location
        val player = event.player

        if (!player.hasPermission(Permissions.COMMAND_JUMP_PAD_GENERIC)) return

        val position = location.toJumpPadPosition() ?: return

        val padAtBlock = JumpPadService.getPadAt(position)
        val padAbove = JumpPadService.getPadAt(position.add(0.0, 1.0, 0.0))

        val pad = padAtBlock ?: padAbove ?: return

        player.sendMessage(JumpPadMessages.padAtBlock(JumpPadInfoDialog.showDialog(pad)))

        event.isCancelled = true
    }
}
