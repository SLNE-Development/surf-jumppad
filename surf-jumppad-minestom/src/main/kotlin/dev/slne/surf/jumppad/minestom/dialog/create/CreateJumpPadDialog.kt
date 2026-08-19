package dev.slne.surf.jumppad.minestom.dialog.create

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
import dev.slne.surf.jumppad.minestom.dialog.JumpPadMainDialog
import dev.slne.surf.jumppad.minestom.dialog.create.results.JumpPadCreateSuccessDialog
import dev.slne.surf.jumppad.minestom.dialog.create.results.JumpPadCreationFailResultDialog
import dev.slne.surf.jumppad.minestom.pad.toJumpPadPosition
import net.kyori.adventure.key.Key
import net.minestom.server.dialog.DialogActionButton
import net.minestom.server.entity.Player
import java.util.*

object CreateJumpPadDialog {

    fun showDialog(player: Player, type: JumpPadType) = dialog {
        val uuid = UUID.randomUUID()
        base {
            title(JumpPadDialogTexts.createTitle)

            body {
                plainMessage(
                    JumpPadDialogTexts.createBody(uuid),
                    JumpPadDialogTexts.CREATE_BODY_WIDTH
                )
            }
            input {
                text(JumpPadInputTexts.LOCATION_KEY) {
                    label(JumpPadInputTexts.createLocationLabel)
                    initial("${player.position.blockX()} ${player.position.blockY()} ${player.position.blockZ()}")
                    width(JumpPadInputTexts.INPUT_WIDTH)
                }
            }
            input {
                text(JumpPadInputTexts.BOX_KEY) {
                    label(JumpPadInputTexts.boxLabel)
                    initial(JumpPadInputTexts.INITIAL_BOX)
                    width(JumpPadInputTexts.INPUT_WIDTH)
                }
            }
            if (type == JumpPadType.STATIC) {
                input {
                    text(JumpPadInputTexts.TARGET_LOCATION_KEY) {
                        label(JumpPadInputTexts.createTargetLocationLabel)
                        initial(JumpPadInputTexts.INITIAL_TARGET_LOCATION)
                        width(JumpPadInputTexts.INPUT_WIDTH)
                    }
                }
            } else {
                input {
                    numberRange(
                        JumpPadInputTexts.STRENGTH_KEY,
                        JumpPadInputTexts.MIN_STRENGTH..JumpPadInputTexts.MAX_STRENGTH
                    ) {
                        label(JumpPadInputTexts.createStrengthLabel)
                        step(1.toFloat())
                        width(JumpPadInputTexts.INPUT_WIDTH)
                    }
                }
            }
        }

        type {
            confirmation(createButton(uuid, type), backButton())
        }
    }

    private fun createButton(uuid: UUID, type: JumpPadType): DialogActionButton = actionButton {
        label(JumpPadButtonTexts.createLabel)
        tooltip(JumpPadButtonTexts.createConfirmTooltip)

        action {
            customPlayerClick { content, player ->
                val locationString = content.getText(JumpPadInputTexts.LOCATION_KEY) ?: ""
                val boxString = content.getText(JumpPadInputTexts.BOX_KEY) ?: ""

                if (!JumpPadInputs.isLocation(locationString) || !JumpPadInputs.isBox(boxString)) {
                    player.showDialog(JumpPadCreationFailResultDialog.showDialog())
                    return@customPlayerClick
                }

                val worldKey = player.toJumpPadPosition()?.worldKey ?: return@customPlayerClick
                val origin = parsePosition(locationString, worldKey) ?: return@customPlayerClick
                val box = JumpPadInputs.parseBox(boxString)

                if (!box.isWithinLimit) {
                    player.showDialog(JumpPadCreationFailResultDialog.showDialog())
                    return@customPlayerClick
                }

                var targetLoc: JumpPadPosition? = null
                var strength = 0

                if (type == JumpPadType.STATIC) {
                    val targetStr = content.getText(JumpPadInputTexts.TARGET_LOCATION_KEY) ?: ""
                    if (!JumpPadInputs.isLocation(targetStr)) {
                        player.showDialog(JumpPadCreationFailResultDialog.showDialog())
                        return@customPlayerClick
                    }
                    targetLoc = parsePosition(targetStr, worldKey) ?: return@customPlayerClick
                } else {
                    strength = content.getFloat(JumpPadInputTexts.STRENGTH_KEY)?.toInt()
                        ?: JumpPadInputTexts.DEFAULT_STRENGTH
                }

                val pad = JumpPad(
                    uuid = uuid,
                    origin = origin,
                    distance = strength,
                    width = box.width,
                    length = box.length,
                    type = type,
                    targetLocation = targetLoc
                )

                JumpPadService.registerPad(pad)
                player.showDialog(JumpPadCreateSuccessDialog.showDialog(pad))
            }
        }
    }

    private fun backButton(): DialogActionButton = actionButton {
        label(JumpPadButtonTexts.backLabel)
        tooltip(JumpPadButtonTexts.cancelTooltip)
        action {
            playerCallback {
                it.showDialog(JumpPadMainDialog.showDialog())
            }
        }
    }

    private fun parsePosition(raw: String, worldKey: Key): JumpPadPosition? {
        val coordinates = JumpPadInputs.parseCoordinates(raw) ?: return null
        return JumpPadPosition(worldKey, coordinates.x, coordinates.y, coordinates.z)
    }
}
