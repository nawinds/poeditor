package ru.aksenov.poeditor.actions

import ru.aksenov.poeditor.PoIcons
import ru.aksenov.poeditor.editor.PoTableFileEditor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class CompilePoAction(
    private val editor: PoTableFileEditor
) : AnAction("Compile MO", "Compile PO to MO", PoIcons.Compile) {

    override fun actionPerformed(e: AnActionEvent) {
        editor.compilePo()
    }
}