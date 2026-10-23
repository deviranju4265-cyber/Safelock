package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SafeLockDatabase
import com.example.data.local.SafeLockPreferences
import com.example.data.local.StudentEntity
import com.example.data.model.DeviceEnforcementState
import com.example.data.model.Schedule
import com.example.policy.DeviceRestrictionManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun readStringFromContext() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("SafeLock", appName)
    }

    @Test
    fun studentIdFormatValidation() {
        val id = SafeLockPreferences.generateStudentId()
        assertTrue("Student ID should start with SL-", id.startsWith("SL-"))
        assertEquals("Student ID should have length 8 (SL-XXXXX)", 8, id.length)
        assertTrue("Student ID should match regex", id.matches(Regex("^SL-[A-Z0-9]{5}$")))
    }

    @Test
    fun schedulePeriodParsingAndEvaluation() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = DeviceRestrictionManager(context)

        val periodsJson = """[{"name":"School","startTime":"08:00","endTime":"15:00","isRestricted":true}]"""
        val periods = manager.parsePeriodsJson(periodsJson)

        assertEquals(1, periods.size)
        assertEquals("School", periods[0].name)
        assertEquals("08:00", periods[0].startTime)
        assertEquals("15:00", periods[0].endTime)
        assertTrue(periods[0].isRestricted)
    }

    @Test
    fun enforcementStateManualLockPrecedence() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = DeviceRestrictionManager(context)

        val state = manager.evaluateEnforcementState(
            isLockedManually = true,
            emergencyUnlockUntil = null,
            activeSchedule = null
        )

        assertEquals(DeviceEnforcementState.MANUAL_LOCKED, state)
    }

    @Test
    fun localRoomDatabaseStorage() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = SafeLockDatabase.getInstance(context)
        val dao = db.dao()

        val entity = StudentEntity(
            studentId = "SL-TEST1",
            userId = "user_123",
            studentName = "Test Student",
            deviceModel = "Pixel",
            pairingStatus = "unpaired",
            parentUidsCsv = "",
            activeScheduleId = null,
            isLockedManually = false,
            emergencyUnlockUntilEpoch = null,
            lastSyncTimeEpoch = System.currentTimeMillis()
        )

        dao.saveStudent(entity)
        val retrieved = dao.getStudent()

        assertNotNull(retrieved)
        assertEquals("SL-TEST1", retrieved?.studentId)
        assertEquals("Test Student", retrieved?.studentName)
    }
}
