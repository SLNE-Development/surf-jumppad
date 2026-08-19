@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.create

import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.builder.actionButton
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.jumppad.core.client.dialog.JumpPadButtonTexts
import dev.slne.surf.jumppad.core.client.dialog.JumpPadDialogTexts
import dev.slne.surf.jumppad.core.client.pad.JumpPadType
import dev.slne.surf.jumppad.dialogs.JumpPadMainDialog
import io.papermc.paper.registry.data.dialog.ActionButton

object DecideForTypeDialog {
    fun showDialog() = dialog {
        base {
            title(JumpPadDialogTexts.decideForTypeTitle)

            body {
                plainMessage(
                    JumpPadDialogTexts.decideForTypeBody,
                    JumpPadDialogTexts.DECIDE_FOR_TYPE_BODY_WIDTH
                )
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
        label(JumpPadButtonTexts.cancelCreationLabel)
        tooltip(JumpPadButtonTexts.backToMainTooltip)
        action {
            playerCallback {
                it.showDialog(JumpPadMainDialog.showDialog())
            }
        }
    }

    private fun createButton(type: JumpPadType): ActionButton = actionButton {
        label { append(type.displayComponent) }
        tooltip(JumpPadButtonTexts.chooseTypeTooltip)
        action {
            playerCallback {
                it.showDialog(CreateJumpPadDialog.showDialog(it, type))
            }
        }
    }
}
