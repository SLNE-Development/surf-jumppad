package dev.slne.surf.jumppad.core.client.message

import dev.slne.surf.api.core.messages.adventure.buildText
import net.kyori.adventure.dialog.DialogLike
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextDecoration

/**
 * The chat messages jump pads send to players.
 */
object JumpPadMessages {
    /**
     * Tells a player that the block they touched belongs to a jump pad and offers to show it.
     *
     * @param infoDialog the dialog describing the jump pad
     * @return the message to send
     */
    fun padAtBlock(infoDialog: DialogLike): Component {
        val clickable = buildText {
            variableValue("HIER", TextDecoration.UNDERLINED)
            hoverEvent(buildText { info("Klicke hier, um dir das JumpPad anzusehen.") })
            clickEvent(ClickEvent.callback { it.showDialog(infoDialog) })
        }

        return buildText {
            appendErrorPrefix()
            error("An dieser Stelle befindet sich ein JumpPad!")
            appendNewErrorPrefixedLine()
            error("Klicke ")
            append(clickable)
            error(" um dir das JumpPad anzusehen!")
        }
    }

    /**
     * Tells a player that there is no jump pad where they looked and offers the list of all of them.
     *
     * @param listDialog the dialog listing every jump pad
     * @return the message to send
     */
    fun noPadAtBlock(listDialog: DialogLike): Component {
        val clickable = buildText {
            variableValue("HIER", TextDecoration.UNDERLINED)
            hoverEvent(buildText { info("Klicke hier, um ir die Liste existierender JumpPads anzusehen.") })
            clickEvent(ClickEvent.callback { it.showDialog(listDialog) })
        }

        return buildText {
            appendErrorPrefix()
            error("An dieser Stelle befindet sich kein JumpPad! ")
            appendNewErrorPrefixedLine()
            error("Klicke ")
            append(clickable)
            error(" um dir die Liste existierender JumpPads anzusehen.")
        }
    }
}
