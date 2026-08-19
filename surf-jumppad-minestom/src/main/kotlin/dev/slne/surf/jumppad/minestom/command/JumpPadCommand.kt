package dev.slne.surf.jumppad.minestom.command

import dev.slne.minestom.lobby.api.command.commandapi.dsl.blockPositionArgument
import dev.slne.minestom.lobby.api.command.commandapi.dsl.commandAPICommand
import dev.slne.minestom.lobby.api.command.commandapi.dsl.playerExecutor
import dev.slne.surf.jumppad.core.client.message.JumpPadMessages
import dev.slne.surf.jumppad.core.client.pad.service.JumpPadService
import dev.slne.surf.jumppad.core.client.permission.JumpPadPermissions
import dev.slne.surf.jumppad.minestom.dialog.JumpPadMainDialog
import dev.slne.surf.jumppad.minestom.dialog.view.JumpPadInfoDialog
import dev.slne.surf.jumppad.minestom.dialog.view.JumpPadListDialog
import dev.slne.surf.jumppad.minestom.pad.toJumpPadPosition
import net.minestom.server.coordinate.Vec

const val JUMP_PAD_LOCATION_ARGUMENT = "jumppadLocation"

fun jumpPadCommand() = commandAPICommand("jumppad") {
    withPermission(JumpPadPermissions.COMMAND_JUMP_PAD_GENERIC)
    blockPositionArgument(JUMP_PAD_LOCATION_ARGUMENT, optional = true)

    playerExecutor { player, arguments ->
        val jumppadLocation = arguments.getOptional<Vec>(JUMP_PAD_LOCATION_ARGUMENT)
        if (jumppadLocation == null) {
            player.showDialog(JumpPadMainDialog.showDialog())
            return@playerExecutor
        }

        val instance = player.instance ?: return@playerExecutor
        val position = jumppadLocation.toJumpPadPosition(instance) ?: return@playerExecutor
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
