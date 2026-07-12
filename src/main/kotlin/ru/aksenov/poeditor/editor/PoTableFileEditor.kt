package ru.aksenov.poeditor.editor

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.event.DocumentEvent as IdeaDocumentEvent
import com.intellij.openapi.editor.event.DocumentListener as IdeaDocumentListener
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.JBColor
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.table.JBTable
import ru.aksenov.poeditor.actions.AddEntryAction
import ru.aksenov.poeditor.actions.CheckPoSyntaxAction
import ru.aksenov.poeditor.actions.ChooseTemplateFileAction
import ru.aksenov.poeditor.actions.CompilePoAction
import ru.aksenov.poeditor.actions.DeleteEntriesAction
import ru.aksenov.poeditor.actions.SavePoAction
import ru.aksenov.poeditor.actions.UpdateTemplateAction
import ru.aksenov.poeditor.actions.UpdateTranslationsAction
import ru.aksenov.poeditor.parser.PoParser
import ru.aksenov.poeditor.parser.PoWriter
import ru.aksenov.poeditor.scanner.TemplatePoResolver
import ru.aksenov.poeditor.scanner.TranslationIdScanner
import ru.aksenov.poeditor.scanner.TranslationTemplateUpdater
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import java.io.File
import java.nio.file.Files
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.JTable
import javax.swing.JTextArea
import javax.swing.SwingUtilities
import javax.swing.event.DocumentEvent as SwingDocumentEvent
import javax.swing.event.DocumentListener as SwingDocumentListener
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.text.DefaultHighlighter

class PoTableFileEditor(
    private val project: Project,
    private val file: VirtualFile
) : UserDataHolderBase(), FileEditor {

    private val propertyChangeSupport = PropertyChangeSupport(this)

    private val panel = JPanel(BorderLayout())
    private val idArea = JBTextArea()
    private val sourceArea = JBTextArea()
    private val translationArea = JBTextArea()
    private val progressLabel = JBLabel()

    private val whitespacePainter = DefaultHighlighter.DefaultHighlightPainter(
        JBColor(Color(255, 230, 150), Color(120, 90, 20))
    )

    private val tableModel: PoTableModel
    private val table: JBTable

    private var selectedModelRow: Int = -1
    private var updatingDetails = false
    private var updatingDocumentFromTable = false
    private var updatingTableFromDocument = false
    private var templateFile: VirtualFile? = null

    init {
        val text = String(file.contentsToByteArray(), file.charset)
        val catalog = PoParser.parse(text)

        tableModel = PoTableModel(catalog) {
            propertyChangeSupport.firePropertyChange(PROP_MODIFIED, false, true)
            updateProgress()
        }

        table = JBTable(tableModel).apply {
            fillsViewportHeight = true
            rowHeight = 28
            autoResizeMode = JTable.AUTO_RESIZE_ALL_COLUMNS
            autoCreateRowSorter = true
        }

        configureColumns()
        installStatusRenderer()
        installTableSelectionListener()
        installDocumentSync()

        val toolbar = createToolbar()

        val splitPane = JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            ScrollPaneFactory.createScrollPane(table),
            createDetailsPanel()
        ).apply {
            resizeWeight = 0.58
            isOneTouchExpandable = true
        }

        panel.add(toolbar.component, BorderLayout.NORTH)
        panel.add(splitPane, BorderLayout.CENTER)
        panel.add(progressLabel, BorderLayout.SOUTH)

        templateFile = TemplatePoResolver.findTemplateFile(file)

        updateProgress()

        if (table.rowCount > 0) {
            table.setRowSelectionInterval(0, 0)
        }
    }

    private fun createToolbar() =
        ActionManager.getInstance().createActionToolbar(
            "PoEditorToolbar",
            DefaultActionGroup().apply {
                add(SavePoAction(this@PoTableFileEditor))
                add(CheckPoSyntaxAction(this@PoTableFileEditor))
                add(AddEntryAction(this@PoTableFileEditor))
                add(DeleteEntriesAction(this@PoTableFileEditor))

                if (isTemplateFile()) {
                    add(UpdateTemplateAction(this@PoTableFileEditor))
                } else {
                    add(CompilePoAction(this@PoTableFileEditor))
                    add(ChooseTemplateFileAction(this@PoTableFileEditor))
                    add(UpdateTranslationsAction(this@PoTableFileEditor))
                }
            },
            true
        )

    private fun configureColumns() {
        val columnModel = table.columnModel

        columnModel.getColumn(PoTableModel.COL_ID).preferredWidth = 180
        columnModel.getColumn(PoTableModel.COL_SOURCE).preferredWidth = 420
        columnModel.getColumn(PoTableModel.COL_TRANSLATION).preferredWidth = 420
    }

    private fun createDetailsPanel(): JComponent {
        idArea.lineWrap = true
        idArea.wrapStyleWord = false

        sourceArea.lineWrap = true
        sourceArea.wrapStyleWord = false

        translationArea.lineWrap = true
        translationArea.wrapStyleWord = false

        val listener = object : SwingDocumentListener {
            override fun insertUpdate(e: SwingDocumentEvent?) = saveDetailsToModel()
            override fun removeUpdate(e: SwingDocumentEvent?) = saveDetailsToModel()
            override fun changedUpdate(e: SwingDocumentEvent?) = saveDetailsToModel()
        }

        idArea.document.addDocumentListener(listener)
        sourceArea.document.addDocumentListener(listener)
        translationArea.document.addDocumentListener(listener)

        val idPanel = wrapWithTitle("Identifier", idArea)
        val sourcePanel = wrapWithTitle("Source text", sourceArea)
        val translationPanel = wrapWithTitle("Translation", translationArea)

        val sourceTranslationSplit = JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            sourcePanel,
            translationPanel
        ).apply {
            resizeWeight = 0.5
            isOneTouchExpandable = true
        }

        return JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            idPanel,
            sourceTranslationSplit
        ).apply {
            resizeWeight = 0.18
            isOneTouchExpandable = true
        }
    }

    private fun wrapWithTitle(title: String, component: JComponent): JComponent {
        val wrapper = JPanel(BorderLayout())
        wrapper.border = IdeBorderFactory.createTitledBorder(title, false)
        wrapper.add(ScrollPaneFactory.createScrollPane(component), BorderLayout.CENTER)
        return wrapper
    }

    private fun installTableSelectionListener() {
        table.selectionModel.addListSelectionListener {
            if (!it.valueIsAdjusting) {
                onTableSelectionChanged()
            }
        }
    }

    private fun onTableSelectionChanged() {
        val viewRow = table.selectedRow
        if (viewRow < 0) return

        val modelRow = table.convertRowIndexToModel(viewRow)
        val entry = tableModel.getEntry(modelRow) ?: return

        selectedModelRow = modelRow
        updatingDetails = true

        idArea.text = entry.context.orEmpty()
        sourceArea.text = entry.msgid
        translationArea.text = entry.translation

        updatingDetails = false

        highlightSpecialWhitespace(idArea)
        highlightSpecialWhitespace(sourceArea)
        highlightSpecialWhitespace(translationArea)
    }

    private fun saveDetailsToModel() {
        if (updatingDetails) return
        if (updatingTableFromDocument) return
        if (selectedModelRow < 0) return

        tableModel.setEntryValues(
            selectedModelRow,
            idArea.text,
            sourceArea.text,
            translationArea.text
        )

        highlightSpecialWhitespace(idArea)
        highlightSpecialWhitespace(sourceArea)
        highlightSpecialWhitespace(translationArea)
    }

    private fun highlightSpecialWhitespace(area: JTextArea) {
        val highlighter = area.highlighter
        highlighter.removeAllHighlights()

        val text = area.text

        try {
            Regex(""" {2,}""").findAll(text).forEach { match ->
                highlighter.addHighlight(
                    match.range.first,
                    match.range.last + 1,
                    whitespacePainter
                )
            }

            text.forEachIndexed { index, ch ->
                if (ch == '\t' || ch == '\n' || ch == '\r') {
                    highlighter.addHighlight(
                        index,
                        index + 1,
                        whitespacePainter
                    )
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun installStatusRenderer() {
        table.setDefaultRenderer(
            Any::class.java,
            object : DefaultTableCellRenderer() {
                override fun getTableCellRendererComponent(
                    table: JTable,
                    value: Any?,
                    isSelected: Boolean,
                    hasFocus: Boolean,
                    row: Int,
                    column: Int
                ): Component {
                    val component = super.getTableCellRendererComponent(
                        table,
                        value,
                        isSelected,
                        hasFocus,
                        row,
                        column
                    )

                    if (!isSelected) {
                        val modelRow = table.convertRowIndexToModel(row)
                        val entry = tableModel.getEntry(modelRow)

                        component.background = when {
                            entry == null -> table.background
                            entry.removedFromTemplate -> JBColor(Color(220, 220, 220), Color(70, 70, 70))
                            entry.isFuzzy -> JBColor(Color(255, 245, 200), Color(95, 75, 25))
                            entry.translation.isBlank() -> JBColor(Color(255, 225, 225), Color(95, 45, 45))
                            entry.translation == entry.msgid && entry.translation.isNotBlank() -> JBColor(Color(225, 235, 255), Color(45, 55, 95))
                            else -> JBColor(Color(225, 255, 225), Color(45, 85, 45))
                        }
                    }

                    return component
                }
            }
        )
    }

    private fun installDocumentSync() {
        val document = FileDocumentManager.getInstance().getDocument(file) ?: return

        document.addDocumentListener(
            object : IdeaDocumentListener {
                override fun documentChanged(event: IdeaDocumentEvent) {
                    if (updatingDocumentFromTable) return

                    ApplicationManager.getApplication().invokeLater {
                        if (updatingDocumentFromTable) return@invokeLater

                        updatingTableFromDocument = true

                        try {
                            val newCatalog = PoParser.parse(document.text)
                            tableModel.reloadCatalog(newCatalog)
                            selectedModelRow = -1
                            updateProgress()
                            configureColumns()

                            if (table.rowCount > 0) {
                                table.setRowSelectionInterval(0, 0)
                            }
                        } finally {
                            updatingTableFromDocument = false
                        }
                    }
                }
            },
            this
        )
    }

    fun savePo() {
        savePo(showNotification = true)
    }

    private fun savePo(showNotification: Boolean) {
        if (updatingTableFromDocument) return

        tableModel.catalog.ensureUtf8Header(detectLanguageFromFileName())

        val resultText = PoWriter.write(tableModel.catalog)
        val fileDocumentManager = FileDocumentManager.getInstance()
        val document = fileDocumentManager.getDocument(file)

        updatingDocumentFromTable = true

        try {
            WriteCommandAction.runWriteCommandAction(project) {
                if (document != null) {
                    document.setText(resultText)
                } else {
                    file.setBinaryContent(resultText.toByteArray(file.charset))
                }
            }

            if (document != null) {
                fileDocumentManager.saveDocument(document)
            }

            tableModel.markSaved()
            updateProgress()
        } finally {
            updatingDocumentFromTable = false
        }

        if (showNotification) {
            notifyUser("PO saved", file.path, NotificationType.INFORMATION)
        }
    }

    fun checkSyntax() {
        val tempCatalog = tableModel.catalog.deepCopy()
        tempCatalog.ensureUtf8Header(detectLanguageFromFileName())

        val tempFile = Files.createTempFile("po-editor-check-", ".po").toFile()
        tempFile.writeText(PoWriter.write(tempCatalog), Charsets.UTF_8)

        val nullOutput = if (System.getProperty("os.name").lowercase().contains("win")) {
            "NUL"
        } else {
            "/dev/null"
        }

        val commandLine = GeneralCommandLine().apply {
            exePath = msgfmtExecutable()
            addParameters("--check", "--check-format", tempFile.absolutePath, "-o", nullOutput)
            withWorkDirectory(file.parent.path)
        }

        runProcess(
            commandLine = commandLine,
            successTitle = "PO syntax is correct",
            errorTitle = "PO syntax error",
            successContent = "OK",
            afterFinish = {
                tempFile.delete()
            }
        )
    }

    fun compilePo() {
        if (isTemplateFile()) {
            notifyUser(
                title = "Cannot compile",
                content = ".pot template files cannot be compiled",
                type = NotificationType.WARNING
            )
            return
        }

        savePo()

        val poPath = file.path
        val moPath = poPath.removeSuffix(".po") + ".mo"

        val commandLine = GeneralCommandLine().apply {
            exePath = msgfmtExecutable()
            addParameters(poPath, "-o", moPath)
            withWorkDirectory(file.parent.path)
        }

        runProcess(
            commandLine = commandLine,
            successTitle = "PO compiled",
            errorTitle = "msgfmt failed",
            successContent = moPath
        )
    }

    fun chooseTemplateFile() {
        if (isTemplateFile()) return

        val descriptor = FileChooserDescriptor(
            true,
            false,
            false,
            false,
            false,
            false
        ).apply {
            title = "Choose POT Template File"
            description = "Choose .pot template file. Existing non-empty translations will not be overwritten."
        }

        val selected = FileChooser.chooseFile(descriptor, project, file.parent) ?: return

        if (selected.extension != "pot") {
            notifyUser(
                title = "Wrong template file",
                content = "Choose .pot file",
                type = NotificationType.WARNING
            )
            return
        }

        templateFile = selected

        val templateCatalog = PoParser.parse(String(selected.contentsToByteArray(), selected.charset))
        val result = TranslationTemplateUpdater.addMissingEntriesFromTemplate(
            targetCatalog = tableModel.catalog,
            templateCatalog = templateCatalog
        )

        if (result.added > 0) {
            tableModel.markModified()
            tableModel.fireTableDataChanged()
            configureColumns()
            updateProgress()
        }

        notifyUser(
            title = "Template attached",
            content = "Added ${result.added} new entries from ${selected.name}",
            type = NotificationType.INFORMATION
        )
    }

    fun updateTranslationsFromTemplate() {
        if (isTemplateFile()) return

        val template = templateFile ?: TemplatePoResolver.findTemplateFile(file)

        if (template == null) {
            notifyUser(
                title = "Template not found",
                content = "Choose .pot template first",
                type = NotificationType.WARNING
            )
            return
        }

        templateFile = template

        val templateCatalog = PoParser.parse(String(template.contentsToByteArray(), template.charset))
        val result = TranslationTemplateUpdater.updateTranslationFromTemplate(
            translationCatalog = tableModel.catalog,
            templateCatalog = templateCatalog
        )

        if (result.added > 0 || result.removedEmpty > 0 || result.markedRemoved > 0) {
            tableModel.markModified()
            tableModel.fireTableDataChanged()
            configureColumns()
            updateProgress()
        }

        notifyUser(
            title = "Translations updated",
            content = "Added ${result.added}, removed empty ${result.removedEmpty}, marked removed ${result.markedRemoved}",
            type = NotificationType.INFORMATION
        )
    }

    fun updateTemplateFromProjectSources() {
        if (!isTemplateFile()) return

        val ids = scanProjectForTranslationIds()

        if (ids.isEmpty()) {
            notifyUser(
                title = "No translation identifiers found",
                content = "Scanner did not find _(...), t(...), i18n(...), translate(...), gettext(...), \$t(...), tr(...) calls",
                type = NotificationType.WARNING
            )
            return
        }

        val result = TranslationTemplateUpdater.updateTemplateWithIds(
            catalog = tableModel.catalog,
            ids = ids
        )

        if (result.added > 0 || result.removedEmpty > 0) {
            tableModel.markModified()
            tableModel.fireTableDataChanged()
            configureColumns()
            updateProgress()
            savePo(showNotification = false)
        }

        notifyUser(
            title = "Template updated",
            content = "Found ${ids.size} identifiers, added ${result.added}, removed empty ${result.removedEmpty}",
            type = NotificationType.INFORMATION
        )
    }

    fun addRow() {
        val selectedViewRow = table.selectedRow
        val selectedModelRow = if (selectedViewRow >= 0) {
            table.convertRowIndexToModel(selectedViewRow)
        } else {
            tableModel.rowCount - 1
        }

        val newModelRow = tableModel.addEntryAfter(selectedModelRow)

        SwingUtilities.invokeLater {
            val newViewRow = table.convertRowIndexToView(newModelRow)

            if (newViewRow >= 0) {
                table.setRowSelectionInterval(newViewRow, newViewRow)
                table.scrollRectToVisible(table.getCellRect(newViewRow, 0, true))
            }
        }
    }

    fun deleteSelectedRows() {
        val selectedViewRows = table.selectedRows

        if (selectedViewRows.isEmpty()) {
            notifyUser(
                title = "Nothing selected",
                content = "Select rows to delete",
                type = NotificationType.WARNING
            )
            return
        }

        val answer = Messages.showYesNoDialog(
            project,
            "Delete ${selectedViewRows.size} selected row(s)? This action cannot be undone before saving.",
            "Delete Translation Rows",
            Messages.getQuestionIcon()
        )

        if (answer != Messages.YES) return

        val selectedModelRows = selectedViewRows
            .map { table.convertRowIndexToModel(it) }

        val deleted = tableModel.removeRows(selectedModelRows)

        selectedModelRow = -1
        clearDetails()
        updateProgress()

        notifyUser(
            title = "Rows deleted",
            content = "Deleted $deleted row(s)",
            type = NotificationType.INFORMATION
        )
    }

    private fun clearDetails() {
        updatingDetails = true
        idArea.text = ""
        sourceArea.text = ""
        translationArea.text = ""
        updatingDetails = false
    }

    private fun scanProjectForTranslationIds(): Set<String> {
        val result = linkedSetOf<String>()
        val allowedExtensions = setOf(
            "kt", "java", "js", "jsx", "ts", "tsx",
            "vue", "svelte", "astro", "jinja", "jinja2", "j2", "twig",
            "dart", "swift",
            "py", "php", "rb", "go", "cs", "c", "cpp", "h", "hpp",
            "html", "xml", "properties"
        )

        val fileIndex = ProjectFileIndex.getInstance(project)
        val fileDocumentManager = FileDocumentManager.getInstance()

        fileIndex.iterateContent { virtualFile ->
            if (virtualFile.isDirectory) return@iterateContent true

            val extension = virtualFile.extension?.lowercase()
            if (extension !in allowedExtensions) return@iterateContent true

            if (virtualFile.length > MAX_SCAN_FILE_SIZE_BYTES) return@iterateContent true

            try {
                val text = fileDocumentManager.getCachedDocument(virtualFile)?.text
                    ?: String(virtualFile.contentsToByteArray(), virtualFile.charset)
                result.addAll(TranslationIdScanner.scanText(text))
            } catch (_: Exception) {
            }

            true
        }

        return result
    }

    private fun runProcess(
        commandLine: GeneralCommandLine,
        successTitle: String,
        errorTitle: String,
        successContent: String,
        afterFinish: (() -> Unit)? = null
    ) {
        val output = StringBuilder()

        try {
            val handler = OSProcessHandler(commandLine)

            handler.addProcessListener(object : ProcessListener {
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    output.append(event.text)
                }

                override fun processTerminated(event: ProcessEvent) {
                    try {
                        val text = output.toString().trim()

                        if (event.exitCode == 0) {
                            notifyUser(
                                title = successTitle,
                                content = text.ifBlank { successContent },
                                type = NotificationType.INFORMATION
                            )
                        } else {
                            notifyUser(
                                title = errorTitle,
                                content = text.ifBlank { "Process finished with exit code ${event.exitCode}" },
                                type = NotificationType.ERROR
                            )
                        }
                    } finally {
                        afterFinish?.invoke()
                    }
                }
            })

            handler.startNotify()
        } catch (e: Exception) {
            afterFinish?.invoke()

            notifyUser(
                title = errorTitle,
                content = e.message ?: "Cannot run process",
                type = NotificationType.ERROR
            )
        }
    }

    private fun msgfmtExecutable(): String {
        val candidates = listOf(
            "/opt/homebrew/bin/msgfmt",
            "/usr/local/bin/msgfmt",
            "msgfmt"
        )

        return candidates.firstOrNull { candidate ->
            candidate == "msgfmt" || File(candidate).canExecute()
        } ?: "msgfmt"
    }

    private fun detectLanguageFromFileName(): String {
        if (isTemplateFile()) return ""

        val name = file.nameWithoutExtension.lowercase()

        if (name.length in 2..5 && name.matches(Regex("""[a-z]{2}(_[a-z]{2})?"""))) {
            return name
        }

        return "ru"
    }

    private fun isTemplateFile(): Boolean {
        return file.extension?.lowercase() == "pot"
    }

    private fun updateProgress() {
        SwingUtilities.invokeLater {
            val prefix = if (isTemplateFile()) {
                "Template entries"
            } else {
                "Translated"
            }

            progressLabel.text =
                "$prefix: ${tableModel.translatedPercent()}% " +
                        "(${tableModel.translatedCount()}/${tableModel.totalCount()})"
        }
    }

    private fun notifyUser(
        title: String,
        content: String,
        type: NotificationType
    ) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("PO Editor")
            .createNotification(title, content, type)
            .notify(project)
    }

    override fun getComponent(): JComponent = panel

    override fun getPreferredFocusedComponent(): JComponent = table

    override fun getName(): String = "PO Table"

    override fun getFile(): VirtualFile = file

    override fun setState(state: FileEditorState) {
    }

    override fun isModified(): Boolean = tableModel.isModified

    override fun isValid(): Boolean = file.isValid

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
        propertyChangeSupport.addPropertyChangeListener(listener)
    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
        propertyChangeSupport.removePropertyChangeListener(listener)
    }

    override fun dispose() {
    }

    companion object {
        private const val PROP_MODIFIED = "modified"
        private const val MAX_SCAN_FILE_SIZE_BYTES = 2_000_000L
    }
}
