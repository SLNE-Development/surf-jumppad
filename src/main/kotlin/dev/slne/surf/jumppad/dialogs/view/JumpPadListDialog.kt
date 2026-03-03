@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.view

// Achtung: Ich habe hier JumpPadService angenommen, passe dies ggf. an deinen echten Klassennamen an
import dev.slne.surf.jumppad.dialogs.DIALOG_TITLE
import dev.slne.surf.jumppad.dialogs.JumpPadMainDialog
import dev.slne.surf.jumppad.dialogs.create.DecideForTypeDialog
import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.jumppad.pad.service.JumpPadService
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import io.papermc.paper.dialog.Dialog
import net.kyori.adventure.text.format.TextDecoration

object JumpPadListDialog {
    fun createDialog(): Dialog {
        val jumpPads = JumpPadService.jumpPads

        val dialogList = buildPadDialogList(jumpPads)
        if (dialogList.isEmpty()) {
            return dialog {
                base {
                    title(DIALOG_TITLE)
                    body {
                        plainMessage(400) {
                            primary(
                                "Du befindest dich in der JumpPad-Übersicht.",
                                TextDecoration.BOLD,
                                TextDecoration.UNDERLINED
                            )
                            appendNewline(2)

                            error("Es existieren aktuell keine JumpPads.")
                        }
                    }
                }
                type {
                    confirmation(createButton(), backButton())
                }
            }
        }

        return dialog {
            base {
                title(DIALOG_TITLE)
                body {
                    plainMessage(400) {
                        primary(
                            "Du befindest dich in der JumpPad-Übersicht.",
                            TextDecoration.BOLD,
                            TextDecoration.UNDERLINED
                        )
                        appendNewline(2)

                        info("Aktuell existieren ")
                        variableValue(jumpPads.size)
                        info(" JumpPads.")
                    }
                }
            }

            type {
                dialogList {
                    addAll(dialogList)
                    buttonWidth(400)
                    columns(1)
                    exitAction(backButton())
                }
            }
        }
    }

    private fun buildPadDialogList(jumpPads: Collection<JumpPad>) =
        jumpPads.map { JumpPadInfoDialog.createDialog(it) }.toObjectSet()

    private fun backButton() = actionButton {
        label { spacer("Zurück") }
        tooltip { info("Klicke hier, um zurück zum Hauptmenü zu gelangen.") }
        action {
            playerCallback {
                it.showDialog(JumpPadMainDialog.createDialog())
            }
        }
    }

    private fun createButton() = actionButton {
        label { success("JumpPad erstellen") }
        tooltip { info("Klicke hier, um ein JumpPad zu erstellen.") }
        action {
            playerCallback {
                it.showDialog(DecideForTypeDialog.createDialog())
            }
        }
    }
}