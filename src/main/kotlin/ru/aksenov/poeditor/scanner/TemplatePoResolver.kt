package ru.aksenov.poeditor.scanner

import com.intellij.openapi.vfs.VirtualFile

object TemplatePoResolver {

    private val candidateNames = listOf(
        "messages.pot",
        "template.pot",
        "translations.pot",
        "locale.pot"
    )

    fun findTemplateFile(currentFile: VirtualFile): VirtualFile? {
        val parent = currentFile.parent ?: return null

        for (candidateName in candidateNames) {
            val candidate = parent.findChild(candidateName)
            if (candidate != null && candidate.path != currentFile.path) {
                return candidate
            }
        }

        return parent.children.firstOrNull {
            it.extension == "pot" && it.path != currentFile.path
        }
    }
}