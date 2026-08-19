package dev.slne.surf.jumppad.core.client.pad.boost

import dev.slne.surf.jumppad.core.client.pad.JumpPad
import dev.slne.surf.jumppad.core.client.pad.JumpPadPosition
import dev.slne.surf.jumppad.core.client.pad.JumpPadType
import net.kyori.adventure.key.Key
import org.junit.jupiter.api.Test
import org.spongepowered.math.vector.Vector3d
import java.util.*
import kotlin.math.sqrt
import kotlin.test.assertEquals

class JumpPadLaunchTest {
    private val world = Key.key("minecraft", "world")

    private fun position(x: Double, y: Double, z: Double) = JumpPadPosition(world, x, y, z)

    private fun pad(
        type: JumpPadType,
        distance: Int = 10,
        origin: JumpPadPosition = position(10.0, 64.0, 20.0),
        target: JumpPadPosition? = null
    ) = JumpPad(
        uuid = UUID.randomUUID(),
        origin = origin,
        type = type,
        distance = distance,
        targetLocation = target,
        width = 3,
        length = 3
    )

    @Test
    fun `starts a launch in the center of the pad block`() {
        val start = JumpPadLaunch.startPosition(pad(JumpPadType.HORIZONTAL_NORTH))

        assertEquals(10.5, start.x, 1e-9)
        assertEquals(64.0, start.y, 1e-9)
        assertEquals(20.5, start.z, 1e-9)
    }

    @Test
    fun `sends directional pads their configured distance into their direction`() {
        val target = JumpPadLaunch.targetPosition(
            pad(JumpPadType.HORIZONTAL_EAST, distance = 15),
            Vector3d.ZERO
        )

        assertEquals(25.0, target.x, 1e-9)
        assertEquals(64.0, target.y, 1e-9)
        assertEquals(20.0, target.z, 1e-9)
    }

    @Test
    fun `sends player direction pads along the flattened view direction`() {
        val target = JumpPadLaunch.targetPosition(
            pad(JumpPadType.PLAYER_DIRECTION, distance = 10),
            Vector3d(0.0, -0.8, 0.6)
        )

        assertEquals(10.0, target.x, 1e-9)
        assertEquals(64.0, target.y, 1e-9)
        assertEquals(30.0, target.z, 1e-9)
    }

    @Test
    fun `uses the configured target of a static pad`() {
        val configured = position(1.0, 2.0, 3.0)
        val target = JumpPadLaunch.targetPosition(
            pad(JumpPadType.STATIC, target = configured),
            Vector3d.ZERO
        )

        assertEquals(configured, target)
    }

    @Test
    fun `lifts a static pad without target straight up`() {
        val target = JumpPadLaunch.targetPosition(pad(JumpPadType.STATIC), Vector3d.ZERO)

        assertEquals(10.0, target.x, 1e-9)
        assertEquals(69.0, target.y, 1e-9)
        assertEquals(20.0, target.z, 1e-9)
    }

    @Test
    fun `centers block aligned targets on their block`() {
        val centered = JumpPadLaunch.centerXZIfBlockAligned(position(4.0, 64.0, -7.0))

        assertEquals(4.5, centered.x, 1e-9)
        assertEquals(64.0, centered.y, 1e-9)
        assertEquals(-6.5, centered.z, 1e-9)
    }

    @Test
    fun `leaves targets with decimal coordinates alone`() {
        val original = position(4.25, 64.0, -7.75)
        assertEquals(original, JumpPadLaunch.centerXZIfBlockAligned(original))
    }

    @Test
    fun `raises the arc with the distance but never below the minimum`() {
        assertEquals(JumpPadLaunch.MIN_ARC_PEAK_HEIGHT, JumpPadLaunch.peakHeight(3.0), 1e-9)
        assertEquals(10.0, JumpPadLaunch.peakHeight(30.0), 1e-9)
    }

    @Test
    fun `launches high enough to reach the requested height`() {
        assertEquals(
            sqrt(2.0 * JumpPadLaunch.MINECRAFT_GRAVITY * 5.0),
            JumpPadLaunch.verticalLaunchVelocity(5.0),
            1e-9
        )
    }

    @Test
    fun `moves a quarter of the remaining distance each tick`() {
        val velocity = JumpPadLaunch.followVelocity(
            desired = Vector3d(4.0, 8.0, -4.0),
            currentX = 0.0,
            currentY = 0.0,
            currentZ = 0.0
        )

        assertEquals(1.0, velocity.x(), 1e-9)
        assertEquals(2.0, velocity.y(), 1e-9)
        assertEquals(-1.0, velocity.z(), 1e-9)
    }
}
