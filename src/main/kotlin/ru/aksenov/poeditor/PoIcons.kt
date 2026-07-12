package ru.aksenov.poeditor

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object PoIcons {
    @JvmField
    val PoFile: Icon = IconLoader.getIcon("/icons/poFile.svg", PoIcons::class.java)

    @JvmField
    val PotFile: Icon = IconLoader.getIcon("/icons/potFile.svg", PoIcons::class.java)

    @JvmField
    val MoFile: Icon = IconLoader.getIcon("/icons/moFile.svg", PoIcons::class.java)

    @JvmField
    val Save: Icon = IconLoader.getIcon("/icons/save.svg", PoIcons::class.java)

    @JvmField
    val Compile: Icon = IconLoader.getIcon("/icons/compile.svg", PoIcons::class.java)

    @JvmField
    val Check: Icon = IconLoader.getIcon("/icons/check.svg", PoIcons::class.java)

    @JvmField
    val UpdateTemplate: Icon = IconLoader.getIcon("/icons/updateTemplate.svg", PoIcons::class.java)

    @JvmField
    val Source: Icon = IconLoader.getIcon("/icons/source.svg", PoIcons::class.java)

    @JvmField
    val Add: Icon = IconLoader.getIcon("/icons/add.svg", PoIcons::class.java)

    @JvmField
    val Delete: Icon = IconLoader.getIcon("/icons/delete.svg", PoIcons::class.java)

    @JvmField
    val UpdateTranslations: Icon = IconLoader.getIcon("/icons/updateTranslations.svg", PoIcons::class.java)
}
