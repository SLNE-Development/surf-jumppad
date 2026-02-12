@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs

import dev.slne.surf.jumppad.dialogs.create.CreateJumpPadDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadListDialog
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.service.jumpPadService
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import io.papermc.paper.registry.data.dialog.ActionButton

object JumpPadMainDialog {
    fun showDialog() = dialog {
        base {
            val pads = jumpPadService.getPads()
            title { primary("JUMPPAD".toSmallCaps()) }

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
            info("Klicke hier, ein JumPad zu erstellen.")
        }
        action {
            playerCallback {
                it.showDialog(CreateJumpPadDialog.showDialog(it))
            }
        }
    }

    internal fun showPadsButton(): ActionButton = actionButton {
        label { primary("JumPad ansehen") }
        tooltip {
            info("Klicke hier, die existierenden JumPads anzusehen.")
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
            info("Klicke hier, um den Vorgang abzubrechen.")
        }
        action {
            playerCallback {
                it.closeDialog()
            }
        }
    }
}