@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.edit.result

import dev.slne.surf.jumppad.dialogs.edit.JumpPadEditDialog
import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import io.papermc.paper.registry.data.dialog.ActionButton
import net.kyori.adventure.text.format.TextDecoration

object JumpPadEditFailResultDialog {
    fun showDialog(pad: JumpPad) = dialog {
        base {
            title {
                primary("JUMPPAD ".toSmallCaps())
                primary("LISTE ".toSmallCaps())
                variableValue("${pad.origin.blockX} ${pad.origin.blockY} ${pad.origin.blockZ} ")
                primary("KONFIGURIEREN ".toSmallCaps())
                error("FEHLER".toSmallCaps())
            }

            body {
                plainMessage(400) {
                    error("Fehler!", TextDecoration.BOLD)
                    appendNewline(2)
                    error("Die angegebenen Felder wurden nicht korrekt ausgefüllt. Bitte versuche es erneut.")
                }
            }
        }

        type {
            notice(backButton(pad))
        }
    }

    private fun backButton(pad: JumpPad): ActionButton = actionButton {
        label { spacer("Zurück") }
        tooltip {
            info("Klicke hier, um zurück zur Erstellung zu gelangen.")
        }
        action {
            playerCallback {
                it.showDialog(JumpPadEditDialog.showDialog(pad))
            }
        }
    }
}