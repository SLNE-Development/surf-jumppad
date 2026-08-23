package dev.slne.surf.jumppad.core.client.pad.boost

import dev.slne.surf.jumppad.core.client.pad.JumpPadPosition
import net.kyori.adventure.key.Key
import org.junit.jupiter.api.Test
import kotlin.math.PI
import kotlin.math.sin
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JumpPadBoostPathTest {
    private val world = Key.key("minecraft", "world")

    private fun position(x: Double, y: Double, z: Double) = JumpPadPosition(world, x, y, z)

    @Test
    fun `refuses paths that barely move horizontally`() {
        assertNull(
            JumpPadBoostPath.between(position(0.0, 64.0, 0.0), position(0.05, 90.0, 0.0))
        )
    }

    @Test
    fun `describes the horizontal direction and the height difference`() {
        val path = assertNotNull(
            JumpPadBoostPath.between(position(0.0, 64.0, 0.0), position(0.0, 70.0, 10.0))
        )

        assertEquals(10.0, path.distance, 1e-9)
        assertEquals(0.0, path.directionX, 1e-9)
        assertEquals(1.0, path.directionZ, 1e-9)
        assertEquals(6.0, path.yOffset, 1e-9)
    }

    @Test
    fun `starts and ends the arc at the path ends`() {
        val path = assertNotNull(
            JumpPadBoostPath.between(position(0.0, 64.0, 0.0), position(0.0, 70.0, 10.0))
        )

        val start = path.positionAt(peak = 5.0, progress = 0.0)
        assertEquals(0.0, start.x(), 1e-9)
        assertEquals(64.0, start.y(), 1e-9)
        assertEquals(0.0, start.z(), 1e-9)

        val end = path.positionAt(peak = 5.0, progress = 1.0)
        assertEquals(0.0, end.x(), 1e-9)
        assertEquals(70.0, end.y(), 1e-9)
        assertEquals(10.0, end.z(), 1e-9)
    }

    @Test
    fun `raises the arc by the peak in the middle of the path`() {
        val path = assertNotNull(
            JumpPadBoostPath.between(position(0.0, 64.0, 0.0), position(0.0, 64.0, 10.0))
        )

        val middle = path.positionAt(peak = 4.0, progress = 0.5)
        assertEquals(64.0 + sin(0.5 * PI) * 4.0, middle.y(), 1e-9)
        assertEquals(5.0, middle.z(), 1e-9)
    }

    @Test
    fun `keeps the boost between twenty and one hundred ticks`() {
        val short = assertNotNull(
            JumpPadBoostPath.between(position(0.0, 64.0, 0.0), position(0.0, 64.0, 1.0))
        )
        assertEquals(20, short.totalTicks)

        val long = assertNotNull(
            JumpPadBoostPath.between(position(0.0, 64.0, 0.0), position(0.0, 64.0, 500.0))
        )
        assertEquals(100, long.totalTicks)

        val medium = assertNotNull(
            JumpPadBoostPath.between(position(0.0, 64.0, 0.0), position(0.0, 64.0, 20.0))
        )
        assertEquals(45, medium.totalTicks)
    }

    @Test
    fun `keeps the arc height when nothing is in the way`() {
        val path = assertNotNull(
            JumpPadBoostPath.between(position(0.0, 64.0, 0.0), position(0.0, 64.0, 20.0))
        )

        assertEquals(8.0, path.resolvePeak(8.0) { _, _, _ -> false }, 1e-9)
    }

    @Test
    fun `lowers the arc height while it flies through solid blocks`() {
        val path = assertNotNull(
            JumpPadBoostPath.between(position(0.0, 64.0, 0.0), position(0.0, 64.0, 20.0))
        )

        val ceilingAt = 70
        val peak = path.resolvePeak(8.0) { _, y, _ -> y >= ceilingAt }

        assertTrue(peak < 8.0)
        assertTrue(peak > JumpPadBoostPath.MIN_PEAK_HEIGHT)

        for (step in 0..100) {
            val progress = step / 100.0
            val y = path.positionAt(peak, progress).y()
            assertTrue(y + JumpPadBoostPath.PLAYER_COLLISION_HEIGHT < ceilingAt)
        }
    }

    @Test
    fun `notices a single solid block on the way`() {
        val path = assertNotNull(
            JumpPadBoostPath.between(position(0.0, 64.0, 0.0), position(0.0, 64.0, 20.0))
        )

        // Half way along the path the arc stands exactly `peak` blocks above its start, so an
        // 8 block arc puts a foot sample on (0, 72, 10) and nowhere else. Only that one block is
        // solid, which keeps the collision scan from being allowed to skip any distinct block.
        val peak = path.resolvePeak(8.0) { x, y, z -> x == 0 && y == 72 && z == 10 }

        assertTrue(peak < 8.0, "a solid block on the path did not lower the arc")
    }

    @Test
    fun `stops lowering the arc height at the minimum`() {
        val path = assertNotNull(
            JumpPadBoostPath.between(position(0.0, 64.0, 0.0), position(0.0, 64.0, 20.0))
        )

        val peak = path.resolvePeak(8.0) { _, _, _ -> true }
        assertTrue(peak <= JumpPadBoostPath.MIN_PEAK_HEIGHT)
    }
}
