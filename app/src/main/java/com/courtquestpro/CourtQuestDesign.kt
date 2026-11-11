package com.courtquestpro

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Shared visual primitives for the CourtQuest performance-studio identity.
 * Internal, immutable and dependency-free; no runtime state or persistence.
 */
internal object CourtQuestDesign {
    val background = Color(0xFF000000)
    val surface = Color(0xFF101113)
    val border = Color(0xFF34363B)
    val controlOutline = Color(0xFF92969E)
    val elevatedSurface = Color(0xFF202226)
    val warmSurface = Color(0xFF321A10)
    val blueprintShadow = Color(0xFF090B0E)
    val textPrimary = Color(0xFFF8F5F0)
    val textSecondary = Color(0xFFB8BBC3)
    val accent = Color(0xFFFF944F)
    val accentVivid = Color(0xFFFF650E)
    val iconGradientStart = Color(0xFFFF913D)
    val iconForeground = Color(0xFF160C06)
    val chipBackground = Color(0x553B2113)
    val chipBorder = Color(0x99FF944F)

    val spacingXs = 4.dp
    val spacingSm = 8.dp
    val spacingMd = 12.dp
    val spacingLg = 16.dp
    val spacingXl = 24.dp
    val spacing2Xl = 32.dp
    val spacing3Xl = 40.dp
}
