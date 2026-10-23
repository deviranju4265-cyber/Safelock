package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.model.DeviceEnforcementState
import com.example.ui.MainViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.device.DeviceAdminSetupDialog
import com.example.ui.home.HomeScreen
import com.example.ui.onboarding.OnboardingScreen
import com.example.ui.requests.RequestsScreen
import com.example.ui.theme.SafeLockBackground
import com.example.ui.theme.SafeLockTheme
import com.example.ui.wallpaper.WallpaperPickerScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

enum class CurrentScreen {
    HOME,
    REQUESTS,
    WALLPAPER
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SafeLockTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = SafeLockBackground
                ) {
                    AppRoot(
                        viewModel = viewModel,
                        activity = this
                    )
                }
            }
        }
    }
}

@Composable
fun AppRoot(
    viewModel: MainViewModel,
    activity: ComponentActivity
) {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            if (auth.currentUser != null) {
                viewModel.onUserAuthenticated()
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (currentUser == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
                viewModel.onUserAuthenticated()
            }
        )
    } else {
        SafeLockApp(
            viewModel = viewModel,
            activity = activity
        )
    }
}

@Composable
fun SafeLockApp(
    viewModel: MainViewModel,
    activity: ComponentActivity
) {
    val studentId by viewModel.studentIdFlow.collectAsState()
    val onboardingCompleted by viewModel.onboardingCompletedFlow.collectAsState()
    val student by viewModel.student.collectAsState()
    val enforcementState by viewModel.enforcementState.collectAsState()
    val approvedApps by viewModel.approvedApps.collectAsState()
    val allInstalledApps by viewModel.allInstalledApps.collectAsState()
    val pairingRequests by viewModel.pairingRequests.collectAsState()
    val studentRequests by viewModel.studentRequests.collectAsState()
    val wallpaperPresetId by viewModel.wallpaperPresetFlow.collectAsState()
    val wallpaperCustomUri by viewModel.wallpaperCustomUriFlow.collectAsState()

    var currentScreen by remember { mutableStateOf(CurrentScreen.HOME) }
    var showDeviceAdminDialog by remember { mutableStateOf(false) }

    val isAdminActive = remember { viewModel.restrictionManager.isDeviceAdminActive() }
    val isDeviceOwner = remember { viewModel.restrictionManager.isDeviceOwner() }

    // Lock Task mode enforcement during manual or schedule restriction
    LaunchedEffect(enforcementState) {
        if (enforcementState == DeviceEnforcementState.MANUAL_LOCKED) {
            viewModel.restrictionManager.startLockTaskMode(activity)
        } else if (enforcementState == DeviceEnforcementState.EMERGENCY_UNLOCKED) {
            viewModel.restrictionManager.stopLockTaskMode(activity)
        }
    }

    if (!onboardingCompleted) {
        OnboardingScreen(
            studentId = studentId ?: "SL-A1B2C",
            installedApps = allInstalledApps,
            onComplete = { name, scheduleJson, selectedPkgs, wallpaperId ->
                viewModel.completeOnboarding(
                    studentName = name,
                    scheduleJson = scheduleJson,
                    desiredAppPackages = selectedPkgs,
                    wallpaperPresetId = wallpaperId
                )
            }
        )
    } else {
        when (currentScreen) {
            CurrentScreen.HOME -> {
                HomeScreen(
                    student = student,
                    enforcementState = enforcementState,
                    approvedApps = approvedApps,
                    pairingRequests = pairingRequests,
                    wallpaperPresetId = wallpaperPresetId,
                    wallpaperCustomUri = wallpaperCustomUri,
                    pendingRequestsCount = studentRequests.count { it.status == "pending" },
                    isDeviceAdminActive = isAdminActive,
                    onLaunchApp = { pkg ->
                        val launched = viewModel.launchApp(pkg)
                        if (!launched) {
                            Toast.makeText(
                                activity,
                                "Application is restricted during this period. Submit a temporary unlock request.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    onEmergencyCall = {
                        viewModel.launchEmergencyCall()
                    },
                    onOpenRequests = { currentScreen = CurrentScreen.REQUESTS },
                    onOpenWallpaper = { currentScreen = CurrentScreen.WALLPAPER },
                    onOpenDeviceAdminDialog = { showDeviceAdminDialog = true },
                    onRespondPairingRequest = { req, approve ->
                        viewModel.respondToPairingRequest(req, approve)
                    }
                )
            }

            CurrentScreen.REQUESTS -> {
                RequestsScreen(
                    requests = studentRequests,
                    installedApps = allInstalledApps,
                    studentId = studentId ?: "",
                    onBack = { currentScreen = CurrentScreen.HOME },
                    onSubmitRequest = { type, details ->
                        viewModel.submitStudentRequest(type, details)
                        Toast.makeText(activity, "Request submitted to parent for approval", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            CurrentScreen.WALLPAPER -> {
                WallpaperPickerScreen(
                    currentPresetId = wallpaperPresetId,
                    currentCustomUri = wallpaperCustomUri,
                    onBack = { currentScreen = CurrentScreen.HOME },
                    onSavePreset = { id -> viewModel.saveWallpaperPreset(id) },
                    onSaveCustomUri = { uri -> viewModel.saveCustomWallpaperUri(uri) }
                )
            }
        }

        if (showDeviceAdminDialog) {
            DeviceAdminSetupDialog(
                isAdminActive = viewModel.restrictionManager.isDeviceAdminActive(),
                isDeviceOwner = viewModel.restrictionManager.isDeviceOwner(),
                onDismiss = { showDeviceAdminDialog = false }
            )
        }
    }
}
