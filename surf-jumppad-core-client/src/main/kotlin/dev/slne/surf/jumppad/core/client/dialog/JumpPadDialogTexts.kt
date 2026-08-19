package dev.slne.surf.jumppad.core.client.dialog

import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.adventure.appendNewline
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.builder.SurfComponentBuilder
import dev.slne.surf.jumppad.core.client.pad.JumpPad
import dev.slne.surf.jumppad.core.client.pad.JumpPadPosition
import dev.slne.surf.jumppad.core.client.pad.JumpPadType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import java.util.*

/**
 * Every piece of text the jump pad dialogs are built from.
 *
 * Dialogs themselves are platform specific, their wording is not.
 */
object JumpPadDialogTexts {

    /**
     * The width the jump pad management dialog renders its body with.
     */
    const val MAIN_BODY_WIDTH = 400

    /**
     * The width the dialog listing every jump pad renders its body with.
     */
    const val LIST_BODY_WIDTH = 400

    /**
     * The width the dialog without any jump pad renders its body with.
     */
    const val EMPTY_LIST_BODY_WIDTH = 300

    /**
     * The width the dialog describing a jump pad renders its body with.
     */
    const val INFO_BODY_WIDTH = 350

    /**
     * The width the dialog deleting a jump pad renders its body with.
     */
    const val DELETE_BODY_WIDTH = 300

    /**
     * The width the dialog asking which kind of jump pad to create renders its body with.
     */
    const val DECIDE_FOR_TYPE_BODY_WIDTH = 400

    /**
     * The width the dialog creating a jump pad renders its body with.
     */
    const val CREATE_BODY_WIDTH = 400

    /**
     * The width the dialog editing a jump pad renders its body with.
     */
    const val EDIT_BODY_WIDTH = 450

    /**
     * The width a dialog reporting a result renders its body with.
     */
    const val RESULT_BODY_WIDTH = 400

    /**
     * The number of columns the dialog listing every jump pad renders its entries in.
     */
    const val LIST_COLUMNS = 3

    /**
     * The width of a single entry in the dialog listing every jump pad.
     */
    const val LIST_BUTTON_WIDTH = 200

    /**
     * The title of the jump pad management dialog.
     */
    val mainTitle: Component = buildText {
        primary("JUMPPAD ".toSmallCaps())
        success("VERWALTUNG".toSmallCaps())
    }

    /**
     * The title of the dialog listing every jump pad.
     */
    val listTitle: Component = buildText {
        primary("JUMPPAD LISTE".toSmallCaps())
    }

    /**
     * The title of the dialog asking which kind of jump pad to create.
     */
    val decideForTypeTitle: Component = buildText {
        primary("JUMPPAD ".toSmallCaps())
        info("TYP WÄHLEN".toSmallCaps())
    }

    /**
     * The title of the dialog creating a jump pad.
     */
    val createTitle: Component = buildText {
        primary("JUMPPAD ".toSmallCaps())
        success("ERSTELLEN".toSmallCaps())
    }

    /**
     * The title of the dialog reporting a successful creation.
     */
    val createSuccessTitle: Component = buildText {
        primary("JUMPPAD ".toSmallCaps())
        success("ERSTELLEN ".toSmallCaps())
        success("ERFOLG".toSmallCaps())
    }

    /**
     * The title of the dialog reporting a failed creation.
     */
    val createFailTitle: Component = buildText {
        primary("JUMPPAD ".toSmallCaps())
        success("ERSTELLEN ".toSmallCaps())
        error("FEHLER".toSmallCaps())
    }

    /**
     * The body shown when no jump pad exists yet.
     */
    val emptyListBody: Component = buildText {
        error("Es existieren aktuell keine JumpPads.")
    }

    /**
     * The body of the dialog asking which kind of jump pad to create.
     */
    val decideForTypeBody: Component = buildText {
        info("Wähle aus, welchen Typ von JumpPad du erstellen möchtest.")
        appendNewline(2)
    }

    /**
     * The body reporting a successfully created jump pad.
     */
    val createSuccessBody: Component = buildText {
        success("Erfolg!", TextDecoration.BOLD)
        appendNewline(2)
        success("Das JumpPad wurde erfolgreich erstellt!")
    }

    /**
     * The body reporting that the typed values could not be used.
     */
    val invalidInputBody: Component = buildText {
        error("Fehler!", TextDecoration.BOLD)
        appendNewline(2)

        error("Die angegebenen Felder wurden nicht korrekt ausgefüllt.")
        appendNewline(2)

        error("Bitte versuche es erneut.")
    }

    /**
     * The body of the jump pad management dialog.
     *
     * @param pads every currently registered jump pad
     * @return the body text
     */
    fun mainBody(pads: Collection<JumpPad>): Component = buildText {
        info("Willkommen in der JumpPad-Verwaltung.")
        appendNewline(2)

        info("Aktuell existieren insgesamt ")
        variableValue(pads.size)
        info(" JumpPads.")
        appendNewline(2)

        if (pads.isNotEmpty()) {
            primary("Statistik nach Typen:")
            appendNewline()
            appendTypeStatistics(pads)
        } else {
            error("Es wurden noch keine JumpPads erstellt.")
        }
    }

    /**
     * The body of the dialog listing every jump pad.
     *
     * @param pads every currently registered jump pad
     * @return the body text
     */
    fun listBody(pads: Collection<JumpPad>): Component = buildText {
        info("Aktuell existieren insgesamt ")
        variableValue(pads.size)
        info(" JumpPads.")
        appendNewline(2)

        primary("Statistik nach Typen:")
        appendNewline()
        appendTypeStatistics(pads)

        appendNewline()
        info("Klicke auf ein JumpPad, um Details zu sehen.")
    }

    /**
     * The title of the dialog describing a single jump pad.
     *
     * @param pad the jump pad being described
     * @return the title text
     */
    fun infoTitle(pad: JumpPad): Component = buildText {
        variableValue(blockPosition(pad.origin))
    }

    /**
     * The body of the dialog describing a single jump pad.
     *
     * @param pad the jump pad being described
     * @param worldName the name of the world the jump pad is placed in
     * @return the body text
     */
    fun infoBody(pad: JumpPad, worldName: String): Component = buildText {
        info("Informationen zum JumpPad am Standort:")
        appendNewline()
        variableValue(blockPosition(pad.origin))
        appendNewline(2)

        primary("UUID: ")
        variableValue(pad.uuid.toString())
        appendNewline()

        primary("Typ: ")
        append(pad.type.displayComponent)
        appendNewline()

        primary("Welt: ")
        variableValue(worldName)
        appendNewline()

        if (pad.type == JumpPadType.STATIC) {
            primary("Ziel: ")
            val target = pad.targetLocation
            if (target != null) {
                variableValue(blockPosition(target))
            } else {
                error("Nicht konfiguriert")
            }
        } else {
            primary("Stärke: ")
            variableValue("${pad.distance} Blöcke")
        }
        appendNewline()

        primary("Bereich: ")
        variableValue("${pad.width}x${pad.length}")
        appendNewline(2)
    }

    /**
     * The title of the dialog deleting a jump pad.
     *
     * @param pad the jump pad being deleted
     * @return the title text
     */
    fun deleteTitle(pad: JumpPad): Component = buildText {
        primary("JUMPPAD ".toSmallCaps())
        primary("LISTE ".toSmallCaps())
        variableValue("${blockPosition(pad.origin)} ")
        error("LÖSCHEN".toSmallCaps())
    }

    /**
     * The body of the dialog deleting a jump pad.
     *
     * @param pad the jump pad being deleted
     * @param worldName the name of the world the jump pad is placed in
     * @return the body text
     */
    fun deleteBody(pad: JumpPad, worldName: String): Component = buildText {
        error("Achtung!", TextDecoration.BOLD)
        appendNewline(2)

        error("Du bist dabei ein JumpPad unwiderruflich zu löschen!")
        appendNewline(2)

        error("Bitte bestätige dein Vorhaben!")
        appendNewline(2)

        info("Im Folgenden findest du die Informationen zum ausgewählten JumpPad.")
        appendNewline(2)

        primary("UUID: ")
        variableValue(pad.uuid.toString())
        appendNewline(2)

        primary("Typ: ")
        append(pad.type.displayComponent)
        appendNewline(2)

        primary("Position: ")
        variableValue(blockPosition(pad.origin))
        appendNewline(2)

        primary("Welt: ")
        variableValue(worldName)
        appendNewline(2)

        if (pad.type == JumpPadType.STATIC) {
            primary("Ziel: ")
            val target = pad.targetLocation
            if (target != null) {
                variableValue(blockPosition(target))
            } else {
                variableValue("Nicht gesetzt")
            }
            appendNewline(2)
        } else {
            primary("Stärke: ")
            variableValue(pad.distance)
            appendNewline(2)
        }

        primary("Box: ")
        variableValue("${pad.width}x${pad.length}")
        appendNewline(2)
    }

    /**
     * The body of the dialog creating a jump pad.
     *
     * @param uuid the identifier the new jump pad will have
     * @return the body text
     */
    fun createBody(uuid: UUID): Component = buildText {
        info("Du bist dabei ein neues JumpPad zu erstellen.")
        appendNewline(2)

        primary("UUID: ")
        variableValue(uuid.toString())
        appendNewline(2)
    }

    /**
     * The title of the dialog editing a jump pad.
     *
     * @param pad the jump pad being edited
     * @return the title text
     */
    fun editTitle(pad: JumpPad): Component = buildText {
        primary("JUMPPAD ".toSmallCaps())
        success("EDITIEREN ".toSmallCaps())
        variableValue(blockPosition(pad.origin))
    }

    /**
     * The title of the dialog reporting a failed edit.
     *
     * @param pad the jump pad being edited
     * @return the title text
     */
    fun editFailTitle(pad: JumpPad): Component = buildText {
        primary("JUMPPAD ".toSmallCaps())
        primary("LISTE ".toSmallCaps())
        variableValue("${blockPosition(pad.origin)} ")
        primary("KONFIGURIEREN ".toSmallCaps())
        error("FEHLER".toSmallCaps())
    }

    /**
     * The body of the dialog editing a jump pad.
     *
     * @param pad the jump pad being edited
     * @return the body text
     */
    fun editBody(pad: JumpPad): Component = buildText {
        info("Du bearbeitest das JumpPad:")
        variableValue(" ${pad.uuid}")
        appendNewline(2)

        primary("Typ: ")
        append(pad.type.displayComponent)
        appendNewline()

        if (pad.type == JumpPadType.STATIC) {
            primary("Ziel: ")
            val target = pad.targetLocation
            variableValue(if (target != null) blockPosition(target) else "Nicht gesetzt")
        } else {
            primary("Power: ")
            variableValue("${pad.distance} Blöcke")
        }
        appendNewline()

        primary("Area: ")
        variableValue("${pad.width}x${pad.length}")
        appendNewline(2)

        info("Passe die Werte über die unteren Felder an.")
    }

    /**
     * Renders the block coordinates of [position] the way the dialogs show them.
     *
     * @param position the position to render
     * @return the rendered coordinates
     */
    fun blockPosition(position: JumpPadPosition) =
        "${position.blockX} ${position.blockY} ${position.blockZ}"

    private fun SurfComponentBuilder.appendTypeStatistics(pads: Collection<JumpPad>) {
        val padsByType = pads.groupBy { it.type }

        JumpPadType.entries.forEach { type ->
            val count = padsByType[type]?.size ?: 0
            if (count > 0) {
                spacer(" - ")
                append(type.displayComponent)
                info(": ")
                variableValue(count)
                appendNewline()
            }
        }
    }
}
