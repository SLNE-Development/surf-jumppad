@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.create

import dev.slne.surf.jumppad.dialogs.JumpPadMainDialog
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import io.papermc.paper.registry.data.dialog.ActionButton

object DecideForTypeDialog {
    fun showDialog() = dialog {
        base {
            title {
                primary("JUMPPAD ".toSmallCaps())
                info("TYP WÄHLEN".toSmallCaps())
            }

            body {
                plainMessage(400) {
                    info("Wähle aus, welchen Typ von JumpPad du erstellen möchtest.")
                    appendNewline(2)
                }
            }
        }

        type {
            multiAction {
                JumpPadType.entries.forEach { type ->

                    val button = createButton(type)
                    action(button)
                }
                exitAction(backButton())
            }
        }

    }

    private fun backButton(): ActionButton = actionButton {
        label { error("Erstellung abbrechen") }
        tooltip {
            info("Klicke hier, um zurück zum Hauptmenü zu gelangen.")
        }
        action {
            playerCallback {
                it.showDialog(JumpPadMainDialog.showDialog())
            }
        }
    }

    private fun createButton(type: JumpPadType): ActionButton = actionButton {
        label { append(type.displayComponent) }
        tooltip {
            info("Klicke hier, ein JumpPad von diesem Typ zu erstellen.")
        }
        action {
            playerCallback {
                if (type == JumpPadType.STATIC) {
                    it.showDialog(CreateJumpPadDialog.showDialog(it, type))
                    return@playerCallback
                }
                it.showDialog(CreateJumpPadDialog.showDialog(it, type))
            }
        }
    }
}