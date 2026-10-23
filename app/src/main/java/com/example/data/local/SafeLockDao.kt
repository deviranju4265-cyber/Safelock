package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SafeLockDao {

    @Query("SELECT * FROM local_student LIMIT 1")
    fun observeStudent(): Flow<StudentEntity?>

    @Query("SELECT * FROM local_student LIMIT 1")
    suspend fun getStudent(): StudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStudent(student: StudentEntity)

    @Query("SELECT * FROM local_schedules WHERE studentId = :studentId")
    fun observeSchedules(studentId: String): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM local_schedules WHERE scheduleId = :scheduleId LIMIT 1")
    suspend fun getSchedule(scheduleId: String): ScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSchedules(schedules: List<ScheduleEntity>)

    @Query("SELECT * FROM local_approved_apps WHERE studentId = :studentId")
    fun observeApprovedApps(studentId: String): Flow<List<ApprovedAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveApprovedApps(apps: List<ApprovedAppEntity>)

    @Query("DELETE FROM local_approved_apps WHERE studentId = :studentId")
    suspend fun clearApprovedApps(studentId: String)

    @Query("SELECT * FROM local_requests WHERE studentId = :studentId ORDER BY createdAtEpoch DESC")
    fun observeCachedRequests(studentId: String): Flow<List<CachedRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveRequests(requests: List<CachedRequestEntity>)

    @Query("DELETE FROM local_student")
    suspend fun clearStudent()
}
