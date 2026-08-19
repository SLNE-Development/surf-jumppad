package dev.slne.surf.jumppad.core.client.pad.input

/**
 * Parses and validates the values players type into the jump pad dialogs.
 */
object JumpPadInputs {
    /**
     * The largest side a jump pad area may have.
     */
    const val MAX_BOX_SIDE = 10

    /**
     * The area a jump pad falls back to when none could be read.
     */
    const val DEFAULT_BOX_SIDE = 3

    private val locationRegex = Regex("""^-?\d+\s-?\d+\s-?\d+$""")
    private val boxRegex = Regex("""^\d+x\d+$""")

    /**
     * Returns whether [raw] is three whole numbers separated by single spaces.
     *
     * @param raw the value typed by the player
     * @return `true` if the value describes a block position
     */
    fun isLocation(raw: String): Boolean = locationRegex.matches(raw)

    /**
     * Returns whether [raw] describes an area, such as `3x3`.
     *
     * @param raw the value typed by the player
     * @return `true` if the value describes an area
     */
    fun isBox(raw: String): Boolean = boxRegex.matches(raw)

    /**
     * Reads the block position [raw] describes.
     *
     * @param raw the value typed by the player
     * @return the coordinates, or `null` if the value is not a block position
     */
    fun parseCoordinates(raw: String): Coordinates? {
        val parts = raw.trim().split(" ")
        if (parts.size < 3) return null

        val x = parts[0].toDoubleOrNull() ?: return null
        val y = parts[1].toDoubleOrNull() ?: return null
        val z = parts[2].toDoubleOrNull() ?: return null

        return Coordinates(x, y, z)
    }

    /**
     * Reads the area [raw] describes, falling back to a [DEFAULT_BOX_SIDE] square.
     *
     * @param raw the value typed by the player
     * @return the width and the length of the area
     */
    fun parseBox(raw: String): Box {
        val parts = raw.split("x").mapNotNull { it.toIntOrNull() }
        if (parts.size != 2) return Box(DEFAULT_BOX_SIDE, DEFAULT_BOX_SIDE)

        return Box(parts[0], parts[1])
    }

    /**
     * A block position typed by a player.
     *
     * @property x the x coordinate
     * @property y the y coordinate
     * @property z the z coordinate
     */
    data class Coordinates(val x: Double, val y: Double, val z: Double)

    /**
     * An area typed by a player.
     *
     * @property width the size of the area on the X axis
     * @property length the size of the area on the Z axis
     */
    data class Box(val width: Int, val length: Int) {
        /**
         * Whether both sides stay within [MAX_BOX_SIDE].
         */
        val isWithinLimit get() = width <= MAX_BOX_SIDE && length <= MAX_BOX_SIDE
    }
}
