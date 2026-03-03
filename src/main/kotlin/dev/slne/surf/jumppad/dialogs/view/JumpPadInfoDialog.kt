@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.view

import dev.slne.surf.jumppad.appendBullet
import dev.slne.surf.jumppad.dialogs.DIALOG_TITLE
import dev.slne.surf.jumppad.dialogs.delete.JumpPadDeleteDialog
import dev.slne.surf.jumppad.formatToCoordString
import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.clickCallback
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import io.papermc.paper.dialog.Dialog
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.entity.Player

object JumpPadInfoDialog {
    fun createDialog(jumpPad: JumpPad): Dialog = dialog {
        base {
            title {
                append(DIALOG_TITLE)
                appendSpace()
                variableValue(jumpPad.originLocation.formatToCoordString())
            }
            body {
                plainMessage(400) {
                    primary("Du siehst dir gerade ein JumpPad an.", TextDecoration.BOLD, TextDecoration.UNDERLINED)
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

                    appendBullet()
                    primary("Startwelt:")
                    appendSpace()
                    variableValue(jumpPad.originLocation.world?.name ?: "Unbekannt")
                    appendNewline(2)

                    appendBullet()
                    primary("Zielposition:")
                    appendSpace()

                    variableValue(jumpPad.targetLocation?.formatToCoordString() ?: "Nicht gesetzt")
                    appendNewline(2)

                    appendBullet()
                    primary("Zielwelt:")
                    appendSpace()
                    variableValue(jumpPad.targetLocation?.world?.name ?: "Keine")
                    appendNewline(2)

                    appendBullet()
                    primary("Boundingbox:")
                    appendSpace()
                    variableValue("${jumpPad.width}x${jumpPad.length}")
                    appendNewline(2)
                }
            }
        }
        type {
            multiAction {
                action(teleportButton(jumpPad))
                action(editButton(jumpPad))
                action(deleteButton(jumpPad))

                columns(1)
                exitAction(backButton())
            }
        }
    }

    private fun backButton() = actionButton {
        label { spacer("Zurück") }
        tooltip { info("Klicke hier, um den Vorgang abzubrechen.") }
        action {
            playerCallback {
                it.showDialog(JumpPadListDialog.createDialog())
            }
        }
    }

    private fun deleteButton(jumpPad: JumpPad) = actionButton {
        label { error("Löschen") }
        tooltip { info("Klicke hier, um das JumpPad zu löschen.") }
        action {
            playerCallback {
                it.showDialog(JumpPadDeleteDialog.createDialog(jumpPad))
            }
        }
    }

    internal fun teleportButton(jumpPad: JumpPad) = actionButton {
        label { primary("Teleportieren") }
        tooltip { info("Klicke hier, um dich zum JumpPad zu teleportieren.") }
        action {
            playerCallback { player ->
                player.teleportAsync(jumpPad.originLocation)
                player.sendText {
                    appendSuccessPrefix()
                    success("Du wurdest zum JumpPad mit der UUID")
                    appendSpace()
                    variableValue(jumpPad.uuid.toString())
                    appendSpace()
                    success("teleportiert!")
                    hoverEvent(buildText {
                        error("Klicke, um dir das JumpPad anzusehen.")
                    })
                    clickCallback {
                        val clickPlayer = it as? Player ?: return@clickCallback
                        clickPlayer.showDialog(createDialog(jumpPad))
                    }
                }
                player.closeDialog()
            }
        }
    }

    private fun editButton(jumpPad: JumpPad) = actionButton {
        label { primary("Konfigurieren") }
        tooltip { info("Klicke hier, um die Einstellungen des JumpPads zu konfigurieren.") }
        action {
            playerCallback {
                it.showDialog(JumpPadDeleteDialog.createDialog(jumpPad))
            }
        }
    }
}