package ru.aksenov.poeditor

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class PoFileType private constructor() : LanguageFileType(PoLanguage) {

    override fun getName(): String = "PO File"

    override fun getDescription(): String = "GNU gettext PO translation file"

    override fun getDefaultExtension(): String = "po"

    override fun getIcon(): Icon = PoIcons.PoFile

    companion object {
        @JvmField
        val INSTANCE = PoFileType()
    }
}