package dev.slne.surf.jumppad.core.client.pad.service

import dev.slne.surf.jumppad.core.client.pad.JumpPad
import dev.slne.surf.jumppad.core.client.pad.JumpPadPosition
import dev.slne.surf.jumppad.core.client.pad.JumpPadType
import net.kyori.adventure.key.Key
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class JumpPadServiceTest {
    private val world = Key.key("minecraft", "world")
    private val otherWorld = Key.key("minecraft", "the_nether")

    private fun pad(
        origin: JumpPadPosition = JumpPadPosition(world, 0.0, 64.0, 0.0),
        width: Int = 3,
        length: Int = 3,
        uuid: UUID = UUID.randomUUID()
    ) = JumpPad(
        uuid = uuid,
        origin = origin,
        type = JumpPadType.VERTICAL,
        distance = 5,
        width = width,
        length = length
    )

    @BeforeEach
    fun reset() {
        JumpPadService.clear()
    }

    @Test
    fun `indexes every block of the trigger area`() {
        val registered = pad()
        assertTrue(JumpPadService.registerPad(registered))

        for (x in -1..1) {
            for (z in -1..1) {
                assertSame(registered, JumpPadService.getPadAt(world, x, 64, z))
            }
        }
    }

    @Test
    fun `does not index blocks outside the trigger area`() {
        JumpPadService.registerPad(pad())

        assertNull(JumpPadService.getPadAt(world, 2, 64, 0))
        assertNull(JumpPadService.getPadAt(world, 0, 65, 0))
        assertNull(JumpPadService.getPadAt(otherWorld, 0, 64, 0))
    }

    @Test
    fun `spreads even sided areas around the origin block`() {
        JumpPadService.registerPad(pad(width = 2, length = 4))

        assertNull(JumpPadService.getPadAt(world, -1, 64, 0))
        assertTrue(JumpPadService.getPadAt(world, 0, 64, 0) != null)
        assertTrue(JumpPadService.getPadAt(world, 1, 64, 0) != null)
        assertNull(JumpPadService.getPadAt(world, 2, 64, 0))

        assertTrue(JumpPadService.getPadAt(world, 0, 64, -1) != null)
        assertTrue(JumpPadService.getPadAt(world, 0, 64, 2) != null)
        assertNull(JumpPadService.getPadAt(world, 0, 64, 3))
    }

    @Test
    fun `refuses to register the same pad twice`() {
        val registered = pad()

        assertTrue(JumpPadService.registerPad(registered))
        assertFalse(JumpPadService.registerPad(registered))
        assertEquals(1, JumpPadService.getPads().size)
    }

    @Test
    fun `moves the trigger area along when a pad is updated`() {
        val registered = pad()
        JumpPadService.registerPad(registered)

        val moved = registered.copy(origin = JumpPadPosition(world, 100.0, 64.0, 100.0))
        JumpPadService.updatePad(moved)

        assertNull(JumpPadService.getPadAt(world, 0, 64, 0))
        assertSame(moved, JumpPadService.getPadAt(world, 100, 64, 100))
        assertEquals(1, JumpPadService.getPads().size)
    }

    @Test
    fun `forgets the trigger area when a pad is deleted`() {
        val registered = pad()
        JumpPadService.registerPad(registered)

        JumpPadService.deletePad(registered)

        assertNull(JumpPadService.getPadAt(world, 0, 64, 0))
        assertTrue(JumpPadService.getPads().isEmpty())
    }

    @Test
    fun `looks pads up by the block a position is inside of`() {
        val registered = pad()
        JumpPadService.registerPad(registered)

        assertSame(
            registered,
            JumpPadService.getPadAt(JumpPadPosition(world, 0.75, 64.25, -0.5))
        )
    }
}
