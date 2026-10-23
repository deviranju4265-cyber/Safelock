package com.example.data.model

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class WallpaperPreset(
    val id: String,
    val name: String,
    val brush: Brush,
    val accentColor: Color
)

object WallpaperPresets {
    val presets = listOf(
        WallpaperPreset(
            id = "cyber_dark",
            name = "Cyber Slate",
            brush = Brush.verticalGradient(
                listOf(Color(0xFF0A0F1D), Color(0xFF131B2E), Color(0xFF0F172A))
            ),
            accentColor = Color(0xFF38BDF8)
        ),
        WallpaperPreset(
            id = "nebula",
            name = "Deep Nebula",
            brush = Brush.verticalGradient(
                listOf(Color(0xFF0F0B1E), Color(0xFF221538), Color(0xFF150D24))
            ),
            accentColor = Color(0xFFA855F7)
        ),
        WallpaperPreset(
            id = "emerald",
            name = "Emerald Matrix",
            brush = Brush.verticalGradient(
                listOf(Color(0xFF051713), Color(0xFF0E2A23), Color(0xFF081C17))
            ),
            accentColor = Color(0xFF10B981)
        ),
        WallpaperPreset(
            id = "twilight",
            name = "Solar Twilight",
            brush = Brush.verticalGradient(
                listOf(Color(0xFF1F110B), Color(0xFF2C1A12), Color(0xFF130905))
            ),
            accentColor = Color(0xFFF59E0B)
        )
    )

    fun getById(id: String): WallpaperPreset {
        return presets.find { it.id == id } ?: presets.first()
    }
}
