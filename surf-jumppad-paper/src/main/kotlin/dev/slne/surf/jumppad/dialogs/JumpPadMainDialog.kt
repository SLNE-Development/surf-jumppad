@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs

import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.builder.actionButton
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.jumppad.core.client.dialog.JumpPadButtonTexts
import dev.slne.surf.jumppad.core.client.dialog.JumpPadDialogTexts
import dev.slne.surf.jumppad.core.client.pad.service.JumpPadService
import dev.slne.surf.jumppad.dialogs.create.DecideForTypeDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadListDialog
import io.papermc.paper.registry.data.dialog.ActionButton

object JumpPadMainDialog {
    fun showDialog() = dialog {
        base {
            val pads = JumpPadService.getPads()
            title(JumpPadDialogTexts.mainTitle)
            body {
                plainMessage(
                    JumpPadDialogTexts.mainBody(pads),
                    JumpPadDialogTexts.MAIN_BODY_WIDTH
                )
            }
        }

        type {
            multiAction {
                action(createPadButton())
                action(showPadsButton())
                exitAction(exitButton())
            }
        }
    }

    private fun createPadButton(): ActionButton = actionButton {
        label(JumpPadButtonTexts.createPadLabel)
        tooltip(JumpPadButtonTexts.createPadTooltip)
        action {
            playerCallback {
                it.showDialog(DecideForTypeDialog.showDialog())
            }
        }
    }

    internal fun showPadsButton(): ActionButton = actionButton {
        label(JumpPadButtonTexts.showPadsLabel)
        tooltip(JumpPadButtonTexts.showPadsTooltip)
        action {
            playerCallback {
                it.showDialog(JumpPadListDialog.showDialog())
            }
        }
    }

    private fun exitButton(): ActionButton = actionButton {
        label(JumpPadButtonTexts.closeLabel)
        tooltip(JumpPadButtonTexts.closeTooltip)
        action {
            playerCallback {
                it.closeDialog()
            }
        }
    }
}
