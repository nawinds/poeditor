package ru.aksenov.poeditor.scanner

import kotlin.test.Test
import kotlin.test.assertEquals

class TranslationIdScannerTest {

    @Test
    fun findsGettextShorthandInJinja2Expressions() {
        val source = """
            <title>{{ _('page_title') }}</title>
            <p>{{ _("page_description") }}</p>
        """.trimIndent()

        assertEquals(
            setOf("page_title", "page_description"),
            TranslationIdScanner.scanText(source)
        )
    }

    @Test
    fun ignoresIdentifiersEndingInUnderscore() {
        val source = "value_('not_a_translation')"

        assertEquals(emptySet(), TranslationIdScanner.scanText(source))
    }
}
