package ru.aksenov.poeditor.parser

object PoParser {

    private enum class Field {
        MSGCTXT,
        MSGID,
        MSGID_PLURAL,
        MSGSTR,
        PREVIOUS_MSGID
    }

    private data class Target(
        val field: Field,
        val index: Int? = null
    )

    fun parse(text: String): PoCatalog {
        val entries = mutableListOf<PoEntry>()

        var current = PoEntry()
        var target: Target? = null
        var touched = false

        fun hasUsefulData(entry: PoEntry): Boolean {
            return entry.translatorComments.isNotEmpty()
                    || entry.extractedComments.isNotEmpty()
                    || entry.references.isNotEmpty()
                    || entry.flags.isNotEmpty()
                    || entry.context != null
                    || entry.msgid.isNotEmpty()
                    || entry.msgidPlural != null
                    || entry.translations.isNotEmpty()
                    || entry.previousMsgid != null
        }

        fun finishEntry() {
            if (touched || hasUsefulData(current)) {
                if (current.translations.isEmpty()) {
                    current.translations.add("")
                }
                entries.add(current)
            }

            current = PoEntry()
            target = null
            touched = false
        }

        text.lineSequence().forEach { rawLine ->
            val line = rawLine.trimStart()

            if (line.isBlank()) {
                finishEntry()
                return@forEach
            }

            touched = true

            when {
                line.startsWith("#.") -> {
                    current.extractedComments.add(line.removePrefix("#.").trimStart())
                    target = null
                }

                line.startsWith("#:") -> {
                    val refs = line.removePrefix("#:")
                        .trim()
                        .split(Regex("""\s+"""))
                        .filter { it.isNotBlank() }

                    current.references.addAll(refs)
                    target = null
                }

                line.startsWith("#,") -> {
                    val flags = line.removePrefix("#,")
                        .split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }

                    current.flags.addAll(flags)
                    target = null
                }

                line.startsWith("#|") -> {
                    parsePreviousLine(line.removePrefix("#|").trimStart(), current)
                    target = Target(Field.PREVIOUS_MSGID)
                }

                line.startsWith("#") -> {
                    current.translatorComments.add(line.removePrefix("#").trimStart())
                    target = null
                }

                line.startsWith("msgctxt") -> {
                    current.context = readDirectiveValue(line)
                    target = Target(Field.MSGCTXT)
                }

                line.startsWith("msgid_plural") -> {
                    current.msgidPlural = readDirectiveValue(line)
                    target = Target(Field.MSGID_PLURAL)
                }

                line.startsWith("msgid") -> {
                    current.msgid = readDirectiveValue(line)
                    target = Target(Field.MSGID)
                }

                line.startsWith("msgstr[") -> {
                    val index = readMsgstrIndex(line)
                    current.ensureTranslationIndex(index)
                    current.translations[index] = readDirectiveValue(line)
                    target = Target(Field.MSGSTR, index)
                }

                line.startsWith("msgstr") -> {
                    current.ensureTranslationIndex(0)
                    current.translations[0] = readDirectiveValue(line)
                    target = Target(Field.MSGSTR, 0)
                }

                line.startsWith("\"") -> {
                    val value = parsePoStringLiteral(line)
                    appendContinuation(current, target, value)
                }
            }
        }

        finishEntry()

        return PoCatalog(entries)
    }

    private fun parsePreviousLine(line: String, entry: PoEntry) {
        when {
            line.startsWith("msgid") -> {
                entry.previousMsgid = readDirectiveValue(line)
            }
        }
    }

    private fun appendContinuation(entry: PoEntry, target: Target?, value: String) {
        if (target == null) return

        when (target.field) {
            Field.MSGCTXT -> {
                entry.context = entry.context.orEmpty() + value
            }

            Field.MSGID -> {
                entry.msgid += value
            }

            Field.MSGID_PLURAL -> {
                entry.msgidPlural = entry.msgidPlural.orEmpty() + value
            }

            Field.MSGSTR -> {
                val index = target.index ?: 0
                entry.ensureTranslationIndex(index)
                entry.translations[index] += value
            }

            Field.PREVIOUS_MSGID -> {
                entry.previousMsgid = entry.previousMsgid.orEmpty() + value
            }
        }
    }

    private fun readMsgstrIndex(line: String): Int {
        val regex = Regex("""msgstr\[(\d+)]""")
        val match = regex.find(line) ?: return 0
        return match.groupValues[1].toIntOrNull() ?: 0
    }

    private fun readDirectiveValue(line: String): String {
        val firstQuote = line.indexOf('"')
        if (firstQuote == -1) return ""
        return parsePoStringLiteral(line.substring(firstQuote))
    }

    private fun parsePoStringLiteral(text: String): String {
        val firstQuote = text.indexOf('"')
        if (firstQuote == -1) return ""

        val result = StringBuilder()
        var i = firstQuote + 1

        while (i < text.length) {
            val ch = text[i]

            when {
                ch == '"' -> {
                    break
                }

                ch == '\\' && i + 1 < text.length -> {
                    val next = text[i + 1]

                    when (next) {
                        'n' -> result.append('\n')
                        't' -> result.append('\t')
                        'r' -> result.append('\r')
                        'b' -> result.append('\b')
                        'f' -> result.append('\u000C')
                        '\\' -> result.append('\\')
                        '"' -> result.append('"')
                        else -> result.append(next)
                    }

                    i += 2
                    continue
                }

                else -> {
                    result.append(ch)
                }
            }

            i++
        }

        return result.toString()
    }
}