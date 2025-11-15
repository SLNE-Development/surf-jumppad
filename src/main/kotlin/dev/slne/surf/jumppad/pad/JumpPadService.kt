package dev.slne.surf.jumppad.pad

import org.bukkit.Location

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

    fun getPads(): List<JumpPad> = pads.toList()

    companion object {
        val INSTANCE = JumpPadManager()
    }
}

val jumpPadService get() = JumpPadManager.INSTANCE