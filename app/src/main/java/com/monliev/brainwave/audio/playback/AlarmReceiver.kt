package com.monliev.brainwave.audio.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.monliev.brainwave.audio.preset.PresetLoader
import com.monliev.brainwave.data.PresetRepository
import com.monliev.brainwave.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
  * AlarmReceiver is triggered by Android's AlarmManager at the scheduled Bedtime.
  * It retrieves the alarm specifications, loads the target preset JSON, and starts 
  * the AudioEngineService playback in the foreground.
  */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("alarm_id", -1)
        if (alarmId == -1) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val alarm = db.sessionAlarmDao().getAlarmById(alarmId)
                
                if (alarm != null && alarm.isActive) {
                    val presetRepo = PresetRepository(context)
                    val defaultPreset = presetRepo.getPresetById(alarm.presetId)
                    
                    val preset = if (defaultPreset != null) {
                        defaultPreset
                    } else {
                        val customPreset = db.customPresetDao().getPresetById(alarm.presetId)
                        customPreset?.toPreset()
                    }
                    
                    if (preset != null) {
                        val serviceIntent = Intent(context, AudioEngineService::class.java).apply {
                            action = AudioEngineService.ACTION_START
                            putExtra(AudioEngineService.EXTRA_PRESET_JSON, PresetLoader.encodePreset(preset))
                        }
                        ContextCompat.startForegroundService(context, serviceIntent)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
