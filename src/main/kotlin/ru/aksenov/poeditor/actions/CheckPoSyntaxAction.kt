package ru.aksenov.poeditor.actions

import ru.aksenov.poeditor.PoIcons
import ru.aksenov.poeditor.editor.PoTableFileEditor
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class CheckPoSyntaxAction(
    private val editor: PoTableFileEditor
) : AnAction("Check Syntax", "Check PO syntax", PoIcons.Check) {

    override fun actionPerformed(e: AnActionEvent) {
        editor.checkSyntax()
    }
}