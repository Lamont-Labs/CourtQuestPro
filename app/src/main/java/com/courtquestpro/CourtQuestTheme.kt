package com.courtquestpro

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp

internal val CourtDisplay = FontFamily(Font(R.font.barlow_condensed_extra_bold, FontWeight.ExtraBold))
internal val CourtBody = FontFamily(
    Font(R.font.barlow_regular, FontWeight.Normal),
    Font(R.font.barlow_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_bold, FontWeight.Bold)
)

private val CourtTypography = Typography(
    displayLarge = TextStyle(fontFamily = CourtDisplay, fontWeight = FontWeight.ExtraBold, fontSize = 58.sp, lineHeight = 60.sp, letterSpacing = (-0.6).sp),
    displayMedium = TextStyle(fontFamily = CourtDisplay, fontWeight = FontWeight.ExtraBold, fontSize = 48.sp, lineHeight = 50.sp, letterSpacing = (-0.4).sp),
    displaySmall = TextStyle(fontFamily = CourtDisplay, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp, lineHeight = 42.sp, letterSpacing = (-0.25).sp),
    headlineLarge = TextStyle(fontFamily = CourtDisplay, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 36.sp, letterSpacing = (-0.2).sp),
    headlineMedium = TextStyle(fontFamily = CourtDisplay, fontWeight = FontWeight.ExtraBold, fontSize = 29.sp, lineHeight = 31.sp),
    headlineSmall = TextStyle(fontFamily = CourtDisplay, fontWeight = FontWeight.ExtraBold, fontSize = 25.sp, lineHeight = 27.sp),
    titleLarge = TextStyle(fontFamily = CourtBody, fontWeight = FontWeight.Bold, fontSize = 19.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = CourtBody, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 21.sp),
    titleSmall = TextStyle(fontFamily = CourtBody, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = CourtBody, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = CourtBody, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = CourtBody, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = CourtBody, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 19.sp),
    labelMedium = TextStyle(fontFamily = CourtBody, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 17.sp),
    labelSmall = TextStyle(fontFamily = CourtBody, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 1.1.sp)
)

@Composable
internal fun CourtQuestTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = CourtQuestDesign.accentVivid, onPrimary = CourtQuestDesign.iconForeground,
            primaryContainer = CourtQuestDesign.warmSurface, onPrimaryContainer = CourtQuestDesign.textPrimary,
            secondary = CourtQuestDesign.accent, onSecondary = CourtQuestDesign.iconForeground,
            secondaryContainer = CourtQuestDesign.warmSurface, onSecondaryContainer = CourtQuestDesign.textPrimary,
            tertiary = CourtQuestDesign.accent, onTertiary = CourtQuestDesign.iconForeground,
            tertiaryContainer = CourtQuestDesign.warmSurface, onTertiaryContainer = CourtQuestDesign.textPrimary,
            surfaceTint = CourtQuestDesign.accentVivid,
            background = CourtQuestDesign.background, onBackground = CourtQuestDesign.textPrimary,
            surface = CourtQuestDesign.surface, onSurface = CourtQuestDesign.textPrimary,
            surfaceVariant = CourtQuestDesign.elevatedSurface, onSurfaceVariant = CourtQuestDesign.textSecondary,
            outline = CourtQuestDesign.controlOutline, outlineVariant = CourtQuestDesign.border,
            error = Color(0xFFFFB4AB)
        ),
        shapes = Shapes(
            extraSmall = RoundedCornerShape(6.dp),
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(14.dp),
            large = RoundedCornerShape(20.dp),
            extraLarge = RoundedCornerShape(24.dp)
        ),
        typography = CourtTypography,
        content = content
    )
}
