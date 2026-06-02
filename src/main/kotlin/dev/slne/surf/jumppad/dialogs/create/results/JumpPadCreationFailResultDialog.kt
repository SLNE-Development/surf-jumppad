@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.create.results

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.appendNewline
import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.builder.actionButton
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.jumppad.dialogs.create.DecideForTypeDialog
import io.papermc.paper.registry.data.dialog.ActionButton
import net.kyori.adventure.text.format.TextDecoration

object JumpPadCreationFailResultDialog {
    fun showDialog() = dialog {
        base {
            title {
                primary("JUMPPAD ".toSmallCaps())
                success("ERSTELLEN ".toSmallCaps())
                error("FEHLER".toSmallCaps())
            }

            body {
                plainMessage(400) {
                    error("Fehler!", TextDecoration.BOLD)
                    appendNewline(2)

                    error("Die angegebenen Felder wurden nicht korrekt ausgefüllt.")
                    appendNewline(2)

                    error("Bitte versuche es erneut.")
                }
            }
        }

        type {
            notice(backButton())
        }
    }

    private fun backButton(): ActionButton = actionButton {
        label { spacer("Zurück") }
        tooltip {
            info("Klicke hier, um zurück zur Erstellung zu gelangen.")
        }
        action {
            playerCallback {
                it.showDialog(DecideForTypeDialog.showDialog())
            }
        }
    }
}