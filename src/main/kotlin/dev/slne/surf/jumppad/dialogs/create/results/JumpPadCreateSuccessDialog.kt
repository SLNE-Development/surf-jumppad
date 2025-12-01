@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.create.results

import dev.slne.surf.jumppad.dialogs.JumpPadMainDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadInfoDialog
import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import io.papermc.paper.registry.data.dialog.ActionButton
import net.kyori.adventure.text.format.TextDecoration

object JumpPadCreateSuccessDialog {
    fun showDialog(pad: JumpPad) = dialog {
        base {
            title {
                primary("JUMPPAD ".toSmallCaps())
                success("ERSTELLEN ".toSmallCaps())
                success("ERFOLG".toSmallCaps())
            }

            body {
                plainMessage(400) {
                    success("Erfolg!", TextDecoration.BOLD)
                    appendNewline(2)
                    success("Das JumpPad wurde erfolgreich erstellt")
                }
            }
        }

        type {
            confirmation(backButton(), teleportButton(pad))
        }
    }

    private fun backButton(): ActionButton = actionButton {
        label { spacer("Zurück") }
        tooltip {
            info("Klicke hier, um zurück zum hauptmenü zu gelangen.")
        }
        action {
            playerCallback {
                it.showDialog(JumpPadMainDialog.showDialog())
            }
        }
    }

    private fun teleportButton(pad: JumpPad): ActionButton = actionButton {
        label { info("Ansehen") }
        tooltip {
            info("Klicke hier, um dir das JumpPad anzusehen.")
        }
        action {
            playerCallback {
                it.showDialog(JumpPadInfoDialog.showDialog(pad))
            }
        }
    }
}