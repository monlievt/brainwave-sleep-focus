package com.monliev.brainwave.audio.core

import kotlin.random.Random

/**
 * NoiseGenerator synthesizes White, Pink, and Brown noise in real-time.
 * It maintains filter state to ensure spectral continuity between blocks.
 */
class NoiseGenerator {
    // Pink noise filter state variables
    private var b0 = 0.0
    private var b1 = 0.0
    private var b2 = 0.0
    private var b3 = 0.0
    private var b4 = 0.0
    private var b5 = 0.0
    private var b6 = 0.0

    // Brown noise filter state variables
    private var lastBrownOutput = 0.0

    private val random = Random(42) // Seeded for determinism in testing

    /**
     * Generates the next sample for the specified [type] of noise: "white", "pink", or "brown".
     * Returns a value normalized roughly within [-1.0, 1.0].
     */
    fun nextSample(type: String): Double {
        val white = random.nextDouble(-1.0, 1.0)
        return when (type.lowercase()) {
            "white" -> white
            "pink" -> generatePinkNoiseSample(white)
            "brown" -> generateBrownNoiseSample(white)
            else -> 0.0
        }
    }

    private fun generatePinkNoiseSample(white: Double): Double {
        b0 = 0.99886 * b0 + white * 0.0555179
        b1 = 0.99332 * b1 + white * 0.0750759
        b2 = 0.96900 * b2 + white * 0.1538520
        b3 = 0.86650 * b3 + white * 0.3104856
        b4 = 0.55000 * b4 + white * 0.5329522
        b5 = -0.7616 * b5 - white * 0.0168980
        
        // INTENTIONAL: b6 below is the value from the PREVIOUS call (state variable)
        // Do NOT reorder these two lines — this is the Paul Kellett filter design
        val pink = b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362
        b6 = white * 0.115926 // updated AFTER use; takes effect next call
        
        return pink * 0.11 // Normalization gain
    }

    private fun generateBrownNoiseSample(white: Double): Double {
        lastBrownOutput = (lastBrownOutput + (0.02 * white)) / 1.02
        return lastBrownOutput * 3.5 // Normalization gain
    }

    /**
     * Resets the filter states.
     */
    fun reset() {
        b0 = 0.0
        b1 = 0.0
        b2 = 0.0
        b3 = 0.0
        b4 = 0.0
        b5 = 0.0
        b6 = 0.0
        lastBrownOutput = 0.0
    }
}
