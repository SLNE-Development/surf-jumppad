@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.create.results

import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.builder.actionButton
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.jumppad.core.client.dialog.JumpPadButtonTexts
import dev.slne.surf.jumppad.core.client.dialog.JumpPadDialogTexts
import dev.slne.surf.jumppad.core.client.pad.JumpPad
import dev.slne.surf.jumppad.dialogs.JumpPadMainDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadInfoDialog
import io.papermc.paper.registry.data.dialog.ActionButton

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

    private fun backButton(): ActionButton = actionButton {
        label(JumpPadButtonTexts.backLabel)
        tooltip(JumpPadButtonTexts.backToMainMenuTooltip)
        action {
            playerCallback {
                it.showDialog(JumpPadMainDialog.showDialog())
            }
        }
    }

    private fun teleportButton(pad: JumpPad): ActionButton = actionButton {
        label(JumpPadButtonTexts.viewLabel)
        tooltip(JumpPadButtonTexts.viewTooltip)
        action {
            playerCallback {
                it.showDialog(JumpPadInfoDialog.showDialog(pad))
            }
        }
    }
}
