package ru.aksenov.poeditor.actions

import ru.aksenov.poeditor.PoIcons
import ru.aksenov.poeditor.editor.PoTableFileEditor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class SavePoAction(
    private val editor: PoTableFileEditor
) : AnAction("Save PO", "Save PO file", PoIcons.Save) {

    override fun actionPerformed(e: AnActionEvent) {
        editor.savePo()
    }
}