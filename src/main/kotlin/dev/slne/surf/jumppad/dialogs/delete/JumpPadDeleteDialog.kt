@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.delete

import dev.slne.surf.jumppad.appendBullet
import dev.slne.surf.jumppad.dialogs.DIALOG_TITLE
import dev.slne.surf.jumppad.dialogs.error.JumpPadActionType
import dev.slne.surf.jumppad.dialogs.error.JumpPadSuccessDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadInfoDialog
import dev.slne.surf.jumppad.formatToCoordString
import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.service.JumpPadService
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import io.papermc.paper.dialog.Dialog
import net.kyori.adventure.text.format.TextDecoration

object JumpPadDeleteDialog {
    fun createDialog(jumpPad: JumpPad): Dialog = dialog {
        base {
            title(DIALOG_TITLE)

            body {
                plainMessage(400) {
                    error(
                        "Du bist dabei eine JumpPad unwiderruflich zu löschen!",
                        TextDecoration.BOLD,
                        TextDecoration.UNDERLINED
                    )
                    appendNewline(2)

                    error("Bitte bestätige dein Vorhaben!")
                    appendNewline(2)

                    info("Aktuelle Werte des JumpPads:")
                    appendNewline(2)

                    appendBullet()
                    primary("UUID:")
                    appendSpace()
                    variableValue(jumpPad.uuid.toString())
                    appendNewline(2)

                    appendBullet()
                    primary("Startposition:")
                    appendSpace()
                    variableValue(jumpPad.originLocation.formatToCoordString())
                    appendNewline(2)

                    if (jumpPad.type == JumpPadType.STATIC) {
                        appendBullet()
                        primary("Zielposition:")
                        appendSpace()
                        variableValue(jumpPad.targetLocation?.formatToCoordString() ?: "Kein festes Ziel")
                        appendNewline(2)
                    } else {
                        appendBullet()
                        primary("Stärke (Blöcke):")
                        appendSpace()
                        variableValue(jumpPad.distance)
                        appendNewline(2)
                    }

                    appendBullet()
                    primary("Welt:")
                    appendSpace()
                    variableValue(jumpPad.originLocation.world?.name ?: "Unbekannt")
                    appendNewline(2)

                    appendBullet()
                    primary("Boundingbox:")
                    appendSpace()
                    variableValue("${jumpPad.width}x${jumpPad.length}")
                }

            }
            type {
                confirmation(confirmButton(jumpPad), backButton(jumpPad))
            }
        }
    }

    private fun backButton(jumpPad: JumpPad) = actionButton {
        label { spacer("Zurück") }
        tooltip { info("Klicke hier, um den Vorgang abzubrechen.") }
        action {
            playerCallback {
                it.showDialog(JumpPadInfoDialog.createDialog(jumpPad))
            }
        }
    }

    private fun confirmButton(jumpPad: JumpPad) = actionButton {
        label { error("Löschen") }
        tooltip { info("Klicke hier, um das JumpPad zu löschen.") }
        action {
            playerCallback {
                JumpPadService.unregisterPad(jumpPad)
                it.showDialog(JumpPadSuccessDialog.createDialog(JumpPadActionType.DELETE, null))
            }
        }
    }
}