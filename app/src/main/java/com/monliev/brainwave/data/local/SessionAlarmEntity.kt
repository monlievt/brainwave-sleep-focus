package com.monliev.brainwave.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_alarms")
data class SessionAlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val presetId: String,
    val presetTitle: String,
    val presetCategory: String,
    val hour: Int,
    val minute: Int,
    val isActive: Boolean = true,
    val daysOfWeek: String = "" // Comma-separated indices "1,2,3,4,5" or empty for once
)
