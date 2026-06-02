package dev.slne.surf.jumppad.storage

import dev.slne.surf.api.core.util.logger
import dev.slne.surf.api.core.util.mutableObjectListOf
import dev.slne.surf.api.paper.extensions.server
import dev.slne.surf.jumppad.pad.JumpPad
import dev.slne.surf.jumppad.pad.JumpPadType
import dev.slne.surf.jumppad.pad.service.JumpPadService
import dev.slne.surf.jumppad.plugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.BinaryTagIO
import net.kyori.adventure.nbt.CompoundBinaryTag
import org.bukkit.Location
import java.util.*
import kotlin.io.path.createParentDirectories
import kotlin.io.path.div
import kotlin.io.path.exists

object StorageService {
    private val log = logger()
    private val jumpPadsPath = plugin.dataPath / "jumppads.dat"

    suspend fun loadPadsFromFile() {
        if (!jumpPadsPath.exists()) {
            migrateLegacyStorage()
            return
        }

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

    @Suppress("DEPRECATION")
    private suspend fun migrateLegacyStorage() {
        storageServiceOld.init()
        if (!storageServiceOld.hasLegacyData()) return

        val migratedPads = storageServiceOld.loadPads()
        if (migratedPads == 0) {
            log.atWarning().log("Legacy pad files were found, but no pads could be migrated.")
            return
        }

        savePads()
        log.atInfo().log("Migrated $migratedPads legacy jump pads to ${jumpPadsPath.fileName}.")
    }

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
        .put("origin", saveLocation(pad.origin))
        .putString("type", pad.type.name)
        .putInt("distance", pad.distance)
        .put("targetLocation", pad.targetLocation?.let { saveLocation(it) } ?: CompoundBinaryTag.empty())
        .putInt("width", pad.width)
        .putInt("length", pad.length)
        .build()

    private fun saveLocation(location: Location) = CompoundBinaryTag.builder(6)
        .putString("world_key", location.world.key().asString())
        .putDouble("x", location.x)
        .putDouble("y", location.y)
        .putDouble("z", location.z)
        .putFloat("yaw", location.yaw)
        .putFloat("pitch", location.pitch)
        .build()

    private fun loadPad(uuid: UUID, tag: CompoundBinaryTag): JumpPad? {
        val originTag = tag.getCompound("origin")
        val typeString = tag.getString("type")
        val distance = tag.getInt("distance")
        val targetLocationTag = tag.getCompound("targetLocation")
        val width = tag.getInt("width")
        val length = tag.getInt("length")

        val origin = loadLocation(originTag) ?: return null
        val targetLocation = if (targetLocationTag.isEmpty) null else loadLocation(targetLocationTag) ?: return null
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

    private fun loadLocation(tag: CompoundBinaryTag): Location? {
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

        val world = server.getWorld(key)
        if (world == null) {
            log.atWarning()
                .log("Could not find world with key: $worldKeyString (world_key: ${worldKeyString}). Skipping this pad.")
            return null
        }

        return Location(world, x, y, z, yaw, pitch)
    }
}
