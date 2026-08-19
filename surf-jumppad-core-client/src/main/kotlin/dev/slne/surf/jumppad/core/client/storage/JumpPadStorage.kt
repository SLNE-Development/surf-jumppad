package dev.slne.surf.jumppad.core.client.storage

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.api.core.util.mutableObjectListOf
import dev.slne.surf.jumppad.core.client.pad.JumpPad
import dev.slne.surf.jumppad.core.client.pad.JumpPadPosition
import dev.slne.surf.jumppad.core.client.pad.JumpPadType
import dev.slne.surf.jumppad.core.client.pad.service.JumpPadService
import dev.slne.surf.jumppad.core.client.platform.JumpPadPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.CompoundBinaryTag
import java.nio.file.Path
import java.util.*
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists

/**
 * Reads and writes the jump pads of a server from and to a single binary tag file.
 *
 * @property jumpPadsPath the file the jump pads are stored in
 */
class JumpPadStorage(private val jumpPadsPath: Path) {
    private val log = logger()

    /**
     * Reads every stored jump pad and registers it.
     *
     * Pads that cannot be read, or that belong to a world this server does not have, are skipped.
     */
    suspend fun loadPads() {
        if (!jumpPadsPath.exists()) return

        val compoundTag = withContext(Dispatchers.IO) { BinaryTagIO.reader().read(jumpPadsPath) }
        val pads = mutableObjectListOf<JumpPad>()

        for ((key, tag) in compoundTag) {
            if (tag !is CompoundBinaryTag) {
                log.atWarning().log("Invalid tag for key: $key. Skipping this pad.")
                continue
            }

            val uuid = try {
                UUID.fromString(key)
            } catch (e: IllegalArgumentException) {
                log.atWarning()
                    .withCause(e)
                    .log("Invalid UUID: $key. Skipping this pad.")
                continue
            }

            val pad = loadPad(uuid, tag)
            if (pad != null) {
                pads.add(pad)
            }
        }

        for (pad in pads) {
            val registered = JumpPadService.registerPad(pad)
            if (!registered) {
                log.atWarning().log("Pad with UUID ${pad.uuid} already exists. Skipping this pad.")
            }
        }
    }

    /**
     * Writes every registered jump pad back to the storage file.
     */
    suspend fun savePads() {
        val pads = JumpPadService.getPads()

        val compoundTag = CompoundBinaryTag.builder(pads.size)
            .apply {
                for (pad in pads) {
                    put(pad.uuid.toString(), savePad(pad))
                }
            }
            .build()

        withContext(Dispatchers.IO) {
            jumpPadsPath.createParentDirectories()
            BinaryTagIO.writer().write(compoundTag, jumpPadsPath)
        }
    }

    private fun savePad(pad: JumpPad): CompoundBinaryTag = CompoundBinaryTag.builder(6)
        .put("origin", savePosition(pad.origin))
        .putString("type", pad.type.name)
        .putInt("distance", pad.distance)
        .put(
            "targetLocation",
            pad.targetLocation?.let { savePosition(it) } ?: CompoundBinaryTag.empty()
        )
        .putInt("width", pad.width)
        .putInt("length", pad.length)
        .build()

    private fun savePosition(position: JumpPadPosition) = CompoundBinaryTag.builder(6)
        .putString("world_key", position.worldKey.asString())
        .putDouble("x", position.x)
        .putDouble("y", position.y)
        .putDouble("z", position.z)
        .putFloat("yaw", position.yaw)
        .putFloat("pitch", position.pitch)
        .build()

    private fun loadPad(uuid: UUID, tag: CompoundBinaryTag): JumpPad? {
        val originTag = tag.getCompound("origin")
        val typeString = tag.getString("type")
        val distance = tag.getInt("distance")
        val targetLocationTag = tag.getCompound("targetLocation")
        val width = tag.getInt("width")
        val length = tag.getInt("length")

        val origin = loadPosition(originTag) ?: return null
        val targetLocation =
            if (targetLocationTag.isEmpty) null else loadPosition(targetLocationTag) ?: return null
        val type = try {
            JumpPadType.valueOf(typeString.uppercase())
        } catch (e: IllegalArgumentException) {
            log.atWarning()
                .withCause(e)
                .log("Invalid type: $typeString. Skipping this pad.")
            return null
        }

        return JumpPad(
            uuid = uuid,
            origin = origin,
            type = type,
            distance = distance,
            targetLocation = targetLocation,
            width = width,
            length = length
        )
    }

    private fun loadPosition(tag: CompoundBinaryTag): JumpPadPosition? {
        val worldKeyString = tag.getString("world_key")
        val x = tag.getDouble("x")
        val y = tag.getDouble("y")
        val z = tag.getDouble("z")
        val yaw = tag.getFloat("yaw")
        val pitch = tag.getFloat("pitch")

        val key = try {
            Key.key(worldKeyString)
        } catch (e: InvalidKeyException) {
            log.atWarning()
                .withCause(e)
                .log("Invalid key: $worldKeyString (world_key: ${worldKeyString}). Skipping this pad.")
            return null
        }

        if (!JumpPadPlatform.isKnownWorld(key)) {
            log.atWarning()
                .log("Could not find world with key: $worldKeyString (world_key: ${worldKeyString}). Skipping this pad.")
            return null
        }

        return JumpPadPosition(key, x, y, z, yaw, pitch)
    }

    companion object {
        /**
         * The name of the file the jump pads are stored in.
         */
        const val FILE_NAME = "jumppads.dat"

        /**
         * Builds a storage keeping its file in [dataDirectory].
         *
         * @param dataDirectory the directory the jump pads are stored in
         * @return the storage
         */
        fun inDirectory(dataDirectory: Path) = JumpPadStorage(dataDirectory.resolve(FILE_NAME))
    }
}
