@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.edit

import dev.slne.surf.jumppad.dialogs.create.results.JumpPadCreationFailResultDialog
import dev.slne.surf.jumppad.dialogs.edit.result.JumpPadEditFailResultDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadInfoDialog
import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.service.jumpPadService
import dev.slne.surf.surfapi.bukkit.api.dialog.base
import dev.slne.surf.surfapi.bukkit.api.dialog.builder.actionButton
import dev.slne.surf.surfapi.bukkit.api.dialog.dialog
import dev.slne.surf.surfapi.bukkit.api.dialog.type
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import io.papermc.paper.registry.data.dialog.ActionButton
import org.bukkit.Location
import org.bukkit.World

object JumpPadEditDialog {
    private const val LOCATION_KEY = "pad_location"
    private const val STRENGTH_KEY = "pad_strength"
    private const val BOX_KEY = "pad_box"
    private const val TYPE_KEY = "pad_type"

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
                        append(pad.type.displayComponent)
                        appendNewline(2)

                        primary("Position: ")
                        variableValue("${pad.origin.blockX} ${pad.origin.blockY} ${pad.origin.blockZ} ")
                        appendNewline(2)

                        primary("Welt: ")
                        variableValue(pad.origin.world?.name ?: "Unbekannt")
                        appendNewline(2)

                        primary("Stärke: ")
                        variableValue(pad.distance.toString())
                        appendNewline(2)

                        primary("Box: ")
                        variableValue("${pad.width}x${pad.length}")
                        appendNewline(2)
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
                        label { text("Box (max. 10x10)") }
                        initial("${pad.width}x${pad.length}")
                        width(400)
                    }
                }
                input {
                    numberRange(STRENGTH_KEY, 1.0..200.0) {
                        label { text("Stärke") }
                        initial(pad.distance.toFloat())
                        step(1.0f)
                        width(400)
                    }
                }
                input {
                    singleOption(TYPE_KEY) {
                        label { text("JumpPad-Typ") }
                        JumpPadType.entries.forEach { type ->
                            option(type.name, type.displayComponent)
                        }
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
            customPlayerClick { content, player ->
                val locationString = content.getText(LOCATION_KEY) ?: ""
                val strengthFloat = content.getFloat(STRENGTH_KEY) ?: 0.0f
                val boxString = content.getText(BOX_KEY) ?: ""

                if (!locationRegex.matches(locationString) || !boxRegex.matches(boxString)) {
                    player.showDialog(JumpPadEditFailResultDialog.showDialog(oldPad))
                    return@customPlayerClick
                }

                val origin = parseLocation(locationString, player.location.world ?: return@customPlayerClick)
                val (width, length) = parseBox(boxString)

                if (width > 10 || length > 10) {
                    player.showDialog(JumpPadCreationFailResultDialog.showDialog())
                    return@customPlayerClick
                }

                val type = content.getText(TYPE_KEY)?.let {
                    runCatching { JumpPadType.valueOf(it) }.getOrNull()
                } ?: oldPad.type

                val updatedPad = oldPad.copy(
                    origin = origin,
                    distance = strengthFloat.toInt(),
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
        label { text("Zurück") }
        tooltip { info("Klicke hier, um den Vorgang abzubrechen.") }
        action {
            playerCallback {
                it.showDialog(JumpPadInfoDialog.showDialog(pad))
            }
        }
    }

    private fun parseBox(box: String): Pair<Int, Int> {
        val parts = box.split("x").mapNotNull { it.toIntOrNull() }
        return if (parts.size == 2) parts[0] to parts[1] else 3 to 3
    }

    private fun parseLocation(raw: String, world: World): Location {
        val parts = raw.trim().split(" ").mapNotNull { it.toDoubleOrNull() }
        if (parts.size < 3) return world.spawnLocation
        return Location(world, parts[0], parts[1], parts[2])
    }
}