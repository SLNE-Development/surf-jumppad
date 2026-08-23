package dev.slne.surf.jumppad.listeners

import dev.slne.surf.jumppad.core.client.message.JumpPadMessages
import dev.slne.surf.jumppad.core.client.pad.service.JumpPadService
import dev.slne.surf.jumppad.dialogs.view.JumpPadInfoDialog
import dev.slne.surf.jumppad.permissions.Permissions
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent

object PlayerInteractListener : Listener {
    @EventHandler
    fun onBlockInteract(event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        val worldKey = block.world.key()

        val pad = JumpPadService.getPadAt(worldKey, block.x, block.y, block.z)
            ?: JumpPadService.getPadAt(worldKey, block.x, block.y + 1, block.z)
            ?: return

        val player = event.player
        if (!player.hasPermission(Permissions.COMMAND_JUMP_PAD_GENERIC)) return

        player.sendMessage(JumpPadMessages.padAtBlock(JumpPadInfoDialog.showDialog(pad)))

        event.isCancelled = true
    }
}
