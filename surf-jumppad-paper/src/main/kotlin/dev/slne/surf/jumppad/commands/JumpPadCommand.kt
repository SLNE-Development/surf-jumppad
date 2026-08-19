package dev.slne.surf.jumppad.commands

import dev.jorel.commandapi.arguments.LocationType
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.locationArgument
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.jumppad.core.client.message.JumpPadMessages
import dev.slne.surf.jumppad.core.client.pad.service.JumpPadService
import dev.slne.surf.jumppad.dialogs.JumpPadMainDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadInfoDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadListDialog
import dev.slne.surf.jumppad.pad.toJumpPadPosition
import dev.slne.surf.jumppad.permissions.Permissions
import org.bukkit.Location

const val JUMP_PAD_LOCATION_ARGUMENT = "jumppadLocation"

fun jumpPadCommand() = commandAPICommand("jumppad") {
    withPermission(Permissions.COMMAND_JUMP_PAD_GENERIC)
    locationArgument(JUMP_PAD_LOCATION_ARGUMENT, LocationType.BLOCK_POSITION, optional = true)

    playerExecutor { player, arguments ->
        val jumppadLocation = arguments.getUnchecked<Location>(JUMP_PAD_LOCATION_ARGUMENT)
        if (jumppadLocation == null) {
            player.showDialog(JumpPadMainDialog.showDialog())
            return@playerExecutor
        }

        val position = jumppadLocation.toJumpPadPosition() ?: return@playerExecutor
        val padAtBlock = JumpPadService.getPadAt(position)
        val padAbove = JumpPadService.getPadAt(position.add(0.0, 1.0, 0.0))
        val pad = padAtBlock ?: padAbove

        if (pad == null) {
            player.sendMessage(JumpPadMessages.noPadAtBlock(JumpPadListDialog.showDialog()))
            return@playerExecutor
        }
        player.showDialog(JumpPadInfoDialog.showDialog(pad))
    }
}
