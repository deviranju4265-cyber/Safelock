package com.example.ui.requests

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InstalledAppInfo
import com.example.data.model.StudentRequest
import com.example.ui.theme.SafeLockAmber
import com.example.ui.theme.SafeLockBackground
import com.example.ui.theme.SafeLockBorder
import com.example.ui.theme.SafeLockCard
import com.example.ui.theme.SafeLockCyan
import com.example.ui.theme.SafeLockEmerald
import com.example.ui.theme.SafeLockIndigo
import com.example.ui.theme.SafeLockRose
import com.example.ui.theme.SafeLockSurface
import com.example.ui.theme.SafeLockTextMuted
import com.example.ui.theme.SafeLockTextPrimary
import com.example.ui.theme.SafeLockTextSecondary
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(
    requests: List<StudentRequest>,
    installedApps: List<InstalledAppInfo>,
    studentId: String,
    onBack: () -> Unit,
    onSubmitRequest: (type: String, details: String) -> Unit
) {
    BackHandler { onBack() }

    var selectedTab by remember { mutableIntStateOf(0) } // 0=All, 1=Pending, 2=Approved, 3=Rejected
    var showCreateDialog by remember { mutableStateOf(false) }

    val filteredRequests = remember(requests, selectedTab) {
        when (selectedTab) {
            1 -> requests.filter { it.status == "pending" }
            2 -> requests.filter { it.status == "approved" }
            3 -> requests.filter { it.status == "rejected" }
            else -> requests
        }
    }

    Scaffold(
        containerColor = SafeLockBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SafeLockTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Student Requests",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafeLockTextPrimary
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = SafeLockCyan,
                contentColor = Color(0xFF021B2B),
                shape = CircleShape,
                modifier = Modifier.testTag("new_request_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Request")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SafeLockSurface,
                contentColor = SafeLockCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = SafeLockCyan
                    )
                }
            ) {
                listOf("All", "Pending", "Approved", "Rejected").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) SafeLockCyan else SafeLockTextSecondary
                            )
                        }
                    )
                }
            }

            // Requests List
            if (filteredRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = SafeLockTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Requests Found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SafeLockTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap the + button to request app permission, schedule change, or emergency unlock.",
                            fontSize = 12.sp,
                            color = SafeLockTextMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredRequests) { request ->
                        RequestCard(request = request)
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateRequestDialog(
            installedApps = installedApps,
            onDismiss = { showCreateDialog = false },
            onSubmit = { type, details ->
                onSubmitRequest(type, details)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun RequestCard(request: StudentRequest) {
    val (statusColor, statusLabel) = when (request.status) {
        "approved" -> Pair(SafeLockEmerald, "Approved")
        "rejected" -> Pair(SafeLockRose, "Rejected")
        else -> Pair(SafeLockAmber, "Pending Decision")
    }

    val (icon, typeTitle) = when (request.type) {
        "emergency_unlock" -> Pair(Icons.Default.Warning, "Emergency Unlock")
        "schedule_change" -> Pair(Icons.Default.Schedule, "Schedule Change")
        else -> Pair(Icons.Default.Apps, "App Permission")
    }

    val formattedTime = remember(request.createdAt) {
        val seconds = request.createdAt?.seconds
        if (seconds != null) {
            val instant = Instant.ofEpochSecond(seconds)
            DateTimeFormatter.ofPattern("MMM d, HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(instant)
        } else {
            "Just now"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SafeLockCard),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SafeLockBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SafeLockCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = typeTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = SafeLockTextPrimary
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = request.details,
                fontSize = 13.sp,
                color = SafeLockTextSecondary
            )

            if (!request.parentDecisionNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = SafeLockSurface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Parent note: ${request.parentDecisionNote}",
                        fontSize = 12.sp,
                        color = SafeLockTextPrimary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: ${request.requestId}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = SafeLockTextMuted
                )
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    color = SafeLockTextMuted
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateRequestDialog(
    installedApps: List<InstalledAppInfo>,
    onDismiss: () -> Unit,
    onSubmit: (type: String, details: String) -> Unit
) {
    var selectedType by remember { mutableStateOf("emergency_unlock") } // emergency_unlock, app_permission, schedule_change

    // Emergency unlock options
    var durationMinutes by remember { mutableIntStateOf(15) }

    // App permission options
    var selectedAppPackage by remember { mutableStateOf(installedApps.firstOrNull()?.packageName ?: "") }
    var appExpanded by remember { mutableStateOf(false) }

    // Notes
    var customNotes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Submit Request to Parent", fontWeight = FontWeight.Bold, color = SafeLockTextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Type selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SafeLockSurface)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        "emergency_unlock" to "Emergency",
                        "app_permission" to "App",
                        "schedule_change" to "Schedule"
                    ).forEach { (typeKey, label) ->
                        val isSelected = selectedType == typeKey
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SafeLockCyan else Color.Transparent)
                                .clickable { selectedType = typeKey }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF021B2B) else SafeLockTextSecondary
                            )
                        }
                    }
                }

                when (selectedType) {
                    "emergency_unlock" -> {
                        Column {
                            Text(
                                text = "Requested Unlock Duration",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SafeLockTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(5, 15, 30, 60).forEach { mins ->
                                    val isSelected = durationMinutes == mins
                                    Button(
                                        onClick = { durationMinutes = mins },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) SafeLockIndigo else SafeLockSurface,
                                            contentColor = if (isSelected) Color.White else SafeLockTextSecondary
                                        )
                                    ) {
                                        Text("${mins}m", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    "app_permission" -> {
                        Column {
                            Text(
                                text = "Select Installed Application",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SafeLockTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            ExposedDropdownMenuBox(
                                expanded = appExpanded,
                                onExpandedChange = { appExpanded = !appExpanded }
                            ) {
                                val selectedLabel = installedApps.find { it.packageName == selectedAppPackage }?.appName
                                    ?: selectedAppPackage
                                OutlinedTextField(
                                    value = selectedLabel,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = appExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SafeLockCyan,
                                        unfocusedBorderColor = SafeLockBorder,
                                        focusedTextColor = SafeLockTextPrimary,
                                        unfocusedTextColor = SafeLockTextPrimary
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = appExpanded,
                                    onDismissRequest = { appExpanded = false }
                                ) {
                                    installedApps.forEach { app ->
                                        DropdownMenuItem(
                                            text = { Text(app.appName) },
                                            onClick = {
                                                selectedAppPackage = app.packageName
                                                appExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    "schedule_change" -> {
                        Text(
                            text = "Describe requested schedule change (e.g. need extended study hours on Tuesday)",
                            fontSize = 12.sp,
                            color = SafeLockTextSecondary
                        )
                    }
                }

                OutlinedTextField(
                    value = customNotes,
                    onValueChange = { customNotes = it },
                    label = { Text("Reason / Note for Parent") },
                    placeholder = { Text("e.g. Need to contact tutor for homework") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SafeLockCyan,
                        unfocusedBorderColor = SafeLockBorder,
                        focusedTextColor = SafeLockTextPrimary,
                        unfocusedTextColor = SafeLockTextPrimary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val details = when (selectedType) {
                        "emergency_unlock" -> "Requesting $durationMinutes minutes emergency unlock. Reason: ${customNotes.ifBlank { "Immediate access needed" }}"
                        "app_permission" -> "Requesting permission for $selectedAppPackage. Note: ${customNotes.ifBlank { "Required for school/tasks" }}"
                        else -> customNotes.ifBlank { "Proposed schedule adjustment" }
                    }
                    onSubmit(selectedType, details)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SafeLockCyan,
                    contentColor = Color(0xFF021B2B)
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Submit Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SafeLockTextSecondary)
            }
        },
        containerColor = SafeLockCard
    )
}
