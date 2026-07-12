package ru.aksenov.poeditor

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.vfs.VirtualFile
import javax.swing.Icon

class MoFileType private constructor() : FileType {

    override fun getName(): String = "MO File"

    override fun getDescription(): String = "Compiled GNU gettext translation file"

    override fun getDefaultExtension(): String = "mo"

    override fun getIcon(): Icon = PoIcons.MoFile

    override fun isBinary(): Boolean = true

    override fun isReadOnly(): Boolean = true

    override fun getCharset(file: VirtualFile, content: ByteArray): String? = null

    companion object {
        @JvmField
        val INSTANCE = MoFileType()
    }
}