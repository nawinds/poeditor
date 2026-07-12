package ru.aksenov.poeditor.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import ru.aksenov.poeditor.PoIcons
import ru.aksenov.poeditor.editor.PoTableFileEditor

class UpdateTemplateAction(
    private val editor: PoTableFileEditor
) : AnAction("Update Template", "Scan project source code and update translation template", PoIcons.UpdateTemplate) {

    override fun actionPerformed(e: AnActionEvent) {
        editor.updateTemplateFromProjectSources()
    }
}