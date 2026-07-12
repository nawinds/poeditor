package ru.aksenov.poeditor.scanner

import ru.aksenov.poeditor.parser.PoParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TranslationTemplateUpdaterTest {

    @Test
    fun importsStandardBabelEntriesStoredInMsgid() {
        val template = PoParser.parse(
            """
                msgid ""
                msgstr ""
                "Content-Type: text/plain; charset=utf-8\n"

                #: main.py:98
                msgid "not_found_header"
                msgstr ""

                #: templates/index.jinja2:3
                msgid "page_title"
                msgstr ""
            """.trimIndent()
        )
        val translation = PoParser.parse(
            """
                msgid ""
                msgstr ""
                "Language: ru\n"
            """.trimIndent()
        )

        val result = TranslationTemplateUpdater.updateTranslationFromTemplate(translation, template)

        assertEquals(2, result.added)
        assertEquals(listOf("not_found_header", "page_title"), translation.translatableEntries.map { it.msgid })
        assertEquals(listOf("main.py:98"), translation.translatableEntries.first().references)
        assertNull(translation.translatableEntries.first().context)
    }

    @Test
    fun keepsExistingTranslationsWhenRefreshing() {
        val template = PoParser.parse("""msgid "page_title"
            |msgstr ""
        """.trimMargin())
        val translation = PoParser.parse("""msgid "page_title"
            |msgstr "Заголовок"
        """.trimMargin())

        val result = TranslationTemplateUpdater.updateTranslationFromTemplate(translation, template)

        assertEquals(0, result.added)
        assertEquals("Заголовок", translation.translatableEntries.single().translation)
    }
}
