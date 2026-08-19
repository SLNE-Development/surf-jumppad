package dev.slne.surf.jumppad.core.client.dialog

import dev.slne.surf.api.core.messages.adventure.buildText
import net.kyori.adventure.text.Component

/**
 * The keys and labels of the input fields the jump pad dialogs offer.
 */
object JumpPadInputTexts {
    const val LOCATION_KEY = "pad_location"
    const val STRENGTH_KEY = "pad_strength"
    const val BOX_KEY = "pad_box"
    const val TARGET_LOCATION_KEY = "pad_target_location"

    /**
     * The width every input field and body of the creation and edit dialogs uses.
     */
    const val INPUT_WIDTH = 400

    /**
     * The smallest launch distance a jump pad can be configured with.
     */
    const val MIN_STRENGTH = 1.0

    /**
     * The largest launch distance a jump pad can be configured with.
     */
    const val MAX_STRENGTH = 200.0

    /**
     * The launch distance a jump pad falls back to when none could be read.
     */
    const val DEFAULT_STRENGTH = 10

    /**
     * The value the box field starts out with while creating a jump pad.
     */
    const val INITIAL_BOX = "3x3"

    /**
     * The value the target field starts out with while creating a jump pad.
     */
    const val INITIAL_TARGET_LOCATION = "X Y Z"

    val createLocationLabel: Component = buildText { text("Startposition") }
    val createTargetLocationLabel: Component = buildText { text("Zielposition") }
    val createStrengthLabel: Component = buildText { text("Stärke (Blöcke)") }

    val editLocationLabel: Component = buildText { text("Location (X Y Z)") }
    val editTargetLocationLabel: Component = buildText { text("Ziel-Location (X Y Z)") }
    val editStrengthLabel: Component = buildText { text("Stärke") }

    val boxLabel: Component = buildText { text("Box (max. 10x10)") }
}
