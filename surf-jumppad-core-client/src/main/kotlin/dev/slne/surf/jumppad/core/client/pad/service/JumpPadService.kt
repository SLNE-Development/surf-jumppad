package dev.slne.surf.jumppad.core.client.pad.service

import dev.slne.surf.jumppad.core.client.pad.JumpPad
import dev.slne.surf.jumppad.core.client.pad.JumpPadPosition
import net.kyori.adventure.key.Key
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
    private val padsByBlock = ConcurrentHashMap<PadBlockKey, JumpPad>()

    /**
     * Registers a new jump pad and indexes all blocks covered by its trigger area.
     *
     * @param pad the jump pad to register
     * @return `true` if the pad was registered, or `false` if a pad with the same UUID already exists
     */
    @Synchronized
    fun registerPad(pad: JumpPad): Boolean {
        if (pads.putIfAbsent(pad.uuid, pad) != null) {
            return false
        }

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
        val registered = pads[pad.uuid] ?: return

        unindexPad(registered)
        pads.remove(pad.uuid, registered)
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
        val previous = pads.put(pad.uuid, pad)

        indexPad(pad)

        if (previous == null || previous == pad) {
            return
        }

        unindexPad(previous)
    }

    /**
     * Finds the jump pad at the given block location.
     *
     * This lookup uses a precomputed block index and therefore does not need to
     * scan all registered jump pads.
     *
     * @param position the position to check
     * @return the jump pad at the given position, or `null` if no pad is indexed there
     */
    fun getPadAt(position: JumpPadPosition): JumpPad? =
        getPadAt(position.worldKey, position.blockX, position.blockY, position.blockZ)

    /**
     * Finds the jump pad at the given block coordinates.
     *
     * @param worldKey the key of the world to look in
     * @param blockX the x coordinate of the block
     * @param blockY the y coordinate of the block
     * @param blockZ the z coordinate of the block
     * @return the jump pad at the given block, or `null` if no pad is indexed there
     */
    fun getPadAt(worldKey: Key, blockX: Int, blockY: Int, blockZ: Int): JumpPad? =
        padsByBlock[PadBlockKey(worldKey, blockX, blockY, blockZ)]

    /**
     * Returns a snapshot of all currently registered jump pads.
     *
     * @return all registered jump pads
     */
    fun getPads(): List<JumpPad> = pads.values.toList()

    /**
     * Removes every registered jump pad and its indexed trigger area.
     */
    @Synchronized
    fun clear() {
        padsByBlock.clear()
        pads.clear()
    }

    private fun indexPad(pad: JumpPad) {
        forEachCoveredBlock(pad) { key -> padsByBlock[key] = pad }
    }

    private fun unindexPad(pad: JumpPad) {
        forEachCoveredBlock(pad) { key -> padsByBlock.remove(key, pad) }
    }

    private inline fun forEachCoveredBlock(pad: JumpPad, action: (PadBlockKey) -> Unit) {
        val origin = pad.origin
        val worldKey = origin.worldKey

        val originX = origin.blockX
        val originY = origin.blockY
        val originZ = origin.blockZ

        val minX = originX - ((pad.width - 1) / 2)
        val maxX = originX + (pad.width / 2)

        val minZ = originZ - ((pad.length - 1) / 2)
        val maxZ = originZ + (pad.length / 2)

        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                action(PadBlockKey(worldKey, x, originY, z))
            }
        }
    }

    private data class PadBlockKey(
        val worldKey: Key,
        val x: Int,
        val y: Int,
        val z: Int
    )
}
