package dev.slne.surf.jumppad.pad.service

import dev.slne.surf.jumppad.pad.JumpPad
import org.bukkit.Location
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages all registered jump pads.
 *
 * Jump pads are stored by their unique identifier and can be queried,
 * updated, removed, or looked up by block location.
 */
object JumpPadService {
    private val pads = ConcurrentHashMap<UUID, JumpPad>()
    private val padsByBlock = ConcurrentHashMap<PadBlockKey, UUID>()

    /**
     * Registers a new jump pad and indexes all blocks covered by its trigger area.
     *
     * @param pad the jump pad to register
     * @return `true` if the pad was registered, or `false` if a pad with the same UUID already exists
     */
    @Synchronized
    fun registerPad(pad: JumpPad): Boolean {
        if (pads.containsKey(pad.uuid)) {
            return false
        }

        pads[pad.uuid] = pad
        indexPad(pad)
        return true
    }

    /**
     * Deletes a jump pad and removes its indexed trigger area.
     *
     * @param pad the jump pad to delete
     */
    @Synchronized
    fun deletePad(pad: JumpPad) {
        val removed = pads.remove(pad.uuid) ?: return
        unindexPad(removed)
    }

    /**
     * Updates an existing jump pad and rebuilds its indexed trigger area.
     *
     * If no jump pad with the same UUID exists yet, the pad is inserted.
     *
     * @param pad the jump pad to update
     */
    @Synchronized
    fun updatePad(pad: JumpPad) {
        pads[pad.uuid]?.let(::unindexPad)

        pads[pad.uuid] = pad
        indexPad(pad)
    }

    /**
     * Finds the jump pad at the given block location.
     *
     * This lookup uses a precomputed block index and therefore does not need to
     * scan all registered jump pads.
     *
     * @param location the location to check
     * @return the jump pad at the given location, or `null` if no pad is indexed there
     */
    fun getPadAt(location: Location): JumpPad? {
        val worldUid = location.world?.uid ?: return null

        val key = PadBlockKey(
            worldUid = worldUid,
            x = location.blockX,
            y = location.blockY,
            z = location.blockZ
        )

        val padId = padsByBlock[key] ?: return null
        return pads[padId]
    }

    /**
     * Returns a snapshot of all currently registered jump pads.
     *
     * @return all registered jump pads
     */
    fun getPads(): List<JumpPad> = pads.values.toList()

    private fun indexPad(pad: JumpPad) {
        for (key in getCoveredBlocks(pad)) {
            padsByBlock[key] = pad.uuid
        }
    }

    private fun unindexPad(pad: JumpPad) {
        for (key in getCoveredBlocks(pad)) {
            padsByBlock.remove(key, pad.uuid)
        }
    }

    private fun getCoveredBlocks(pad: JumpPad): Sequence<PadBlockKey> = sequence {
        val worldUid = pad.origin.world?.uid ?: return@sequence

        val originX = pad.origin.blockX
        val originY = pad.origin.blockY
        val originZ = pad.origin.blockZ

        val minX = originX - ((pad.width - 1) / 2)
        val maxX = originX + (pad.width / 2)

        val minZ = originZ - ((pad.length - 1) / 2)
        val maxZ = originZ + (pad.length / 2)

        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                yield(
                    PadBlockKey(
                        worldUid = worldUid,
                        x = x,
                        y = originY,
                        z = z
                    )
                )
            }
        }
    }

    private data class PadBlockKey(
        val worldUid: UUID,
        val x: Int,
        val y: Int,
        val z: Int
    )
}