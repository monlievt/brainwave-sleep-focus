package com.monliev.brainwave.data

import android.content.Context
import com.monliev.brainwave.audio.preset.Preset
import com.monliev.brainwave.audio.preset.PresetLoader
import java.io.IOException

/**
 * PresetRepository loads and manages preset models from Android assets folder.
 */
class PresetRepository(private val context: Context) {
    private val presets = mutableListOf<Preset>()

    init {
        loadPresetsFromAssets()
    }

    private fun loadPresetsFromAssets() {
        val assetManager = context.assets
        try {
            val files = assetManager.list("presets") ?: emptyArray()
            for (fileName in files) {
                if (fileName.endsWith(".json")) {
                    try {
                        val jsonString = assetManager.open("presets/$fileName").bufferedReader().use { it.readText() }
                        val preset = PresetLoader.loadPreset(jsonString)
                        presets.add(preset)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Gets all loaded presets.
     */
    fun getPresets(): List<Preset> = presets

    /**
     * Finds a preset by its unique ID.
     */
    fun getPresetById(id: String): Preset? = presets.find { it.preset_id == id }

    /**
     * Filters presets by category name (case-insensitive).
     */
    fun getPresetsByCategory(category: String): List<Preset> {
        return presets.filter { it.category.equals(category, ignoreCase = true) }
    }
}
