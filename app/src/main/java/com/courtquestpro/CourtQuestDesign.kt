package com.courtquestpro

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Shared visual primitives for the CourtQuest performance-studio identity.
 * Internal, immutable and dependency-free; no runtime state or persistence.
 */
internal object CourtQuestDesign {
    val background = Color(0xFF121315)
    val elevatedSurface = Color(0xFF252127)
    val warmSurface = Color(0xFF4B281D)
    val textPrimary = Color(0xFFF7F3EF)
    val textSecondary = Color(0xFFB7ACA6)
    val accent = Color(0xFFFFA35C)
    val accentVivid = Color(0xFFFF783D)
    val iconGradientStart = Color(0xFFFFAD71)
    val iconForeground = Color(0xFF211713)
    val chipBackground = Color(0x553A2B29)
    val chipBorder = Color(0x66FFB080)

    val spacingXs = 4.dp
    val spacingSm = 8.dp
    val spacingMd = 12.dp
    val spacingLg = 16.dp
    val spacingXl = 24.dp
    val spacing2Xl = 32.dp
    val spacing3Xl = 40.dp
}
