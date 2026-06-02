@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.delete

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.appendNewline
import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.builder.actionButton
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.jumppad.dialogs.view.JumpPadInfoDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadListDialog
import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.service.JumpPadService
import io.papermc.paper.dialog.Dialog
import net.kyori.adventure.text.format.TextDecoration

object JumpPadDeleteDialog {
    fun showDialog(pad: JumpPad): Dialog = dialog {
        base {
            title {
                primary("JUMPPAD ".toSmallCaps())
                primary("LISTE ".toSmallCaps())
                variableValue("${pad.origin.blockX} ${pad.origin.blockY} ${pad.origin.blockZ} ")
                error("LÖSCHEN".toSmallCaps())

                body {
                    plainMessage(300) {
                        error("Achtung!", TextDecoration.BOLD)
                        appendNewline(2)

                        error("Du bist dabei ein JumpPad unwiderruflich zu löschen!")
                        appendNewline(2)

                        error("Bitte bestätige dein Vorhaben!")
                        appendNewline(2)

                        info("Im Folgenden findest du die Informationen zum ausgewählten JumpPad.")
                        appendNewline(2)

                        primary("UUID: ")
                        variableValue(pad.uuid.toString())
                        appendNewline(2)

                        primary("Typ: ")
                        append(pad.type.displayComponent)
                        appendNewline(2)

                        primary("Position: ")
                        variableValue("${pad.origin.blockX} ${pad.origin.blockY} ${pad.origin.blockZ}")
                        appendNewline(2)

                        primary("Welt: ")
                        variableValue(pad.origin.world?.name ?: "Unbekannt")
                        appendNewline(2)

                        if (pad.type == JumpPadType.STATIC) {
                            primary("Ziel: ")
                            val target = pad.targetLocation
                            if (target != null) {
                                variableValue("${target.blockX} ${target.blockY} ${target.blockZ}")
                            } else {
                                variableValue("Nicht gesetzt")
                            }
                            appendNewline(2)
                        } else {
                            primary("Stärke: ")
                            variableValue(pad.distance)
                            appendNewline(2)
                        }

                        primary("Box: ")
                        variableValue("${pad.width}x${pad.length}")
                        appendNewline(2)
                    }
                }
            }
            type {
                confirmation(confirmButton(pad), backButton(pad))
            }
        }
    }

    private fun backButton(pad: JumpPad) = actionButton {
        label { spacer("Zurück") }
        tooltip { info("Klicke hier, um den Vorgang abzubrechen.") }
        action {
            playerCallback {
                it.showDialog(JumpPadInfoDialog.showDialog(pad))
            }
        }
    }

    private fun confirmButton(pad: JumpPad) = actionButton {
        label { error("Löschen") }
        tooltip { info("Klicke hier, um das jumpPad zu löschen.") }
        action {
            playerCallback {
                JumpPadService.deletePad(pad)
                it.showDialog(JumpPadListDialog.showDialog())
            }
        }
    }
}