package com.monliev.brainwave.audio.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.session.MediaSession
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import com.monliev.brainwave.MainActivity
import com.monliev.brainwave.audio.preset.Preset
import com.monliev.brainwave.audio.preset.PresetLoader
import com.monliev.brainwave.audio.preset.SequenceScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * AudioEngineService is a Foreground Service that manages background audio playback
 * of Binaural Beat presets, acquires WAKELOCKs during active playback, and displays
 * a persistent notification with playback controls and Sleep Timer status.
 * It also automatically pauses playback if headphones are disconnected.
 */
class AudioEngineService : Service(), AudioTrackManager.OnCompletionListener {

    private val binder = LocalBinder()
    private val audioTrackManager = AudioTrackManager()
    
    private var wakeLock: PowerManager.WakeLock? = null
    private var mediaSession: MediaSession? = null
    
    private var currentPreset: Preset? = null
    private var currentScheduler: SequenceScheduler? = null
    private var isServiceRunningInForeground = false

    private var masterVolume: Float = 1.0f
    private var noiseType: String? = null
    private var noiseAmplitude: Float = 0.0f

    private var volTone: Float = 1.0f
    private var volWhite: Float = 0.0f
    private var volPink: Float = 0.0f
    private var volBrown: Float = 0.0f
    private var volRain: Float = 0.0f
    private var volRiver: Float = 0.0f
    private var volOcean: Float = 0.0f

    // Sleep Timer coroutine fields
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var timerJob: Job? = null
    private var sleepTimerSecondsRemaining = 0
    private var sessionStartTimestamp: Long = 0

    // Broadcast receiver for headphone disconnect
    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                // Headphone disconnected -> Auto-pause playback to avoid blasting speaker
                pausePlayback()
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "brainwave_playback_channel"
        const val NOTIFICATION_ID = 101

        // Intent Actions
        const val ACTION_START = "com.monliev.brainwave.ACTION_START"
        const val ACTION_PAUSE = "com.monliev.brainwave.ACTION_PAUSE"
        const val ACTION_RESUME = "com.monliev.brainwave.ACTION_RESUME"
        const val ACTION_STOP = "com.monliev.brainwave.ACTION_STOP"
        const val ACTION_SET_TIMER = "com.monliev.brainwave.ACTION_SET_TIMER"

        // Intent Extras
        const val EXTRA_PRESET_JSON = "extra_preset_json"
        const val EXTRA_TIMER_DURATION_SECONDS = "extra_timer_duration_seconds"
    }

    inner class LocalBinder : Binder() {
        fun getService(): AudioEngineService = this@AudioEngineService
    }

    override fun onCreate() {
        super.onCreate()
        
        // 1. Initialize WakeLock
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Brainwave:AudioPlaybackLock")

        // 2. Initialize MediaSession
        mediaSession = MediaSession(this, "BrainwaveSession").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    resumePlayback()
                }

                override fun onPause() {
                    pausePlayback()
                }

                override fun onStop() {
                    stopPlayback()
                }
            })
            isActive = true
        }

        // 3. Set audio manager completion listener
        audioTrackManager.onCompletionListener = this
        createNotificationChannel()

        // 4. Register ACTION_AUDIO_BECOMING_NOISY receiver dynamically
        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(becomingNoisyReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val json = intent.getStringExtra(EXTRA_PRESET_JSON)
                if (json != null) {
                    try {
                        val preset = PresetLoader.loadPreset(json)
                        startPlayback(preset)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        stopSelf()
                    }
                }
            }
            ACTION_PAUSE -> pausePlayback()
            ACTION_RESUME -> resumePlayback()
            ACTION_STOP -> stopPlayback()
            ACTION_SET_TIMER -> {
                val duration = intent.getIntExtra(EXTRA_TIMER_DURATION_SECONDS, 0)
                setSleepTimer(duration)
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCompletion() {
        stopPlayback()
    }

    // --- Playback Controls ---

    private fun startPlayback(preset: Preset) {
        currentPreset = preset
        sessionStartTimestamp = System.currentTimeMillis()
        val scheduler = SequenceScheduler(preset, 44100.0)
        currentScheduler = scheduler

        acquireWakeLock()
        audioTrackManager.setMasterVolume(masterVolume)
        // Set default preset noise configuration to individual mixer levels
        volTone = 1.0f
        volWhite = 0.0f
        volPink = 0.0f
        volBrown = 0.0f
        volRain = 0.0f
        volRiver = 0.0f
        volOcean = 0.0f
        val type = preset.background_noise?.type
        val amplitude = preset.background_noise?.amplitude ?: 0.0f
        if (!type.isNullOrEmpty() && !type.equals("none", ignoreCase = true)) {
            val amp = amplitude.coerceIn(0.0f, 1.0f)
            when (type.lowercase()) {
                "white" -> volWhite = amp
                "pink" -> volPink = amp
                "brown" -> volBrown = amp
            }
        }
        audioTrackManager.setMixerLevels(volTone, volWhite, volPink, volBrown, volRain, volRiver, volOcean)
        audioTrackManager.play(scheduler, noiseType, noiseAmplitude)

        updateNotification()
    }

    fun pausePlayback() {
        audioTrackManager.pause()
        releaseWakeLock()
        pauseTimer()
        updateNotification()
    }

    fun resumePlayback() {
        val scheduler = currentScheduler
        if (scheduler != null && !scheduler.isFinished()) {
            acquireWakeLock()
            audioTrackManager.resume()
            resumeTimer()
            updateNotification()
        }
    }

    fun stopPlayback() {
        val durationMs = System.currentTimeMillis() - sessionStartTimestamp
        val durationSec = (durationMs / 1000).toInt()
        val preset = currentPreset
        if (preset != null && durationSec >= 5) {
            val log = com.monliev.brainwave.data.local.SessionLogEntity(
                presetTitle = preset.title,
                category = preset.category,
                timestamp = System.currentTimeMillis(),
                durationSeconds = durationSec
            )
            serviceScope.launch(Dispatchers.IO) {
                try {
                    com.monliev.brainwave.data.local.AppDatabase.getDatabase(applicationContext)
                        .sessionLogDao().insertLog(log)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        cancelTimer()
        audioTrackManager.stop()
        releaseWakeLock()
        
        currentPreset = null
        currentScheduler = null

        if (isServiceRunningInForeground) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            isServiceRunningInForeground = false
        }
        stopSelf()
    }

    fun setVolume(volume: Float) {
        masterVolume = volume
        audioTrackManager.setMasterVolume(volume)
    }

    fun setNoise(type: String?, amplitude: Float) {
        noiseType = type
        noiseAmplitude = amplitude
        volTone = 1.0f
        volWhite = 0.0f
        volPink = 0.0f
        volBrown = 0.0f
        volRain = 0.0f
        volRiver = 0.0f
        volOcean = 0.0f
        if (!type.isNullOrEmpty() && !type.equals("none", ignoreCase = true)) {
            val amp = amplitude.coerceIn(0.0f, 1.0f)
            when (type.lowercase()) {
                "white" -> volWhite = amp
                "pink" -> volPink = amp
                "brown" -> volBrown = amp
            }
        }
        audioTrackManager.setMixerLevels(volTone, volWhite, volPink, volBrown, volRain, volRiver, volOcean)
    }

    fun setMixerLevels(tone: Float, white: Float, pink: Float, brown: Float, rain: Float, river: Float, ocean: Float) {
        volTone = tone
        volWhite = white
        volPink = pink
        volBrown = brown
        volRain = rain
        volRiver = river
        volOcean = ocean
        audioTrackManager.setMixerLevels(tone, white, pink, brown, rain, river, ocean)
    }

    fun getPlayingPreset(): Preset? = currentPreset
    fun getScheduler(): SequenceScheduler? = currentScheduler
    fun isPlaying(): Boolean = wakeLock?.isHeld == true
    fun getSleepTimerRemainingSeconds(): Int = sleepTimerSecondsRemaining
    fun getCurrentBeatFrequency(): Double = currentScheduler?.getCurrentBeatFrequency() ?: 0.0

    /**
     * Checks if headphones are connected. Accessible by Bound UI clients.
     */
    fun isHeadphoneConnected(): Boolean {
        return DeviceOutputMonitor.isHeadphoneConnected(this)
    }

    // --- Sleep Timer Logic ---

    fun setSleepTimer(seconds: Int) {
        cancelTimer()
        if (seconds <= 0) {
            sleepTimerSecondsRemaining = 0
            updateNotification()
            return
        }
        sleepTimerSecondsRemaining = seconds
        startTimerCountdown()
    }

    private fun startTimerCountdown() {
        timerJob = serviceScope.launch {
            while (sleepTimerSecondsRemaining > 0) {
                delay(1000)
                sleepTimerSecondsRemaining--
                updateNotification()

                if (sleepTimerSecondsRemaining <= 0) {
                    audioTrackManager.stopWithFade(5.0)
                    break
                }
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun resumeTimer() {
        if (sleepTimerSecondsRemaining > 0 && timerJob == null) {
            startTimerCountdown()
        }
    }

    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        sleepTimerSecondsRemaining = 0
    }

    // --- WakeLock Hygiene ---

    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == false) {
            wakeLock?.acquire()
        }
    }

    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    // --- Foreground Notification ---

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Playback Control",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification with playback controls for Brainwave"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun formatTime(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }

    private fun updateNotification() {
        val preset = currentPreset ?: return
        val isAudioPlaying = isPlaying()

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = android.net.Uri.parse("brainwave://player/${preset.preset_id}")
        }
        val mainPendingIntent = PendingIntent.getActivity(
            this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action Intents
        val stopIntent = Intent(this, AudioEngineService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = Intent(this, AudioEngineService::class.java).apply {
            action = if (isAudioPlaying) ACTION_PAUSE else ACTION_RESUME
        }
        val togglePendingIntent = PendingIntent.getService(
            this, 2, toggleIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val timerText = if (sleepTimerSecondsRemaining > 0) {
            " (Timer: ${formatTime(sleepTimerSecondsRemaining)})"
        } else {
            ""
        }

        // Build native notification using platform style
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }

        builder.setContentTitle(preset.title)
            .setContentText("Category: ${preset.category}$timerText")
            .setSmallIcon(android.R.drawable.ic_media_play) // Fallback system icon
            .setContentIntent(mainPendingIntent)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .addAction(
                Notification.Action.Builder(
                    if (isAudioPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                    if (isAudioPlaying) "Pause" else "Play",
                    togglePendingIntent
                ).build()
            )
            .addAction(
                Notification.Action.Builder(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "Stop",
                    stopPendingIntent
                ).build()
            )
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(mediaSession?.sessionToken)
                    .setShowActionsInCompactView(0, 1)
            )

        val notification = builder.build()
        
        if (!isServiceRunningInForeground) {
            startForeground(NOTIFICATION_ID, notification)
            isServiceRunningInForeground = true
        } else {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
        unregisterReceiver(becomingNoisyReceiver) // Unregister headphone receiver
        serviceJob.cancel() // Cancel all coroutines running in serviceScope
        mediaSession?.release()
        mediaSession = null
    }
}
