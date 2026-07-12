package ru.aksenov.poeditor.editor

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class PoFileEditorProvider : FileEditorProvider {

    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file.extension == "po" || file.extension == "pot"
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return PoTableFileEditor(project, file)
    }

    override fun getEditorTypeId(): String {
        return "po-table-editor"
    }

    override fun getPolicy(): FileEditorPolicy {
        return FileEditorPolicy.PLACE_BEFORE_DEFAULT_EDITOR
    }
}