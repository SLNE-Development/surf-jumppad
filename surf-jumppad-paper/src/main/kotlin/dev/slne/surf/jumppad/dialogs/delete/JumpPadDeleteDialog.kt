@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.delete

import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.builder.actionButton
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.jumppad.core.client.dialog.JumpPadButtonTexts
import dev.slne.surf.jumppad.core.client.dialog.JumpPadDialogTexts
import dev.slne.surf.jumppad.core.client.pad.JumpPad
import dev.slne.surf.jumppad.core.client.pad.service.JumpPadService
import dev.slne.surf.jumppad.dialogs.view.JumpPadInfoDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadListDialog
import dev.slne.surf.jumppad.pad.world
import io.papermc.paper.dialog.Dialog

object JumpPadDeleteDialog {
    fun showDialog(pad: JumpPad): Dialog = dialog {
        base {
            title(JumpPadDialogTexts.deleteTitle(pad))

            body {
                plainMessage(
                    JumpPadDialogTexts.deleteBody(pad, pad.origin.world()?.name ?: "Unbekannt"),
                    JumpPadDialogTexts.DELETE_BODY_WIDTH
                )
            }
        }

        type {
            confirmation(confirmButton(pad), backButton(pad))
        }
    }

    private fun backButton(pad: JumpPad) = actionButton {
        label(JumpPadButtonTexts.backLabel)
        tooltip(JumpPadButtonTexts.cancelTooltip)
        action {
            playerCallback {
                it.showDialog(JumpPadInfoDialog.showDialog(pad))
            }
        }
    }

    private fun confirmButton(pad: JumpPad) = actionButton {
        label(JumpPadButtonTexts.deleteLabel)
        tooltip(JumpPadButtonTexts.deleteConfirmTooltip)
        action {
            playerCallback {
                JumpPadService.deletePad(pad)
                it.showDialog(JumpPadListDialog.showDialog())
            }
        }
    }
}
