package com.monliev.brainwave.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monliev.brainwave.audio.preset.Preset
import com.monliev.brainwave.audio.preset.Step
import com.monliev.brainwave.audio.preset.BackgroundNoise

@Entity(tableName = "custom_presets")
data class CustomPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val presetId: String,
    val category: String,
    val title: String,
    val description: String,
    val carrierFrequencyHz: Double,
    val beatFrequencyHz: Double,
    val noiseType: String,
    val noiseVolume: Float,
    val timerMinutes: Int,
    val isFavorite: Boolean = false,
    val isCustom: Boolean = true
) {
    fun toPreset(): Preset {
        val duration = if (timerMinutes > 0) timerMinutes * 60 else 1800 // default 30 mins
        return Preset(
            preset_id = presetId,
            category = category,
            title = title,
            description = description,
            carrier_frequency_hz = carrierFrequencyHz,
            total_duration_seconds = duration,
            steps = listOf(
                Step(
                    type = "stable",
                    beat_frequency_hz = beatFrequencyHz,
                    duration_seconds = duration
                )
            ),
            background_noise = if (noiseType != "none") {
                BackgroundNoise(type = noiseType, amplitude = noiseVolume)
            } else {
                null
            }
        )
    }

    companion object {
        fun fromPreset(preset: Preset, isFavorite: Boolean = true, isCustom: Boolean = false): CustomPresetEntity {
            val activeStep = preset.steps.firstOrNull()
            val beatHz = activeStep?.beat_frequency_hz ?: activeStep?.start_beat_frequency_hz ?: 10.0
            val noise = preset.background_noise
            return CustomPresetEntity(
                presetId = preset.preset_id,
                category = preset.category,
                title = preset.title,
                description = preset.description,
                carrierFrequencyHz = preset.carrier_frequency_hz,
                beatFrequencyHz = beatHz,
                noiseType = noise?.type ?: "none",
                noiseVolume = noise?.amplitude ?: 0.15f,
                timerMinutes = if (preset.total_duration_seconds > 0) preset.total_duration_seconds / 60 else 0,
                isFavorite = isFavorite,
                isCustom = isCustom
            )
        }
    }
}
