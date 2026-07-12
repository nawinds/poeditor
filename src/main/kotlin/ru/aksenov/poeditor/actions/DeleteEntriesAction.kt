package ru.aksenov.poeditor.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import ru.aksenov.poeditor.PoIcons
import ru.aksenov.poeditor.editor.PoTableFileEditor

class DeleteEntriesAction(
    private val editor: PoTableFileEditor
) : AnAction("Delete Row", "Delete selected translation rows", PoIcons.Delete) {

    override fun actionPerformed(e: AnActionEvent) {
        editor.deleteSelectedRows()
    }
}