@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.error

import dev.slne.surf.jumppad.dialogs.DIALOG_TITLE
import dev.slne.surf.jumppad.dialogs.create.CreateJumpPadDialog
import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.teleporter.dialogs.edit.JumpPadEditDialog
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration

enum class InvalidField(val message: String) {
    START_LOCATION("Die Startposition wurde nicht korrekt angegeben."),
    TARGET_LOCATION("Die Zielposition wurde nicht korrekt angegeben."),
    PEAK_INVALID("Die Sprunghöhe ist keine gültige Zahl."),
    BOX_SIZE("Die angegebene Box überschreitet die maximale Größe von 10x10."),
    BOX_INVALID("Das Format der Box ist ungültig. Verwende z. B. 3x3.")
}

object JumpPadErrorDialog {

    fun createDialog(
        actionType: JumpPadActionType,
        invalidFields: List<InvalidField>,
        previousValues: Map<String, String>,
        jumpPad: JumpPad? = null,
        padType: JumpPadType
    ) = dialog {
        base {
            title(DIALOG_TITLE)

            body {
                plainMessage(400) {
                    error("Es ist ein Fehler aufgetreten!", TextDecoration.BOLD, TextDecoration.UNDERLINED)
                    appendNewline(2)

                    error("Die folgenden Felder wurden nicht korrekt ausgefüllt:")
                    appendNewline(2)

                    appendCollectionNewLine(
                        collection = invalidFields,
                        linePrefix = Component.empty()
                    ) { field ->
                        buildText {
                            variableValue(field.message)
                        }
                    }

                    appendNewline(2)
                    error("Bitte korrigiere die Eingaben und versuche es erneut.")
                }
            }
        }

        type {
            notice(backButton(actionType, jumpPad, previousValues, padType))
        }
    }

    private fun backButton(
        type: JumpPadActionType,
        jumpPad: JumpPad?,
        previousValues: Map<String, String>,
        padType: JumpPadType
    ) = actionButton {
        label { spacer("Zurück") }
        action {
            playerCallback {
                when (type) {
                    JumpPadActionType.CREATE ->
                        it.showDialog(CreateJumpPadDialog.createDialog(it, padType, previousValues))

                    JumpPadActionType.EDIT ->
                        it.showDialog(JumpPadEditDialog.createDialog(jumpPad!!))

                    else -> {
                        it.closeDialog()
                        it.sendText {
                            appendErrorPrefix()
                            error("Es ist ein Fehler aufgetreten!")
                        }
                    }
                }
            }
        }
    }
}