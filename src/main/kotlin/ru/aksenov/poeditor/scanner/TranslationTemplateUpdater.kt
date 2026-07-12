package ru.aksenov.poeditor.scanner

import ru.aksenov.poeditor.parser.PoCatalog
import ru.aksenov.poeditor.parser.PoEntry

object TranslationTemplateUpdater {

    data class UpdateResult(
        val added: Int = 0,
        val removedEmpty: Int = 0,
        val markedRemoved: Int = 0
    )

    fun updateTemplateWithIds(
        catalog: PoCatalog,
        ids: Set<String>
    ): UpdateResult {
        val normalizedIds = ids
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()

        val existingIds = catalog.translatableEntries
            .mapNotNull(::entryKey)
            .toSet()

        val newIds = normalizedIds
            .filter { it !in existingIds }
            .sorted()

        for (id in newIds) {
            catalog.entries.add(
                PoEntry(
                    msgid = id,
                    translations = mutableListOf("")
                )
            )
        }

        val before = catalog.entries.size

        catalog.entries.removeAll { entry ->
            !entry.isHeader &&
                    entryKey(entry) !in normalizedIds &&
                    entry.translation.isBlank()
        }

        val removed = before - catalog.entries.size

        return UpdateResult(
            added = newIds.size,
            removedEmpty = removed
        )
    }

    fun addMissingEntriesFromTemplate(
        targetCatalog: PoCatalog,
        templateCatalog: PoCatalog
    ): UpdateResult {
        val existingIds = targetCatalog.translatableEntries
            .mapNotNull(::entryKey)
            .toSet()

        val templateEntries = templateCatalog.translatableEntries
            .filter { entryKey(it) != null }

        var added = 0

        for (templateEntry in templateEntries) {
            val id = entryKey(templateEntry) ?: continue

            if (id !in existingIds) {
                targetCatalog.entries.add(newTranslationEntry(templateEntry))
                added++
            }
        }

        return UpdateResult(added = added)
    }

    fun updateTranslationFromTemplate(
        translationCatalog: PoCatalog,
        templateCatalog: PoCatalog
    ): UpdateResult {
        val templateById = templateCatalog.translatableEntries
            .mapNotNull { entry -> entryKey(entry)?.let { it to entry } }
            .toMap()

        val existingById = translationCatalog.translatableEntries
            .mapNotNull { entry -> entryKey(entry)?.let { it to entry } }
            .toMap()

        var added = 0
        var markedRemoved = 0

        for ((id, templateEntry) in templateById) {
            val existing = existingById[id]

            if (existing == null) {
                translationCatalog.entries.add(newTranslationEntry(templateEntry))
                added++
            } else {
                existing.removedFromTemplate = false

                if (!templateEntry.context.isNullOrBlank()) {
                    existing.msgid = templateEntry.msgid
                }

                existing.msgidPlural = templateEntry.msgidPlural
                existing.extractedComments = templateEntry.extractedComments.toMutableList()
                existing.references = templateEntry.references.toMutableList()
                existing.flags.addAll(templateEntry.flags.filterNot { it == "fuzzy" })
            }
        }

        val before = translationCatalog.entries.size

        translationCatalog.entries.removeAll { entry ->
            if (entry.isHeader) return@removeAll false

            val id = entryKey(entry)

            if (entry.isCompletelyEmpty()) {
                return@removeAll true
            }

            if (id == null) {
                return@removeAll false
            }

            if (id !in templateById.keys) {
                if (entry.translation.isBlank()) {
                    return@removeAll true
                }

                entry.removedFromTemplate = true
                markedRemoved++
            }

            false
        }

        val removedEmpty = before - translationCatalog.entries.size

        return UpdateResult(
            added = added,
            removedEmpty = removedEmpty,
            markedRemoved = markedRemoved
        )
    }

    private fun entryKey(entry: PoEntry): String? {
        return entry.context?.takeIf { it.isNotBlank() }
            ?: entry.msgid.takeIf { it.isNotBlank() }
    }

    private fun newTranslationEntry(templateEntry: PoEntry): PoEntry {
        val translationCount = if (templateEntry.msgidPlural != null) {
            templateEntry.translations.size.coerceAtLeast(2)
        } else {
            1
        }

        return PoEntry(
            extractedComments = templateEntry.extractedComments.toMutableList(),
            references = templateEntry.references.toMutableList(),
            flags = templateEntry.flags.filterNot { it == "fuzzy" }.toMutableSet(),
            context = templateEntry.context,
            msgid = templateEntry.msgid,
            msgidPlural = templateEntry.msgidPlural,
            translations = MutableList(translationCount) { "" }
        )
    }
}
