package com.monliev.brainwave.audio.preset

/**
 * Data class holding parameters computed for a single audio sample.
 */
data class SampleParameters(
    val carrierFrequency: Double,
    val beatFrequency: Double,
    val fadeFactor: Double
)

/**
 * SequenceScheduler processes a [Preset] sequence step-by-step, calculating
 * the precise carrier frequency, beat frequency, and step fade factor for each audio sample dynamically.
 */
class SequenceScheduler(
    val preset: Preset,
    private val sampleRate: Double = 44100.0
) {
    private var currentStepIndex = 0
    private var elapsedSamplesInStep = 0L
    private var lastBeatFrequency: Double = 10.0 // Default fallback

    init {
        // Initialize fallback beat frequency based on the first step
        val firstStep = preset.steps.firstOrNull()
        lastBeatFrequency = firstStep?.beat_frequency_hz 
            ?: firstStep?.start_beat_frequency_hz 
            ?: firstStep?.end_beat_frequency_hz 
            ?: 10.0
    }

    /**
     * Checks if the entire preset sequence has finished.
     */
    fun isFinished(): Boolean {
        return currentStepIndex >= preset.steps.size
    }

    /**
     * Retrieves the frequencies and step-level fade factor for the next sample.
     * If finished, returns carrier = 0.0, beat = 0.0, fade = 0.0.
     */
    fun nextSample(): SampleParameters {
        if (isFinished()) {
            return SampleParameters(0.0, 0.0, 0.0)
        }

        val step = preset.steps[currentStepIndex]
        val stepDurationSamples = (step.duration_seconds * sampleRate).toLong()

        // 1. Calculate Beat Frequency
        val beatFreq = when (step.type) {
            "stable" -> {
                step.beat_frequency_hz ?: lastBeatFrequency
            }
            "transition" -> {
                val start = step.start_beat_frequency_hz ?: lastBeatFrequency
                val end = step.end_beat_frequency_hz ?: start
                val fraction = if (stepDurationSamples > 0) {
                    elapsedSamplesInStep.toDouble() / stepDurationSamples
                } else {
                    1.0
                }
                start + (end - start) * fraction
            }
            else -> lastBeatFrequency
        }

        val carrierFreq = preset.carrier_frequency_hz

        // 2. Calculate Step Fade Factor (linear fade-in / fade-out)
        val fadeInSamples = (step.fade_in_seconds * sampleRate).toLong()
        val fadeOutSamples = (step.fade_out_seconds * sampleRate).toLong()

        val fIn = if (fadeInSamples > 0 && elapsedSamplesInStep < fadeInSamples) {
            elapsedSamplesInStep.toDouble() / fadeInSamples
        } else {
            1.0
        }

        val fOut = if (fadeOutSamples > 0 && elapsedSamplesInStep > (stepDurationSamples - fadeOutSamples)) {
            val remainingSamples = stepDurationSamples - elapsedSamplesInStep
            remainingSamples.toDouble() / fadeOutSamples
        } else {
            1.0
        }

        val fadeFactor = minOf(1.0, minOf(fIn, fOut)).coerceIn(0.0, 1.0)

        // Advance progress
        val currentElapsed = elapsedSamplesInStep
        elapsedSamplesInStep++

        if (elapsedSamplesInStep >= stepDurationSamples) {
            // Record last frequency of this step
            lastBeatFrequency = when (step.type) {
                "stable" -> step.beat_frequency_hz ?: lastBeatFrequency
                "transition" -> step.end_beat_frequency_hz ?: (step.start_beat_frequency_hz ?: lastBeatFrequency)
                else -> lastBeatFrequency
            }
            
            // Move to next step
            currentStepIndex++
            elapsedSamplesInStep = 0
        }

        return SampleParameters(carrierFreq, beatFreq, fadeFactor)
    }

    /**
     * Resets the scheduler state to start from the beginning.
     */
    fun reset() {
        currentStepIndex = 0
        elapsedSamplesInStep = 0L
        val firstStep = preset.steps.firstOrNull()
        lastBeatFrequency = firstStep?.beat_frequency_hz 
            ?: firstStep?.start_beat_frequency_hz 
            ?: firstStep?.end_beat_frequency_hz 
            ?: 10.0
    }

    /**
     * Gets the current real-time beat frequency of the active step.
     */
    fun getCurrentBeatFrequency(): Double {
        if (isFinished()) return 0.0
        val step = preset.steps[currentStepIndex]
        val stepDurationSamples = (step.duration_seconds * sampleRate).toLong()
        return when (step.type) {
            "stable" -> step.beat_frequency_hz ?: lastBeatFrequency
            "transition" -> {
                val start = step.start_beat_frequency_hz ?: lastBeatFrequency
                val end = step.end_beat_frequency_hz ?: start
                val fraction = if (stepDurationSamples > 0) {
                    elapsedSamplesInStep.toDouble() / stepDurationSamples
                } else {
                    1.0
                }
                start + (end - start) * fraction
            }
            else -> lastBeatFrequency
        }
    }

    /**
     * Gets the current step index.
     */
    fun getCurrentStepIndex(): Int = currentStepIndex
}
