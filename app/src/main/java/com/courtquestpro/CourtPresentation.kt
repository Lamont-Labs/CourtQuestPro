package com.courtquestpro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun CourtEyebrow(text: String, modifier: Modifier = Modifier, color: Color = CourtQuestDesign.accent) {
    Text(text, modifier, color = color, style = MaterialTheme.typography.labelSmall)
}

@Composable
internal fun CourtHeading(title: String, detail: String? = null, onDetail: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, Modifier.weight(1f), color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.headlineSmall)
        if (detail != null && onDetail != null) TextButton(onClick = onDetail) { Text(detail) }
    }
}

@Composable
internal fun CourtMetric(label: String, value: String, modifier: Modifier = Modifier, accent: Boolean = false) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(value, color = if (accent) CourtQuestDesign.accent else CourtQuestDesign.textPrimary,
            style = MaterialTheme.typography.headlineLarge)
        Text(label, color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
internal fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    CourtQuestPremiumSurface(modifier, PaddingValues(16.dp)) { CourtMetric(label, value) }
}

@Composable
internal fun CourtRule() { HorizontalDivider(color = CourtQuestDesign.border) }

@Composable
internal fun CourtStatus(text: String) {
    Text(text, Modifier.background(CourtQuestDesign.warmSurface, RoundedCornerShape(8.dp))
        .padding(horizontal = 10.dp, vertical = 6.dp),
        color = CourtQuestDesign.accent, style = MaterialTheme.typography.labelMedium)
}
