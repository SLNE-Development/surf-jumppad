package dev.slne.surf.jumppad.commands.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.jumppad.config.JumpPadConfigHolder
import dev.slne.surf.jumppad.pad.service.JumpPadService
import dev.slne.surf.jumppad.permissions.Permissions
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText


fun CommandAPICommand.jumpPadReloadCommand() = subcommand("reload") {
    withPermission(Permissions.COMMAND_JUMP_PAD_GENERIC)

    anyExecutor { sender, _ ->
        JumpPadConfigHolder.reload()
        JumpPadService.registerPads()

        sender.sendText {
            appendSuccessPrefix()
            success("Successfully reloaded JumpPad config!")

            appendNewSuccessPrefixedLine()
            success("Sucesfully loaded")
            appendSpace()
            variableValue(JumpPadService.jumpPadCount)
            appendSpace()
            success("JumpPads!")
        }
    }
}