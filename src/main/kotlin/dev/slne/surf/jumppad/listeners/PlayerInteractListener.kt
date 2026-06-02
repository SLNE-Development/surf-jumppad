package dev.slne.surf.jumppad.listeners

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.jumppad.dialogs.view.JumpPadInfoDialog
import dev.slne.surf.jumppad.pad.service.JumpPadService
import dev.slne.surf.jumppad.permissions.Permissions
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextDecoration
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

        val padAtBlock = JumpPadService.getPadAt(location)

        val blockAboveLocation = location.clone().add(0.0, 1.0, 0.0)
        val padAbove = JumpPadService.getPadAt(blockAboveLocation)

        val pad = padAtBlock ?: padAbove ?: return

        val clickable = buildText {
            variableValue("HIER", TextDecoration.UNDERLINED)
            hoverEvent(buildText { info("Klicke hier, um dir das JumpPad anzusehen.") })
            clickEvent(ClickEvent.callback { it.showDialog(JumpPadInfoDialog.showDialog(pad)) })
        }

        player.sendText {
            appendErrorPrefix()
            error("An dieser Stelle befindet sich ein JumpPad!")
            appendNewErrorPrefixedLine()
            error("Klicke ")
            append(clickable)
            error(" um dir das JumpPad anzusehen!")
        }

        event.isCancelled = true
    }
}