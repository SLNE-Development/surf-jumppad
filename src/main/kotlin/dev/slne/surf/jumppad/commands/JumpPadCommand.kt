package dev.slne.surf.jumppad.commands

import dev.jorel.commandapi.arguments.LocationType
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.locationArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.jumppad.dialogs.JumpPadMainDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadInfoDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadListDialog
import dev.slne.surf.jumppad.pad.jumpPadService
import dev.slne.surf.jumppad.permissions.Permissions
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location

fun jumpPadCommand() = commandAPICommand("jumppad") {

    withPermission(Permissions.COMMAND_JUMP_PAD_GENERIC)
    locationArgument("location", LocationType.BLOCK_POSITION, optional = true)

    playerExecutor { player, arguments ->
        val location = arguments.getUnchecked<Location>("location")
        if (location == null) {
            player.showDialog(JumpPadMainDialog.showDialog())
            return@playerExecutor
        }
        val padAtBlock = jumpPadService.getPadAt(location)
        val padAbove = jumpPadService.getPadAt(location.clone().add(0.0, 1.0, 0.0))
        val pad = padAtBlock ?: padAbove

        if (pad == null) {
            val clickable = buildText {
                text("HIER", Colors.VARIABLE_VALUE, TextDecoration.UNDERLINED)
                hoverEvent(HoverEvent.showText(buildText { info("Klicke hier, um ir die Liste existierender jumpPads anzusehen.") }))
                clickEvent(ClickEvent.callback { player.showDialog(JumpPadListDialog.showDialog()) })
            }

            player.sendText {
                appendPrefix()
                error("An dieser Stelle befindet sich kein JumpPad! ")
                appendNewPrefixedLine()
                error("Klicke ")
                append(clickable)
                error(" um dir die Liste existierender JumpPads anzusehen.")
            }
            return@playerExecutor
        }
        player.showDialog(JumpPadInfoDialog.showDialog(pad))
    }
}