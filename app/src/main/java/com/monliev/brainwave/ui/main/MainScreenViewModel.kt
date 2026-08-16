package com.monliev.brainwave.ui.main

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.monliev.brainwave.audio.playback.AudioEngineService
import com.monliev.brainwave.audio.preset.Preset
import com.monliev.brainwave.data.PresetRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import com.monliev.brainwave.data.local.AppDatabase
import com.monliev.brainwave.data.local.CustomPresetEntity
import com.monliev.brainwave.data.local.SessionLogEntity
import com.monliev.brainwave.data.local.SessionAlarmEntity
import com.monliev.brainwave.audio.playback.AlarmScheduler
import kotlinx.coroutines.Dispatchers

/**
 * MainScreenViewModel coordinates the communication between the Compose UI screens,
 * the AudioEngineService playback states, and local application preferences like Night Mode.
 */
class MainScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val presetRepository = PresetRepository(application)
    private var boundService: AudioEngineService? = null

    // Lists of presets
    val allPresets: List<Preset> = presetRepository.getPresets()
    
    // Playback state flows
    val isPlaying = MutableStateFlow(false)
    val currentPreset = MutableStateFlow<Preset?>(null)
    val timerSecondsRemaining = MutableStateFlow(0)
    val isHeadphoneConnected = MutableStateFlow(false)
    val currentBeatFrequency = MutableStateFlow(10.0)

    // Local preferences state flows
    val isDarkMode = MutableStateFlow(true)
    val isPremium = MutableStateFlow(false)
    val isSchedulerUnlockedTemporarily = MutableStateFlow(false)

    // Mixer volumes state flows
    val volumeTone = MutableStateFlow(1.0f)
    val volumeWhite = MutableStateFlow(0.0f)
    val volumePink = MutableStateFlow(0.0f)
    val volumeBrown = MutableStateFlow(0.0f)
    val volumeRain = MutableStateFlow(0.0f)
    val volumeRiver = MutableStateFlow(0.0f)
    val volumeOcean = MutableStateFlow(0.0f)
    val volumeCampfire = MutableStateFlow(0.0f)
    val volumeWind = MutableStateFlow(0.0f)
    val volumeCoffeeShop = MutableStateFlow(0.0f)
    
    // Deep Link Flow
    val pendingDeepLinkPreset = MutableStateFlow<String?>(null)

    // Daily free mixer use — tracks if the user already used their 1x free mixer session today.
    // Resets automatically on a new calendar day. Persisted via SharedPreferences.
    val isMixerFreeUsedToday = MutableStateFlow(false)

    // Room database flows
    val customPresets = MutableStateFlow<List<CustomPresetEntity>>(emptyList())
    val sessionLogs = MutableStateFlow<List<SessionLogEntity>>(emptyList())
    val alarms = MutableStateFlow<List<SessionAlarmEntity>>(emptyList())


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioEngineService.LocalBinder
            boundService = binder.getService()
            boundService?.setMixerLevels(volumeTone.value, volumeWhite.value, volumePink.value, volumeBrown.value)
            boundService?.setNatureMixerLevels(volumeRain.value, volumeRiver.value, volumeOcean.value, volumeCampfire.value, volumeWind.value, volumeCoffeeShop.value)
            startStateQueryLoop()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            boundService = null
        }
    }

    init {
        // Load Dark Mode setting
        val sharedPrefs = application.getSharedPreferences("brainwave_prefs", Context.MODE_PRIVATE)
        isDarkMode.value = sharedPrefs.getBoolean("dark_mode_enabled", true)
        isPremium.value = sharedPrefs.getBoolean("is_premium_unlocked", false)
        
        val unlockUntil = sharedPrefs.getLong("rewarded_alarm_scheduler_unlocked_until", 0L)
        isSchedulerUnlockedTemporarily.value = (unlockUntil > System.currentTimeMillis())

        // Load daily free mixer use tracking — compare stored date string to today's date
        val storedFreeDate = sharedPrefs.getString("mixer_free_use_date", "")
        val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        isMixerFreeUsedToday.value = (storedFreeDate == todayDate)

        // Bind to AudioEngineService
        val intent = Intent(application, AudioEngineService::class.java)
        application.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        // Load Room DB Data
        val db = AppDatabase.getDatabase(application)
        viewModelScope.launch {
            db.customPresetDao().getAllPresetsFlow().collect {
                customPresets.value = it
            }
        }
        viewModelScope.launch {
            db.sessionLogDao().getAllLogsFlow().collect {
                sessionLogs.value = it
            }
        }
        viewModelScope.launch {
            db.sessionAlarmDao().getAllAlarmsFlow().collect {
                alarms.value = it
            }
        }
    }

    private fun startStateQueryLoop() {
        viewModelScope.launch {
            while (true) {
                boundService?.let { service ->
                    isPlaying.value = service.isPlaying()
                    currentPreset.value = service.getPlayingPreset()
                    timerSecondsRemaining.value = service.getSleepTimerRemainingSeconds()
                    isHeadphoneConnected.value = service.isHeadphoneConnected()
                    currentBeatFrequency.value = service.getCurrentBeatFrequency()
                }
                val sharedPrefs = getApplication<Application>().getSharedPreferences("brainwave_prefs", Context.MODE_PRIVATE)
                val unlockUntil = sharedPrefs.getLong("rewarded_alarm_scheduler_unlocked_until", 0L)
                isSchedulerUnlockedTemporarily.value = (unlockUntil > System.currentTimeMillis())
                delay(500)
            }
        }
    }

    /**
     * Toggles the application-wide dark mode state and persists it.
     */
    fun toggleDarkMode(enabled: Boolean) {
        isDarkMode.value = enabled
        val sharedPrefs = getApplication<Application>().getSharedPreferences("brainwave_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("dark_mode_enabled", enabled).apply()
    }

    /**
     * Unlocks or locks premium status locally.
     */
    fun setPremium(enabled: Boolean) {
        isPremium.value = enabled
        val sharedPrefs = getApplication<Application>().getSharedPreferences("brainwave_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("is_premium_unlocked", enabled).apply()
    }

    /**
     * Unlocks the bedtime alarm scheduler temporarily for 24 hours.
     * Also grants full mixer access for the same 24-hour window.
     */
    fun unlockSchedulerTemporarily() {
        val unlockTime = System.currentTimeMillis() + (24 * 60 * 60 * 1000)
        val sharedPrefs = getApplication<Application>().getSharedPreferences("brainwave_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putLong("rewarded_alarm_scheduler_unlocked_until", unlockTime).apply()
        isSchedulerUnlockedTemporarily.value = true
    }

    /**
     * Records that the user has used their free daily mixer session today.
     * Resets automatically when the calendar date changes.
     */
    fun recordMixerFreeUse() {
        val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val sharedPrefs = getApplication<Application>().getSharedPreferences("brainwave_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("mixer_free_use_date", todayDate).apply()
        isMixerFreeUsedToday.value = true
    }

    /**
     * Updates the individual channel levels in the UI and pushes them to the active service.
     */
    fun updateMixerLevels(tone: Float, white: Float, pink: Float, brown: Float) {
        volumeTone.value = tone
        volumeWhite.value = white
        volumePink.value = pink
        volumeBrown.value = brown
        boundService?.setMixerLevels(tone, white, pink, brown)
    }

    /**
     * Updates the premium nature and ambient sound channel levels.
     */
    fun updateNatureMixerLevels(rain: Float, river: Float, ocean: Float, campfire: Float, wind: Float, coffeeShop: Float) {
        volumeRain.value = rain
        volumeRiver.value = river
        volumeOcean.value = ocean
        volumeCampfire.value = campfire
        volumeWind.value = wind
        volumeCoffeeShop.value = coffeeShop
        boundService?.setNatureMixerLevels(rain, river, ocean, campfire, wind, coffeeShop)
    }

    /**
     * Handles dynamic deep link parsing and triggers the flow if valid.
     */
    fun handleDeepLinkUri(uriString: String?) {
        if (uriString == null) return
        try {
            val uri = android.net.Uri.parse(uriString)
            if (uri.scheme == "brainwave" && uri.host == "player") {
                val presetId = uri.lastPathSegment
                if (!presetId.isNullOrEmpty()) {
                    pendingDeepLinkPreset.value = presetId
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Clears the active deep link once consumed.
     */
    fun clearPendingDeepLink() {
        pendingDeepLinkPreset.value = null
    }


    /**
     * Starts playing the specified preset by sending a start Intent to the foreground service.
     */
    fun startPlayback(preset: Preset) {
        // Reset and initialize default noise levels for the UI
        volumeTone.value = 1.0f
        volumeWhite.value = 0.0f
        volumePink.value = 0.0f
        volumeBrown.value = 0.0f
        val defaultNoiseType = preset.background_noise?.type
        val defaultNoiseAmp = preset.background_noise?.amplitude ?: 0.0f
        if (!defaultNoiseType.isNullOrEmpty() && !defaultNoiseType.equals("none", ignoreCase = true)) {
            when (defaultNoiseType.lowercase()) {
                "white" -> volumeWhite.value = defaultNoiseAmp
                "pink" -> volumePink.value = defaultNoiseAmp
                "brown" -> volumeBrown.value = defaultNoiseAmp
            }
        }

        val app = getApplication<Application>()
        val json = Json.encodeToString(Preset.serializer(), preset)
        val intent = Intent(app, AudioEngineService::class.java).apply {
            action = AudioEngineService.ACTION_START
            putExtra(AudioEngineService.EXTRA_PRESET_JSON, json)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            app.startForegroundService(intent)
        } else {
            app.startService(intent)
        }
        
        isPlaying.value = true
        currentPreset.value = preset
    }

    /**
     * Toggles manual play/pause.
     */
    fun togglePlayPause() {
        val service = boundService ?: return
        if (service.isPlaying()) {
            service.pausePlayback()
        } else {
            service.resumePlayback()
        }
    }

    /**
     * Stops current playback session.
     */
    fun stopPlayback() {
        val app = getApplication<Application>()
        val intent = Intent(app, AudioEngineService::class.java).apply {
            action = AudioEngineService.ACTION_STOP
        }
        app.startService(intent)
        
        isPlaying.value = false
        currentPreset.value = null
        timerSecondsRemaining.value = 0
    }

    /**
     * Updates master volume.
     */
    fun setVolume(volume: Float) {
        boundService?.setVolume(volume)
    }

    /**
     * Sets sleep timer in minutes.
     */
    fun setSleepTimer(minutes: Int) {
        val app = getApplication<Application>()
        val seconds = minutes * 60
        val intent = Intent(app, AudioEngineService::class.java).apply {
            action = AudioEngineService.ACTION_SET_TIMER
            putExtra(AudioEngineService.EXTRA_TIMER_DURATION_SECONDS, seconds)
        }
        app.startService(intent)
    }

    /**
     * Sets background noise type and volume.
     */
    fun setNoise(type: String?, amplitude: Float) {
        boundService?.setNoise(type, amplitude)
    }

    /**
     * Gets presets filtered by category.
     */
    fun getPresetsByCategory(category: String): List<Preset> {
        return presetRepository.getPresetsByCategory(category)
    }

    // --- Room Database CRUD and Scheduler Operations ---

    fun isFavorite(presetId: String): Boolean {
        return customPresets.value.any { it.presetId == presetId && it.isFavorite }
    }

    fun toggleFavorite(preset: Preset) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(getApplication())
            val existing = db.customPresetDao().getPresetById(preset.preset_id)
            if (existing != null && existing.isFavorite) {
                if (existing.isCustom) {
                    db.customPresetDao().insertPreset(existing.copy(isFavorite = false))
                } else {
                    db.customPresetDao().deletePreset(existing)
                }
            } else {
                if (existing != null) {
                    db.customPresetDao().insertPreset(existing.copy(isFavorite = true))
                } else {
                    db.customPresetDao().insertPreset(
                        CustomPresetEntity.fromPreset(preset, isFavorite = true, isCustom = false)
                    )
                }
            }
        }
    }

    fun createCustomPreset(
        title: String,
        category: String,
        carrierHz: Double,
        beatHz: Double,
        noiseType: String,
        noiseVolume: Float,
        timerMins: Int
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(getApplication())
            val presetId = "custom_${System.currentTimeMillis()}"
            val entity = CustomPresetEntity(
                presetId = presetId,
                category = category,
                title = title,
                description = "Custom preset based on $beatHz Hz beat.",
                carrierFrequencyHz = carrierHz,
                beatFrequencyHz = beatHz,
                noiseType = noiseType,
                noiseVolume = noiseVolume,
                timerMinutes = timerMins,
                isFavorite = false,
                isCustom = true
            )
            db.customPresetDao().insertPreset(entity)
        }
    }

    fun deleteCustomPreset(presetId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(getApplication())
            db.customPresetDao().deletePresetById(presetId)
        }
    }

    fun addAlarm(presetId: String, title: String, category: String, hour: Int, minute: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(getApplication())
            val alarm = SessionAlarmEntity(
                presetId = presetId,
                presetTitle = title,
                presetCategory = category,
                hour = hour,
                minute = minute,
                isActive = true
            )
            val id = db.sessionAlarmDao().insertAlarm(alarm)
            AlarmScheduler.scheduleAlarm(getApplication(), alarm.copy(id = id.toInt()))
        }
    }

    fun deleteAlarm(alarm: SessionAlarmEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(getApplication())
            AlarmScheduler.cancelAlarm(getApplication(), alarm.id)
            db.sessionAlarmDao().deleteAlarm(alarm)
        }
    }

    fun toggleAlarmActive(alarm: SessionAlarmEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(getApplication())
            val updated = alarm.copy(isActive = !alarm.isActive)
            db.sessionAlarmDao().insertAlarm(updated)
            AlarmScheduler.scheduleAlarm(getApplication(), updated)
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(getApplication())
            db.sessionLogDao().clearAllLogs()
        }
    }

    // --- Statistics Helper Functions ---

    fun getTotalMinutesListened(): Int {
        return sessionLogs.value.sumOf { it.durationSeconds } / 60
    }

    fun getDailyStreak(): Int {
        val logs = sessionLogs.value.sortedByDescending { it.timestamp }
        if (logs.isEmpty()) return 0

        val dateFormat = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
        val uniqueDays = logs.map { dateFormat.format(java.util.Date(it.timestamp)) }.distinct()
        
        val todayStr = dateFormat.format(java.util.Date())
        val yesterdayStr = dateFormat.format(java.util.Date(System.currentTimeMillis() - 24L * 60L * 60L * 1000L))
        
        val latestDay = uniqueDays.firstOrNull() ?: return 0
        if (latestDay != todayStr && latestDay != yesterdayStr) return 0
        
        var streak = 1
        val sdf = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
        
        for (i in 0 until uniqueDays.size - 1) {
            val currentDayDate = sdf.parse(uniqueDays[i]) ?: break
            val nextDayDate = sdf.parse(uniqueDays[i + 1]) ?: break
            
            val diffMs = currentDayDate.time - nextDayDate.time
            val diffDays = (diffMs / (24L * 60L * 60L * 1000L)).toInt()
            
            if (diffDays == 1) {
                streak++
            } else if (diffDays > 1) {
                break
            }
        }
        return streak
    }

    fun getMinutesByCategory(category: String): Int {
        return sessionLogs.value
            .filter { it.category.equals(category, ignoreCase = true) }
            .sumOf { it.durationSeconds } / 60
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unbindService(serviceConnection)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
