package com.courtquestpro

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Consistent primary action for training, onboarding and scorekeeping.
 * Callers own the action, loading state and persistence.
 */
@Composable
internal fun CourtQuestPrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 52.dp).clip(RoundedCornerShape(14.dp))
            .background(if (enabled) Brush.verticalGradient(listOf(CourtQuestDesign.iconGradientStart,
                CourtQuestDesign.accentVivid, Color(0xFFF05206)))
                else Brush.verticalGradient(listOf(CourtQuestDesign.elevatedSurface, CourtQuestDesign.elevatedSurface)))
            .border(1.dp, if (enabled) Brush.verticalGradient(listOf(Color(0xFFFFB17C), CourtQuestDesign.accentVivid))
                else Brush.verticalGradient(listOf(CourtQuestDesign.border, CourtQuestDesign.border)), RoundedCornerShape(14.dp)),
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = CourtQuestDesign.iconForeground,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = CourtQuestDesign.textSecondary
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        content = content
    )
}
