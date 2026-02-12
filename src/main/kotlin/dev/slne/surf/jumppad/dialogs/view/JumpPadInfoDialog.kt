@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.view

import dev.slne.surf.jumppad.dialogs.delete.JumpPadDeleteDialog
import dev.slne.surf.jumppad.dialogs.edit.JumpPadEditDialog
import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import io.papermc.paper.dialog.Dialog

object JumpPadInfoDialog {
    fun showDialog(pad: JumpPad): Dialog = dialog {
        base {
            title {
                variableValue("${pad.origin.blockX} ${pad.origin.blockY} ${pad.origin.blockZ}")
            }
            body {
                plainMessage(350) {
                    info("Informationen zum JumpPad am Standort:")
                    appendNewline()
                    variableValue("${pad.origin.blockX} ${pad.origin.blockY} ${pad.origin.blockZ}")
                    appendNewline(2)

                    primary("UUID: ")
                    variableValue(pad.uuid.toString())
                    appendNewline()

                    primary("Typ: ")
                    append(pad.type.displayComponent)
                    appendNewline()

                    primary("Welt: ")
                    variableValue(pad.origin.world?.name ?: "unbekannt")
                    appendNewline()

                    if (pad.type == JumpPadType.STATIC) {
                        primary("Ziel: ")
                        val target = pad.targetLocation
                        if (target != null) {
                            variableValue("${target.blockX} ${target.blockY} ${target.blockZ}")
                        } else {
                            error("Nicht konfiguriert")
                        }
                    } else {
                        primary("Stärke: ")
                        variableValue("${pad.distance} Blöcke")
                    }
                    appendNewline()

                    primary("Bereich: ")
                    variableValue("${pad.width}x${pad.length}")
                    appendNewline(2)
                }
            }
        }
        type {
            multiAction {
                action(teleportButton(pad))
                action(editButton(pad))
                action(deleteButton(pad))

                columns(1)
                exitAction(backButton())
            }
        }
    }

    private fun backButton() = actionButton {
        label { spacer("Zurück") }
        tooltip { info("Klicke hier, um den Vorgang abzubrechen.") }
        action {
            playerCallback {
                it.showDialog(JumpPadListDialog.showDialog())
            }
        }
    }

    private fun deleteButton(pad: JumpPad) = actionButton {
        label { error("Löschen") }
        tooltip { info("Klicke hier, um das JumpPad zu löschen.") }
        action {
            playerCallback {
                it.showDialog(JumpPadDeleteDialog.showDialog(pad))
            }
        }
    }

    internal fun teleportButton(pad: JumpPad) = actionButton {
        label { primary("Teleportieren") }
        tooltip { info("Klicke hier, um dich zum JumpPad zu teleportieren.") }
        action {
            playerCallback {
                it.teleportAsync(pad.origin)
                it.closeDialog()
            }
        }
    }

    private fun editButton(pad: JumpPad) = actionButton {
        label { primary("Konfigurieren") }
        tooltip { info("Klicke hier, um die Einstellungen des JumpPads zu konfigurieren.") }
        action {
            playerCallback {
                it.showDialog(JumpPadEditDialog.showDialog(pad))
            }
        }
    }
}