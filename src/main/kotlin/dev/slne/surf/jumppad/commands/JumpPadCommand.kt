package dev.slne.surf.jumppad.commands

import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.jumppad.dialogs.JumpPadMainDialog
import dev.slne.surf.jumppad.permissions.Permissions

fun jumpPadCommand() = commandAPICommand("jumppad") {

    withPermission(Permissions.COMMAND_JUMP_PAD_GENERIC)

    playerExecutor { player, _ ->
        player.showDialog(JumpPadMainDialog.showDialog())
    }
}