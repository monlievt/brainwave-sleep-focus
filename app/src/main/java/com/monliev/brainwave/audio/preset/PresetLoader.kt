package com.monliev.brainwave.audio.preset

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

/**
 * PresetLoader parses and validates Preset JSON documents.
 */
object PresetLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    /**
     * Parses a Preset JSON string and validates it.
     * Throws [Exception] or [IllegalArgumentException] if parsing or validation fails.
     */
    fun loadPreset(jsonContent: String): Preset {
        val preset = json.decodeFromString<Preset>(jsonContent)
        PresetValidator.validate(preset)
        return preset
    }

    /**
     * Serializes a Preset object back into JSON string.
     */
    fun encodePreset(preset: Preset): String {
        return json.encodeToString(preset)
    }
}
