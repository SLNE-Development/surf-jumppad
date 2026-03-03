package dev.slne.surf.jumppad.pad

import kotlinx.serialization.Transient
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.util.BoundingBox
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.util.*

fun jumpPad(
    uuid: UUID = UUID.randomUUID(),
    origin: Location,
    type: JumpPadType,
    distance: Int,
    width: Int = 1,
    length: Int = 1,
    target: Location? = null
) = JumpPad(
    uuid = uuid,
    type = type,
    distance = distance,
    width = width,
    length = length,
    originWorldId = origin.world!!.uid,
    originX = origin.x,
    originY = origin.y,
    originZ = origin.z,
    originYaw = origin.yaw,
    originPitch = origin.pitch,
    targetWorldId = target?.world?.uid,
    targetX = target?.x,
    targetY = target?.y,
    targetZ = target?.z,
    targetYaw = target?.yaw,
    targetPitch = target?.pitch
)

@ConfigSerializable
data class JumpPad(
    val uuid: UUID,
    val type: JumpPadType,
    val distance: Int,
    var width: Int,
    var length: Int,

    var originWorldId: UUID,
    var originX: Double,
    var originY: Double,
    var originZ: Double,
    var originYaw: Float,
    var originPitch: Float,

    var targetWorldId: UUID? = null,
    var targetX: Double? = null,
    var targetY: Double? = null,
    var targetZ: Double? = null,
    var targetYaw: Float? = null,
    var targetPitch: Float? = null
) {

    @Transient
    var originLocation: Location
        get() = Location(Bukkit.getWorld(originWorldId), originX, originY, originZ, originYaw, originPitch)
        set(value) {
            originWorldId = value.world?.uid ?: originWorldId
            originX = value.x
            originY = value.y
            originZ = value.z
            originYaw = value.yaw
            originPitch = value.pitch
        }

    @Transient
    var targetLocation: Location?
        get() = targetWorldId?.let {
            Location(
                Bukkit.getWorld(it),
                targetX ?: 0.0,
                targetY ?: 0.0,
                targetZ ?: 0.0,
                targetYaw ?: 0f,
                targetPitch ?: 0f
            )
        }
        set(value) {
            targetWorldId = value?.world?.uid
            targetX = value?.x
            targetY = value?.y
            targetZ = value?.z
            targetYaw = value?.yaw
            targetPitch = value?.pitch
        }

    @Transient
    val boundingBox: BoundingBox
        get() {
            val halfWidth = width / 2.0
            val halfLength = length / 2.0
            return BoundingBox(
                originX - halfWidth, originY, originZ - halfLength,
                originX + halfWidth, originY + 1.0, originZ + halfLength
            )
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JumpPad

        return uuid == other.uuid
    }

    override fun hashCode(): Int = uuid.hashCode()

    override fun toString(): String {
        return "JumpPad(uuid=$uuid, type=$type, origin=$originLocation, boundingBox=$boundingBox)"
    }
}