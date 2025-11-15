@file:Suppress("UnstableApiUsage")
@file:OptIn(dev.slne.surf.surfapi.bukkit.api.nms.NmsUseWithCaution::class)

package dev.slne.surf.jumppad.dialogs.view

import dev.slne.surf.jumppad.dialogs.JumpPadMainDialog
import dev.slne.surf.jumppad.dialogs.create.CreateJumpPadDialog
import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.jumpPadService
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.surfapi.core.api.messages.CommonComponents
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase

object JumpPadListDialog {

    fun showDialog(): Dialog {
        val pads = jumpPadService.getPads()

        val dialogList = buildPadDialogList(pads)
        if (dialogList.isEmpty()) {
            return dialog {
                base {
                    title {
                        primary("JUMPPAD ".toSmallCaps())
                        primary("LISTE".toSmallCaps())
                    }
                    body {
                        plainMessage(300) {
                            error("Es existieren aktuell keine JumpPads.")
                        }
                    }
                }
                type {
                    confirmation(backButton(), createButton())
                }
            }
        }

        return dialog {
            base {
                title {
                    primary("JUMPPAD".toSmallCaps())
                    append(CommonComponents.EM_DASH)
                    primary("LISTE".toSmallCaps())
                }
                afterAction(DialogBase.DialogAfterAction.WAIT_FOR_RESPONSE)

                body {
                    plainMessage(400) {
                        info("Aktuell existieren ")
                        variableValue(pads.size)
                        info(" JumpPads.")
                        appendNewline(2)
                        spacer("- ")
                        text("Horizontal: ", Colors.YELLOW)
                        variableValue(pads.count { it.type == JumpPadType.HORIZONTAL })
                        appendNewline()
                        spacer("- ")
                        text("Vertikal: ", Colors.RED)
                        variableValue(pads.count { it.type == JumpPadType.VERTICAL })
                    }
                }
            }

            type {
                dialogList {
                    addAll(dialogList)
                    buttonWidth(200)
                    columns(1)
                    exitAction(backButton())
                }
            }
        }
    }

    private fun buildPadDialogList(pads: Collection<JumpPad>) =
        pads.map { JumpPadInfoDialog.showDialog(it) }.toObjectSet()

    private fun backButton() = actionButton {
        label { spacer("Zurück") }
        tooltip { info("Klicke hier, um zurück zum Hauptmenü zu gelangen.") }
        action {
            playerCallback {
                it.showDialog(JumpPadMainDialog.showDialog())
            }
        }
    }

    private fun createButton() = actionButton {
        label { success("JumpPad erstellen") }
        tooltip { info("Klicke hier, um ein JumpPad zu erstellen.") }
        action {
            playerCallback {
                it.showDialog(CreateJumpPadDialog.showDialog(it))
            }
        }
    }
}
