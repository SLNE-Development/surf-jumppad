package dev.slne.surf.jumppad.commands

import dev.jorel.commandapi.arguments.LocationType
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.locationArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.jumppad.dialogs.JumpPadMainDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadInfoDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadListDialog
import dev.slne.surf.jumppad.pad.service.JumpPadService
import dev.slne.surf.jumppad.permissions.Permissions
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location

fun jumpPadCommand() = commandAPICommand("jumppad") {
    withPermission(Permissions.COMMAND_JUMP_PAD_GENERIC)
    locationArgument("jumppadLocation", LocationType.BLOCK_POSITION, optional = true)

    playerExecutor { player, arguments ->
        val jumppadLocation = arguments.getUnchecked<Location>("jumppadLocation ")
        if (jumppadLocation == null) {
            player.showDialog(JumpPadMainDialog.showDialog())
            return@playerExecutor
        }
        val padAtBlock = JumpPadService.getPadAt(jumppadLocation)
        val padAbove = JumpPadService.getPadAt(jumppadLocation.clone().add(0.0, 1.0, 0.0))
        val pad = padAtBlock ?: padAbove

        if (pad == null) {
            val clickable = buildText {
                variableValue("HIER", TextDecoration.UNDERLINED)
                hoverEvent(buildText { info("Klicke hier, um ir die Liste existierender JumpPads anzusehen.") })
                clickEvent(ClickEvent.callback { it.showDialog(JumpPadListDialog.showDialog()) })
            }

            player.sendText {
                appendErrorPrefix()
                error("An dieser Stelle befindet sich kein JumpPad! ")
                appendNewErrorPrefixedLine()
                error("Klicke ")
                append(clickable)
                error(" um dir die Liste existierender JumpPads anzusehen.")
            }
            return@playerExecutor
        }
        player.showDialog(JumpPadInfoDialog.showDialog(pad))
    }
}