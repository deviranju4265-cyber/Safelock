package com.example.data.remote

import android.content.Context
import com.example.R
import com.example.data.local.ApprovedAppEntity
import com.example.data.local.CachedRequestEntity
import com.example.data.local.SafeLockDao
import com.example.data.local.ScheduleEntity
import com.example.data.local.StudentEntity
import com.example.data.model.ApprovedApps
import com.example.data.model.DeviceCommand
import com.example.data.model.PairingRequest
import com.example.data.model.Schedule
import com.example.data.model.Student
import com.example.data.model.StudentRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SafeLockRepository(
    private val db: FirebaseFirestore,
    private val dao: SafeLockDao
) {
    constructor(context: Context, dao: SafeLockDao) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        dao
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")
    }

    suspend fun registerOrGetStudent(
        studentId: String,
        studentName: String,
        deviceModel: String
    ): Student {
        val uid = requireUserId()
        val docRef = db.collection("students").document(studentId)

        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val student = snapshot.toObject(Student::class.java) ?: Student(
                    id = studentId,
                    studentId = studentId,
                    userId = uid,
                    studentName = studentName,
                    deviceModel = deviceModel
                )
                saveStudentToLocal(student)
                student
            } else {
                val newStudent = Student(
                    id = studentId,
                    studentId = studentId,
                    userId = uid,
                    studentName = studentName,
                    deviceModel = deviceModel,
                    pairingStatus = "unpaired",
                    parentUids = emptyList(),
                    isLockedManually = false
                )
                val payload = mapOf(
                    "studentId" to studentId,
                    "userId" to uid,
                    "studentName" to studentName,
                    "deviceModel" to deviceModel,
                    "pairingStatus" to "unpaired",
                    "parentUids" to emptyList<String>(),
                    "activeScheduleId" to null,
                    "isLockedManually" to false,
                    "emergencyUnlockUntil" to null,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
                saveStudentToLocal(newStudent)
                newStudent
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            // Fallback to local cache if offline
            val local = dao.getStudent()
            if (local != null) {
                Student(
                    id = local.studentId,
                    studentId = local.studentId,
                    userId = local.userId,
                    studentName = local.studentName,
                    deviceModel = local.deviceModel,
                    pairingStatus = local.pairingStatus,
                    parentUids = if (local.parentUidsCsv.isBlank()) emptyList() else local.parentUidsCsv.split(","),
                    activeScheduleId = local.activeScheduleId,
                    isLockedManually = local.isLockedManually
                )
            } else {
                throw e
            }
        }
    }

    fun observeStudent(studentId: String): Flow<Student?> = flow {
        val docRef = db.collection("students").document(studentId)
        val snapshotFlow = docRef.snapshots().map { snapshot ->
            if (snapshot.exists()) {
                val student = snapshot.toObject(Student::class.java)
                if (student != null) {
                    saveStudentToLocal(student)
                }
                student
            } else {
                null
            }
        }.catch { error ->
            if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
            // Emit locally cached state when offline
            val local = dao.getStudent()
            if (local != null) {
                emit(
                    Student(
                        id = local.studentId,
                        studentId = local.studentId,
                        userId = local.userId,
                        studentName = local.studentName,
                        deviceModel = local.deviceModel,
                        pairingStatus = local.pairingStatus,
                        parentUids = if (local.parentUidsCsv.isBlank()) emptyList() else local.parentUidsCsv.split(","),
                        activeScheduleId = local.activeScheduleId,
                        isLockedManually = local.isLockedManually
                    )
                )
            } else {
                throw error
            }
        }
        emitAll(snapshotFlow)
    }

    fun observePairingRequests(studentId: String): Flow<List<PairingRequest>> {
        val path = "pairing_requests"
        return db.collection(path)
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("status", "pending")
            .snapshots()
            .map { it.toObjects(PairingRequest::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                emit(emptyList())
            }
    }

    suspend fun respondToPairingRequest(
        request: PairingRequest,
        approve: Boolean
    ) {
        requireUserId()
        val reqRef = db.collection("pairing_requests").document(request.id)
        val studentRef = db.collection("students").document(request.studentId)

        try {
            val newStatus = if (approve) "approved" else "rejected"
            reqRef.update(
                mapOf(
                    "status" to newStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            if (approve) {
                studentRef.update(
                    mapOf(
                        "pairingStatus" to "paired",
                        "parentUids" to FieldValue.arrayUnion(request.parentId),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, reqRef.path)
            throw e
        }
    }

    fun observeApprovedApps(studentId: String): Flow<ApprovedApps?> {
        val docRef = db.collection("approved_apps").document(studentId)
        return docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    val apps = snapshot.toObject(ApprovedApps::class.java)
                    if (apps != null) {
                        saveApprovedAppsToLocal(apps)
                    }
                    apps
                } else {
                    null
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
                emit(null)
            }
    }

    fun observeSchedule(scheduleId: String): Flow<Schedule?> {
        val docRef = db.collection("schedules").document(scheduleId)
        return docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    val s = snapshot.toObject(Schedule::class.java)
                    if (s != null) {
                        saveScheduleToLocal(s)
                    }
                    s
                } else {
                    null
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.GET, docRef.path)
                emit(null)
            }
    }

    suspend fun proposeSchedule(
        studentId: String,
        name: String,
        daysOfWeek: List<Int>,
        periodsJson: String
    ): String {
        requireUserId()
        val scheduleId = "sch_${UUID.randomUUID().toString().take(8)}"
        val docRef = db.collection("schedules").document(scheduleId)

        val payload = mapOf(
            "scheduleId" to scheduleId,
            "studentId" to studentId,
            "name" to name,
            "daysOfWeek" to daysOfWeek,
            "periodsJson" to periodsJson,
            "isApproved" to false,
            "proposedBy" to "student",
            "status" to "proposed",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
            return scheduleId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun submitStudentRequest(
        studentId: String,
        type: String,
        details: String
    ): String {
        val uid = requireUserId()
        val requestId = "req_${UUID.randomUUID().toString().take(8)}"
        val docRef = db.collection("student_requests").document(requestId)

        val payload = mapOf(
            "requestId" to requestId,
            "studentId" to studentId,
            "userId" to uid,
            "type" to type,
            "details" to details,
            "status" to "pending",
            "parentDecisionNote" to null,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
            dao.saveRequests(
                listOf(
                    CachedRequestEntity(
                        requestId = requestId,
                        studentId = studentId,
                        type = type,
                        details = details,
                        status = "pending",
                        createdAtEpoch = System.currentTimeMillis()
                    )
                )
            )
            return requestId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    fun observeStudentRequests(studentId: String): Flow<List<StudentRequest>> {
        val path = "student_requests"
        return db.collection(path)
            .whereEqualTo("studentId", studentId)
            .snapshots()
            .map { snapshot ->
                val requests = snapshot.toObjects(StudentRequest::class.java)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
                val entities = requests.map { r ->
                    CachedRequestEntity(
                        requestId = r.requestId,
                        studentId = r.studentId,
                        type = r.type,
                        details = r.details,
                        status = r.status,
                        createdAtEpoch = (r.createdAt?.seconds ?: 0L) * 1000
                    )
                }
                dao.saveRequests(entities)
                requests
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                emit(emptyList())
            }
    }

    fun observePendingCommands(studentId: String): Flow<List<DeviceCommand>> {
        val path = "device_commands"
        return db.collection(path)
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("status", "pending")
            .snapshots()
            .map { it.toObjects(DeviceCommand::class.java) }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                emit(emptyList())
            }
    }

    suspend fun acknowledgeCommand(commandId: String, appliedSuccessfully: Boolean) {
        val docRef = db.collection("device_commands").document(commandId)
        val status = if (appliedSuccessfully) "applied" else "failed"
        try {
            docRef.update(
                mapOf(
                    "status" to status,
                    "executedAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
        }
    }

    private suspend fun saveStudentToLocal(student: Student) {
        dao.saveStudent(
            StudentEntity(
                studentId = student.studentId,
                userId = student.userId,
                studentName = student.studentName,
                deviceModel = student.deviceModel,
                pairingStatus = student.pairingStatus,
                parentUidsCsv = student.parentUids.joinToString(","),
                activeScheduleId = student.activeScheduleId,
                isLockedManually = student.isLockedManually,
                emergencyUnlockUntilEpoch = student.emergencyUnlockUntil?.seconds?.times(1000),
                lastSyncTimeEpoch = System.currentTimeMillis()
            )
        )
    }

    private suspend fun saveApprovedAppsToLocal(apps: ApprovedApps) {
        dao.clearApprovedApps(apps.studentId)
        val entities = apps.packages.mapIndexed { idx, pkg ->
            val label = apps.appNames.getOrNull(idx) ?: pkg
            ApprovedAppEntity(
                packageName = pkg,
                appName = label,
                studentId = apps.studentId,
                isApproved = true
            )
        }
        dao.saveApprovedApps(entities)
    }

    private suspend fun saveScheduleToLocal(schedule: Schedule) {
        dao.saveSchedules(
            listOf(
                ScheduleEntity(
                    scheduleId = schedule.scheduleId,
                    studentId = schedule.studentId,
                    name = schedule.name,
                    daysOfWeekCsv = schedule.daysOfWeek.joinToString(","),
                    periodsJson = schedule.periodsJson,
                    isApproved = schedule.isApproved,
                    status = schedule.status,
                    updatedAtEpoch = System.currentTimeMillis()
                )
            )
        )
    }
}
