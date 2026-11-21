package com.courtquestpro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Compact, centered app header; no redundant Player Lab badge or corner icons. */
@Composable
internal fun AppHeader(title: String, subtitle: String = "", compact: Boolean = false) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "COURTQUEST",
            color = CourtQuestDesign.textPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        Text(
            title,
            color = CourtQuestDesign.textPrimary,
            style = if (compact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                color = CourtQuestDesign.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.height(10.dp))
        Box(
            Modifier.width(36.dp).height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(CourtQuestDesign.accentVivid)
        )
    }
}
