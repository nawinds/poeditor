package ru.aksenov.poeditor.actions

import ru.aksenov.poeditor.PoIcons
import com.intellij.ide.IdeView
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil

abstract class CreateGettextFileAction(
    text: String,
    description: String,
    icon: javax.swing.Icon,
    private val extension: String,
    private val dialogTitle: String,
    private val defaultName: String
) : AnAction(text, description, icon) {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT) ?: return
        val view: IdeView = e.getData(LangDataKeys.IDE_VIEW) ?: return
        val directory = view.orChooseDirectory ?: return

        val rawName = Messages.showInputDialog(
            project,
            "Enter $extension file name:",
            dialogTitle,
            Messages.getQuestionIcon(),
            defaultName,
            null
        ) ?: return

        val fileName = if (rawName.endsWith(".$extension", ignoreCase = true)) {
            rawName
        } else {
            "$rawName.$extension"
        }

        WriteCommandAction.runWriteCommandAction(project) {
            val file = directory.createFile(fileName)
            VfsUtil.saveText(file.virtualFile, defaultContent(project.name))
            view.selectElement(file)
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.getData(CommonDataKeys.PROJECT)
        val view = e.getData(LangDataKeys.IDE_VIEW)

        e.presentation.isEnabledAndVisible = project != null && view?.orChooseDirectory != null
    }

    protected abstract fun defaultContent(projectName: String): String
}

class CreatePoFileAction : CreateGettextFileAction(
    text = "PO Translation File",
    description = "Create PO translation file",
    icon = PoIcons.PoFile,
    extension = "po",
    dialogTitle = "New PO Translation File",
    defaultName = "messages.po"
) {

    override fun defaultContent(projectName: String): String {
        return """
            msgid ""
            msgstr ""
            "Project-Id-Version: $projectName\n"
            "Report-Msgid-Bugs-To: \n"
            "POT-Creation-Date: \n"
            "PO-Revision-Date: \n"
            "Last-Translator: \n"
            "Language-Team: \n"
            "Language: ru\n"
            "MIME-Version: 1.0\n"
            "Content-Type: text/plain; charset=UTF-8\n"
            "Content-Transfer-Encoding: 8bit\n"
            
            msgctxt "example.id"
            msgid "Example source text"
            msgstr ""
            
        """.trimIndent()
    }
}
