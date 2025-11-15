@file:Suppress("UnstableApiUsage")
@file:OptIn(NmsUseWithCaution::class)

package dev.slne.surf.jumppad.dialogs.create.results

import dev.slne.surf.jumppad.dialogs.create.CreateJumpPadDialog
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.bukkit.api.nms.NmsUseWithCaution
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import io.papermc.paper.registry.data.dialog.ActionButton
import org.bukkit.entity.Player

object JumpPadCreationFailResultDialog {

    fun showDialog(player: Player) = dialog {
        base {
            title {
                primary("JUMPPAD ".toSmallCaps())
                success("ERSTELLEN ".toSmallCaps())
                error("FEHLER".toSmallCaps())
            }

            body {
                plainMessage(400) {
                    info("Die angegebenen Felder wurden nicht korrekt ausgefüllt. Bitte versuche es erneut.")
                }
            }
        }

        type {
            notice(backButton(player))
        }
    }

    private fun backButton(player: Player): ActionButton = actionButton {
        label { spacer("Zurück") }
        tooltip {
            info("Klicke hier, um zurück zur Erstellung zu gelangen.")
        }
        action {
            playerCallback {
                it.showDialog(CreateJumpPadDialog.showDialog(player))
            }
        }
    }
}