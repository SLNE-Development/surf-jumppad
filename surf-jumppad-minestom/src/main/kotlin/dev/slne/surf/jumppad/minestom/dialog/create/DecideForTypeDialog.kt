package dev.slne.surf.jumppad.minestom.dialog.create

import dev.slne.surf.api.minestom.dialog.base
import dev.slne.surf.api.minestom.dialog.builder.actionButton
import dev.slne.surf.api.minestom.dialog.dialog
import dev.slne.surf.api.minestom.dialog.type
import dev.slne.surf.jumppad.core.client.dialog.JumpPadButtonTexts
import dev.slne.surf.jumppad.core.client.dialog.JumpPadDialogTexts
import dev.slne.surf.jumppad.core.client.pad.JumpPadType
import dev.slne.surf.jumppad.minestom.dialog.JumpPadMainDialog
import net.minestom.server.dialog.DialogActionButton

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
                    action(createButton(type))
                }
                exitAction(backButton())
            }
        }
    }

    private fun backButton(): DialogActionButton = actionButton {
        label(JumpPadButtonTexts.cancelCreationLabel)
        tooltip(JumpPadButtonTexts.backToMainTooltip)
        action {
            playerCallback {
                it.showDialog(JumpPadMainDialog.showDialog())
            }
        }
    }

    private fun createButton(type: JumpPadType): DialogActionButton = actionButton {
        label { append(type.displayComponent) }
        tooltip(JumpPadButtonTexts.chooseTypeTooltip)
        action {
            playerCallback {
                it.showDialog(CreateJumpPadDialog.showDialog(it, type))
            }
        }
    }
}
