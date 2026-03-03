@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.create

import dev.slne.surf.jumppad.appendBullet
import dev.slne.surf.jumppad.dialogs.DIALOG_TITLE
import dev.slne.surf.jumppad.dialogs.error.InvalidField
import dev.slne.surf.jumppad.dialogs.error.JumpPadActionType
import dev.slne.surf.jumppad.dialogs.error.JumpPadErrorDialog
import dev.slne.surf.jumppad.dialogs.error.JumpPadSuccessDialog
import dev.slne.surf.jumppad.formatToCoordString
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.jumpPad
import dev.slne.surf.jumppad.pad.service.JumpPadService
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import io.papermc.paper.registry.data.dialog.ActionButton
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.*

object CreateJumpPadDialog {
    private const val LOCATION_KEY = "pad_location"
    private const val STRENGTH_KEY = "pad_strength"
    private const val BOX_KEY = "pad_box"
    private const val TARGET_LOCATION_KEY = "pad_target_location"

    private val locationRegex by lazy { Regex("^-?\\d+(\\.\\d+)?\\s-?\\d+(\\.\\d+)?\\s-?\\d+(\\.\\d+)?$") }
    private val boxRegex by lazy { Regex("^\\d+x\\d+$") }

    fun createDialog(
        player: Player,
        type: JumpPadType,
        previousValues: Map<String, String>? = null
    ) = dialog {
        val uuid = JumpPadService.generateUnusedId()
        base {
            title(DIALOG_TITLE)

            body {
                plainMessage(400) {
                    primary(
                        "Du bist dabei ein neues JumpPad zu erstellen.",
                        TextDecoration.BOLD,
                        TextDecoration.UNDERLINED
                    )
                    appendNewline(2)

                    appendBullet()
                    primary("UUID:")
                    appendSpace()
                    variableValue(uuid.toString())
                    appendNewline(2)

                    appendBullet()
                    primary("Ausgewählter Typ:")
                    appendSpace()
                    append(type.displayComponent)
                }
            }

            input {
                text(LOCATION_KEY) {
                    label { text("Startposition (X Y Z)") }
                    initial(previousValues?.get(LOCATION_KEY) ?: player.location.formatToCoordString())
                    width(400)
                }
            }

            if (type == JumpPadType.STATIC) {
                input {
                    text(TARGET_LOCATION_KEY) {
                        label { text("Zielposition (X Y Z)") }
                        initial(previousValues?.get(TARGET_LOCATION_KEY) ?: "0.00 100.00 0.00")
                        width(400)
                    }
                }
            } else {
                input {
                    val initialStrength = previousValues?.get(STRENGTH_KEY)?.toDoubleOrNull() ?: 10.0
                    numberRange(STRENGTH_KEY, 1.0..200.0) {
                        label { text("Stärke (Blöcke)") }
                        initial(initialStrength.toFloat())
                        step(1f)
                        width(400)
                    }
                }
            }

            input {
                text(BOX_KEY) {
                    label { text("Boundingbox (max. 10x10)") }
                    initial(previousValues?.get(BOX_KEY) ?: "3x3")
                    width(400)
                }
            }
        }

        type {
            confirmation(createButton(uuid, type), backButton())
        }
    }

    private fun createButton(uuid: UUID, type: JumpPadType): ActionButton = actionButton {
        label { success("JumpPad erstellen") }
        tooltip { info("Klicke hier, um das JumpPad zu erstellen.") }

        action {
            customPlayerClick { content, player ->
                val invalidFields = mutableListOf<InvalidField>()
                val values = mutableMapOf<String, String>()

                val locStr = content.getText(LOCATION_KEY) ?: ""
                val boxStr = content.getText(BOX_KEY) ?: ""
                values[LOCATION_KEY] = locStr
                values[BOX_KEY] = boxStr

                if (!locationRegex.matches(locStr)) {
                    invalidFields.add(InvalidField.START_LOCATION)
                }

                if (!boxRegex.matches(boxStr)) {
                    invalidFields.add(InvalidField.BOX_INVALID)
                } else {
                    val (w, l) = parseBox(boxStr)
                    if (w > 10 || l > 10) invalidFields.add(InvalidField.BOX_SIZE)
                }

                var targetLoc: Location? = null
                var strength = 0

                if (type == JumpPadType.STATIC) {
                    val targetStr = content.getText(TARGET_LOCATION_KEY) ?: ""
                    values[TARGET_LOCATION_KEY] = targetStr
                    if (!locationRegex.matches(targetStr)) {
                        invalidFields.add(InvalidField.TARGET_LOCATION)
                    } else {
                        targetLoc = parseLocation(targetStr, player.location.world)
                    }
                } else {
                    val strengthVal = content.getFloat(STRENGTH_KEY)
                    values[STRENGTH_KEY] = strengthVal?.toString() ?: "10"
                    strength = strengthVal?.toInt() ?: 10
                }

                if (invalidFields.isNotEmpty()) {
                    player.showDialog(
                        JumpPadErrorDialog.createDialog(
                            actionType = JumpPadActionType.CREATE,
                            invalidFields = invalidFields,
                            previousValues = values,
                            padType = type
                        )
                    )
                    return@customPlayerClick
                }

                val origin = parseLocation(locStr, player.location.world)
                val (width, length) = parseBox(boxStr)

                val pad = jumpPad(
                    uuid = uuid,
                    origin = origin,
                    type = type,
                    distance = if (type == JumpPadType.STATIC) 0 else strength,
                    width = width,
                    length = length,
                    target = if (type == JumpPadType.STATIC) targetLoc else null
                )

                JumpPadService.registerPad(pad)
                player.showDialog(JumpPadSuccessDialog.createDialog(JumpPadActionType.CREATE, pad))
            }
        }
    }

    private fun backButton(): ActionButton = actionButton {
        label { spacer("Zurück") }
        action {
            playerCallback { it.showDialog(DecideForTypeDialog.createDialog()) }
        }
    }

    private fun parseBox(box: String): Pair<Int, Int> {
        val parts = box.split("x").mapNotNull { it.toIntOrNull() }
        return if (parts.size == 2) parts[0] to parts[1] else 3 to 3
    }

    private fun parseLocation(raw: String, world: World): Location {
        val parts = raw.trim().split(" ")
        val x = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
        val y = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
        val z = parts.getOrNull(2)?.toDoubleOrNull() ?: 0.0
        return Location(world, x, y, z)
    }
}