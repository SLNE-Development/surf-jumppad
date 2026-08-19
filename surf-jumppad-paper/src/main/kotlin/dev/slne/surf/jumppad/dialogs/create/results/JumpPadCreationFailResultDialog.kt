@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.create.results

import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.builder.actionButton
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.jumppad.core.client.dialog.JumpPadButtonTexts
import dev.slne.surf.jumppad.core.client.dialog.JumpPadDialogTexts
import dev.slne.surf.jumppad.dialogs.create.DecideForTypeDialog
import io.papermc.paper.registry.data.dialog.ActionButton

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

    private fun backButton(): ActionButton = actionButton {
        label(JumpPadButtonTexts.backLabel)
        tooltip(JumpPadButtonTexts.backToCreationTooltip)
        action {
            playerCallback {
                it.showDialog(DecideForTypeDialog.showDialog())
            }
        }
    }
}
