package com.monliev.brainwave.audio.preset

/**
 * PresetValidator validates [Preset] objects against the business and technical rules
 * defined in audio-engine-spec.
 */
object PresetValidator {

    /**
     * Validates a [Preset]. Throws [IllegalArgumentException] if any validation fails.
     */
    fun validate(preset: Preset) {
        // Rule 1: carrier_frequency_hz in range 180–300 Hz (warning if outside)
        if (preset.carrier_frequency_hz !in 180.0..300.0) {
            println("WARNING: Preset '${preset.preset_id}' carrier frequency ${preset.carrier_frequency_hz} Hz is outside the recommended range of 180-300 Hz.")
        }

        // Rule 5: steps must not be empty
        require(preset.steps.isNotEmpty()) {
            "Preset '${preset.preset_id}' must contain at least one step."
        }

        // Rule 6: background_noise amplitude in range 0.0 - 1.0
        preset.background_noise?.let { noise ->
            require(noise.amplitude in 0.0f..1.0f) {
                "Preset '${preset.preset_id}' background noise amplitude must be in range [0.0, 1.0]. Was ${noise.amplitude}."
            }
        }

        // Validate each step
        var prevStepEndFreq: Double? = null

        for ((index, step) in preset.steps.withIndex()) {
            // Rule 4: fade_in + fade_out <= duration
            require(step.duration_seconds > 0) {
                "Step $index in preset '${preset.preset_id}' must have a positive duration. Was ${step.duration_seconds}s."
            }
            
            require(step.fade_in_seconds >= 0 && step.fade_out_seconds >= 0) {
                "Step $index in preset '${preset.preset_id}' cannot have negative fade values."
            }

            require(step.fade_in_seconds + step.fade_out_seconds <= step.duration_seconds) {
                "Step $index in preset '${preset.preset_id}' has fade sum (${step.fade_in_seconds + step.fade_out_seconds}s) exceeding duration (${step.duration_seconds}s)."
            }

            when (step.type) {
                "stable" -> {
                    val beatFreq = step.beat_frequency_hz
                    require(beatFreq != null) {
                        "Step $index (stable) in preset '${preset.preset_id}' must define beat_frequency_hz."
                    }
                    // Rule 2: beat_frequency_hz in range 0.5 - 100.0
                    require(beatFreq in 0.5..100.0) {
                        "Step $index beat frequency $beatFreq Hz is outside [0.5, 100.0] Hz range."
                    }
                    prevStepEndFreq = beatFreq
                }
                "transition" -> {
                    val startFreq = step.start_beat_frequency_hz
                    val endFreq = step.end_beat_frequency_hz

                    // Rule 3: start_beat_frequency_hz must be specified on first step
                    val resolvedStartFreq = if (startFreq == null) {
                        require(index > 0) {
                            "Step $index (transition) is the first step in preset '${preset.preset_id}' and must define start_beat_frequency_hz explicitly."
                        }
                        requireNotNull(prevStepEndFreq) {
                            "Step $index (transition) is missing start_beat_frequency_hz and previous step final frequency is undefined."
                        }
                        prevStepEndFreq
                    } else {
                        startFreq
                    }

                    require(endFreq != null) {
                        "Step $index (transition) in preset '${preset.preset_id}' must define end_beat_frequency_hz."
                    }

                    // Rule 2: start & end beat frequencies in range 0.5 - 100.0
                    require(resolvedStartFreq in 0.5..100.0) {
                        "Step $index start beat frequency $resolvedStartFreq Hz is outside [0.5, 100.0] Hz range."
                    }
                    require(endFreq in 0.5..100.0) {
                        "Step $index end beat frequency $endFreq Hz is outside [0.5, 100.0] Hz range."
                    }

                    prevStepEndFreq = endFreq
                }
                else -> {
                    throw IllegalArgumentException("Step $index in preset '${preset.preset_id}' has unknown type '${step.type}'.")
                }
            }
        }
    }
}
