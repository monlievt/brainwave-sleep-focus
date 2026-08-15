package com.monliev.brainwave.audio.core

import kotlin.math.tanh

/**
 * SoftLimiter applies a smooth tanh limiting function to keep audio samples bounded within [-1.0, 1.0].
 * This prevents hard clipping distortion.
 */
object SoftLimiter {
    /**
     * Applies soft limiting using tanh(sample).
     */
    fun limit(sample: Double): Double {
        return tanh(sample)
    }

    /**
     * Applies soft limiting using tanh(sample) on float values.
     */
    fun limit(sample: Float): Float {
        return tanh(sample.toDouble()).toFloat()
    }
}
