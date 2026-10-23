package com.example.ui.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.DeviceEnforcementState
import com.example.data.model.InstalledAppInfo
import com.example.data.model.PairingRequest
import com.example.data.model.Student
import com.example.data.model.WallpaperPresets
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
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    student: Student?,
    enforcementState: DeviceEnforcementState,
    approvedApps: List<InstalledAppInfo>,
    pairingRequests: List<PairingRequest>,
    wallpaperPresetId: String,
    wallpaperCustomUri: String?,
    pendingRequestsCount: Int,
    isDeviceAdminActive: Boolean,
    onLaunchApp: (String) -> Unit,
    onEmergencyCall: () -> Unit,
    onOpenRequests: () -> Unit,
    onOpenWallpaper: () -> Unit,
    onOpenDeviceAdminDialog: () -> Unit,
    onRespondPairingRequest: (PairingRequest, Boolean) -> Unit
) {
    val context = LocalContext.current

    // Digital clock state updating automatically every second
    var currentDateTime by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            currentDateTime = LocalDateTime.now()
            delay(1000L)
        }
    }

    val timeFormat = remember { DateTimeFormatter.ofPattern("HH:mm:ss") }
    val dateFormat = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d") }

    val preset = WallpaperPresets.getById(wallpaperPresetId)

    Box(modifier = Modifier.fillMaxSize()) {
        // Student-controlled wallpaper background
        if (wallpaperPresetId == "custom" && !wallpaperCustomUri.isNullOrBlank()) {
            AsyncImage(
                model = Uri.parse(wallpaperCustomUri),
                contentDescription = "Custom Wallpaper",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Soft overlay to maintain accessible contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(preset.brush)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Top Bar: Logo, Name, Student ID Chip, Admin Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.img),
                        contentDescription = "SafeLock Logo",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "SafeLock",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SafeLockTextPrimary
                    )
                }

                // Student ID Chip with copy button
                if (student != null) {
                    Surface(
                        color = SafeLockSurface.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(20.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SafeLockBorder)),
                        modifier = Modifier.clickable {
                            val clipboard =
                                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Student ID", student.studentId))
                            Toast.makeText(context, "Student ID copied: ${student.studentId}", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = student.studentId,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = SafeLockCyan
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy ID",
                                tint = SafeLockCyan,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Digital Clock
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentDateTime.format(timeFormat),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = SafeLockTextPrimary,
                    letterSpacing = 2.sp
                )
                Text(
                    text = currentDateTime.format(dateFormat),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = SafeLockTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Enforcement State Banner
            EnforcementStateCard(
                state = enforcementState,
                onEmergencyCall = onEmergencyCall
            )

            // Pending Pairing Requests Banner
            AnimatedVisibility(visible = pairingRequests.isNotEmpty()) {
                val req = pairingRequests.firstOrNull()
                if (req != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = SafeLockCard),
                        shape = RoundedCornerShape(14.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SafeLockIndigo))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pairing Invitation",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SafeLockIndigo
                                )
                                Text(
                                    text = "${req.parentName} wants to link with this device",
                                    fontSize = 13.sp,
                                    color = SafeLockTextPrimary
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                IconButton(
                                    onClick = { onRespondPairingRequest(req, false) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(SafeLockRose.copy(alpha = 0.2f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, "Reject", tint = SafeLockRose, modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = { onRespondPairingRequest(req, true) },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(SafeLockEmerald.copy(alpha = 0.2f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Check, "Approve", tint = SafeLockEmerald, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Responsive Application Grid
            Text(
                text = "APPROVED APPLICATIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SafeLockTextSecondary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val gridColumns = when {
                    maxWidth > 600.dp -> GridCells.Fixed(5)
                    maxWidth > 400.dp -> GridCells.Fixed(4)
                    else -> GridCells.Fixed(3)
                }

                if (approvedApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SafeLockSurface.copy(alpha = 0.8f))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Apps,
                                contentDescription = null,
                                tint = SafeLockTextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Approved Applications Yet",
                                fontWeight = FontWeight.SemiBold,
                                color = SafeLockTextPrimary,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Apps approved by your parent will appear here. Emergency calls remain active.",
                                color = SafeLockTextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = gridColumns,
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(approvedApps) { app ->
                            AppGridItem(
                                app = app,
                                isRestricted = enforcementState == DeviceEnforcementState.SCHEDULE_RESTRICTED ||
                                        enforcementState == DeviceEnforcementState.MANUAL_LOCKED,
                                onLaunch = { onLaunchApp(app.packageName) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Navigation Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SafeLockSurface.copy(alpha = 0.95f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emergency Calling (Always Available)
                IconButton(
                    onClick = onEmergencyCall,
                    modifier = Modifier.testTag("emergency_call_button")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Emergency Call",
                            tint = SafeLockRose
                        )
                        Text("Emergency", fontSize = 9.sp, color = SafeLockRose, fontWeight = FontWeight.Bold)
                    }
                }

                // Requests
                IconButton(
                    onClick = onOpenRequests,
                    modifier = Modifier.testTag("requests_nav_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (pendingRequestsCount > 0) {
                                Badge(containerColor = SafeLockCyan) {
                                    Text("$pendingRequestsCount")
                                }
                            }
                        }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Requests",
                                tint = SafeLockTextPrimary
                            )
                            Text("Requests", fontSize = 9.sp, color = SafeLockTextSecondary)
                        }
                    }
                }

                // Wallpaper Customization (Student-Controlled)
                IconButton(
                    onClick = onOpenWallpaper,
                    modifier = Modifier.testTag("wallpaper_nav_button")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Wallpaper,
                            contentDescription = "Wallpaper",
                            tint = SafeLockTextPrimary
                        )
                        Text("Wallpaper", fontSize = 9.sp, color = SafeLockTextSecondary)
                    }
                }

                // Device Admin / MDM Status
                IconButton(
                    onClick = onOpenDeviceAdminDialog,
                    modifier = Modifier.testTag("device_info_button")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (isDeviceAdminActive) Icons.Default.Shield else Icons.Default.Info,
                            contentDescription = "Protection",
                            tint = if (isDeviceAdminActive) SafeLockEmerald else SafeLockAmber
                        )
                        Text(
                            if (isDeviceAdminActive) "Protected" else "Setup Admin",
                            fontSize = 9.sp,
                            color = if (isDeviceAdminActive) SafeLockEmerald else SafeLockAmber
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EnforcementStateCard(
    state: DeviceEnforcementState,
    onEmergencyCall: () -> Unit
) {
    val (bg, iconColor, title, desc) = when (state) {
        DeviceEnforcementState.NORMAL -> Quad(
            SafeLockEmerald.copy(alpha = 0.15f),
            SafeLockEmerald,
            "Normal Use Authorized",
            "Approved applications are available for unrestricted use."
        )
        DeviceEnforcementState.SCHEDULE_RESTRICTED -> Quad(
            SafeLockAmber.copy(alpha = 0.15f),
            SafeLockAmber,
            "Restricted Period Active",
            "Schedule policy is active. You can request temporary unlock."
        )
        DeviceEnforcementState.MANUAL_LOCKED -> Quad(
            SafeLockRose.copy(alpha = 0.15f),
            SafeLockRose,
            "Device Locked by Parent",
            "Remote lock applied. Essential phone calling remains active."
        )
        DeviceEnforcementState.EMERGENCY_UNLOCKED -> Quad(
            SafeLockCyan.copy(alpha = 0.2f),
            SafeLockCyan,
            "Emergency Unlock Active",
            "Temporary authorized session in progress."
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(iconColor.copy(alpha = 0.4f)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (state == DeviceEnforcementState.MANUAL_LOCKED) Icons.Default.Lock else Icons.Default.Shield,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = SafeLockTextPrimary
                )
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = SafeLockTextSecondary
                )
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun AppGridItem(
    app: InstalledAppInfo,
    isRestricted: Boolean,
    onLaunch: () -> Unit
) {
    val context = LocalContext.current
    val pm = remember { context.packageManager }

    val iconDrawable = remember(app.packageName) {
        try {
            pm.getApplicationIcon(app.packageName)
        } catch (e: Exception) {
            null
        }
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onLaunch() }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SafeLockCard)
                .border(1.dp, SafeLockBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (iconDrawable != null) {
                val bitmap = remember(iconDrawable) {
                    val bmp = android.graphics.Bitmap.createBitmap(
                        iconDrawable.intrinsicWidth.coerceAtLeast(1),
                        iconDrawable.intrinsicHeight.coerceAtLeast(1),
                        android.graphics.Bitmap.Config.ARGB_8888
                    )
                    val canvas = android.graphics.Canvas(bmp)
                    iconDrawable.setBounds(0, 0, canvas.width, canvas.height)
                    iconDrawable.draw(canvas)
                    bmp
                }
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = app.appName,
                    modifier = Modifier.size(42.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = app.appName,
                    tint = SafeLockCyan,
                    modifier = Modifier.size(32.dp)
                )
            }

            if (isRestricted && !app.isSystemEssential) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Restricted",
                        tint = SafeLockAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = app.appName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = SafeLockTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
