package com.courtquestpro

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

internal val CourtDisplay = FontFamily(Font(R.font.barlow_condensed_extra_bold, FontWeight.ExtraBold))
internal val CourtBody = FontFamily(
    Font(R.font.barlow_regular, FontWeight.Normal),
    Font(R.font.barlow_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_bold, FontWeight.Bold)
)

private val CourtTypography = Typography(
    displayLarge = TextStyle(fontFamily = CourtDisplay, fontWeight = FontWeight.ExtraBold, fontSize = 64.sp, lineHeight = 66.sp),
    displayMedium = TextStyle(fontFamily = CourtDisplay, fontWeight = FontWeight.ExtraBold, fontSize = 52.sp, lineHeight = 54.sp),
    displaySmall = TextStyle(fontFamily = CourtDisplay, fontWeight = FontWeight.ExtraBold, fontSize = 42.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontFamily = CourtDisplay, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, lineHeight = 39.sp),
    headlineMedium = TextStyle(fontFamily = CourtDisplay, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, lineHeight = 33.sp),
    headlineSmall = TextStyle(fontFamily = CourtDisplay, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp, lineHeight = 29.sp),
    titleLarge = TextStyle(fontFamily = CourtBody, fontWeight = FontWeight.Bold, fontSize = 21.sp, lineHeight = 27.sp),
    titleMedium = TextStyle(fontFamily = CourtBody, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontFamily = CourtBody, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = CourtBody, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = CourtBody, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = CourtBody, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = CourtBody, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 19.sp),
    labelMedium = TextStyle(fontFamily = CourtBody, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 17.sp),
    labelSmall = TextStyle(fontFamily = CourtBody, fontWeight = FontWeight.Bold, fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 1.sp)
)

@Composable
internal fun CourtQuestTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = CourtQuestDesign.accentVivid, onPrimary = CourtQuestDesign.iconForeground,
            primaryContainer = CourtQuestDesign.warmSurface, onPrimaryContainer = CourtQuestDesign.textPrimary,
            secondary = CourtQuestDesign.accent, onSecondary = CourtQuestDesign.iconForeground,
            background = CourtQuestDesign.background, onBackground = CourtQuestDesign.textPrimary,
            surface = CourtQuestDesign.surface, onSurface = CourtQuestDesign.textPrimary,
            surfaceVariant = CourtQuestDesign.elevatedSurface, onSurfaceVariant = CourtQuestDesign.textSecondary,
            outline = CourtQuestDesign.border, outlineVariant = CourtQuestDesign.border,
            error = Color(0xFFFFB4AB)
        ),
        typography = CourtTypography,
        content = content
    )
}
