package dev.slne.surf.jumppad.core.client.pad.input

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JumpPadInputsTest {

    @Test
    fun `accepts whole coordinates separated by single spaces`() {
        assertTrue(JumpPadInputs.isLocation("1 2 3"))
        assertTrue(JumpPadInputs.isLocation("-10 64 -300"))
        assertTrue(JumpPadInputs.isLocation("0 0 0"))
    }

    @Test
    fun `rejects anything that is not three whole coordinates`() {
        assertFalse(JumpPadInputs.isLocation("1 2"))
        assertFalse(JumpPadInputs.isLocation("1 2 3 4"))
        assertFalse(JumpPadInputs.isLocation("1.5 2 3"))
        assertFalse(JumpPadInputs.isLocation("1  2 3"))
        assertFalse(JumpPadInputs.isLocation(" 1 2 3"))
        assertFalse(JumpPadInputs.isLocation("X Y Z"))
        assertFalse(JumpPadInputs.isLocation(""))
    }

    @Test
    fun `accepts areas written as two numbers around an x`() {
        assertTrue(JumpPadInputs.isBox("3x3"))
        assertTrue(JumpPadInputs.isBox("10x1"))
    }

    @Test
    fun `rejects areas that are not two numbers around an x`() {
        assertFalse(JumpPadInputs.isBox("3 x 3"))
        assertFalse(JumpPadInputs.isBox("-3x3"))
        assertFalse(JumpPadInputs.isBox("3x"))
        assertFalse(JumpPadInputs.isBox("3x3x3"))
        assertFalse(JumpPadInputs.isBox(""))
    }

    @Test
    fun `reads the coordinates of a block position`() {
        assertEquals(
            JumpPadInputs.Coordinates(1.0, 2.0, 3.0),
            JumpPadInputs.parseCoordinates("1 2 3")
        )
        assertEquals(
            JumpPadInputs.Coordinates(-10.0, 64.0, -300.0),
            JumpPadInputs.parseCoordinates("-10 64 -300")
        )
    }

    @Test
    fun `reports coordinates it cannot read`() {
        assertNull(JumpPadInputs.parseCoordinates("1 2"))
        assertNull(JumpPadInputs.parseCoordinates("a b c"))
    }

    @Test
    fun `reads the sides of an area`() {
        assertEquals(JumpPadInputs.Box(3, 5), JumpPadInputs.parseBox("3x5"))
    }

    @Test
    fun `falls back to a default area when the sides cannot be read`() {
        assertEquals(
            JumpPadInputs.Box(
                JumpPadInputs.DEFAULT_BOX_SIDE,
                JumpPadInputs.DEFAULT_BOX_SIDE
            ),
            JumpPadInputs.parseBox("nonsense")
        )
    }

    @Test
    fun `limits the sides an area may have`() {
        assertTrue(JumpPadInputs.Box(10, 10).isWithinLimit)
        assertFalse(JumpPadInputs.Box(11, 10).isWithinLimit)
        assertFalse(JumpPadInputs.Box(10, 11).isWithinLimit)
    }
}
