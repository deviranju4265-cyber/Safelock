package com.example.ui.wallpaper

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.WallpaperPresets
import com.example.ui.theme.SafeLockBackground
import com.example.ui.theme.SafeLockBorder
import com.example.ui.theme.SafeLockCard
import com.example.ui.theme.SafeLockCyan
import com.example.ui.theme.SafeLockEmerald
import com.example.ui.theme.SafeLockSurface
import com.example.ui.theme.SafeLockTextPrimary
import com.example.ui.theme.SafeLockTextSecondary

@Composable
fun WallpaperPickerScreen(
    currentPresetId: String,
    currentCustomUri: String?,
    onBack: () -> Unit,
    onSavePreset: (String) -> Unit,
    onSaveCustomUri: (String) -> Unit
) {
    BackHandler { onBack() }

    var selectedPresetId by remember { mutableStateOf(currentPresetId) }
    var selectedCustomUri by remember { mutableStateOf(currentCustomUri) }
    var isCustomSelected by remember { mutableStateOf(currentPresetId == "custom" && !currentCustomUri.isNullOrBlank()) }

    // Android Photo Picker launcher (zero-permission standard)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedCustomUri = uri.toString()
            isCustomSelected = true
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
                    text = "Personalize Wallpaper",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SafeLockTextPrimary
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Live Wallpaper Preview",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SafeLockTextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, SafeLockBorder, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCustomSelected && !selectedCustomUri.isNullOrBlank()) {
                        AsyncImage(
                            model = Uri.parse(selectedCustomUri),
                            contentDescription = "Custom preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f))
                        )
                    } else {
                        val activePreset = WallpaperPresets.getById(selectedPresetId)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(activePreset.brush)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "12:45",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafeLockTextPrimary
                        )
                        Text(
                            text = "SafeLock Student Launcher",
                            fontSize = 12.sp,
                            color = SafeLockTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Built-in Themes",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SafeLockTextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    WallpaperPresets.presets.forEach { preset ->
                        val isSelected = !isCustomSelected && preset.id == selectedPresetId
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(preset.brush)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) SafeLockCyan else SafeLockBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    isCustomSelected = false
                                    selectedPresetId = preset.id
                                }
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
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = preset.name,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SafeLockTextPrimary,
                                        fontSize = 14.sp
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

                Spacer(modifier = Modifier.height(18.dp))

                // Custom Gallery Selection
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("pick_gallery_wallpaper_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SafeLockTextPrimary
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (isCustomSelected) SafeLockCyan else SafeLockBorder)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Photo Gallery",
                        tint = if (isCustomSelected) SafeLockCyan else SafeLockTextSecondary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isCustomSelected) "Custom Photo Chosen (Tap to Change)" else "Choose from Photo Gallery",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }

            // Apply Button
            Button(
                onClick = {
                    if (isCustomSelected && !selectedCustomUri.isNullOrBlank()) {
                        onSaveCustomUri(selectedCustomUri!!)
                    } else {
                        onSavePreset(selectedPresetId)
                    }
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("apply_wallpaper_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SafeLockCyan,
                    contentColor = Color(0xFF021B2B)
                )
            ) {
                Text(
                    text = "Apply Wallpaper",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
