package com.monliev.brainwave.audio

import com.monliev.brainwave.audio.core.BinauralToneGenerator
import com.monliev.brainwave.audio.preset.Preset
import com.monliev.brainwave.audio.preset.SequenceScheduler
import com.monliev.brainwave.audio.preset.Step
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * PhaseContinuityTest verifies that when transitions occur between stable and transition steps,
 * or as frequency changes dynamically, the phase remains continuous and there are no clicking
 * or popping sounds (no sudden phase jumps).
 */
class PhaseContinuityTest {

    @Test
    fun testTransitionPhaseContinuity() {
        val sampleRate = 44100.0
        
        // Mock preset: 2 seconds of stable 10Hz beat -> 2 seconds of transition 10Hz to 4Hz beat
        val preset = Preset(
            preset_id = "test_continuity",
            category = "STUDY",
            title = "Continuity Test",
            description = "Test continuity",
            carrier_frequency_hz = 200.0,
            steps = listOf(
                Step(
                    type = "stable",
                    beat_frequency_hz = 10.0,
                    duration_seconds = 2
                ),
                Step(
                    type = "transition",
                    start_beat_frequency_hz = 10.0,
                    end_beat_frequency_hz = 4.0,
                    duration_seconds = 2
                )
            )
        )

        val scheduler = SequenceScheduler(preset, sampleRate)
        val generator = BinauralToneGenerator(sampleRate)

        // Generate 4 seconds of audio (40 blocks of 100ms)
        val blockSize = 4410
        val totalBlocks = 40
        
        val leftCombined = FloatArray(blockSize * totalBlocks)
        val rightCombined = FloatArray(blockSize * totalBlocks)

        val tempLeft = FloatArray(blockSize)
        val tempRight = FloatArray(blockSize)

        for (b in 0 until totalBlocks) {
            generator.generateBlock(scheduler, tempLeft, tempRight)
            System.arraycopy(tempLeft, 0, leftCombined, b * blockSize, blockSize)
            System.arraycopy(tempRight, 0, rightCombined, b * blockSize, blockSize)
        }

        // Theoretical maximum delta between samples for a 205Hz signal (200 + 10/2):
        // 2 * PI * 205.0 / 44100.0 = 0.0292.
        // We set our threshold to 0.035 to allow a safety margin.
        // If a phase discontinuity occurs (a click/jump), the delta will be significantly larger (up to 2.0).
        val maxAllowedDelta = 0.035

        for (i in 1 until leftCombined.size) {
            val deltaLeft = abs(leftCombined[i] - leftCombined[i - 1])
            val deltaRight = abs(rightCombined[i] - rightCombined[i - 1])

            assertTrue(
                "Phase discontinuity (click/pop) detected in left channel at sample $i. Delta was $deltaLeft, max allowed is $maxAllowedDelta",
                deltaLeft < maxAllowedDelta
            )
            assertTrue(
                "Phase discontinuity (click/pop) detected in right channel at sample $i. Delta was $deltaRight, max allowed is $maxAllowedDelta",
                deltaRight < maxAllowedDelta
            )
        }
    }
}
