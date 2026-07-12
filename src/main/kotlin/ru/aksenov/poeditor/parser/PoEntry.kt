package ru.aksenov.poeditor.parser

data class PoCatalog(
    val entries: MutableList<PoEntry>
) {
    val translatableEntries: List<PoEntry>
        get() = entries.filterNot { it.isHeader }

    fun ensureUtf8Header(language: String = "ru") {
        val header = entries.firstOrNull { it.isHeader }

        if (header == null) {
            entries.add(
                0,
                PoEntry(
                    msgid = "",
                    translations = mutableListOf(defaultHeader(language))
                )
            )
            return
        }

        val currentHeader = header.translations.firstOrNull().orEmpty()
        val lines = currentHeader
            .split('\n')
            .filter { it.isNotEmpty() }
            .toMutableList()

        fun hasHeader(prefix: String): Boolean {
            return lines.any { it.startsWith(prefix) }
        }

        if (!hasHeader("Project-Id-Version:")) {
            lines.add(0, "Project-Id-Version: PO Editor")
        }

        if (!hasHeader("Report-Msgid-Bugs-To:")) {
            lines.add("Report-Msgid-Bugs-To: ")
        }

        if (!hasHeader("PO-Revision-Date:")) {
            lines.add("PO-Revision-Date: ")
        }

        if (!hasHeader("Last-Translator:")) {
            lines.add("Last-Translator: ")
        }

        if (!hasHeader("Language-Team:")) {
            lines.add("Language-Team: ")
        }

        if (!hasHeader("Language:")) {
            lines.add("Language: $language")
        }

        if (!hasHeader("MIME-Version:")) {
            lines.add("MIME-Version: 1.0")
        }

        if (!hasHeader("Content-Type:")) {
            lines.add("Content-Type: text/plain; charset=UTF-8")
        }

        if (!hasHeader("Content-Transfer-Encoding:")) {
            lines.add("Content-Transfer-Encoding: 8bit")
        }

        header.translations = mutableListOf(lines.joinToString("\n", postfix = "\n"))
    }

    fun deepCopy(): PoCatalog {
        return PoCatalog(
            entries.map { it.deepCopy() }.toMutableList()
        )
    }

    private fun defaultHeader(language: String): String {
        return buildString {
            append("Project-Id-Version: PO Editor\n")
            append("Report-Msgid-Bugs-To: \n")
            append("POT-Creation-Date: \n")
            append("PO-Revision-Date: \n")
            append("Last-Translator: \n")
            append("Language-Team: \n")
            append("Language: $language\n")
            append("MIME-Version: 1.0\n")
            append("Content-Type: text/plain; charset=UTF-8\n")
            append("Content-Transfer-Encoding: 8bit\n")
        }
    }
}

data class PoEntry(
    var translatorComments: MutableList<String> = mutableListOf(),
    var extractedComments: MutableList<String> = mutableListOf(),
    var references: MutableList<String> = mutableListOf(),
    var flags: MutableSet<String> = linkedSetOf(),

    var context: String? = null,
    var msgid: String = "",
    var msgidPlural: String? = null,
    var translations: MutableList<String> = mutableListOf(),

    var previousMsgid: String? = null,

    /**
     * Runtime-only status. Не сохраняем в .po,
     * чтобы msgfmt не ругался на кастомные флаги.
     */
    var removedFromTemplate: Boolean = false
) {
    val isHeader: Boolean
        get() = msgid.isEmpty() && context == null && msgidPlural == null

    val isFuzzy: Boolean
        get() = "fuzzy" in flags

    val translation: String
        get() = translations.getOrNull(0).orEmpty()

    val isTranslated: Boolean
        get() = translation.isNotBlank()

    fun identifier(): String {
        return context.orEmpty()
    }

    fun ensureTranslationIndex(index: Int) {
        while (translations.size <= index) {
            translations.add("")
        }
    }

    fun setTranslation(value: String) {
        ensureTranslationIndex(0)
        translations[0] = value
    }

    fun isCompletelyEmpty(): Boolean {
        return context.isNullOrBlank() &&
                msgid.isBlank() &&
                translation.isBlank() &&
                translatorComments.isEmpty() &&
                extractedComments.isEmpty() &&
                references.isEmpty() &&
                flags.isEmpty()
    }

    fun deepCopy(): PoEntry {
        return copy(
            translatorComments = translatorComments.toMutableList(),
            extractedComments = extractedComments.toMutableList(),
            references = references.toMutableList(),
            flags = flags.toMutableSet(),
            translations = translations.toMutableList()
        )
    }
}