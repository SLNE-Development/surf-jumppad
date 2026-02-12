@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.view

import dev.slne.surf.jumppad.dialogs.JumpPadMainDialog
import dev.slne.surf.jumppad.dialogs.create.DecideForTypeDialog
import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.service.jumpPadService
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import io.papermc.paper.dialog.Dialog

object JumpPadListDialog {
    fun showDialog(): Dialog {
        val pads = jumpPadService.getPads()

        val dialogList = buildPadDialogList(pads)
        if (dialogList.isEmpty()) {
            return dialog {
                base {
                    title {
                        primary("JUMPPAD LISTE".toSmallCaps())
                    }
                    body {
                        plainMessage(300) {
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
                title {
                    primary("JUMPPAD LISTE".toSmallCaps())
                }

                body {
                    plainMessage(400) {
                        info("Aktuell existieren insgesamt ")
                        variableValue(pads.size)
                        info(" JumpPads.")
                        appendNewline(2)

                        primary("Statistik nach Typen:")
                        appendNewline()

                        val padsByType = pads.groupBy { it.type }
                        JumpPadType.entries.forEach { type ->
                            val count = padsByType[type]?.size ?: 0
                            if (count > 0) {
                                spacer(" - ")
                                append(type.displayComponent)
                                info(": ")
                                variableValue(count)
                                appendNewline()
                            }
                        }
                        appendNewline()
                        info("Klicke auf ein JumpPad, um Details zu sehen.")
                    }
                }
            }

            type {
                dialogList {
                    addAll(dialogList)
                    buttonWidth(200)
                    columns(3)
                    exitAction(backButton())
                }
            }
        }
    }

    private fun buildPadDialogList(pads: Collection<JumpPad>) =
        pads.map { JumpPadInfoDialog.showDialog(it) }.toObjectSet()

    private fun backButton() = actionButton {
        label { spacer("Zurück") }
        tooltip { info("Klicke hier, um zurück zum Hauptmenü zu gelangen.") }
        action {
            playerCallback {
                it.showDialog(JumpPadMainDialog.showDialog())
            }
        }
    }

    private fun createButton() = actionButton {
        label { success("JumpPad erstellen") }
        tooltip { info("Klicke hier, um ein JumpPad zu erstellen.") }
        action {
            playerCallback {
                it.showDialog(DecideForTypeDialog.showDialog())
            }
        }
    }
}