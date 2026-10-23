package com.example.ui

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SafeLockDatabase
import com.example.data.local.SafeLockPreferences
import com.example.data.model.ApprovedApps
import com.example.data.model.DeviceCommand
import com.example.data.model.DeviceEnforcementState
import com.example.data.model.InstalledAppInfo
import com.example.data.model.PairingRequest
import com.example.data.model.Schedule
import com.example.data.model.Student
import com.example.data.model.StudentRequest
import com.example.data.remote.SafeLockRepository
import com.example.policy.AppLauncherHelper
import com.example.policy.DeviceRestrictionManager
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = SafeLockDatabase.getInstance(application)
    private val preferences = SafeLockPreferences(application)
    val appLauncherHelper = AppLauncherHelper(application)
    val restrictionManager = DeviceRestrictionManager(application)
    private val repository = SafeLockRepository(application, db.dao())

    val studentIdFlow: StateFlow<String?> = preferences.studentIdFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val onboardingCompletedFlow: StateFlow<Boolean> = preferences.onboardingCompletedFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val wallpaperPresetFlow: StateFlow<String> = preferences.wallpaperPresetFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "cyber_dark")

    val wallpaperCustomUriFlow: StateFlow<String?> = preferences.wallpaperCustomUriFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _student = MutableStateFlow<Student?>(null)
    val student: StateFlow<Student?> = _student.asStateFlow()

    private val _pairingRequests = MutableStateFlow<List<PairingRequest>>(emptyList())
    val pairingRequests: StateFlow<List<PairingRequest>> = _pairingRequests.asStateFlow()

    private val _activeSchedule = MutableStateFlow<Schedule?>(null)
    val activeSchedule: StateFlow<Schedule?> = _activeSchedule.asStateFlow()

    private val _approvedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val approvedApps: StateFlow<List<InstalledAppInfo>> = _approvedApps.asStateFlow()

    private val _studentRequests = MutableStateFlow<List<StudentRequest>>(emptyList())
    val studentRequests: StateFlow<List<StudentRequest>> = _studentRequests.asStateFlow()

    private val _enforcementState = MutableStateFlow(DeviceEnforcementState.NORMAL)
    val enforcementState: StateFlow<DeviceEnforcementState> = _enforcementState.asStateFlow()

    private val _allInstalledApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val allInstalledApps: StateFlow<List<InstalledAppInfo>> = _allInstalledApps.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val studentId = preferences.getOrCreateStudentId()
            refreshInstalledApps(emptySet())

            // Observe preferences
            preferences.onboardingCompletedFlow.collect { completed ->
                if (completed && Firebase.auth.currentUser != null) {
                    startObservingStudentData(studentId)
                }
            }
        }
    }

    fun onUserAuthenticated() {
        viewModelScope.launch {
            val studentId = preferences.getOrCreateStudentId()
            val isOnboardingDone = preferences.getOrCreateStudentId().isNotBlank() && onboardingCompletedFlow.value
            if (isOnboardingDone) {
                startObservingStudentData(studentId)
            }
        }
    }

    fun refreshInstalledApps(approvedPkgs: Set<String>) {
        val apps = appLauncherHelper.getInstalledLauncherApps(approvedPkgs)
        _allInstalledApps.value = apps
    }

    fun startObservingStudentData(studentId: String) {
        viewModelScope.launch {
            // 1. Observe Student profile
            launch {
                repository.observeStudent(studentId).collect { s ->
                    _student.value = s
                    if (s != null && !s.activeScheduleId.isNullOrBlank()) {
                        observeSchedule(s.activeScheduleId)
                    }
                    updateEnforcementState()
                }
            }

            // 2. Observe Approved Applications
            launch {
                repository.observeApprovedApps(studentId).collect { approved ->
                    val pkgs = approved?.packages?.toSet() ?: emptySet()
                    refreshInstalledApps(pkgs)
                    _approvedApps.value = _allInstalledApps.value.filter { it.isApproved }
                    updateEnforcementState()
                }
            }

            // 3. Observe Pairing Requests
            launch {
                repository.observePairingRequests(studentId).collect { reqs ->
                    _pairingRequests.value = reqs
                }
            }

            // 4. Observe Student Requests
            launch {
                repository.observeStudentRequests(studentId).collect { reqs ->
                    _studentRequests.value = reqs
                }
            }

            // 5. Observe Remote Device Commands & Acknowledge
            launch {
                repository.observePendingCommands(studentId).collect { commands ->
                    for (cmd in commands) {
                        handleRemoteCommand(cmd)
                    }
                }
            }
        }
    }

    private fun observeSchedule(scheduleId: String) {
        viewModelScope.launch {
            repository.observeSchedule(scheduleId).collect { sch ->
                _activeSchedule.value = sch
                updateEnforcementState()
            }
        }
    }

    fun updateEnforcementState() {
        val s = _student.value
        val state = restrictionManager.evaluateEnforcementState(
            isLockedManually = s?.isLockedManually ?: false,
            emergencyUnlockUntil = s?.emergencyUnlockUntil,
            activeSchedule = _activeSchedule.value
        )
        _enforcementState.value = state
    }

    private suspend fun handleRemoteCommand(command: DeviceCommand) {
        try {
            when (command.commandType) {
                "lock_now" -> {
                    _enforcementState.value = DeviceEnforcementState.MANUAL_LOCKED
                }
                "unlock" -> {
                    _enforcementState.value = DeviceEnforcementState.NORMAL
                }
                "emergency_unlock" -> {
                    _enforcementState.value = DeviceEnforcementState.EMERGENCY_UNLOCKED
                }
            }
            repository.acknowledgeCommand(command.commandId, appliedSuccessfully = true)
        } catch (e: Exception) {
            Log.e("MainViewModel", "Failed to apply command ${command.commandId}", e)
            repository.acknowledgeCommand(command.commandId, appliedSuccessfully = false)
        }
    }

    fun completeOnboarding(
        studentName: String,
        scheduleJson: String,
        desiredAppPackages: List<String>,
        wallpaperPresetId: String
    ) {
        viewModelScope.launch {
            val studentId = preferences.getOrCreateStudentId()
            val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"

            // 1. Propose schedule
            val scheduleId = repository.proposeSchedule(
                studentId = studentId,
                name = "Standard Schedule",
                daysOfWeek = listOf(1, 2, 3, 4, 5),
                periodsJson = scheduleJson
            )

            // 2. Register student with activeScheduleId
            val student = repository.registerOrGetStudent(
                studentId = studentId,
                studentName = studentName,
                deviceModel = deviceModel
            )
            _student.value = student

            // 3. Submit desired apps request if any selected
            if (desiredAppPackages.isNotEmpty()) {
                repository.submitStudentRequest(
                    studentId = studentId,
                    type = "app_permission",
                    details = "Initial setup request for ${desiredAppPackages.size} apps: ${desiredAppPackages.joinToString(", ")}"
                )
            }

            // 4. Save local preferences
            preferences.saveWallpaperPreset(wallpaperPresetId)
            preferences.setOnboardingCompleted(true)

            // Start observing real-time
            startObservingStudentData(studentId)
        }
    }

    fun respondToPairingRequest(request: PairingRequest, approve: Boolean) {
        viewModelScope.launch {
            try {
                repository.respondToPairingRequest(request, approve)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error responding to pairing request", e)
            }
        }
    }

    fun submitStudentRequest(type: String, details: String) {
        viewModelScope.launch {
            val studentId = preferences.getOrCreateStudentId()
            try {
                repository.submitStudentRequest(studentId, type, details)
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error submitting student request", e)
            }
        }
    }

    fun saveWallpaperPreset(presetId: String) {
        viewModelScope.launch {
            preferences.saveWallpaperPreset(presetId)
        }
    }

    fun saveCustomWallpaperUri(uri: String) {
        viewModelScope.launch {
            preferences.saveCustomWallpaperUri(uri)
        }
    }

    fun launchApp(packageName: String): Boolean {
        // Enforce restriction rules
        val state = _enforcementState.value
        val isEssential = appLauncherHelper.isEssentialSystemApp(packageName)

        if ((state == DeviceEnforcementState.SCHEDULE_RESTRICTED || state == DeviceEnforcementState.MANUAL_LOCKED) && !isEssential) {
            return false
        }
        return appLauncherHelper.launchApp(packageName)
    }

    fun launchEmergencyCall() {
        appLauncherHelper.launchEmergencyDialer()
    }
}
