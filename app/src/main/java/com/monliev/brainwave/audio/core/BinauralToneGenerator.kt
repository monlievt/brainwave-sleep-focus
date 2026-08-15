package com.monliev.brainwave.audio.core

import com.monliev.brainwave.audio.preset.SequenceScheduler

/**
 * BinauralToneGenerator synthesizes binaural beats by generating two separate sine waves,
 * one for each channel, with a frequency difference equal to the target beat frequency.
 * f_left = carrier_freq - (beat_freq / 2)
 * f_right = carrier_freq + (beat_freq / 2)
 */
class BinauralToneGenerator(
    private val sampleRate: Double = 44100.0
) {
    private val leftOscillator = PhaseAccumulator(sampleRate)
    private val rightOscillator = PhaseAccumulator(sampleRate)

    /**
     * Generates a single stereo sample pair for static carrier and beat frequencies.
     * Returns a [Pair] of (leftSample, rightSample).
     */
    fun nextSample(carrierFrequency: Double, beatFrequency: Double): Pair<Float, Float> {
        val halfBeat = beatFrequency / 2.0
        val leftFreq = carrierFrequency - halfBeat
        val rightFreq = carrierFrequency + halfBeat
        
        val left = leftOscillator.nextSample(leftFreq).toFloat()
        val right = rightOscillator.nextSample(rightFreq).toFloat()
        
        return Pair(left, right)
    }

    /**
     * Generates a block of stereo samples with static frequencies.
     * [leftBuffer] and [rightBuffer] must be of the same size.
     */
    fun generateBlock(
        carrierFrequency: Double,
        beatFrequency: Double,
        leftBuffer: FloatArray,
        rightBuffer: FloatArray
    ) {
        for (i in leftBuffer.indices) {
            val (left, right) = nextSample(carrierFrequency, beatFrequency)
            leftBuffer[i] = left
            rightBuffer[i] = right
        }
    }

    /**
     * Generates a block of stereo samples dynamically using a [SequenceScheduler] (sample-by-sample frequency updates).
     * [leftBuffer] and [rightBuffer] must be of the same size.
     */
    fun generateBlock(
        scheduler: SequenceScheduler,
        leftBuffer: FloatArray,
        rightBuffer: FloatArray
    ) {
        for (i in leftBuffer.indices) {
            val params = scheduler.nextSample()
            val (left, right) = nextSample(params.carrierFrequency, params.beatFrequency)
            leftBuffer[i] = left
            rightBuffer[i] = right
        }
    }

    /**
     * Resets the phases of both oscillators.
     */
    fun reset() {
        leftOscillator.reset()
        rightOscillator.reset()
    }
}
