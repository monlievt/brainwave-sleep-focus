package com.monliev.brainwave.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session_logs")
data class SessionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val presetTitle: String,
    val category: String,
    val timestamp: Long,
    val durationSeconds: Int
)
