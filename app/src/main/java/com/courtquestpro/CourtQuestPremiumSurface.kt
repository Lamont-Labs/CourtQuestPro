package com.courtquestpro

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

/**
 * Shared elevated surface for training and performance content.
 *
 * Pure presentation: no navigation, data writes, or implicit click behavior.
 * The caller supplies content and handles interaction explicitly.
 */
@Composable
internal fun CourtQuestPremiumSurface(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    listOf(
                        androidx.compose.ui.graphics.Color(0xD9363033),
                        androidx.compose.ui.graphics.Color(0xE91D1E23),
                        androidx.compose.ui.graphics.Color(0xF015171B)
                    )
                ),
                shape
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        androidx.compose.ui.graphics.Color(0x8CFFB080),
                        androidx.compose.ui.graphics.Color(0x354D4345),
                        androidx.compose.ui.graphics.Color(0x1F89858B)
                    )
                ),
                shape
            )
            .padding(contentPadding)
    ) {
        content()
    }
}
