@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.edit.result

import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.builder.actionButton
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.jumppad.core.client.dialog.JumpPadButtonTexts
import dev.slne.surf.jumppad.core.client.dialog.JumpPadDialogTexts
import dev.slne.surf.jumppad.core.client.pad.JumpPad
import dev.slne.surf.jumppad.dialogs.edit.JumpPadEditDialog
import io.papermc.paper.registry.data.dialog.ActionButton

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

    private fun backButton(pad: JumpPad): ActionButton = actionButton {
        label(JumpPadButtonTexts.backLabel)
        tooltip(JumpPadButtonTexts.backToCreationTooltip)
        action {
            playerCallback {
                it.showDialog(JumpPadEditDialog.showDialog(pad))
            }
        }
    }
}
