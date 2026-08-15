package com.monliev.brainwave.audio

import com.monliev.brainwave.audio.core.NoiseGenerator
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * DcOffsetTest verifies that the mean value of 1 second of noise samples
 * (for white, pink, and brown noise) is close to 0.0 (DC offset is near zero).
 */
class DcOffsetTest {

    @Test
    fun testNoiseDcOffset() {
        val noiseGenerator = NoiseGenerator()
        val durationSamples = 44100 // 1 second of audio

        val types = listOf("white", "pink", "brown")

        for (type in types) {
            noiseGenerator.reset()
            var sum = 0.0
            
            for (i in 0 until durationSamples) {
                sum += noiseGenerator.nextSample(type)
            }
            
            val mean = sum / durationSamples
            
            // Assert mean is close to 0.0 with a tolerance of 0.05
            assertEquals("DC offset for $type noise ($mean) is out of bounds", 0.0, mean, 0.05)
        }
    }
}
