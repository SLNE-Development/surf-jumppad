package dev.slne.surf.jumppad.minestom.dialog.view

import dev.slne.surf.api.minestom.dialog.base
import dev.slne.surf.api.minestom.dialog.builder.actionButton
import dev.slne.surf.api.minestom.dialog.dialog
import dev.slne.surf.api.minestom.dialog.type
import dev.slne.surf.jumppad.core.client.dialog.JumpPadButtonTexts
import dev.slne.surf.jumppad.core.client.dialog.JumpPadDialogTexts
import dev.slne.surf.jumppad.core.client.pad.JumpPad
import dev.slne.surf.jumppad.minestom.dialog.delete.JumpPadDeleteDialog
import dev.slne.surf.jumppad.minestom.dialog.edit.JumpPadEditDialog
import dev.slne.surf.jumppad.minestom.pad.instance
import dev.slne.surf.jumppad.minestom.pad.toPos
import net.minestom.server.dialog.Dialog

object JumpPadInfoDialog {
    fun showDialog(pad: JumpPad): Dialog = dialog {
        base {
            title(JumpPadDialogTexts.infoTitle(pad))
            body {
                plainMessage(
                    JumpPadDialogTexts.infoBody(pad, pad.origin.worldKey.value()),
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
            playerCallback { player ->
                val instance = pad.origin.instance() ?: return@playerCallback
                if (player.instance == instance) {
                    player.teleport(pad.origin.toPos())
                } else {
                    player.setInstance(instance, pad.origin.toPos())
                }
                player.closeDialog()
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
