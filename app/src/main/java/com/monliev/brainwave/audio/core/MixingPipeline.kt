package com.monliev.brainwave.audio.core

import com.monliev.brainwave.audio.preset.SequenceScheduler

/**
 * MixingPipeline orchestrates the mixing of binaural beat tone and background noise layers,
 * applies master volume level, and soft limits the combined output to prevent clipping.
 */
class MixingPipeline(
    private val toneGenerator: BinauralToneGenerator,
    private val noiseGenerator: NoiseGenerator
) {
    /**
     * Generates and mixes a block of stereo samples into separate left and right buffers.
     * [leftBuffer] and [rightBuffer] must be of the same size.
     */
    fun mixBlock(
        scheduler: SequenceScheduler,
        leftBuffer: FloatArray,
        rightBuffer: FloatArray,
        noiseType: String?,
        noiseAmplitude: Float,
        masterVolume: Float
    ) {
        val numSamples = leftBuffer.size

        // Configure mix parameters
        val useNoise = !noiseType.isNullOrEmpty() && !noiseType.equals("none", ignoreCase = true)
        val nAmp = noiseAmplitude.toDouble().coerceIn(0.0, 1.0)
        val toneAmp = 1.0 - nAmp
        val mVol = masterVolume.toDouble().coerceIn(0.0, 1.0)

        for (i in 0 until numSamples) {
            val params = scheduler.nextSample()
            
            // Generate tone
            val (toneL, toneR) = toneGenerator.nextSample(params.carrierFrequency, params.beatFrequency)
            
            // Generate noise (identically to both channels)
            val noiseVal = if (useNoise) {
                noiseGenerator.nextSample(noiseType)
            } else {
                0.0
            }

            // Mix: Left & Right channels
            val mixedL = (toneL * toneAmp) + (noiseVal * nAmp)
            val mixedR = (toneR * toneAmp) + (noiseVal * nAmp)

            // Apply step-level fadeFactor, master volume, and soft limiter tanh()
            leftBuffer[i] = SoftLimiter.limit(mixedL * params.fadeFactor * mVol).toFloat()
            rightBuffer[i] = SoftLimiter.limit(mixedR * params.fadeFactor * mVol).toFloat()
        }
    }

    /**
     * Generates and mixes a block of stereo samples into a single interleaved buffer.
     * Length of [interleavedBuffer] must be even (2 * block_size).
     */
    fun mixBlock(
        scheduler: SequenceScheduler,
        interleavedBuffer: FloatArray,
        noiseType: String?,
        noiseAmplitude: Float,
        masterVolume: Float
    ) {
        val numSamples = interleavedBuffer.size / 2
        val leftBuffer = FloatArray(numSamples)
        val rightBuffer = FloatArray(numSamples)

        mixBlock(scheduler, leftBuffer, rightBuffer, noiseType, noiseAmplitude, masterVolume)

        // Interleave left and right buffers
        for (i in 0 until numSamples) {
            interleavedBuffer[2 * i] = leftBuffer[i]
            interleavedBuffer[2 * i + 1] = rightBuffer[i]
        }
    }
}
