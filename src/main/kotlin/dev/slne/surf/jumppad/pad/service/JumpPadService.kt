package dev.slne.surf.jumppad.pad.service

import dev.slne.surf.jumppad.pad.JumpPad
import org.bukkit.Location
import java.util.UUID

class JumpPadManager {

    private val pads = mutableSetOf<JumpPad>()

    /** Spatial index: world UUID → (encoded block key → pad) for O(1) lookup. */
    private val padIndex = HashMap<UUID, HashMap<Long, JumpPad>>()

    fun registerPad(pad: JumpPad) {
        val existing = pads.find { it.uuid == pad.uuid }
        if (existing != null) {
            pads.remove(existing)
            unindexPad(existing)
        }
        addPad(pad)
    }

    fun addPad(pad: JumpPad) {
        pads.add(pad)
        indexPad(pad)
    }

    fun deletePad(pad: JumpPad) {
        pads.removeIf { it.uuid == pad.uuid }
        unindexPad(pad)
    }

    fun updatePad(pad: JumpPad) {
        deletePad(pad)
        addPad(pad)
    }

    fun getPadAt(location: Location): JumpPad? {
        val worldId = location.world?.uid ?: return null
        val worldMap = padIndex[worldId] ?: return null
        return worldMap[blockKey(location.blockX, location.blockY, location.blockZ)]
    }

    fun getPads(): List<JumpPad> = pads.toList()

    private fun indexPad(pad: JumpPad) {
        val worldId = pad.origin.world?.uid ?: return
        val worldMap = padIndex.getOrPut(worldId) { HashMap() }
        val ox = pad.origin.blockX
        val oy = pad.origin.blockY
        val oz = pad.origin.blockZ
        val halfW = pad.width / 2
        val halfL = pad.length / 2
        for (dx in -halfW..halfW) {
            for (dz in -halfL..halfL) {
                worldMap[blockKey(ox + dx, oy, oz + dz)] = pad
            }
        }
    }

    private fun unindexPad(pad: JumpPad) {
        val worldId = pad.origin.world?.uid ?: return
        val worldMap = padIndex[worldId] ?: return
        val ox = pad.origin.blockX
        val oy = pad.origin.blockY
        val oz = pad.origin.blockZ
        val halfW = pad.width / 2
        val halfL = pad.length / 2
        for (dx in -halfW..halfW) {
            for (dz in -halfL..halfL) {
                worldMap.remove(blockKey(ox + dx, oy, oz + dz))
            }
        }
    }

    companion object {
        val INSTANCE = JumpPadManager()

        /**
         * Encodes block coordinates into a single Long for use as a HashMap key.
         *
         * Bit layout: [63..38] x (26 bits) | [37..26] y (12 bits) | [25..0] z (26 bits).
         *
         * Safe for all valid Minecraft block coordinates:
         *   X/Z within ±30,000,000 (< 2^25) and Y within -2048..2047.
         * Values outside this range may alias, but they lie beyond the world border.
         */
        private fun blockKey(x: Int, y: Int, z: Int): Long =
            (x.toLong() and 0x3FFFFFF) or
                    ((y.toLong() and 0xFFF) shl 26) or
                    ((z.toLong() and 0x3FFFFFF) shl 38)
    }
}

val jumpPadService get() = JumpPadManager.INSTANCE