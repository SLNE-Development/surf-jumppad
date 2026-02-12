@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.create

import dev.slne.surf.jumppad.dialogs.JumpPadMainDialog
import dev.slne.surf.jumppad.dialogs.create.results.JumpPadCreateSuccessDialog
import dev.slne.surf.jumppad.dialogs.create.results.JumpPadCreationFailResultDialog
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
import org.bukkit.entity.Player
import java.util.*

object CreateJumpPadDialog {
    private const val LOCATION_KEY = "pad_location"
    private const val STRENGTH_KEY = "pad_strength"
    private const val BOX_KEY = "pad_box"
    private const val TARGET_LOCATION_KEY = "pad_target_location"

    private val locationRegex by lazy { Regex("^-?\\d+\\s-?\\d+\\s-?\\d+$") }
    private val boxRegex by lazy { Regex("^\\d+x\\d+$") }

    fun showDialog(player: Player, type: JumpPadType) = dialog {
        val uuid = UUID.randomUUID()
        base {
            title {
                primary("JUMPPAD ".toSmallCaps())
                success("ERSTELLEN".toSmallCaps())
            }

            body {
                plainMessage(400) {
                    info("Du bist dabei ein neues JumpPad zu erstellen.")
                    appendNewline(2)

                    primary("UUID: ")
                    variableValue(uuid.toString())
                    appendNewline(2)
                }
            }
            input {
                text(LOCATION_KEY) {
                    label { text("Location") }
                    initial("${player.location.blockX} ${player.location.blockY} ${player.location.blockZ}")
                    width(400)
                }
            }
            input {
                text(BOX_KEY) {
                    label { text("Box (max. 10x10)") }
                    initial("3x3")
                    width(400)
                }
            }
            if (type == JumpPadType.STATIC) {
                input {
                    text(TARGET_LOCATION_KEY) {
                        label { text("Location") }
                        initial("X Y Z")
                        width(400)
                    }
                }
            } else {
                input {
                    numberRange(STRENGTH_KEY, 1.0..200.0) {
                        label { text("Stärke (Blöcke)") }
                        step(1.toFloat())
                        width(400)
                    }
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
                val locationString = content.getText(LOCATION_KEY) ?: ""
                val boxString = content.getText(BOX_KEY) ?: ""

                if (!locationRegex.matches(locationString) || !boxRegex.matches(boxString)) {
                    player.showDialog(JumpPadCreationFailResultDialog.showDialog())
                    return@customPlayerClick
                }

                val origin = parseLocation(locationString, player.location.world)
                val (width, length) = parseBox(boxString)

                if (width > 10 || length > 10) {
                    player.showDialog(JumpPadCreationFailResultDialog.showDialog())
                    return@customPlayerClick
                }

                var targetLoc: Location? = null
                var strength = 0

                if (type == JumpPadType.STATIC) {
                    val targetStr = content.getText(TARGET_LOCATION_KEY) ?: ""
                    if (!locationRegex.matches(targetStr)) {
                        player.showDialog(JumpPadCreationFailResultDialog.showDialog())
                        return@customPlayerClick
                    }
                    targetLoc = parseLocation(targetStr, player.location.world)
                } else {
                    strength = content.getFloat(STRENGTH_KEY)?.toInt() ?: 10
                }

                val pad = JumpPad(
                    uuid = uuid,
                    origin = origin,
                    distance = strength,
                    width = width,
                    length = length,
                    type = type,
                    targetLocation = targetLoc
                )

                jumpPadService.addPad(pad)
                player.showDialog(JumpPadCreateSuccessDialog.showDialog(pad))
            }
        }
    }

    private fun backButton(): ActionButton = actionButton {
        label { spacer("Zurück") }
        tooltip {
            info("Klicke hier, um den Vorgang abzubrechen.")
        }
        action {
            playerCallback {
                it.showDialog(JumpPadMainDialog.showDialog())
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
        if (parts.size < 3) {
            throw IllegalArgumentException("Ungültiges Location-Format: $raw")
        }
        val x = parts[0].toDoubleOrNull() ?: 0.0
        val y = parts[1].toDoubleOrNull() ?: 0.0
        val z = parts[2].toDoubleOrNull() ?: 0.0
        return Location(world, x, y, z)
    }
}