package com.example.ui.device

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.receiver.SafeLockDeviceAdminReceiver
import com.example.ui.theme.SafeLockAmber
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

@Composable
fun DeviceAdminSetupDialog(
    isAdminActive: Boolean,
    isDeviceOwner: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = if (isAdminActive) SafeLockEmerald else SafeLockAmber,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Android Device Protection",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafeLockTextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Status card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SafeLockSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusRow(
                            label = "Device Administration",
                            isActive = isAdminActive,
                            activeDesc = "Enabled (Protection Active)",
                            inactiveDesc = "Not Enabled"
                        )
                        StatusRow(
                            label = "Fully Managed / Device Owner",
                            isActive = isDeviceOwner,
                            activeDesc = "Provisioned (Kiosk Lock-Task Enforced)",
                            inactiveDesc = "Standard User Mode (Screen Pinning Active)"
                        )
                        StatusRow(
                            label = "Emergency Call Access",
                            isActive = true,
                            activeDesc = "Always Accessible (Zero Restriction)",
                            inactiveDesc = "N/A"
                        )
                    }
                }

                // Supported Android APIs Documentation
                Text(
                    text = "Supported Android Management Architecture",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafeLockCyan
                )
                Text(
                    text = "SafeLock implements exclusively official Android SDK APIs and policies:\n" +
                            "• Lock Task Mode (startLockTask / stopLockTask)\n" +
                            "• DeviceAdminReceiver & DevicePolicyManager\n" +
                            "• BootCompletedReceiver recovery on system reboot\n" +
                            "• Native Intent-based approved app launching\n" +
                            "• Android Photo Picker for zero-permission wallpaper\n" +
                            "No exploits or undocumented accessibility abuse are used.",
                    fontSize = 12.sp,
                    color = SafeLockTextSecondary,
                    lineHeight = 18.sp
                )

                // Activation Button if Admin not active
                if (!isAdminActive) {
                    Button(
                        onClick = {
                            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                                putExtra(
                                    DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                                    ComponentName(context, SafeLockDeviceAdminReceiver::class.java)
                                )
                                putExtra(
                                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                    "SafeLock requires Device Administrator rights to enforce parental security schedules and prevent unauthorized lock tampering."
                                )
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("enable_device_admin_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SafeLockCyan,
                            contentColor = Color(0xFF021B2B)
                        )
                    ) {
                        Text("Enable Device Administrator", fontWeight = FontWeight.Bold)
                    }
                }

                // MDM Enrollment guide
                Surface(
                    color = SafeLockSurface,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "School / MDM Device Owner Enrollment:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafeLockTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "dpm set-device-owner com.aistudio.safelock.krvpxt/.receiver.SafeLockDeviceAdminReceiver",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SafeLockCyan
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SafeLockSurface,
                    contentColor = SafeLockTextPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close")
            }
        },
        containerColor = SafeLockCard
    )
}

@Composable
private fun StatusRow(
    label: String,
    isActive: Boolean,
    activeDesc: String,
    inactiveDesc: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isActive) SafeLockEmerald else SafeLockAmber,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SafeLockTextPrimary)
            Text(
                text = if (isActive) activeDesc else inactiveDesc,
                fontSize = 11.sp,
                color = if (isActive) SafeLockEmerald else SafeLockTextMuted
            )
        }
    }
}
