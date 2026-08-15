package com.monliev.brainwave.audio

import com.monliev.brainwave.audio.preset.Preset
import com.monliev.brainwave.audio.preset.PresetLoader
import com.monliev.brainwave.audio.preset.PresetValidator
import com.monliev.brainwave.audio.preset.Step
import com.monliev.brainwave.audio.preset.BackgroundNoise
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/**
 * PresetValidationTest verifies that the default 20 preset JSON assets are fully valid
 * and that invalid configurations are correctly rejected by the validator.
 */
class PresetValidationTest {

    @Test
    fun testValidateAll20DefaultPresets() {
        val presetsDir = File("/Volumes/Backup/Antigravity/Binaural Beats/assets/presets")
        assertTrue("Presets directory does not exist!", presetsDir.exists())
        
        val files = presetsDir.listFiles { _, name -> name.endsWith(".json") }
        assertNotNull("Presets folder is empty or not readable", files)
        assertTrue("There should be at least 20 preset files, found ${files!!.size}", files!!.size >= 20)

        for (file in files) {
            val jsonContent = file.readText()
            try {
                val preset = PresetLoader.loadPreset(jsonContent)
                assertNotNull("Preset loaded from ${file.name} is null", preset)
            } catch (e: Exception) {
                fail("Failed to load/validate preset from ${file.name}: ${e.message}")
            }
        }
    }

    @Test
    fun testInvalidPresetScenarios() {
        // Scenario 1: Empty steps
        assertFailsValidation(
            Preset("id_1", "STUDY", "Title", "Desc", 200.0, steps = emptyList())
        )

        // Scenario 2: Step with non-positive duration
        assertFailsValidation(
            Preset("id_2", "STUDY", "Title", "Desc", 200.0, steps = listOf(
                Step("stable", beat_frequency_hz = 10.0, duration_seconds = 0)
            ))
        )

        // Scenario 3: Fade sum exceeds duration
        assertFailsValidation(
            Preset("id_3", "STUDY", "Title", "Desc", 200.0, steps = listOf(
                Step("stable", beat_frequency_hz = 10.0, duration_seconds = 10, fade_in_seconds = 6, fade_out_seconds = 5)
            ))
        )

        // Scenario 4: First step is transition but has no start frequency
        assertFailsValidation(
            Preset("id_4", "STUDY", "Title", "Desc", 200.0, steps = listOf(
                Step("transition", end_beat_frequency_hz = 5.0, duration_seconds = 10)
            ))
        )

        // Scenario 5: Beat frequency out of range (too low)
        assertFailsValidation(
            Preset("id_5", "STUDY", "Title", "Desc", 200.0, steps = listOf(
                Step("stable", beat_frequency_hz = 0.2, duration_seconds = 10)
            ))
        )

        // Scenario 6: Beat frequency out of range (too high)
        assertFailsValidation(
            Preset("id_6", "STUDY", "Title", "Desc", 200.0, steps = listOf(
                Step("stable", beat_frequency_hz = 120.0, duration_seconds = 10)
            ))
        )

        // Scenario 7: Background noise amplitude out of bounds
        assertFailsValidation(
            Preset("id_7", "STUDY", "Title", "Desc", 200.0, 
                steps = listOf(Step("stable", beat_frequency_hz = 10.0, duration_seconds = 10)),
                background_noise = BackgroundNoise("pink", 1.5f)
            )
        )
    }

    private fun assertFailsValidation(preset: Preset) {
        try {
            PresetValidator.validate(preset)
            fail("Preset validation succeeded but should have failed for: $preset")
        } catch (e: IllegalArgumentException) {
            // Expected to fail
        }
    }
}
