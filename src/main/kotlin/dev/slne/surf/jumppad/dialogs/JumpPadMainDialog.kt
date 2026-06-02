@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.appendNewline
import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.builder.actionButton
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.jumppad.dialogs.create.DecideForTypeDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadListDialog
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.service.JumpPadService
import io.papermc.paper.registry.data.dialog.ActionButton

object JumpPadMainDialog {
    fun showDialog() = dialog {
        base {
            val pads = JumpPadService.getPads()
            title {
                primary("JUMPPAD ".toSmallCaps())
                success("VERWALTUNG".toSmallCaps())
            }
            body {
                plainMessage(400) {
                    info("Willkommen in der JumpPad-Verwaltung.")
                    appendNewline(2)

                    info("Aktuell existieren insgesamt ")
                    variableValue(pads.size)
                    info(" JumpPads.")
                    appendNewline(2)

                    if (pads.isNotEmpty()) {
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
                    } else {
                        error("Es wurden noch keine JumpPads erstellt.")
                    }
                }
            }
        }

        type {
            multiAction {
                action(createPadButton())
                action(showPadsButton())
                exitAction(exitButton())
            }
        }
    }

    private fun createPadButton(): ActionButton = actionButton {
        label { success("JumPad erstellen") }
        tooltip {
            info("Klicke hier, um ein neues JumpPad zu erstellen.")
        }
        action {
            playerCallback {
                it.showDialog(DecideForTypeDialog.showDialog())
            }
        }
    }

    internal fun showPadsButton(): ActionButton = actionButton {
        label { primary("JumPad ansehen") }
        tooltip {
            info("Klicke hier, um die Liste aller existierenden JumpPads zu öffnen.")
        }
        action {
            playerCallback {
                it.showDialog(JumpPadListDialog.showDialog())
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