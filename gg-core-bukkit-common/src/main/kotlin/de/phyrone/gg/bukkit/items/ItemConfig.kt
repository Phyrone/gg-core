package de.phyrone.plugincore.items

import de.phyrone.core.bukkit.placeholder.PlaceholderManager
import java.io.File

class ItemConfig internal constructor(
    private val file: File,
    private val placeholderManager: PlaceholderManager
) {
    private val config: _root_ide_package_.de.phyrone.plugincore.config.ConfigWrapper =
        _root_ide_package_.de.phyrone.plugincore.config.ConfigWrapper()

    fun loadFile() {
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            config.save(file)
        } else {
            config.load(file)
        }
    }

    fun registerItem(item: _root_ide_package_.de.phyrone.plugincore.config.ItemConfigEntry) {
        config.addSpec(item)
    }

    fun getItem(item: _root_ide_package_.de.phyrone.plugincore.config.ItemConfigEntry): DynamicItem {
        TODO()
    }

}