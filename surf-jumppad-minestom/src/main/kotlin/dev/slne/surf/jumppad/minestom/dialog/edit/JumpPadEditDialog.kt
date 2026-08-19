package dev.slne.surf.jumppad.minestom.dialog.edit

import dev.slne.surf.api.minestom.dialog.base
import dev.slne.surf.api.minestom.dialog.builder.actionButton
import dev.slne.surf.api.minestom.dialog.dialog
import dev.slne.surf.api.minestom.dialog.type
import dev.slne.surf.jumppad.core.client.dialog.JumpPadButtonTexts
import dev.slne.surf.jumppad.core.client.dialog.JumpPadDialogTexts
import dev.slne.surf.jumppad.core.client.dialog.JumpPadInputTexts
import dev.slne.surf.jumppad.core.client.pad.JumpPad
import dev.slne.surf.jumppad.core.client.pad.JumpPadPosition
import dev.slne.surf.jumppad.core.client.pad.JumpPadType
import dev.slne.surf.jumppad.core.client.pad.input.JumpPadInputs
import dev.slne.surf.jumppad.core.client.pad.service.JumpPadService
import dev.slne.surf.jumppad.minestom.dialog.edit.result.JumpPadEditFailResultDialog
import dev.slne.surf.jumppad.minestom.dialog.view.JumpPadInfoDialog
import dev.slne.surf.jumppad.minestom.pad.toJumpPadPosition
import net.kyori.adventure.key.Key
import net.minestom.server.dialog.DialogActionButton

object JumpPadEditDialog {

    fun showDialog(pad: JumpPad) = dialog {
        base {
            title(JumpPadDialogTexts.editTitle(pad))

            body {
                plainMessage(
                    JumpPadDialogTexts.editBody(pad),
                    JumpPadDialogTexts.EDIT_BODY_WIDTH
                )
            }

            input {
                text(JumpPadInputTexts.LOCATION_KEY) {
                    label(JumpPadInputTexts.editLocationLabel)
                    initial(JumpPadDialogTexts.blockPosition(pad.origin))
                    width(JumpPadInputTexts.INPUT_WIDTH)
                }

                text(JumpPadInputTexts.BOX_KEY) {
                    label(JumpPadInputTexts.boxLabel)
                    initial("${pad.width}x${pad.length}")
                    width(JumpPadInputTexts.INPUT_WIDTH)
                }

                if (pad.type == JumpPadType.STATIC) {
                    text(JumpPadInputTexts.TARGET_LOCATION_KEY) {
                        label(JumpPadInputTexts.editTargetLocationLabel)
                        val target = pad.targetLocation
                        initial(if (target != null) JumpPadDialogTexts.blockPosition(target) else "")
                        width(JumpPadInputTexts.INPUT_WIDTH)
                    }
                } else {
                    numberRange(
                        JumpPadInputTexts.STRENGTH_KEY,
                        JumpPadInputTexts.MIN_STRENGTH..JumpPadInputTexts.MAX_STRENGTH
                    ) {
                        label(JumpPadInputTexts.editStrengthLabel)
                        initial(pad.distance.toFloat())
                        step(1.0f)
                        width(JumpPadInputTexts.INPUT_WIDTH)
                    }
                }
            }
        }

        type {
            confirmation(saveButton(pad), backButton(pad))
        }
    }

    private fun saveButton(oldPad: JumpPad): DialogActionButton = actionButton {
        label(JumpPadButtonTexts.saveLabel)
        tooltip(JumpPadButtonTexts.saveTooltip)
        action {
            customPlayerClick { content, player ->
                val locationString = content.getText(JumpPadInputTexts.LOCATION_KEY) ?: ""
                val boxString = content.getText(JumpPadInputTexts.BOX_KEY) ?: ""

                if (!JumpPadInputs.isLocation(locationString) || !JumpPadInputs.isBox(boxString)) {
                    player.showDialog(JumpPadEditFailResultDialog.showDialog(oldPad))
                    return@customPlayerClick
                }

                val worldKey = player.toJumpPadPosition()?.worldKey ?: return@customPlayerClick
                val origin = parsePosition(locationString, worldKey) ?: return@customPlayerClick
                val box = JumpPadInputs.parseBox(boxString)

                if (!box.isWithinLimit) {
                    player.showDialog(JumpPadEditFailResultDialog.showDialog(oldPad))
                    return@customPlayerClick
                }

                var targetLoc: JumpPadPosition? = null
                var strength = oldPad.distance

                if (oldPad.type == JumpPadType.STATIC) {
                    val targetStr = content.getText(JumpPadInputTexts.TARGET_LOCATION_KEY) ?: ""
                    if (!JumpPadInputs.isLocation(targetStr)) {
                        player.showDialog(JumpPadEditFailResultDialog.showDialog(oldPad))
                        return@customPlayerClick
                    }
                    targetLoc = parsePosition(targetStr, worldKey) ?: return@customPlayerClick
                } else {
                    strength = content.getFloat(JumpPadInputTexts.STRENGTH_KEY)?.toInt()
                        ?: oldPad.distance
                }

                val updatedPad = oldPad.copy(
                    origin = origin,
                    distance = strength,
                    width = box.width,
                    length = box.length,
                    targetLocation = targetLoc
                )

                JumpPadService.updatePad(updatedPad)
                player.showDialog(JumpPadInfoDialog.showDialog(updatedPad))
            }
        }
    }

    private fun backButton(pad: JumpPad): DialogActionButton = actionButton {
        label(JumpPadButtonTexts.plainBackLabel)
        tooltip(JumpPadButtonTexts.cancelTooltip)
        action {
            playerCallback { it.showDialog(JumpPadInfoDialog.showDialog(pad)) }
        }
    }

    private fun parsePosition(raw: String, worldKey: Key): JumpPadPosition? {
        val coordinates = JumpPadInputs.parseCoordinates(raw) ?: return null
        return JumpPadPosition(worldKey, coordinates.x, coordinates.y, coordinates.z)
    }
}
