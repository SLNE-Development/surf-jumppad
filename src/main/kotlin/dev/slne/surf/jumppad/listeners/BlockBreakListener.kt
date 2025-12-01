package dev.slne.surf.jumppad.listeners

import dev.slne.surf.jumppad.dialogs.view.JumpPadInfoDialog
import dev.slne.surf.jumppad.pad.jumpPadService
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

object BlockBreakListener : Listener {
    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val location = event.block.location
        val player = event.player

        val padAtBlock = jumpPadService.getPadAt(location)

        val blockAboveLocation = location.clone().add(0.0, 1.0, 0.0)
        val padAbove = jumpPadService.getPadAt(blockAboveLocation)

        val pad = padAtBlock ?: padAbove ?: return

        val clickable = buildText {
            text("HIER", Colors.VARIABLE_VALUE, TextDecoration.UNDERLINED)
            hoverEvent(HoverEvent.showText(buildText { info("Klicke hier, um dir das JumpPad anzusehen.") }))
            clickEvent(ClickEvent.callback { player.showDialog(JumpPadInfoDialog.showDialog(pad)) })
        }

        player.sendText {
            appendPrefix()
            error("Dies ist ein JumpPad und kann nicht entfernt werden.")
            appendNewPrefixedLine()
            error("Klicke ")
            append(clickable)
            error(" um dir das JumpPad anzusehen!")
        }

        event.isCancelled = true
    }
}