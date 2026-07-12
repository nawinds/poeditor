package ru.aksenov.poeditor.parser

object PoWriter {

    fun write(catalog: PoCatalog): String {
        return catalog.entries
            .joinToString(separator = "\n\n") { writeEntry(it).trimEnd() }
            .trimEnd() + "\n"
    }

    private fun writeEntry(entry: PoEntry): String {
        val sb = StringBuilder()

        entry.translatorComments.forEach { comment ->
            if (comment.isBlank()) {
                sb.appendLine("#")
            } else {
                sb.appendLine("# $comment")
            }
        }

        entry.extractedComments.forEach { comment ->
            sb.appendLine("#. $comment")
        }

        if (entry.references.isNotEmpty()) {
            sb.appendLine("#: ${entry.references.joinToString(" ")}")
        }

        if (entry.flags.isNotEmpty()) {
            sb.appendLine("#, ${entry.flags.joinToString(", ")}")
        }

        entry.previousMsgid?.let {
            appendPoString(sb, "#| msgid", it)
        }

        entry.context?.let {
            appendPoString(sb, "msgctxt", it)
        }

        appendPoString(sb, "msgid", entry.msgid)

        if (entry.msgidPlural != null) {
            appendPoString(sb, "msgid_plural", entry.msgidPlural.orEmpty())

            val count = entry.translations.size.coerceAtLeast(2)
            for (i in 0 until count) {
                appendPoString(sb, "msgstr[$i]", entry.translations.getOrNull(i).orEmpty())
            }
        } else {
            appendPoString(sb, "msgstr", entry.translations.getOrNull(0).orEmpty())
        }

        return sb.toString()
    }

    private fun appendPoString(sb: StringBuilder, keyword: String, value: String) {
        val parts = splitKeepingNewlines(value)

        if (parts.size == 1) {
            sb.append(keyword)
                .append(" ")
                .appendLine(quote(parts[0]))
            return
        }

        sb.append(keyword).appendLine(" \"\"")

        for (part in parts) {
            sb.appendLine(quote(part))
        }
    }

    private fun splitKeepingNewlines(value: String): List<String> {
        if (!value.contains('\n')) {
            return listOf(value)
        }

        val result = mutableListOf<String>()
        var start = 0

        for (i in value.indices) {
            if (value[i] == '\n') {
                result.add(value.substring(start, i + 1))
                start = i + 1
            }
        }

        if (start < value.length) {
            result.add(value.substring(start))
        }

        if (result.isEmpty()) {
            result.add("")
        }

        return result
    }

    private fun quote(value: String): String {
        val escaped = buildString {
            for (ch in value) {
                when (ch) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\t' -> append("\\t")
                    '\r' -> append("\\r")
                    '\b' -> append("\\b")
                    '\u000C' -> append("\\f")
                    else -> append(ch)
                }
            }
        }

        return "\"$escaped\""
    }
}