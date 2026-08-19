package dev.slne.surf.jumppad.minestom.dialog

import dev.slne.surf.api.minestom.dialog.base
import dev.slne.surf.api.minestom.dialog.builder.actionButton
import dev.slne.surf.api.minestom.dialog.dialog
import dev.slne.surf.api.minestom.dialog.type
import dev.slne.surf.jumppad.core.client.dialog.JumpPadButtonTexts
import dev.slne.surf.jumppad.core.client.dialog.JumpPadDialogTexts
import dev.slne.surf.jumppad.core.client.pad.service.JumpPadService
import dev.slne.surf.jumppad.minestom.dialog.create.DecideForTypeDialog
import dev.slne.surf.jumppad.minestom.dialog.view.JumpPadListDialog
import net.minestom.server.dialog.DialogActionButton

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

    private fun createPadButton(): DialogActionButton = actionButton {
        label(JumpPadButtonTexts.createPadLabel)
        tooltip(JumpPadButtonTexts.createPadTooltip)
        action {
            playerCallback {
                it.showDialog(DecideForTypeDialog.showDialog())
            }
        }
    }

    internal fun showPadsButton(): DialogActionButton = actionButton {
        label(JumpPadButtonTexts.showPadsLabel)
        tooltip(JumpPadButtonTexts.showPadsTooltip)
        action {
            playerCallback {
                it.showDialog(JumpPadListDialog.showDialog())
            }
        }
    }

    private fun exitButton(): DialogActionButton = actionButton {
        label(JumpPadButtonTexts.closeLabel)
        tooltip(JumpPadButtonTexts.closeTooltip)
        action {
            playerCallback {
                it.closeDialog()
            }
        }
    }
}
