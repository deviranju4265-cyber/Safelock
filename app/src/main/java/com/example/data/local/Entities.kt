package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_student")
data class StudentEntity(
    @PrimaryKey val studentId: String,
    val userId: String,
    val studentName: String,
    val deviceModel: String,
    val pairingStatus: String,
    val parentUidsCsv: String,
    val activeScheduleId: String?,
    val isLockedManually: Boolean,
    val emergencyUnlockUntilEpoch: Long?,
    val lastSyncTimeEpoch: Long
)

@Entity(tableName = "local_schedules")
data class ScheduleEntity(
    @PrimaryKey val scheduleId: String,
    val studentId: String,
    val name: String,
    val daysOfWeekCsv: String,
    val periodsJson: String,
    val isApproved: Boolean,
    val status: String,
    val updatedAtEpoch: Long
)

@Entity(tableName = "local_approved_apps")
data class ApprovedAppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val studentId: String,
    val isApproved: Boolean
)

@Entity(tableName = "local_requests")
data class CachedRequestEntity(
    @PrimaryKey val requestId: String,
    val studentId: String,
    val type: String,
    val details: String,
    val status: String,
    val createdAtEpoch: Long
)
