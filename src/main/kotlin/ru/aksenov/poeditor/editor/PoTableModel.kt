package ru.aksenov.poeditor.editor

import ru.aksenov.poeditor.parser.PoCatalog
import ru.aksenov.poeditor.parser.PoEntry
import javax.swing.table.AbstractTableModel

class PoTableModel(
    val catalog: PoCatalog,
    private val onModifiedStateChanged: () -> Unit
) : AbstractTableModel() {

    private val visibleEntries: List<PoEntry>
        get() = catalog.entries.filterNot { it.isHeader }

    var isModified: Boolean = false
        private set

    override fun getRowCount(): Int = visibleEntries.size

    override fun getColumnCount(): Int = 3

    override fun getColumnName(column: Int): String {
        return when (column) {
            COL_ID -> "Identifier"
            COL_SOURCE -> "Source text"
            COL_TRANSLATION -> "Translation"
            else -> ""
        }
    }

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
        val entry = visibleEntries[rowIndex]

        return when (columnIndex) {
            COL_ID -> entry.context.orEmpty()
            COL_SOURCE -> entry.msgid
            COL_TRANSLATION -> entry.translation
            else -> ""
        }
    }

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
        return true
    }

    override fun setValueAt(value: Any?, rowIndex: Int, columnIndex: Int) {
        val entry = visibleEntries[rowIndex]
        val text = value?.toString().orEmpty()

        when (columnIndex) {
            COL_ID -> entry.context = text.ifBlank { null }
            COL_SOURCE -> entry.msgid = text
            COL_TRANSLATION -> entry.setTranslation(text)
        }

        markModified()
        fireTableRowsUpdated(rowIndex, rowIndex)
    }

    fun getEntry(rowIndex: Int): PoEntry? {
        if (rowIndex !in visibleEntries.indices) return null
        return visibleEntries[rowIndex]
    }

    fun setEntryValues(
        rowIndex: Int,
        id: String,
        source: String,
        translation: String
    ) {
        val entry = getEntry(rowIndex) ?: return

        entry.context = id.ifBlank { null }
        entry.msgid = source
        entry.setTranslation(translation)

        markModified()
        fireTableRowsUpdated(rowIndex, rowIndex)
    }

    fun addEntryAfter(rowIndex: Int): Int {
        val newEntry = PoEntry(
            context = "new.identifier",
            msgid = "",
            translations = mutableListOf("")
        )

        val visible = visibleEntries
        val insertIndexInCatalog = if (rowIndex in visible.indices) {
            val selectedEntry = visible[rowIndex]
            catalog.entries.indexOf(selectedEntry) + 1
        } else {
            catalog.entries.size
        }

        catalog.entries.add(insertIndexInCatalog, newEntry)

        markModified()
        fireTableDataChanged()

        return visibleEntries.indexOf(newEntry)
    }

    fun removeRows(modelRows: Int): Int {
        return removeRows(listOf(modelRows))
    }

    fun removeRows(modelRows: List<Int>): Int {
        val entriesToRemove = modelRows
            .distinct()
            .mapNotNull { getEntry(it) }
            .toSet()

        if (entriesToRemove.isEmpty()) return 0

        catalog.entries.removeAll(entriesToRemove)

        markModified()
        fireTableDataChanged()

        return entriesToRemove.size
    }

    fun translatedPercent(): Int {
        val entries = visibleEntries
        if (entries.isEmpty()) return 100

        val translated = entries.count { it.translation.isNotBlank() }
        return translated * 100 / entries.size
    }

    fun translatedCount(): Int {
        return visibleEntries.count { it.translation.isNotBlank() }
    }

    fun totalCount(): Int {
        return visibleEntries.size
    }

    fun markModified() {
        isModified = true
        onModifiedStateChanged()
    }

    fun markSaved() {
        isModified = false
        onModifiedStateChanged()
    }

    fun reloadCatalog(newCatalog: PoCatalog) {
        catalog.entries.clear()
        catalog.entries.addAll(newCatalog.entries)
        isModified = false
        fireTableDataChanged()
        onModifiedStateChanged()
    }

    companion object {
        const val COL_ID = 0
        const val COL_SOURCE = 1
        const val COL_TRANSLATION = 2
    }
}