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
        volTone: Float,
        volWhite: Float,
        volPink: Float,
        volBrown: Float,
        volRain: Float,
        volRiver: Float,
        volOcean: Float,
        masterVolume: Float
    ) {
        val numSamples = leftBuffer.size

        // Configure mix parameters
        val vTone = volTone.toDouble().coerceIn(0.0, 1.0)
        val vWhite = volWhite.toDouble().coerceIn(0.0, 1.0)
        val vPink = volPink.toDouble().coerceIn(0.0, 1.0)
        val vBrown = volBrown.toDouble().coerceIn(0.0, 1.0)
        val vRain = volRain.toDouble().coerceIn(0.0, 1.0)
        val vRiver = volRiver.toDouble().coerceIn(0.0, 1.0)
        val vOcean = volOcean.toDouble().coerceIn(0.0, 1.0)
        val mVol = masterVolume.toDouble().coerceIn(0.0, 1.0)

        for (i in 0 until numSamples) {
            val params = scheduler.nextSample()
            
            // Generate tone
            val (toneL, toneR) = toneGenerator.nextSample(params.carrierFrequency, params.beatFrequency)
            
            // Generate combined noises (identically to both channels)
            val noiseVal = (noiseGenerator.nextSample("white") * vWhite) +
                           (noiseGenerator.nextSample("pink") * vPink) +
                           (noiseGenerator.nextSample("brown") * vBrown) +
                           (noiseGenerator.nextSample("rain") * vRain) +
                           (noiseGenerator.nextSample("river") * vRiver) +
                           (noiseGenerator.nextSample("ocean") * vOcean)

            // Mix: Left & Right channels (tone multiplied by tone level, combined noise added directly)
            val mixedL = (toneL * vTone) + noiseVal
            val mixedR = (toneR * vTone) + noiseVal

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
        volTone: Float,
        volWhite: Float,
        volPink: Float,
        volBrown: Float,
        volRain: Float,
        volRiver: Float,
        volOcean: Float,
        masterVolume: Float
    ) {
        val numSamples = interleavedBuffer.size / 2
        val leftBuffer = FloatArray(numSamples)
        val rightBuffer = FloatArray(numSamples)

        mixBlock(scheduler, leftBuffer, rightBuffer, volTone, volWhite, volPink, volBrown, volRain, volRiver, volOcean, masterVolume)

        // Interleave left and right buffers
        for (i in 0 until numSamples) {
            interleavedBuffer[2 * i] = leftBuffer[i]
            interleavedBuffer[2 * i + 1] = rightBuffer[i]
        }
    }
}
