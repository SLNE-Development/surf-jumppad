package dev.slne.surf.jumppad.commands

import dev.jorel.commandapi.arguments.LocationType
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.locationArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.jumppad.commands.subcommands.jumpPadReloadCommand
import dev.slne.surf.jumppad.dialogs.JumpPadMainDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadInfoDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadListDialog
import dev.slne.surf.jumppad.pad.service.JumpPadService
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
    jumpPadReloadCommand()
    locationArgument("jumppadLocation", LocationType.BLOCK_POSITION, optional = true)

    playerExecutor { player, arguments ->
        val jumppadLocation = arguments.getUnchecked<Location>("jumppadLocation ")
        if (jumppadLocation == null) {
            player.showDialog(JumpPadMainDialog.createDialog())
            return@playerExecutor
        }
        val padAtBlock = JumpPadService.getPadAt(jumppadLocation)
        val padAbove = JumpPadService.getPadAt(jumppadLocation.clone().add(0.0, 1.0, 0.0))
        val pad = padAtBlock ?: padAbove

        if (pad == null) {
            val clickable = buildText {
                text("HIER", Colors.VARIABLE_VALUE, TextDecoration.UNDERLINED)
                hoverEvent(HoverEvent.showText(buildText { info("Klicke hier, um ir die Liste existierender JumpPads anzusehen.") }))
                clickEvent(ClickEvent.callback { player.showDialog(JumpPadListDialog.createDialog()) })
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
        player.showDialog(JumpPadInfoDialog.createDialog(pad))
    }
}