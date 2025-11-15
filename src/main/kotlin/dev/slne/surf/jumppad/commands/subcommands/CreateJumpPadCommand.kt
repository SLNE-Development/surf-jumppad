package dev.slne.surf.jumppad.commands.subcommands

import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.slne.surf.jumppad.permissions.Permissions

fun createJmpPadCommand(commandName: String) = commandAPICommand(commandName) {
    withPermission(Permissions.COMMAND_JUMP_PAD_CREATE)

}