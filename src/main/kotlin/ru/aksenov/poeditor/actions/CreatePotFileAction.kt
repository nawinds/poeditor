package ru.aksenov.poeditor.actions

import ru.aksenov.poeditor.PoIcons

class CreatePotFileAction : CreateGettextFileAction(
    text = "POT Translation Template",
    description = "Create POT translation template",
    icon = PoIcons.PotFile,
    extension = "pot",
    dialogTitle = "New POT Translation Template",
    defaultName = "messages.pot"
) {

    override fun defaultContent(projectName: String): String {
        return """
            msgid ""
            msgstr ""
            "Project-Id-Version: $projectName\n"
            "Report-Msgid-Bugs-To: \n"
            "POT-Creation-Date: \n"
            "MIME-Version: 1.0\n"
            "Content-Type: text/plain; charset=UTF-8\n"
            "Content-Transfer-Encoding: 8bit\n"
            
            msgctxt "example.id"
            msgid "Example source text"
            msgstr ""
            
        """.trimIndent()
    }
}
