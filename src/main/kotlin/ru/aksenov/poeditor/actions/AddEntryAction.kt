package ru.aksenov.poeditor.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import ru.aksenov.poeditor.PoIcons
import ru.aksenov.poeditor.editor.PoTableFileEditor

class AddEntryAction(
    private val editor: PoTableFileEditor
) : AnAction("Add Row", "Add new translation row", PoIcons.Add) {

    override fun actionPerformed(e: AnActionEvent) {
        editor.addRow()
    }
}