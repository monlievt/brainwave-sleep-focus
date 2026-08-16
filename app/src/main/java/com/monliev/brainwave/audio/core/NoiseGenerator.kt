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

    // Nature sounds state variables
    private var oceanPhase = 0.0
    private var rainDropletFilter = 0.0
    private var riverPhase1 = 0.0
    private var riverPhase2 = 0.0
    private var riverFilterState = 0.0

    private val random = Random(42) // Seeded for determinism in testing

    /**
     * Generates the next sample for the specified [type] of noise: "white", "pink", "brown", "rain", "river", "ocean".
     * Returns a value normalized roughly within [-1.0, 1.0].
     */
    fun nextSample(type: String): Double {
        val white = random.nextDouble(-1.0, 1.0)
        return when (type.lowercase()) {
            "white" -> white
            "pink" -> generatePinkNoiseSample(white)
            "brown" -> generateBrownNoiseSample(white)
            "rain" -> generateRainSample(white)
            "river" -> generateRiverSample(white)
            "ocean" -> generateOceanSample(white)
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

    private fun generateOceanSample(white: Double): Double {
        val pink = generatePinkNoiseSample(white)
        val brown = generateBrownNoiseSample(white)
        oceanPhase = (oceanPhase + (2.0 * Math.PI * 0.08) / 44100.0) % (2.0 * Math.PI)
        val waveMod = 0.5 + 0.45 * Math.sin(oceanPhase)
        return (pink * 0.65 + brown * 0.35) * waveMod * 1.4
    }

    private fun generateRainSample(white: Double): Double {
        val pink = generatePinkNoiseSample(white)
        val steadyRain = pink * 0.85 + white * 0.15
        var dropTrigger = 0.0
        if (random.nextDouble() > 0.9995) {
            dropTrigger = random.nextDouble(-0.8, 0.8)
        }
        rainDropletFilter = 0.85 * rainDropletFilter + dropTrigger * 0.15
        return (steadyRain + rainDropletFilter * 0.6) * 1.2
    }

    private fun generateRiverSample(white: Double): Double {
        val pink = generatePinkNoiseSample(white)
        riverPhase1 = (riverPhase1 + (2.0 * Math.PI * 1.5) / 44100.0) % (2.0 * Math.PI)
        riverPhase2 = (riverPhase2 + (2.0 * Math.PI * 3.5) / 44100.0) % (2.0 * Math.PI)
        val mod1 = 0.6 + 0.4 * Math.sin(riverPhase1)
        val mod2 = 0.5 + 0.5 * Math.sin(riverPhase2)
        val riverBase = (pink * 0.7 * mod1) + (white * 0.3 * mod2)
        riverFilterState = 0.97 * riverFilterState + 0.03 * riverBase
        return (riverBase - riverFilterState) * 1.5
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
        oceanPhase = 0.0
        rainDropletFilter = 0.0
        riverPhase1 = 0.0
        riverPhase2 = 0.0
        riverFilterState = 0.0
    }
}
