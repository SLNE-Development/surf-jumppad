@file:Suppress("UnstableApiUsage")

package dev.slne.surf.jumppad.dialogs.create

import dev.slne.surf.jumppad.dialogs.JumpPadMainDialog
import dev.slne.surf.jumppad.dialogs.create.results.JumpPadCreateSuccessDialog
import dev.slne.surf.jumppad.dialogs.create.results.JumpPadCreationFailResultDialog
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
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import java.util.*

object CreateJumpPadDialog {
    private const val LOCATION_KEY = "pad_location"
    private const val STRENGTH_KEY = "pad_strength"
    private const val BOX_KEY = "pad_box"
    private const val TYPE_KEY = "pad_type"

    private const val TYPE_KEY_HORIZONTAL_NORTH = "pad_type_horizontal_north"
    private const val TYPE_KEY_HORIZONTAL_SOUTH = "pad_type_horizontal_south"
    private const val TYPE_KEY_HORIZONTAL_EAST = "pad_type_horizontal_east"
    private const val TYPE_KEY_HORIZONTAL_WEST = "pad_type_horizontal_west"
    private const val TYPE_KEY_VERTICAL = "pad_type_vertical"

    private val locationRegex by lazy { Regex("^-?\\d+\\s-?\\d+\\s-?\\d+$") }
    private val boxRegex by lazy { Regex("^\\d+x\\d+$") }

    fun showDialog(player: Player) = dialog {
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
                    label { text("Box") }
                    initial("3x3")
                    width(400)
                }
            }
            input {
                numberRange(STRENGTH_KEY, 1.0..10.0) {
                    label { text("Stärke") }
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
            confirmation(createButton(uuid), backButton())
        }
    }

    private fun createButton(uuid: UUID): ActionButton = actionButton {
        label { success("JumpPad erstellen") }
        tooltip {
            info("Klicke hier, um das JumpPad zu erstellen.")
        }
        action {
            customClick { content, audience ->
                val player = audience as? Player ?: return@customClick

                val locationString = content.getText(LOCATION_KEY) ?: ""
                val strengthFloat = content.getFloat(STRENGTH_KEY) ?: 0.0f
                val boxString = content.getText(BOX_KEY) ?: ""

                val validLocation = locationRegex.matches(locationString)
                val validBox = boxRegex.matches(boxString)

                if (!validLocation || !validBox) {
                    player.showDialog(JumpPadCreationFailResultDialog.showDialog(player))
                    return@customClick
                }

                val origin = parseLocation(locationString, player.location.world)
                val (width, length) = parseBox(boxString)

                val strength = strengthFloat.toDouble()

                val type = when (content.getText(TYPE_KEY)) {
                    TYPE_KEY_HORIZONTAL_NORTH -> JumpPadType.HORIZONTAL_NORTH
                    TYPE_KEY_HORIZONTAL_SOUTH -> JumpPadType.HORIZONTAL_SOUTH
                    TYPE_KEY_HORIZONTAL_EAST -> JumpPadType.HORIZONTAL_EAST
                    TYPE_KEY_HORIZONTAL_WEST -> JumpPadType.HORIZONTAL_WEST
                    TYPE_KEY_VERTICAL -> JumpPadType.VERTICAL
                    else -> JumpPadType.HORIZONTAL_NORTH
                }

                val pad = JumpPad(
                    uuid = uuid,
                    origin = origin,
                    strength = strength,
                    width = width,
                    length = length,
                    type = type
                )
                jumpPadService.addPad(pad)
                markPadArea(pad)
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

    private fun markPadArea(pad: JumpPad) {
        val origin = pad.origin
        val world = origin.world

        val halfWidth = pad.width / 2
        val halfLength = pad.length / 2

        val blockType = when (pad.type) {
            JumpPadType.VERTICAL -> Material.RED_WOOL
            JumpPadType.HORIZONTAL_NORTH -> Material.BLUE_WOOL
            JumpPadType.HORIZONTAL_SOUTH -> Material.YELLOW_WOOL
            JumpPadType.HORIZONTAL_EAST -> Material.GREEN_WOOL
            JumpPadType.HORIZONTAL_WEST -> Material.ORANGE_WOOL
        }

        for (dx in -halfWidth..halfWidth) {
            for (dz in -halfLength..halfLength) {
                val x = origin.blockX + dx
                val y = origin.blockY - 1
                val z = origin.blockZ + dz

                val block: Block = world.getBlockAt(x, y, z)
                block.type = blockType
            }
        }
    }
}
