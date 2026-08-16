package com.monliev.brainwave.audio.playback

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Process
import com.monliev.brainwave.audio.core.BinauralToneGenerator
import com.monliev.brainwave.audio.core.MixingPipeline
import com.monliev.brainwave.audio.core.NoiseGenerator
import com.monliev.brainwave.audio.preset.SequenceScheduler
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * AudioTrackManager handles the audio thread loop, writes generated PCM float data
 * to an AudioTrack instance in STREAM mode, and handles play, pause, resume, and volume updates.
 * Includes manual cross-fading (300ms) for transitions.
 */
class AudioTrackManager(
    private val sampleRate: Double = 44100.0,
    private val blockSize: Int = 4410 // 100ms block size
) {
    private enum class PlaybackState {
        IDLE, PLAYING, PAUSING, STOPPING, PAUSED
    }

    private val toneGenerator = BinauralToneGenerator(sampleRate)
    private val noiseGenerator = NoiseGenerator()
    private val mixingPipeline = MixingPipeline(toneGenerator, noiseGenerator)

    private var audioTrack: AudioTrack? = null
    private var audioThread: Thread? = null
    
    private val isPlaying = AtomicBoolean(false)
    private val schedulerRef = AtomicReference<SequenceScheduler?>()
    private val state = AtomicReference(PlaybackState.IDLE)
    
    @Volatile private var manualFadeFactor = 0.0
    @Volatile private var targetFadeFactor = 0.0
    @Volatile private var fadeRatePerSample = 0.0

    @Volatile private var masterVolume: Float = 1.0f
    @Volatile private var volTone: Float = 1.0f
    @Volatile private var volWhite: Float = 0.0f
    @Volatile private var volPink: Float = 0.0f
    @Volatile private var volBrown: Float = 0.0f
    @Volatile private var volRain: Float = 0.0f
    @Volatile private var volRiver: Float = 0.0f
    @Volatile private var volOcean: Float = 0.0f

    /**
     * Interface to listen for preset completion events.
     */
    interface OnCompletionListener {
        fun onCompletion()
    }

    var onCompletionListener: OnCompletionListener? = null

    /**
     * Starts audio playback with the given [scheduler] and noise settings.
     * Starts with a smooth 300ms fade-in.
     */
    @Synchronized
    fun play(scheduler: SequenceScheduler, initialNoiseType: String?, initialNoiseAmplitude: Float) {
        stop() // Ensure clean state before starting
        
        schedulerRef.set(scheduler)
        
        // Reset and load default preset noise levels
        volTone = 1.0f
        volWhite = 0.0f
        volPink = 0.0f
        volBrown = 0.0f
        volRain = 0.0f
        volRiver = 0.0f
        volOcean = 0.0f
        if (!initialNoiseType.isNullOrEmpty() && !initialNoiseType.equals("none", ignoreCase = true)) {
            val amp = initialNoiseAmplitude.coerceIn(0.0f, 1.0f)
            when (initialNoiseType.lowercase()) {
                "white" -> volWhite = amp
                "pink" -> volPink = amp
                "brown" -> volBrown = amp
            }
        }

        isPlaying.set(true)
        state.set(PlaybackState.PLAYING)

        // Setup manual cross-fade (300ms fade-in)
        manualFadeFactor = 0.0
        targetFadeFactor = 1.0
        fadeRatePerSample = 1.0 / (0.3 * sampleRate)

        // Initialize AudioTrack
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val audioFormat = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
            .setSampleRate(sampleRate.toInt())
            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
            .build()

        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate.toInt(),
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_FLOAT
        )
        val bufferSize = (minBufferSize * 2).coerceAtLeast(blockSize * 2 * 4)

        audioTrack = AudioTrack(
            audioAttributes,
            audioFormat,
            bufferSize,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )

        audioTrack?.play()

        // Start Audio Thread
        audioThread = Thread {
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
            renderLoop()
        }.apply {
            name = "AudioEngineThread"
            start()
        }
    }

    /**
     * Pauses playback. Smoothly fades out (300ms) before pausing the track.
     */
    @Synchronized
    fun pause() {
        if (state.get() == PlaybackState.PLAYING) {
            state.set(PlaybackState.PAUSING)
            targetFadeFactor = 0.0
            fadeRatePerSample = 1.0 / (0.3 * sampleRate)

            // Wait for thread to finish rendering fade-out and pause
            audioThread?.join(1000)
            audioThread = null
        }
    }

    /**
     * Resumes playback. Smoothly fades in (300ms).
     */
    @Synchronized
    fun resume() {
        val scheduler = schedulerRef.get()
        if (state.get() == PlaybackState.PAUSED && scheduler != null) {
            isPlaying.set(true)
            state.set(PlaybackState.PLAYING)
            targetFadeFactor = 1.0
            fadeRatePerSample = 1.0 / (0.3 * sampleRate)

            audioTrack?.play()
            
            audioThread = Thread {
                Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
                renderLoop()
            }.apply {
                name = "AudioEngineThread"
                start()
            }
        }
    }

    /**
     * Stops playback immediately.
     */
    @Synchronized
    fun stop() {
        isPlaying.set(false)
        state.set(PlaybackState.IDLE)
        
        audioThread?.join(500)
        audioThread = null

        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null

        schedulerRef.set(null)
        toneGenerator.reset()
        noiseGenerator.reset()
    }

    /**
     * Initiates a fade-out over [durationSeconds] and then stops playback.
     */
    @Synchronized
    fun stopWithFade(durationSeconds: Double) {
        if (state.get() == PlaybackState.PLAYING) {
            state.set(PlaybackState.STOPPING)
            targetFadeFactor = 0.0
            fadeRatePerSample = 1.0 / (durationSeconds * sampleRate)
        }
    }

    /**
     * Updates the master volume level.
     */
    fun setMasterVolume(volume: Float) {
        masterVolume = volume.coerceIn(0.0f, 1.0f)
    }

    /**
     * Updates the mixer channel levels on the fly.
     */
    fun setMixerLevels(tone: Float, white: Float, pink: Float, brown: Float, rain: Float, river: Float, ocean: Float) {
        volTone = tone.coerceIn(0.0f, 1.0f)
        volWhite = white.coerceIn(0.0f, 1.0f)
        volPink = pink.coerceIn(0.0f, 1.0f)
        volBrown = brown.coerceIn(0.0f, 1.0f)
        volRain = rain.coerceIn(0.0f, 1.0f)
        volRiver = river.coerceIn(0.0f, 1.0f)
        volOcean = ocean.coerceIn(0.0f, 1.0f)
    }

    private fun renderLoop() {
        val interleavedBuffer = FloatArray(blockSize * 2)

        while (isPlaying.get() && state.get() != PlaybackState.PAUSED) {
            val scheduler = schedulerRef.get()
            if (scheduler == null || scheduler.isFinished()) {
                // Preset sequence has finished
                isPlaying.set(false)
                state.set(PlaybackState.IDLE)
                onCompletionListener?.onCompletion()
                break
            }

            // Mix and limit block
            mixingPipeline.mixBlock(
                scheduler = scheduler,
                interleavedBuffer = interleavedBuffer,
                volTone = volTone,
                volWhite = volWhite,
                volPink = volPink,
                volBrown = volBrown,
                volRain = volRain,
                volRiver = volRiver,
                volOcean = volOcean,
                masterVolume = masterVolume
            )

            // Apply manual cross-fade sample-by-sample
            for (i in 0 until blockSize) {
                if (manualFadeFactor < targetFadeFactor) {
                    manualFadeFactor = (manualFadeFactor + fadeRatePerSample).coerceAtMost(targetFadeFactor)
                } else if (manualFadeFactor > targetFadeFactor) {
                    manualFadeFactor = (manualFadeFactor - fadeRatePerSample).coerceAtLeast(targetFadeFactor)
                }

                interleavedBuffer[2 * i] *= manualFadeFactor.toFloat()
                interleavedBuffer[2 * i + 1] *= manualFadeFactor.toFloat()
            }

            // Write to track
            val track = audioTrack
            if (track != null && isPlaying.get()) {
                val written = track.write(
                    interleavedBuffer,
                    0,
                    interleavedBuffer.size,
                    AudioTrack.WRITE_BLOCKING
                )
                if (written < 0) {
                    break
                }
            } else {
                break
            }

            // Check if manual fade-out to 0.0 has completed for pausing/stopping
            if (manualFadeFactor == 0.0 && (state.get() == PlaybackState.PAUSING || state.get() == PlaybackState.STOPPING)) {
                if (state.get() == PlaybackState.PAUSING) {
                    state.set(PlaybackState.PAUSED)
                    isPlaying.set(false)
                    audioTrack?.pause()
                } else {
                    // STOPPING finished
                    stop()
                    onCompletionListener?.onCompletion()
                }
                break
            }
        }
    }
}
