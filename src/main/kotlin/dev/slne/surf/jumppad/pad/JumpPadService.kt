package dev.slne.surf.jumppad.pad

import dev.slne.surf.surfapi.bukkit.api.extensions.server
import dev.slne.surf.surfapi.bukkit.api.glow.glowingApi
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.entity.Player

class JumpPadManager {

    private val pads = mutableSetOf<JumpPad>()

    fun registerPad(pad: JumpPad) {
        pads.removeIf { it.uuid == pad.uuid }
        addPad(pad)
    }

    fun unregisterPad(pad: JumpPad) {
        deletePad(pad)
    }

    fun addPad(pad: JumpPad) {
        pads.add(pad)
    }

    fun deletePad(pad: JumpPad) {
        pads.removeIf { it.uuid == pad.uuid }
    }

    fun updatePad(pad: JumpPad) {
        deletePad(pad)
        addPad(pad)
    }

    fun getPadAt(location: Location): JumpPad? {
        return pads.firstOrNull { pad ->
            val dx = location.blockX - pad.origin.blockX
            val dz = location.blockZ - pad.origin.blockZ

            dx in -(pad.width / 2)..(pad.width / 2) &&
                    dz in -(pad.length / 2)..(pad.length / 2) &&
                    location.blockY == pad.origin.blockY
        }
    }

    fun visualizePadForAll(pad: JumpPad) {
        for (player in server.onlinePlayers) {
            visualizePad(player, pad)
        }
    }

    fun visualizePadsForSpecific(player: Player) {
        getPads().forEach { pad ->
            visualizePad(player, pad)
        }
    }

    private fun visualizePad(player: Player, pad: JumpPad) {
        val origin = pad.origin
        val world = origin.world

        val halfWidth = pad.width / 2
        val halfLength = pad.length / 2

        val color = when (pad.type) {
            JumpPadType.VERTICAL -> NamedTextColor.LIGHT_PURPLE
            JumpPadType.HORIZONTAL_NORTH -> NamedTextColor.BLUE
            JumpPadType.HORIZONTAL_SOUTH -> NamedTextColor.RED
            JumpPadType.HORIZONTAL_EAST -> NamedTextColor.YELLOW
            JumpPadType.HORIZONTAL_WEST -> NamedTextColor.GREEN
        }

        for (dx in -halfWidth..halfWidth) {
            for (dz in -halfLength..halfLength) {
                val x = origin.blockX + dx
                val y = origin.blockY - 1
                val z = origin.blockZ + dz

                val location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

                glowingApi.makeGlowing(location, player, color)
            }
        }
    }

    fun updatePadVisualization(pad: JumpPad) {
        removePadVisualization(pad)
        visualizePadForAll(pad)
    }

    private fun removePadVisualization(pad: JumpPad) {
        val origin = pad.origin
        val world = origin.world

        val halfWidth = pad.width / 2
        val halfLength = pad.length / 2

        for (dx in -halfWidth..halfWidth) {
            for (dz in -halfLength..halfLength) {
                val x = origin.blockX + dx
                val y = origin.blockY - 1
                val z = origin.blockZ + dz

                val location = Location(world, x.toDouble(), y.toDouble(), z.toDouble())

                for (player in server.onlinePlayers) {
                    glowingApi.removeGlowing(location, player)
                }
            }
        }
    }

    fun getPads(): List<JumpPad> = pads.toList()

    companion object {
        val INSTANCE = JumpPadManager()
    }
}

val jumpPadService get() = JumpPadManager.INSTANCE