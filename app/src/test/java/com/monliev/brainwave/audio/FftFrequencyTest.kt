package com.monliev.brainwave.audio

import com.monliev.brainwave.audio.core.BinauralToneGenerator
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * FftFrequencyTest verifies that the carrier and beat frequencies are correctly
 * synthesized in each channel.
 * Target Left Frequency = carrier - beat/2
 * Target Right Frequency = carrier + beat/2
 * Frequency tolerance is +/- 0.5 Hz.
 */
class FftFrequencyTest {

    @Test
    fun testBinauralFrequencies() {
        val sampleRate = 44100.0
        val generator = BinauralToneGenerator(sampleRate)

        val carrier = 200.0
        val beat = 10.0
        val targetLeft = carrier - (beat / 2.0) // 195.0 Hz
        val targetRight = carrier + (beat / 2.0) // 205.0 Hz

        // Generate 1 second of audio
        val N = 44100
        val leftBuffer = FloatArray(N)
        val rightBuffer = FloatArray(N)

        generator.generateBlock(carrier, beat, leftBuffer, rightBuffer)

        // Find the peak frequencies using a high-resolution DFT sweep
        val peakLeft = findPeakFrequency(leftBuffer, targetLeft - 5.0, targetLeft + 5.0, 0.1, sampleRate)
        val peakRight = findPeakFrequency(rightBuffer, targetRight - 5.0, targetRight + 5.0, 0.1, sampleRate)

        // Verify frequency with +/- 0.5 Hz tolerance
        assertEquals("Left peak frequency $peakLeft Hz is out of tolerance for target $targetLeft Hz", targetLeft, peakLeft, 0.5)
        assertEquals("Right peak frequency $peakRight Hz is out of tolerance for target $targetRight Hz", targetRight, peakRight, 0.5)
    }

    /**
     * Scans a range of frequencies to find the one with the maximum DFT magnitude.
     */
    private fun findPeakFrequency(
        buffer: FloatArray,
        startFreq: Double,
        endFreq: Double,
        step: Double,
        sampleRate: Double
    ): Double {
        var peakFreq = startFreq
        var maxMag = -1.0

        var currentFreq = startFreq
        while (currentFreq <= endFreq) {
            val mag = computeDftMagnitude(buffer, currentFreq, sampleRate)
            if (mag > maxMag) {
                maxMag = mag
                peakFreq = currentFreq
            }
            currentFreq += step
        }
        return peakFreq
    }

    /**
     * Computes the magnitude of the DFT at a specific frequency.
     */
    private fun computeDftMagnitude(buffer: FloatArray, frequency: Double, sampleRate: Double): Double {
        var cosSum = 0.0
        var sinSum = 0.0
        val N = buffer.size

        for (n in 0 until N) {
            val angle = 2.0 * PI * frequency * n / sampleRate
            cosSum += buffer[n] * cos(angle)
            sinSum += buffer[n] * sin(angle)
        }

        return sqrt(cosSum * cosSum + sinSum * sinSum)
    }
}
