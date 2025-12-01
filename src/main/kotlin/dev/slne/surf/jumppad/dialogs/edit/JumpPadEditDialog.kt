@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.edit

import dev.slne.surf.jumppad.dialogs.edit.result.JumpPadEditFailResultDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadInfoDialog
import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.jumpPadService
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import io.papermc.paper.registry.data.dialog.ActionButton
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player

object JumpPadEditDialog {
    private const val LOCATION_KEY = "pad_location"
    private const val STRENGTH_KEY = "pad_strength"
    private const val BOX_KEY = "pad_box"
    private const val TYPE_KEY = "pad_type"

    private const val TYPE_KEY_HORIZONTAL_NORTH = "pad_type_horizontal_north"
    private const val TYPE_KEY_HORIZONTAL_SOUTH = "pad_type_horizontal_south"
    private const val TYPE_KEY_HORIZONTAL_EAST = "pad_type_horizontal_east"
    private const val TYPE_KEY_HORIZONTAL_WEST = "pad_type_horizontal_west"
    private const val TYPE_KEY_VERTICAL = "pad_type_vertical"

    private val locationRegex by lazy { Regex("^-?\\d+\\s-?\\d+\\s-?\\d+\$") }
    private val boxRegex by lazy { Regex("^\\d+x\\d+$") }

    fun showDialog(pad: JumpPad) = dialog {
        base {
            title {
                primary("JUMPPAD ".toSmallCaps())
                primary("LISTE ".toSmallCaps())
                success("KONFIGURIEREN ".toSmallCaps())
                variableValue("${pad.origin.blockX} ${pad.origin.blockY} ${pad.origin.blockZ} ")

                body {
                    plainMessage(400) {
                        info("Du konfigurierst gerade ein JumpPad.")
                        appendNewline(2)
                        primary("UUID: ")
                        variableValue(pad.uuid.toString())
                        appendNewline(2)

                        info("Im Folgenden siehst du die aktuellen Werte des JumpPads.")
                        appendNewline(2)

                        primary("Typ: ")
                        variableValue(pad.type.name)
                        appendNewline(2)

                        primary("Position: ")
                        variableValue("${pad.origin.blockX} ${pad.origin.blockY} ${pad.origin.blockZ} ")
                        appendNewline(2)

                        primary("Welt: ")
                        variableValue(pad.origin.world?.name.toString())
                        appendNewline(2)

                        primary("Stärke: ")
                        variableValue(pad.strength.toString())
                        appendNewline(2)

                        primary("Breite: ")
                        variableValue(pad.width.toString())
                        appendNewline(2)

                        primary("Länge: ")
                        variableValue(pad.length.toString())
                    }
                }
                input {
                    text(LOCATION_KEY) {
                        label { text("Location") }
                        initial("${pad.origin.blockX} ${pad.origin.blockY} ${pad.origin.blockZ}")
                        width(400)
                    }
                }
                input {
                    text(BOX_KEY) {
                        label { text("Box") }
                        initial("${pad.width}x${pad.length}")
                        width(400)
                    }
                }
                input {
                    numberRange(STRENGTH_KEY, 1.0..10.0) {
                        label { text("Stärke") }
                        initial(pad.strength.toFloat())
                        step(.1f)
                        width(400)
                    }
                }
                input {
                    singleOption(TYPE_KEY) {
                        label { text("JumpPad-Typ") }
                        option(TYPE_KEY_HORIZONTAL_NORTH, buildText { text("Horizontal Nord") })
                        option(TYPE_KEY_HORIZONTAL_SOUTH, buildText { text("Horizontal Süd") })
                        option(TYPE_KEY_HORIZONTAL_EAST, buildText { text("Horizontal Ost") })
                        option(TYPE_KEY_HORIZONTAL_WEST, buildText { text("Horizontal West") })
                        option(TYPE_KEY_VERTICAL, buildText { text("Vertikal") })
                    }
                }
            }

            type {
                confirmation(saveButton(pad), backButton(pad))
            }
        }
    }

    private fun saveButton(oldPad: JumpPad): ActionButton = actionButton {
        label { success("Änderungen speichern") }
        tooltip { info("Klicke hier, um die Änderungen zu übernehmen.") }
        action {
            customClick { content, audience ->
                val player = audience as? Player ?: return@customClick

                val locationString = content.getText(LOCATION_KEY) ?: ""
                val strengthFloat = content.getFloat(STRENGTH_KEY) ?: 0.0f
                val boxString = content.getText(BOX_KEY) ?: ""

                val validLocation = locationRegex.matches(locationString)
                val validBox = boxRegex.matches(boxString)

                if (!validLocation || !validBox) {
                    player.showDialog(JumpPadEditFailResultDialog.showDialog(oldPad))
                    return@customClick
                }
                val origin = parseLocation(locationString, player.location.world)
                val (width, length) = parseBox(boxString)
                val strength = strengthFloat.toDouble()

                val type = when (content.getText(TYPE_KEY)) {
                    TYPE_KEY_VERTICAL -> JumpPadType.VERTICAL
                    TYPE_KEY_HORIZONTAL_NORTH -> JumpPadType.HORIZONTAL_NORTH
                    TYPE_KEY_HORIZONTAL_SOUTH -> JumpPadType.HORIZONTAL_SOUTH
                    TYPE_KEY_HORIZONTAL_EAST -> JumpPadType.HORIZONTAL_EAST
                    TYPE_KEY_HORIZONTAL_WEST -> JumpPadType.HORIZONTAL_WEST
                    else -> oldPad.type
                }

                val updatedPad = oldPad.copy(
                    uuid = oldPad.uuid,
                    origin = origin,
                    strength = strength,
                    width = width,
                    length = length,
                    type = type
                )

                jumpPadService.updatePad(updatedPad)
                player.showDialog(JumpPadInfoDialog.showDialog(updatedPad))
            }
        }
    }

    private fun backButton(pad: JumpPad): ActionButton = actionButton {
        label { spacer("Zurück") }
        tooltip { info("Klicke hier, um den Vorgang abzubrechen.") }
        action {
            playerCallback {
                it.showDialog(JumpPadInfoDialog.showDialog(pad))
            }
        }
    }

    private fun parseBox(box: String): Pair<Int, Int> {
        return box.split("x").mapNotNull { it.toIntOrNull() }.let {
            if (it.size == 2) it[0] to it[1] else 3 to 3
        }
    }

    private fun parseLocation(raw: String, world: World): Location {
        val parts = raw.trim().split(" ")
        if (parts.size < 3) throw IllegalArgumentException("Ungültiges Location-Format: $raw")
        val x = parts[0].toDoubleOrNull() ?: 0.0
        val y = parts[1].toDoubleOrNull() ?: 0.0
        val z = parts[2].toDoubleOrNull() ?: 0.0
        return Location(world, x, y, z)
    }
}
