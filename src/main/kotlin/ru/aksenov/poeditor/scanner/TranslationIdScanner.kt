package ru.aksenov.poeditor.scanner

object TranslationIdScanner {

    private val regexes = listOf(
        Regex("""(?<![\w$])_\s*\(\s*["']([^"']+)["']\s*\)"""),
        Regex("""\bt\s*\(\s*["']([^"']+)["']\s*\)"""),
        Regex("""\bi18n\s*\(\s*["']([^"']+)["']\s*\)"""),
        Regex("""\btranslate\s*\(\s*["']([^"']+)["']\s*\)"""),
        Regex("""\bgettext\s*\(\s*["']([^"']+)["']\s*\)"""),
        Regex($$"""\$t\s*\(\s*["']([^"']+)["']\s*\)"""),
        Regex("""\btr\s*\(\s*["']([^"']+)["']\s*\)""")
    )

    fun scanText(text: String): Set<String> {
        val result = linkedSetOf<String>()

        for (regex in regexes) {
            regex.findAll(text).forEach { match ->
                val id = match.groupValues[1].trim()
                if (id.isNotBlank()) {
                    result.add(id)
                }
            }
        }

        return result
    }
}
