package ru.aksenov.poeditor.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import ru.aksenov.poeditor.PoIcons
import ru.aksenov.poeditor.editor.PoTableFileEditor

class ChooseTemplateFileAction(
    private val editor: PoTableFileEditor
) : AnAction("Choose Template", "Choose .pot template file and add missing entries", PoIcons.Source) {

    override fun actionPerformed(e: AnActionEvent) {
        editor.chooseTemplateFile()
    }
}