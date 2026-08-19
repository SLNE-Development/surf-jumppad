@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.view

import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.builder.actionButton
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.jumppad.core.client.dialog.JumpPadButtonTexts
import dev.slne.surf.jumppad.core.client.dialog.JumpPadDialogTexts
import dev.slne.surf.jumppad.core.client.pad.JumpPad
import dev.slne.surf.jumppad.dialogs.delete.JumpPadDeleteDialog
import dev.slne.surf.jumppad.dialogs.edit.JumpPadEditDialog
import dev.slne.surf.jumppad.pad.toLocation
import dev.slne.surf.jumppad.pad.world
import io.papermc.paper.dialog.Dialog

object JumpPadInfoDialog {
    fun showDialog(pad: JumpPad): Dialog = dialog {
        base {
            title(JumpPadDialogTexts.infoTitle(pad))
            body {
                plainMessage(
                    JumpPadDialogTexts.infoBody(pad, pad.origin.world()?.name ?: "unbekannt"),
                    JumpPadDialogTexts.INFO_BODY_WIDTH
                )
            }
        }
        type {
            multiAction {
                action(teleportButton(pad))
                action(editButton(pad))
                action(deleteButton(pad))

                columns(1)
                exitAction(backButton())
            }
        }
    }

    private fun backButton() = actionButton {
        label(JumpPadButtonTexts.backLabel)
        tooltip(JumpPadButtonTexts.cancelTooltip)
        action {
            playerCallback {
                it.showDialog(JumpPadListDialog.showDialog())
            }
        }
    }

    private fun deleteButton(pad: JumpPad) = actionButton {
        label(JumpPadButtonTexts.deleteLabel)
        tooltip(JumpPadButtonTexts.deleteTooltip)
        action {
            playerCallback {
                it.showDialog(JumpPadDeleteDialog.showDialog(pad))
            }
        }
    }

    internal fun teleportButton(pad: JumpPad) = actionButton {
        label(JumpPadButtonTexts.teleportLabel)
        tooltip(JumpPadButtonTexts.teleportTooltip)
        action {
            playerCallback {
                val origin = pad.origin.toLocation() ?: return@playerCallback
                it.teleportAsync(origin)
                it.closeDialog()
            }
        }
    }

    private fun editButton(pad: JumpPad) = actionButton {
        label(JumpPadButtonTexts.editLabel)
        tooltip(JumpPadButtonTexts.editTooltip)
        action {
            playerCallback {
                it.showDialog(JumpPadEditDialog.showDialog(pad))
            }
        }
    }
}
