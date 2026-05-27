package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val email: String,
    val passwordHash: String,
    val isLoggedIn: Boolean = false,
    val cycleLength: Int = 28, // typical/default cycle length
    val periodLength: Int = 5,  // typical/default period length
    val lastPeriodStartDate: String = "", // "yyyy-MM-dd"
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0,
    val waterGoalMl: Int = 2000,
    val isLocalEncryptionEnabled: Boolean = true,
    val isCloudSyncEnabled: Boolean = true
)

@Entity(tableName = "cycle_logs")
data class CycleLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val startDate: String, // "yyyy-MM-dd"
    val endDate: String = "", // "yyyy-MM-dd" (or empty if ongoing)
    val cycleLengthDays: Int = 28,
    val periodLengthDays: Int = 5
)

@Entity(tableName = "daily_logs")
data class DailyLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val date: String, // "yyyy-MM-dd"
    // Moods: Happy, Crampy, Sad, Anxious, Calm, Energetic, Tired
    val mood: String = "", 
    // Symptoms: Cramps, Headache, Bloating, Acne, None
    val symptom: String = "", 
    // Symptom Progression: None, Mild, Moderate, Severe
    val symptomSeverity: String = "None",
    val waterIntakeMl: Int = 0,
    val notes: String = ""
)

@Entity(tableName = "notifications")
data class InAppNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int, // User ID or -1 for global notifications from Admin
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "info" // "info", "alert", "period", "tip", "admin"
)
