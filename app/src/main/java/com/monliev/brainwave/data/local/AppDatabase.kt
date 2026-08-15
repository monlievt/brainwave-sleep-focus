package com.monliev.brainwave.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomPresetDao {
    @Query("SELECT * FROM custom_presets")
    fun getAllPresetsFlow(): Flow<List<CustomPresetEntity>>

    @Query("SELECT * FROM custom_presets WHERE presetId = :presetId LIMIT 1")
    fun getPresetById(presetId: String): CustomPresetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPreset(preset: CustomPresetEntity)

    @Delete
    fun deletePreset(preset: CustomPresetEntity)
    
    @Query("DELETE FROM custom_presets WHERE presetId = :presetId")
    fun deletePresetById(presetId: String)
}

@Dao
interface SessionLogDao {
    @Query("SELECT * FROM session_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<SessionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLog(log: SessionLogEntity)

    @Query("DELETE FROM session_logs")
    fun clearAllLogs()
}

@Dao
interface SessionAlarmDao {
    @Query("SELECT * FROM session_alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarmsFlow(): Flow<List<SessionAlarmEntity>>

    @Query("SELECT * FROM session_alarms WHERE id = :id LIMIT 1")
    fun getAlarmById(id: Int): SessionAlarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAlarm(alarm: SessionAlarmEntity): Long

    @Query("UPDATE session_alarms SET isActive = :isActive WHERE id = :id")
    fun updateAlarmStatus(id: Int, isActive: Boolean)

    @Delete
    fun deleteAlarm(alarm: SessionAlarmEntity)
}

@Database(entities = [CustomPresetEntity::class, SessionLogEntity::class, SessionAlarmEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customPresetDao(): CustomPresetDao
    abstract fun sessionLogDao(): SessionLogDao
    abstract fun sessionAlarmDao(): SessionAlarmDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "brainwave_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
