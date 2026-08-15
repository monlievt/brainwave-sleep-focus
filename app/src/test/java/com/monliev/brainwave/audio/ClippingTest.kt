package com.monliev.brainwave.audio

import com.monliev.brainwave.audio.core.BinauralToneGenerator
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ClippingTest verifies that the synthesized raw audio samples do not exceed the
 * maximum amplitude range of [-1.0, 1.0] (clipping check).
 */
class ClippingTest {

    @Test
    fun testBinauralGeneratorClipping() {
        val generator = BinauralToneGenerator(44100.0)
        
        // Generate 1 second of audio at a block size of 44100
        val leftBuffer = FloatArray(44100)
        val rightBuffer = FloatArray(44100)
        
        generator.generateBlock(
            carrierFrequency = 200.0,
            beatFrequency = 10.0,
            leftBuffer = leftBuffer,
            rightBuffer = rightBuffer
        )
        
        // Assert no sample goes beyond [-1.0f, 1.0f]
        for (i in leftBuffer.indices) {
            val leftSample = leftBuffer[i]
            val rightSample = rightBuffer[i]
            
            assertTrue("Left channel clipped at index $i: $leftSample", leftSample in -1.0f..1.0f)
            assertTrue("Right channel clipped at index $i: $rightSample", rightSample in -1.0f..1.0f)
        }
    }
}
