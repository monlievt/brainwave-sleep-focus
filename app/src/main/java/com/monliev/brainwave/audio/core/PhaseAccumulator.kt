package com.monliev.brainwave.audio.core

import kotlin.math.PI
import kotlin.math.sin

/**
 * PhaseAccumulator is a phase-continuous oscillator helper that accumulates phase
 * over time using double-precision arithmetic. This prevents click/pop artifacts
 * when frequencies are changed dynamically.
 */
class PhaseAccumulator(private val sampleRate: Double) {
    private var phase: Double = 0.0

    /**
     * Returns the next sample value for a sine wave at the given [frequency] in Hz.
     * Advances the internal phase state.
     */
    fun nextSample(frequency: Double): Double {
        val sampleValue = sin(phase)
        val phaseStep = 2.0 * PI * frequency / sampleRate
        phase += phaseStep
        
        // Wrap phase inside [0, 2*PI) to avoid precision loss over time
        if (phase >= 2.0 * PI) {
            phase = phase.rem(2.0 * PI)
        }
        
        return sampleValue
    }

    /**
     * Resets the accumulator phase to 0.0.
     */
    fun reset() {
        phase = 0.0
    }

    /**
     * Gets the current phase value.
     */
    fun getPhase(): Double = phase
}
