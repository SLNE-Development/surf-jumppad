package dev.slne.surf.jumppad.minestom.dialog.edit.result

import dev.slne.surf.api.minestom.dialog.base
import dev.slne.surf.api.minestom.dialog.builder.actionButton
import dev.slne.surf.api.minestom.dialog.dialog
import dev.slne.surf.api.minestom.dialog.type
import dev.slne.surf.jumppad.core.client.dialog.JumpPadButtonTexts
import dev.slne.surf.jumppad.core.client.dialog.JumpPadDialogTexts
import dev.slne.surf.jumppad.core.client.pad.JumpPad
import dev.slne.surf.jumppad.minestom.dialog.edit.JumpPadEditDialog
import net.minestom.server.dialog.DialogActionButton

object JumpPadEditFailResultDialog {
    fun showDialog(pad: JumpPad) = dialog {
        base {
            title(JumpPadDialogTexts.editFailTitle(pad))

            body {
                plainMessage(
                    JumpPadDialogTexts.invalidInputBody,
                    JumpPadDialogTexts.RESULT_BODY_WIDTH
                )
            }
        }

        type {
            notice(backButton(pad))
        }
    }

    private fun backButton(pad: JumpPad): DialogActionButton = actionButton {
        label(JumpPadButtonTexts.backLabel)
        tooltip(JumpPadButtonTexts.backToCreationTooltip)
        action {
            playerCallback {
                it.showDialog(JumpPadEditDialog.showDialog(pad))
            }
        }
    }
}
