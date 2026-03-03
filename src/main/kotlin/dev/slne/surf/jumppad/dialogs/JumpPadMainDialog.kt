@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs

import dev.slne.surf.jumppad.dialogs.create.DecideForTypeDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadListDialog
import dev.slne.surf.jumppad.pad.service.JumpPadService
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import io.papermc.paper.registry.data.dialog.ActionButton
import net.kyori.adventure.text.format.TextDecoration

val DIALOG_TITLE = buildText { primary("JUMPPAD".toSmallCaps()) }

object JumpPadMainDialog {
    fun createDialog() = dialog {
        base {
            val jumpPadCount = JumpPadService.jumpPadCount
            title(DIALOG_TITLE)

            body {
                plainMessage(400) {
                    primary("Du befindest dich im Hauptmenü.", TextDecoration.BOLD, TextDecoration.UNDERLINED)
                    appendNewline(2)

                    info("Aktuell existieren ")
                    variableValue(jumpPadCount)
                    info(" JumpPads.")
                    appendNewline(2)
                }
            }
        }

        type {
            multiAction {
                action(createJumpPadButton())
                action(showPadsButton())
                exitAction(exitButton())
            }
        }
    }

    private fun createJumpPadButton(): ActionButton = actionButton {
        label { success("JumpPad erstellen") }
        tooltip {
            info("Klicke hier, um ein neues JumpPad zu erstellen.")
        }
        action {
            playerCallback {
                it.showDialog(DecideForTypeDialog.createDialog())
            }
        }
    }

    internal fun showPadsButton(): ActionButton = actionButton {
        label { primary("JumpPads verwalten") }
        tooltip {
            info("Klicke hier, um die existierenden JumpPads anzusehen.")
        }
        action {
            playerCallback {
                it.showDialog(JumpPadListDialog.createDialog())
            }
        }
    }

    private fun exitButton(): ActionButton = actionButton {
        label { spacer("Schließen") }
        tooltip {
            info("Klicke hier, um das Menü zu verlassen.")
        }
        action {
            playerCallback {
                it.closeDialog()
            }
        }
    }
}