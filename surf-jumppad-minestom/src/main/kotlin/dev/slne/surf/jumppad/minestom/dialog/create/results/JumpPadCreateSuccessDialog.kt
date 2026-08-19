package dev.slne.surf.jumppad.minestom.dialog.create.results

import dev.slne.surf.api.minestom.dialog.base
import dev.slne.surf.api.minestom.dialog.builder.actionButton
import dev.slne.surf.api.minestom.dialog.dialog
import dev.slne.surf.api.minestom.dialog.type
import dev.slne.surf.jumppad.core.client.dialog.JumpPadButtonTexts
import dev.slne.surf.jumppad.core.client.dialog.JumpPadDialogTexts
import dev.slne.surf.jumppad.core.client.pad.JumpPad
import dev.slne.surf.jumppad.minestom.dialog.JumpPadMainDialog
import dev.slne.surf.jumppad.minestom.dialog.view.JumpPadInfoDialog
import net.minestom.server.dialog.DialogActionButton

object JumpPadCreateSuccessDialog {
    fun showDialog(pad: JumpPad) = dialog {
        base {
            title(JumpPadDialogTexts.createSuccessTitle)

            body {
                plainMessage(
                    JumpPadDialogTexts.createSuccessBody,
                    JumpPadDialogTexts.RESULT_BODY_WIDTH
                )
            }
        }

        type {
            confirmation(teleportButton(pad), backButton())
        }
    }

    private fun backButton(): DialogActionButton = actionButton {
        label(JumpPadButtonTexts.backLabel)
        tooltip(JumpPadButtonTexts.backToMainMenuTooltip)
        action {
            playerCallback {
                it.showDialog(JumpPadMainDialog.showDialog())
            }
        }
    }

    private fun teleportButton(pad: JumpPad): DialogActionButton = actionButton {
        label(JumpPadButtonTexts.viewLabel)
        tooltip(JumpPadButtonTexts.viewTooltip)
        action {
            playerCallback {
                it.showDialog(JumpPadInfoDialog.showDialog(pad))
            }
        }
    }
}
