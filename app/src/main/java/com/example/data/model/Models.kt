package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Student(
    @DocumentId val id: String = "",
    val studentId: String = "",
    val userId: String = "",
    val studentName: String = "",
    val deviceModel: String = "",
    val pairingStatus: String = "unpaired", // unpaired, pending_approval, paired
    val parentUids: List<String> = emptyList(),
    val activeScheduleId: String? = null,
    val isLockedManually: Boolean = false,
    val emergencyUnlockUntil: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PairingRequest(
    @DocumentId val id: String = "",
    val requestId: String = "",
    val studentId: String = "",
    val parentId: String = "",
    val parentName: String = "",
    val status: String = "pending", // pending, approved, rejected, expired
    @ServerTimestamp val createdAt: Timestamp? = null,
    val expiresAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class TimePeriod(
    val name: String = "",
    val startTime: String = "08:00", // HH:mm
    val endTime: String = "15:00",   // HH:mm
    val isRestricted: Boolean = true
)

@IgnoreExtraProperties
data class Schedule(
    @DocumentId val id: String = "",
    val scheduleId: String = "",
    val studentId: String = "",
    val name: String = "School & Bedtime",
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5), // 1=Mon .. 7=Sun
    val periodsJson: String = "[]",
    val isApproved: Boolean = false,
    val proposedBy: String = "student",
    val status: String = "draft", // active, draft, proposed
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ApprovedApps(
    @DocumentId val id: String = "",
    val studentId: String = "",
    val packages: List<String> = emptyList(),
    val appNames: List<String> = emptyList(),
    val updatedBy: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StudentRequest(
    @DocumentId val id: String = "",
    val requestId: String = "",
    val studentId: String = "",
    val userId: String = "",
    val type: String = "app_permission", // app_permission, schedule_change, emergency_unlock
    val details: String = "",
    val status: String = "pending", // pending, approved, rejected
    val parentDecisionNote: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class DeviceCommand(
    @DocumentId val id: String = "",
    val commandId: String = "",
    val studentId: String = "",
    val parentId: String = "",
    val commandType: String = "", // lock_now, unlock, emergency_unlock, update_apps, update_schedule
    val payload: String? = null,
    val status: String = "pending", // pending, delivered, executing, applied, failed, expired
    @ServerTimestamp val createdAt: Timestamp? = null,
    val executedAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class InstalledAppInfo(
    val packageName: String,
    val appName: String,
    val isApproved: Boolean = false,
    val isSystemEssential: Boolean = false
)

enum class DeviceEnforcementState {
    NORMAL,
    SCHEDULE_RESTRICTED,
    MANUAL_LOCKED,
    EMERGENCY_UNLOCKED
}
