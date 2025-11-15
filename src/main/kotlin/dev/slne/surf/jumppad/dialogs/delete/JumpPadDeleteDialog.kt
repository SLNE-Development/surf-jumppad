@file:Suppress("UnstableApiUsage")
@file:OptIn(NmsUseWithCaution::class)

package dev.slne.surf.jumppad.dialogs.delete

import dev.slne.surf.jumppad.dialogs.view.JumpPadInfoDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadListDialog
import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.jumpPadService
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.bukkit.api.nms.NmsUseWithCaution
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import io.papermc.paper.dialog.Dialog
import net.kyori.adventure.text.format.TextDecoration

object JumpPadDeleteDialog {

    fun showDialog(pad: JumpPad): Dialog = dialog {
        base {
            title {
                primary("JUMPPAD ".toSmallCaps())
                primary("LISTE ".toSmallCaps())
                if (pad.type == JumpPadType.HORIZONTAL) {
                    text("${pad.origin.blockX} ${pad.origin.blockY} ${pad.origin.blockZ} ", Colors.YELLOW)
                } else
                    text("${pad.origin.blockX} ${pad.origin.blockY} ${pad.origin.blockZ} ", Colors.RED)
                error("LÖSCHEN".toSmallCaps())
            }
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
                    variableValue(pad.type.name)
                    appendNewline(2)

                    primary("Position: ")
                    variableValue("${pad.origin.blockX} ${pad.origin.blockY} ${pad.origin.blockZ} ")
                    appendNewline(2)

                    primary("Welt: ")
                    variableValue(pad.origin.world?.name.toString())
                    appendNewline(2)

                    primary("Stärke: ")
                    variableValue(pad.strength.toString())
                    appendNewline(2)

                    primary("Breite: ")
                    variableValue(pad.width.toString())
                    appendNewline(2)

                    primary("Länge: ")
                    variableValue(pad.length.toString())
                }
            }
        }
        type {
            confirmation(confirmButton(pad), backButton(pad))
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
                jumpPadService.deletePad(pad)
                it.showDialog(JumpPadListDialog.showDialog())
            }
        }
    }
}