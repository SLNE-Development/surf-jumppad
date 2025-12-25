@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.create.results

import dev.slne.surf.jumppad.dialogs.create.CreateJumpPadDialog
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
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
                it.showDialog(CreateJumpPadDialog.showDialog(it))
            }
        }
    }
}