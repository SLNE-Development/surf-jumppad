package dev.slne.surf.jumppad.minestom.dialog.create.results

import dev.slne.surf.api.minestom.dialog.base
import dev.slne.surf.api.minestom.dialog.builder.actionButton
import dev.slne.surf.api.minestom.dialog.dialog
import dev.slne.surf.api.minestom.dialog.type
import dev.slne.surf.jumppad.core.client.dialog.JumpPadButtonTexts
import dev.slne.surf.jumppad.core.client.dialog.JumpPadDialogTexts
import dev.slne.surf.jumppad.minestom.dialog.create.DecideForTypeDialog
import net.minestom.server.dialog.DialogActionButton

object JumpPadCreationFailResultDialog {
    fun showDialog() = dialog {
        base {
            title(JumpPadDialogTexts.createFailTitle)

            body {
                plainMessage(
                    JumpPadDialogTexts.invalidInputBody,
                    JumpPadDialogTexts.RESULT_BODY_WIDTH
                )
            }
        }

        type {
            notice(backButton())
        }
    }

    private fun backButton(): DialogActionButton = actionButton {
        label(JumpPadButtonTexts.backLabel)
        tooltip(JumpPadButtonTexts.backToCreationTooltip)
        action {
            playerCallback {
                it.showDialog(DecideForTypeDialog.showDialog())
            }
        }
    }
}
