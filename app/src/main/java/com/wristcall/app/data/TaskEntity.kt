package com.wristcall.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database Entity representing a phone call task delegated from Wear OS smartwatch.
 */
@Entity(tableName = "task_history")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val targetNumber: String,
    val taskPrompt: String,
    val status: String,
    val summary: String,
    val transcript: String,
    val timestamp: Long = System.currentTimeMillis()
)
