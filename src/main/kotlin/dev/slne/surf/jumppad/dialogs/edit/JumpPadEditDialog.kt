@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.edit

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.appendNewline
import dev.slne.surf.api.paper.dialog.base
import dev.slne.surf.api.paper.dialog.builder.actionButton
import dev.slne.surf.api.paper.dialog.dialog
import dev.slne.surf.api.paper.dialog.type
import dev.slne.surf.jumppad.dialogs.edit.result.JumpPadEditFailResultDialog
import dev.slne.surf.jumppad.dialogs.view.JumpPadInfoDialog
import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.service.JumpPadService
import io.papermc.paper.registry.data.dialog.ActionButton
import org.bukkit.Location
import org.bukkit.World

object JumpPadEditDialog {
    private const val LOCATION_KEY = "pad_location"
    private const val STRENGTH_KEY = "pad_strength"
    private const val BOX_KEY = "pad_box"
    private const val TARGET_LOCATION_KEY = "pad_target_location"

    private val locationRegex by lazy { Regex("^-?\\d+\\s-?\\d+\\s-?\\d+\$") }
    private val boxRegex by lazy { Regex("^\\d+x\\d+$") }

    fun showDialog(pad: JumpPad) = dialog {
        base {
            title {
                primary("JUMPPAD ".toSmallCaps())
                success("EDITIEREN ".toSmallCaps())
                variableValue("${pad.origin.blockX} ${pad.origin.blockY} ${pad.origin.blockZ}")
            }

            body {
                plainMessage(450) {
                    info("Du bearbeitest das JumpPad:")
                    variableValue(" ${pad.uuid}")
                    appendNewline(2)

                    primary("Typ: ")
                    append(pad.type.displayComponent)
                    appendNewline()

                    if (pad.type == JumpPadType.STATIC) {
                        primary("Ziel: ")
                        val target = pad.targetLocation
                        variableValue(if (target != null) "${target.blockX} ${target.blockY} ${target.blockZ}" else "Nicht gesetzt")
                    } else {
                        primary("Power: ")
                        variableValue("${pad.distance} Blöcke")
                    }
                    appendNewline()

                    primary("Area: ")
                    variableValue("${pad.width}x${pad.length}")
                    appendNewline(2)

                    info("Passe die Werte über die unteren Felder an.")
                }
            }

            input {
                text(LOCATION_KEY) {
                    label { text("Location (X Y Z)") }
                    initial("${pad.origin.blockX} ${pad.origin.blockY} ${pad.origin.blockZ}")
                    width(400)
                }

                text(BOX_KEY) {
                    label { text("Box (max. 10x10)") }
                    initial("${pad.width}x${pad.length}")
                    width(400)
                }

                if (pad.type == JumpPadType.STATIC) {
                    text(TARGET_LOCATION_KEY) {
                        label { text("Ziel-Location (X Y Z)") }
                        val target = pad.targetLocation
                        initial(if (target != null) "${target.blockX} ${target.blockY} ${target.blockZ}" else "")
                        width(400)
                    }
                } else {
                    numberRange(STRENGTH_KEY, 1.0..200.0) {
                        label { text("Stärke") }
                        initial(pad.distance.toFloat())
                        step(1.0f)
                        width(400)
                    }
                }
            }
        }

        type {
            confirmation(saveButton(pad), backButton(pad))
        }
    }

    private fun saveButton(oldPad: JumpPad): ActionButton = actionButton {
        label { success("Änderungen speichern") }
        tooltip { info("Klicke hier, um die Änderungen zu übernehmen.") }
        action {
            customPlayerClick { content, player ->
                val locationString = content.getText(LOCATION_KEY) ?: ""
                val boxString = content.getText(BOX_KEY) ?: ""

                if (!locationRegex.matches(locationString) || !boxRegex.matches(boxString)) {
                    player.showDialog(JumpPadEditFailResultDialog.showDialog(oldPad))
                    return@customPlayerClick
                }

                val world = player.location.world ?: return@customPlayerClick
                val origin = parseLocation(locationString, world)
                val (width, length) = parseBox(boxString)

                if (width > 10 || length > 10) {
                    player.showDialog(JumpPadEditFailResultDialog.showDialog(oldPad))
                    return@customPlayerClick
                }

                var targetLoc: Location? = null
                var strength = oldPad.distance

                if (oldPad.type == JumpPadType.STATIC) {
                    val targetStr = content.getText(TARGET_LOCATION_KEY) ?: ""
                    if (!locationRegex.matches(targetStr)) {
                        player.showDialog(JumpPadEditFailResultDialog.showDialog(oldPad))
                        return@customPlayerClick
                    }
                    targetLoc = parseLocation(targetStr, world)
                } else {
                    strength = content.getFloat(STRENGTH_KEY)?.toInt() ?: oldPad.distance
                }

                val updatedPad = oldPad.copy(
                    origin = origin,
                    distance = strength,
                    width = width,
                    length = length,
                    targetLocation = targetLoc
                )

                JumpPadService.updatePad(updatedPad)
                player.showDialog(JumpPadInfoDialog.showDialog(updatedPad))
            }
        }
    }

    private fun backButton(pad: JumpPad): ActionButton = actionButton {
        label { text("Zurück") }
        tooltip { info("Klicke hier, um den Vorgang abzubrechen.") }
        action {
            playerCallback { it.showDialog(JumpPadInfoDialog.showDialog(pad)) }
        }
    }

    private fun parseBox(box: String): Pair<Int, Int> {
        val parts = box.split("x").mapNotNull { it.toIntOrNull() }
        return if (parts.size == 2) parts[0] to parts[1] else 3 to 3
    }

    private fun parseLocation(raw: String, world: World): Location {
        val parts = raw.trim().split(Regex("\\s+")).mapNotNull { it.toDoubleOrNull() }
        if (parts.size < 3) return world.spawnLocation
        return Location(world, parts[0], parts[1], parts[2])
    }
}