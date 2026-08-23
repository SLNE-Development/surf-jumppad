package dev.slne.surf.jumppad.core.client.pad.service

import dev.slne.surf.jumppad.core.client.pad.JumpPad
import dev.slne.surf.jumppad.core.client.pad.JumpPadPosition
import dev.slne.surf.jumppad.core.client.pad.JumpPadType
import net.kyori.adventure.key.Key
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.RepeatedTest
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JumpPadServiceConcurrencyTest {
    private val world = Key.key("minecraft", "world")
    private val padId: UUID = UUID.fromString("00000000-0000-0000-0000-0000000000ff")

    private fun pad(originX: Double) = JumpPad(
        uuid = padId,
        origin = JumpPadPosition(world, originX, 64.0, 0.0),
        type = JumpPadType.VERTICAL,
        distance = 5,
        width = 3,
        length = 3
    )

    @BeforeEach
    fun reset() {
        JumpPadService.clear()
    }

    /**
     * Both areas of the edited pad cover the block the reader watches, so every single lookup has
     * to answer with a pad.
     */
    @RepeatedTest(20)
    fun `an edited pad stays visible on the blocks both of its areas cover`() {
        val left = pad(originX = 0.0)
        val right = pad(originX = 1.0)
        assertTrue(JumpPadService.registerPad(left))

        val running = AtomicBoolean(true)
        val misses = AtomicLong()
        val reads = AtomicLong()
        val started = CountDownLatch(2)

        val reader = Thread {
            started.countDown()
            started.await()

            while (running.get()) {
                // (0, 64, 0) sits inside the 3x3 area of both origins.
                if (JumpPadService.getPadAt(world, 0, 64, 0) == null) {
                    misses.incrementAndGet()
                }
                reads.incrementAndGet()
            }
        }

        val writer = Thread {
            started.countDown()
            started.await()

            var toggle = false
            while (running.get()) {
                JumpPadService.updatePad(if (toggle) right else left)
                toggle = !toggle
            }
        }

        reader.start()
        writer.start()

        // Long enough to interleave many thousands of reads with many thousands of edits.
        Thread.sleep(50)
        running.set(false)

        reader.join(TimeUnit.SECONDS.toMillis(5))
        writer.join(TimeUnit.SECONDS.toMillis(5))

        assertTrue(reads.get() > 0, "the reader never ran")
        assertEquals(0L, misses.get(), "the pad disappeared while it was being edited")
        assertEquals(1, JumpPadService.getPads().size)
    }
}
