package dev.slne.surf.jumppad.core.client.dialog

import dev.slne.surf.api.core.messages.adventure.buildText
import net.kyori.adventure.text.Component

/**
 * The labels and tooltips of the buttons the jump pad dialogs offer.
 */
object JumpPadButtonTexts {
    val createPadLabel: Component = buildText { success("JumPad erstellen") }
    val createPadTooltip: Component =
        buildText { info("Klicke hier, um ein neues JumpPad zu erstellen.") }

    val showPadsLabel: Component = buildText { primary("JumPad ansehen") }
    val showPadsTooltip: Component =
        buildText { info("Klicke hier, um die Liste aller existierenden JumpPads zu öffnen.") }

    val closeLabel: Component = buildText { spacer("Schließen") }
    val closeTooltip: Component = buildText { info("Klicke hier, um das Menü zu verlassen.") }

    val backLabel: Component = buildText { spacer("Zurück") }
    val plainBackLabel: Component = buildText { text("Zurück") }

    val backToMainTooltip: Component =
        buildText { info("Klicke hier, um zurück zum Hauptmenü zu gelangen.") }
    val backToMainMenuTooltip: Component =
        buildText { info("Klicke hier, um zurück zum hauptmenü zu gelangen.") }
    val cancelTooltip: Component = buildText { info("Klicke hier, um den Vorgang abzubrechen.") }
    val backToCreationTooltip: Component =
        buildText { info("Klicke hier, um zurück zur Erstellung zu gelangen.") }

    val createLabel: Component = buildText { success("JumpPad erstellen") }
    val createFromListTooltip: Component =
        buildText { info("Klicke hier, um ein JumpPad zu erstellen.") }
    val createConfirmTooltip: Component =
        buildText { info("Klicke hier, um das JumpPad zu erstellen.") }

    val cancelCreationLabel: Component = buildText { error("Erstellung abbrechen") }
    val chooseTypeTooltip: Component =
        buildText { info("Klicke hier, ein JumpPad von diesem Typ zu erstellen.") }

    val teleportLabel: Component = buildText { primary("Teleportieren") }
    val teleportTooltip: Component =
        buildText { info("Klicke hier, um dich zum JumpPad zu teleportieren.") }

    val editLabel: Component = buildText { primary("Konfigurieren") }
    val editTooltip: Component =
        buildText { info("Klicke hier, um die Einstellungen des JumpPads zu konfigurieren.") }

    val deleteLabel: Component = buildText { error("Löschen") }
    val deleteTooltip: Component = buildText { info("Klicke hier, um das JumpPad zu löschen.") }
    val deleteConfirmTooltip: Component =
        buildText { info("Klicke hier, um das jumpPad zu löschen.") }

    val viewLabel: Component = buildText { info("Ansehen") }
    val viewTooltip: Component = buildText { info("Klicke hier, um dir das JumpPad anzusehen.") }

    val saveLabel: Component = buildText { success("Änderungen speichern") }
    val saveTooltip: Component = buildText { info("Klicke hier, um die Änderungen zu übernehmen.") }
}
