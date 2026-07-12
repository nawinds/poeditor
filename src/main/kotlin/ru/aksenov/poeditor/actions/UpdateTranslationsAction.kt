package ru.aksenov.poeditor.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import ru.aksenov.poeditor.PoIcons
import ru.aksenov.poeditor.editor.PoTableFileEditor

class UpdateTranslationsAction(
    private val editor: PoTableFileEditor
) : AnAction("Update Translations", "Update current PO file from selected template", PoIcons.UpdateTranslations) {

    override fun actionPerformed(e: AnActionEvent) {
        editor.updateTranslationsFromTemplate()
    }
}