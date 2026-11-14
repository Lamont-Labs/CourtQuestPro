package com.courtquestpro

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Shared visual primitives for the CourtQuest performance-studio identity.
 * Internal, immutable and dependency-free; no runtime state or persistence.
 */
internal object CourtQuestDesign {
    val background = Color(0xFF050505)
    val surface = Color(0xFF101112)
    val border = Color(0xFF292A2C)
    val controlOutline = Color(0xFF777A7D)
    val elevatedSurface = Color(0xFF191A1C)
    val warmSurface = Color(0xFF291207)
    val blueprintShadow = Color(0xFF030303)
    val textPrimary = Color(0xFFF7F4EF)
    val textSecondary = Color(0xFFAAA9A6)
    val accent = Color(0xFFFFA36B)
    val accentVivid = Color(0xFFFF5B0A)
    val iconGradientStart = Color(0xFFFF9A54)
    val iconForeground = Color(0xFF1B0C03)
    val chipBackground = Color(0x442B160C)
    val chipBorder = Color(0x99FF7130)

    val spacingXs = 4.dp
    val spacingSm = 8.dp
    val spacingMd = 12.dp
    val spacingLg = 16.dp
    val spacingXl = 24.dp
    val spacing2Xl = 32.dp
    val spacing3Xl = 40.dp
}
