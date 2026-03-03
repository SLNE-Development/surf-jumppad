@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.error

import dev.slne.surf.jumppad.appendBullet
import dev.slne.surf.jumppad.dialogs.DIALOG_TITLE
import dev.slne.surf.jumppad.dialogs.JumpPadMainDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadInfoDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadListDialog
import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import io.papermc.paper.registry.data.dialog.ActionButton
import net.kyori.adventure.text.format.TextDecoration

object JumpPadSuccessDialog {
    fun createDialog(type: JumpPadActionType, jumpPad: JumpPad?) = dialog {
        base {
            title(DIALOG_TITLE)

            body {
                plainMessage(400) {
                    success(
                        "Der Vorgang wurde erfolgreich abgeschlossen!",
                        TextDecoration.BOLD,
                        TextDecoration.UNDERLINED
                    )
                    appendNewline(2)

                    success(
                        when (type) {
                            JumpPadActionType.CREATE -> "Das JumpPad wurde erfolgreich erstellt!"
                            JumpPadActionType.EDIT -> "Die Änderungen wurden erfolgreich gespeichert!"
                            JumpPadActionType.DELETE -> "Das ausgewählte JumpPad wurde erfolgreich gelöscht!"
                        }
                    )
                    appendNewline(2)

                    if (type != JumpPadActionType.DELETE && jumpPad != null) {
                        appendBullet()
                        primary("UUID: ")
                        variableValue(jumpPad.uuid.toString())
                    }
                }
            }
        }

        type {
            when (type) {
                JumpPadActionType.CREATE -> {
                    confirmation(viewJumpPadButton(jumpPad!!), mainMenuButton())
                }

                JumpPadActionType.EDIT -> {
                    notice(backToInfoButton(jumpPad!!))
                }

                JumpPadActionType.DELETE -> {
                    notice(backToListButton())
                }
            }
        }
    }

    private fun viewJumpPadButton(jumpPad: JumpPad): ActionButton = actionButton {
        label { spacer("JumpPad ansehen") }
        tooltip { info("Klicke, um Details des neuen JumpPads zu sehen.") }
        action {
            playerCallback {
                it.showDialog(JumpPadInfoDialog.createDialog(jumpPad))
            }
        }
    }

    private fun mainMenuButton(): ActionButton = actionButton {
        label { spacer("Zurück") }
        tooltip { info("Klicke, um zum Hauptmenü zurückzukehren.") }
        action {
            playerCallback {
                it.showDialog(JumpPadMainDialog.createDialog())
            }
        }
    }

    private fun backToInfoButton(jumpPad: JumpPad): ActionButton = actionButton {
        label { spacer("Zurück") }
        tooltip { info("Zurück zur JumpPad-Ansicht.") }
        action {
            playerCallback {
                it.showDialog(JumpPadInfoDialog.createDialog(jumpPad))
            }
        }
    }

    private fun backToListButton(): ActionButton = actionButton {
        label { spacer("Zurück") }
        tooltip { info("Zurück zur JumpPad-Übersicht.") }
        action {
            playerCallback {
                it.showDialog(JumpPadListDialog.createDialog())
            }
        }
    }
}