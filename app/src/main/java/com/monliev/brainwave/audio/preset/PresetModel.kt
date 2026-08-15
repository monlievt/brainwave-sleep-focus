package com.monliev.brainwave.audio.preset

import kotlinx.serialization.Serializable

/**
 * Data representation of a Binaural Beat Preset based on the audio-engine-spec schema.
 */
@Serializable
data class Preset(
    val preset_id: String,
    val category: String,
    val title: String,
    val description: String,
    val carrier_frequency_hz: Double,
    val total_duration_seconds: Int = 0,
    val steps: List<Step>,
    val background_noise: BackgroundNoise? = null
)

/**
 * Represents a single sequence step in a Preset.
 * Can be "stable" (constant frequency) or "transition" (linearly interpolated frequency).
 */
@Serializable
data class Step(
    val type: String, // "stable" or "transition"
    val beat_frequency_hz: Double? = null,
    val start_beat_frequency_hz: Double? = null,
    val end_beat_frequency_hz: Double? = null,
    val duration_seconds: Int,
    val fade_in_seconds: Int = 0,
    val fade_out_seconds: Int = 0
)

/**
 * Optional background noise layer parameters.
 */
@Serializable
data class BackgroundNoise(
    val type: String, // "white", "pink", "brown"
    val amplitude: Float
)
