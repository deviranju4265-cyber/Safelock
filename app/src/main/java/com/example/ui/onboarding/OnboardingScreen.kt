package com.example.ui.onboarding

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InstalledAppInfo
import com.example.data.model.WallpaperPresets
import com.example.ui.theme.SafeLockBackground
import com.example.ui.theme.SafeLockBorder
import com.example.ui.theme.SafeLockCard
import com.example.ui.theme.SafeLockCyan
import com.example.ui.theme.SafeLockEmerald
import com.example.ui.theme.SafeLockIndigo
import com.example.ui.theme.SafeLockSurface
import com.example.ui.theme.SafeLockTextMuted
import com.example.ui.theme.SafeLockTextPrimary
import com.example.ui.theme.SafeLockTextSecondary
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun OnboardingScreen(
    studentId: String,
    installedApps: List<InstalledAppInfo>,
    onComplete: (
        name: String,
        scheduleJson: String,
        selectedPackages: List<String>,
        wallpaperPresetId: String
    ) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentStep by remember { mutableIntStateOf(1) }
    var studentName by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }

    // Schedule proposal
    var schoolStart by remember { mutableStateOf("08:30") }
    var schoolEnd by remember { mutableStateOf("15:30") }
    var bedtimeStart by remember { mutableStateOf("21:30") }
    var bedtimeEnd by remember { mutableStateOf("06:30") }

    // Desired apps to request
    val selectedPackages = remember { mutableStateListOf<String>() }

    // Wallpaper
    var selectedWallpaperId by remember { mutableStateOf("cyber_dark") }

    var isSubmitting by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SafeLockBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Header progress
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SafeLock Setup",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafeLockCyan
                )
                Text(
                    text = "Step $currentStep of 5",
                    fontSize = 13.sp,
                    color = SafeLockTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Step Indicator dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..5).forEach { step ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (step <= currentStep) SafeLockCyan else SafeLockBorder
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Step Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentStep) {
                    1 -> StepStudentIdentity(
                        studentName = studentName,
                        onNameChange = {
                            studentName = it
                            nameError = false
                        },
                        nameError = nameError,
                        studentId = studentId,
                        onCopyId = {
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Student ID", studentId))
                            Toast.makeText(context, "Student ID copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )

                    2 -> StepScheduleProposal(
                        schoolStart = schoolStart,
                        onSchoolStartChange = { schoolStart = it },
                        schoolEnd = schoolEnd,
                        onSchoolEndChange = { schoolEnd = it },
                        bedtimeStart = bedtimeStart,
                        onBedtimeStartChange = { bedtimeStart = it },
                        bedtimeEnd = bedtimeEnd,
                        onBedtimeEndChange = { bedtimeEnd = it }
                    )

                    3 -> StepDesiredApps(
                        installedApps = installedApps,
                        selectedPackages = selectedPackages,
                        onToggle = { pkg ->
                            if (selectedPackages.contains(pkg)) {
                                selectedPackages.remove(pkg)
                            } else {
                                selectedPackages.add(pkg)
                            }
                        }
                    )

                    4 -> StepWallpaperSelection(
                        selectedPresetId = selectedWallpaperId,
                        onSelect = { selectedWallpaperId = it }
                    )

                    5 -> StepReview(
                        studentName = studentName,
                        studentId = studentId,
                        schoolStart = schoolStart,
                        schoolEnd = schoolEnd,
                        bedtimeStart = bedtimeStart,
                        bedtimeEnd = bedtimeEnd,
                        selectedAppCount = selectedPackages.size,
                        selectedWallpaperName = WallpaperPresets.getById(selectedWallpaperId).name
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    Button(
                        onClick = { currentStep-- },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SafeLockSurface,
                            contentColor = SafeLockTextSecondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (currentStep == 1) {
                            if (studentName.trim().isBlank()) {
                                nameError = true
                                return@Button
                            }
                            currentStep++
                        } else if (currentStep < 5) {
                            currentStep++
                        } else {
                            // Final Submit
                            isSubmitting = true
                            val periods = JSONArray().apply {
                                put(
                                    JSONObject().apply {
                                        put("name", "School Hours")
                                        put("startTime", schoolStart)
                                        put("endTime", schoolEnd)
                                        put("isRestricted", true)
                                    }
                                )
                                put(
                                    JSONObject().apply {
                                        put("name", "Bedtime")
                                        put("startTime", bedtimeStart)
                                        put("endTime", bedtimeEnd)
                                        put("isRestricted", true)
                                    }
                                )
                            }
                            onComplete(
                                studentName.trim(),
                                periods.toString(),
                                selectedPackages.toList(),
                                selectedWallpaperId
                            )
                        }
                    },
                    modifier = Modifier.testTag("onboarding_next_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SafeLockCyan,
                        contentColor = Color(0xFF021B2B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF021B2B),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (currentStep == 5) "Submit & Finish" else "Next")
                    }
                }
            }
        }
    }
}

@Composable
private fun StepStudentIdentity(
    studentName: String,
    onNameChange: (String) -> Unit,
    nameError: Boolean,
    studentId: String,
    onCopyId: () -> Unit
) {
    Column {
        Text(
            text = "Welcome to SafeLock",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = SafeLockTextPrimary
        )
        Text(
            text = "Let's set up your profile and generate your unique Student ID for your parent.",
            fontSize = 14.sp,
            color = SafeLockTextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        OutlinedTextField(
            value = studentName,
            onValueChange = onNameChange,
            label = { Text("Student Name") },
            placeholder = { Text("e.g. Alex Smith") },
            isError = nameError,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("student_name_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SafeLockCyan,
                unfocusedBorderColor = SafeLockBorder,
                focusedTextColor = SafeLockTextPrimary,
                unfocusedTextColor = SafeLockTextPrimary,
                focusedContainerColor = SafeLockSurface,
                unfocusedContainerColor = SafeLockSurface
            )
        )
        if (nameError) {
            Text(
                text = "Please enter your name",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Student ID Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SafeLockCard),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SafeLockBorder))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "YOUR UNIQUE STUDENT ID",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafeLockTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = studentId,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = SafeLockCyan
                    )
                    IconButton(
                        onClick = onCopyId,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SafeLockSurface)
                            .testTag("copy_id_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy ID",
                            tint = SafeLockCyan
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Provide this Student ID to your parent's Lock Monitor app to pair devices.",
                    fontSize = 12.sp,
                    color = SafeLockTextMuted
                )
            }
        }
    }
}

@Composable
private fun StepScheduleProposal(
    schoolStart: String,
    onSchoolStartChange: (String) -> Unit,
    schoolEnd: String,
    onSchoolEndChange: (String) -> Unit,
    bedtimeStart: String,
    onBedtimeStartChange: (String) -> Unit,
    bedtimeEnd: String,
    onBedtimeEndChange: (String) -> Unit
) {
    Column {
        Text(
            text = "Initial Schedule Proposal",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = SafeLockTextPrimary
        )
        Text(
            text = "Propose standard restricted periods (e.g. school hours and sleep time). Your parent will review and activate it.",
            fontSize = 14.sp,
            color = SafeLockTextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SafeLockCard),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SafeLockBorder))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = SafeLockIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "School Restricted Hours (Mon–Fri)",
                        fontWeight = FontWeight.SemiBold,
                        color = SafeLockTextPrimary,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = schoolStart,
                        onValueChange = onSchoolStartChange,
                        label = { Text("Start") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SafeLockIndigo,
                            unfocusedBorderColor = SafeLockBorder,
                            focusedTextColor = SafeLockTextPrimary,
                            unfocusedTextColor = SafeLockTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = schoolEnd,
                        onValueChange = onSchoolEndChange,
                        label = { Text("End") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SafeLockIndigo,
                            unfocusedBorderColor = SafeLockBorder,
                            focusedTextColor = SafeLockTextPrimary,
                            unfocusedTextColor = SafeLockTextPrimary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SafeLockCard),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SafeLockBorder))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = SafeLockEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bedtime Hours (Daily Overnight)",
                        fontWeight = FontWeight.SemiBold,
                        color = SafeLockTextPrimary,
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = bedtimeStart,
                        onValueChange = onBedtimeStartChange,
                        label = { Text("Bedtime") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SafeLockEmerald,
                            unfocusedBorderColor = SafeLockBorder,
                            focusedTextColor = SafeLockTextPrimary,
                            unfocusedTextColor = SafeLockTextPrimary
                        )
                    )
                    OutlinedTextField(
                        value = bedtimeEnd,
                        onValueChange = onBedtimeEndChange,
                        label = { Text("Wakeup") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SafeLockEmerald,
                            unfocusedBorderColor = SafeLockBorder,
                            focusedTextColor = SafeLockTextPrimary,
                            unfocusedTextColor = SafeLockTextPrimary
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun StepDesiredApps(
    installedApps: List<InstalledAppInfo>,
    selectedPackages: List<String>,
    onToggle: (String) -> Unit
) {
    Column {
        Text(
            text = "Request Desired Apps",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = SafeLockTextPrimary
        )
        Text(
            text = "Select apps you want to request approval for. Initial selections are requests, not yet approved.",
            fontSize = 14.sp,
            color = SafeLockTextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(SafeLockCard)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(installedApps) { app ->
                val isSelected = selectedPackages.contains(app.packageName)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggle(app.packageName) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onToggle(app.packageName) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = SafeLockCyan,
                            uncheckedColor = SafeLockBorder,
                            checkmarkColor = Color(0xFF021B2B)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.appName,
                            fontWeight = FontWeight.Medium,
                            color = SafeLockTextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = app.packageName,
                            color = SafeLockTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepWallpaperSelection(
    selectedPresetId: String,
    onSelect: (String) -> Unit
) {
    Column {
        Text(
            text = "Personalize Wallpaper",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = SafeLockTextPrimary
        )
        Text(
            text = "SafeLock wallpaper is student-controlled! Choose your favorite style. You can customize with personal photos anytime later.",
            fontSize = 14.sp,
            color = SafeLockTextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            WallpaperPresets.presets.forEach { preset ->
                val isSelected = preset.id == selectedPresetId
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(preset.brush)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) SafeLockCyan else SafeLockBorder,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSelect(preset.id) }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Wallpaper,
                                contentDescription = null,
                                tint = preset.accentColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = preset.name,
                                fontWeight = FontWeight.SemiBold,
                                color = SafeLockTextPrimary,
                                fontSize = 16.sp
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = SafeLockCyan
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepReview(
    studentName: String,
    studentId: String,
    schoolStart: String,
    schoolEnd: String,
    bedtimeStart: String,
    bedtimeEnd: String,
    selectedAppCount: Int,
    selectedWallpaperName: String
) {
    Column {
        Text(
            text = "Review & Complete",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = SafeLockTextPrimary
        )
        Text(
            text = "Confirm your details. Once submitted, your profile will synchronize with the secure cloud.",
            fontSize = 14.sp,
            color = SafeLockTextSecondary,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SafeLockCard),
            shape = RoundedCornerShape(16.dp),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SafeLockBorder))
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                ReviewRow(label = "Student Name", value = studentName)
                ReviewRow(label = "Student ID", value = studentId, isCode = true)
                ReviewRow(label = "School Schedule", value = "$schoolStart – $schoolEnd (Mon–Fri)")
                ReviewRow(label = "Bedtime Period", value = "$bedtimeStart – $bedtimeEnd (Daily)")
                ReviewRow(label = "Requested Apps", value = "$selectedAppCount applications")
                ReviewRow(label = "Chosen Wallpaper", value = selectedWallpaperName)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SafeLockSurface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = SafeLockCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Emergency calling remains accessible at all times without restrictions.",
                fontSize = 12.sp,
                color = SafeLockTextSecondary
            )
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String, isCode: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = SafeLockTextSecondary, fontSize = 13.sp)
        Text(
            text = value,
            color = if (isCode) SafeLockCyan else SafeLockTextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontFamily = if (isCode) FontFamily.Monospace else FontFamily.Default,
            fontSize = 14.sp
        )
    }
}
