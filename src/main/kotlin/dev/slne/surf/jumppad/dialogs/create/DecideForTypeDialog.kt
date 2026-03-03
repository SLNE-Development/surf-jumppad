@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.create

import dev.slne.surf.jumppad.dialogs.DIALOG_TITLE
import dev.slne.surf.jumppad.dialogs.JumpPadMainDialog
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import io.papermc.paper.registry.data.dialog.ActionButton
import net.kyori.adventure.text.format.TextDecoration

object DecideForTypeDialog {
    fun createDialog() = dialog {
        base {
            title(DIALOG_TITLE)

            body {
                plainMessage(400) {
                    primary(
                        "Du bist dabei ein neues JumpPad zu erstellen.",
                        TextDecoration.BOLD,
                        TextDecoration.UNDERLINED
                    )
                    appendNewline(2)

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
        label { spacer("Zurück") }
        tooltip {
            info("Klicke hier, um zurück zum Hauptmenü zu gelangen.")
        }
        action {
            playerCallback {
                it.showDialog(JumpPadMainDialog.createDialog())
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
                it.showDialog(CreateJumpPadDialog.createDialog(it, type))
            }
        }
    }
}