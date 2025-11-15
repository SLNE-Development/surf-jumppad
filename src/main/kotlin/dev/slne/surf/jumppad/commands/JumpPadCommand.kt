package dev.slne.surf.jumppad.commands

import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.slne.surf.jumppad.commands.subcommands.createJmpPadCommand
import dev.slne.surf.jumppad.permissions.Permissions

fun jumpPadCommand() = commandAPICommand("jump-pad") {
    withPermission(Permissions.COMMAND_JUMP_PAD_GENERIC)
    createJmpPadCommand("create")
}