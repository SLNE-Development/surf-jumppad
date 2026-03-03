@file:Suppress("UnstableApiUsage")

package dev.slne.surf.teleporter.dialogs.edit

import dev.slne.surf.jumppad.appendBullet
import dev.slne.surf.jumppad.dialogs.DIALOG_TITLE
import dev.slne.surf.jumppad.dialogs.error.InvalidField
import dev.slne.surf.jumppad.dialogs.error.JumpPadActionType
import dev.slne.surf.jumppad.dialogs.error.JumpPadErrorDialog
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
import io.papermc.paper.registry.data.dialog.ActionButton
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.World

object JumpPadEditDialog {
    private const val LOCATION_KEY = "jumppad_location"
    private const val TARGET_LOCATION_KEY = "jumppad_target_location"
    private const val PEAK_HEIGHT_KEY = "jumppad_peak"
    private const val BOX_KEY = "jumppad_box"

    private val locationRegex by lazy { Regex("^-?\\d+(\\.\\d+)?(?:\\s+-?\\d+(\\.\\d+)?){2}$") }
    private val boxRegex by lazy { Regex("^\\d+x\\d+$") }

    fun createDialog(jumpPad: JumpPad) = dialog {
        base {
            title(DIALOG_TITLE)

            body {
                plainMessage(400) {
                    primary("Du konfigurierst gerade ein JumpPad.", TextDecoration.BOLD, TextDecoration.UNDERLINED)
                    appendNewline(2)

                    info("Im Folgenden siehst du die aktuellen Werte des Teleporters:")
                    appendNewline(2)

                    appendBullet()
                    primary("Typ:")
                    appendSpace()
                    append(jumpPad.type.displayComponent)
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
                    appendNewline(2)
                }
            }

            input {
                text(LOCATION_KEY) {
                    label { text("Startposition (X Y Z)") }
                    initial("${jumpPad.originX} ${jumpPad.originY} ${jumpPad.originZ}")
                    width(400)
                }
            }

            input {
                text(TARGET_LOCATION_KEY) {
                    label { text("Zielposition (X Y Z)") }
                    initial(if (jumpPad.targetX != null) "${jumpPad.targetX} ${jumpPad.targetY} ${jumpPad.targetZ}" else "")
                    width(400)
                }
            }

            input {
                text(PEAK_HEIGHT_KEY) {
                    label { text("Stärke (Blöcke)") }
                    initial(jumpPad.distance.toString())
                    width(400)
                }
            }

            input {
                text(BOX_KEY) {
                    label { text("Boundingbox (max. 10x10)") }
                    initial("${jumpPad.width}x${jumpPad.length}")
                    width(400)
                }
            }
        }

        type {
            confirmation(saveButton(jumpPad), backButton(jumpPad))
        }
    }

    private fun saveButton(jumpPad: JumpPad): ActionButton = actionButton {
        label { success("Speichern") }
        action {
            customPlayerClick { content, player ->
                val locStr = content.getText(LOCATION_KEY) ?: ""
                val targetStr = content.getText(TARGET_LOCATION_KEY) ?: ""
                val peakStr = content.getText(PEAK_HEIGHT_KEY) ?: ""
                val boxStr = content.getText(BOX_KEY) ?: ""

                val invalidFields = mutableListOf<InvalidField>()
                if (!locationRegex.matches(locStr)) invalidFields.add(InvalidField.START_LOCATION)
                if (targetStr.isNotEmpty() && !locationRegex.matches(targetStr)) invalidFields.add(InvalidField.TARGET_LOCATION)
                if (!boxRegex.matches(boxStr)) invalidFields.add(InvalidField.BOX_INVALID)

                val peak = peakStr.toIntOrNull()
                if (peak == null) invalidFields.add(InvalidField.PEAK_INVALID)

                if (invalidFields.isNotEmpty()) {
                    player.showDialog(
                        JumpPadErrorDialog.createDialog(
                            JumpPadActionType.EDIT,
                            invalidFields,
                            emptyMap(),
                            jumpPad,
                            jumpPad.type
                        )
                    )
                    return@customPlayerClick
                }

                val (width, length) = parseBox(boxStr)
                val world = jumpPad.originLocation.world!!

                jumpPad.apply {
                    this.originLocation = parseSimpleLocation(locStr, world)
                    this.targetLocation = if (targetStr.isNotEmpty()) parseSimpleLocation(targetStr, world) else null
                    this.width = width
                    this.length = length
                }
                JumpPadService.savePads()

                player.showDialog(JumpPadSuccessDialog.createDialog(JumpPadActionType.EDIT, jumpPad))
            }
        }
    }

    private fun backButton(jumpPad: JumpPad) = actionButton {
        label { spacer("Zurück") }
        action { playerCallback { it.showDialog(JumpPadInfoDialog.createDialog(jumpPad)) } }
    }

    private fun parseBox(box: String): Pair<Int, Int> {
        val parts = box.split("x").mapNotNull { it.toIntOrNull() }
        return if (parts.size == 2) parts[0] to parts[1] else 1 to 1
    }

    private fun parseSimpleLocation(raw: String, world: World): Location {
        val p = raw.split(" ").map { it.toDoubleOrNull() ?: 0.0 }
        return Location(world, p[0], p[1], p[2])
    }
}