@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.view

import dev.slne.surf.api.core.util.toObjectSet
import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.builder.actionButton
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.jumppad.core.client.dialog.JumpPadButtonTexts
import dev.slne.surf.jumppad.core.client.dialog.JumpPadDialogTexts
import dev.slne.surf.jumppad.core.client.pad.JumpPad
import dev.slne.surf.jumppad.core.client.pad.service.JumpPadService
import dev.slne.surf.jumppad.dialogs.JumpPadMainDialog
import dev.slne.surf.jumppad.dialogs.create.DecideForTypeDialog
import io.papermc.paper.dialog.Dialog

object JumpPadListDialog {
    fun showDialog(): Dialog {
        val pads = JumpPadService.getPads()

        val dialogList = buildPadDialogList(pads)
        if (dialogList.isEmpty()) {
            return dialog {
                base {
                    title(JumpPadDialogTexts.listTitle)
                    body {
                        plainMessage(
                            JumpPadDialogTexts.emptyListBody,
                            JumpPadDialogTexts.EMPTY_LIST_BODY_WIDTH
                        )
                    }
                }
                type {
                    confirmation(createButton(), backButton())
                }
            }
        }

        return dialog {
            base {
                title(JumpPadDialogTexts.listTitle)

                body {
                    plainMessage(
                        JumpPadDialogTexts.listBody(pads),
                        JumpPadDialogTexts.LIST_BODY_WIDTH
                    )
                }
            }

            type {
                dialogList {
                    addAll(dialogList)
                    buttonWidth(JumpPadDialogTexts.LIST_BUTTON_WIDTH)
                    columns(JumpPadDialogTexts.LIST_COLUMNS)
                    exitAction(backButton())
                }
            }
        }
    }

    private fun buildPadDialogList(pads: Collection<JumpPad>) =
        pads.map { JumpPadInfoDialog.showDialog(it) }.toObjectSet()

    private fun backButton() = actionButton {
        label(JumpPadButtonTexts.backLabel)
        tooltip(JumpPadButtonTexts.backToMainTooltip)
        action {
            playerCallback {
                it.showDialog(JumpPadMainDialog.showDialog())
            }
        }
    }

    private fun createButton() = actionButton {
        label(JumpPadButtonTexts.createLabel)
        tooltip(JumpPadButtonTexts.createFromListTooltip)
        action {
            playerCallback {
                it.showDialog(DecideForTypeDialog.showDialog())
            }
        }
    }
}
