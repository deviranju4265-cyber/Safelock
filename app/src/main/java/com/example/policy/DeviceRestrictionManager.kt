package com.example.policy

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.example.data.model.DeviceEnforcementState
import com.example.data.model.Schedule
import com.example.data.model.TimePeriod
import com.example.receiver.SafeLockDeviceAdminReceiver
import com.google.firebase.Timestamp
import org.json.JSONArray
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class DeviceRestrictionManager(private val context: Context) {

    private val dpm: DevicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val adminComponent = ComponentName(context, SafeLockDeviceAdminReceiver::class.java)

    fun isDeviceAdminActive(): Boolean {
        return dpm.isAdminActive(adminComponent)
    }

    fun isDeviceOwner(): Boolean {
        return dpm.isDeviceOwnerApp(context.packageName)
    }

    fun startLockTaskMode(activity: Activity) {
        try {
            activity.startLockTask()
        } catch (e: Exception) {
            Log.w("RestrictionManager", "Cannot start lock task mode: ${e.message}")
        }
    }

    fun stopLockTaskMode(activity: Activity) {
        try {
            activity.stopLockTask()
        } catch (e: Exception) {
            Log.w("RestrictionManager", "Cannot stop lock task mode: ${e.message}")
        }
    }

    fun evaluateEnforcementState(
        isLockedManually: Boolean,
        emergencyUnlockUntil: Timestamp?,
        activeSchedule: Schedule?
    ): DeviceEnforcementState {
        val now = Timestamp.now()

        // 1. Emergency unlock takes precedence
        if (emergencyUnlockUntil != null && emergencyUnlockUntil.seconds > now.seconds) {
            return DeviceEnforcementState.EMERGENCY_UNLOCKED
        }

        // 2. Manual parent lock takes precedence over normal schedule
        if (isLockedManually) {
            return DeviceEnforcementState.MANUAL_LOCKED
        }

        // 3. Evaluate schedule
        if (activeSchedule != null && activeSchedule.isApproved) {
            val isRestricted = isCurrentTimeRestricted(activeSchedule)
            if (isRestricted) {
                return DeviceEnforcementState.SCHEDULE_RESTRICTED
            }
        }

        return DeviceEnforcementState.NORMAL
    }

    fun isCurrentTimeRestricted(schedule: Schedule): Boolean {
        return try {
            val today = LocalDate.now().dayOfWeek.value // 1=Mon .. 7=Sun
            if (!schedule.daysOfWeek.contains(today)) {
                return false
            }

            val periods = parsePeriodsJson(schedule.periodsJson)
            val currentTime = LocalTime.now()

            for (period in periods) {
                if (!period.isRestricted) continue

                val start = LocalTime.parse(period.startTime, DateTimeFormatter.ofPattern("HH:mm"))
                val end = LocalTime.parse(period.endTime, DateTimeFormatter.ofPattern("HH:mm"))

                if (start.isBefore(end)) {
                    // Standard period within the same day, e.g. 08:00 to 15:00
                    if (!currentTime.isBefore(start) && currentTime.isBefore(end)) {
                        return true
                    }
                } else {
                    // Overnight period, e.g. 21:00 to 07:00
                    if (!currentTime.isBefore(start) || currentTime.isBefore(end)) {
                        return true
                    }
                }
            }
            false
        } catch (e: Exception) {
            Log.e("RestrictionManager", "Error parsing schedule periods", e)
            false
        }
    }

    fun parsePeriodsJson(jsonString: String): List<TimePeriod> {
        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<TimePeriod>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    TimePeriod(
                        name = obj.optString("name", "Restricted"),
                        startTime = obj.optString("startTime", "08:00"),
                        endTime = obj.optString("endTime", "15:00"),
                        isRestricted = obj.optBoolean("isRestricted", true)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
