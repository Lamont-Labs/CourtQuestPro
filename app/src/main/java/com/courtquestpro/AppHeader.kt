package com.courtquestpro

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AppHeader(title: String, subtitle: String = "", compact: Boolean = false) {
    Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 14.dp)) {
        FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(painterResource(R.drawable.courtquest_signature_emblem), null,
                    modifier = Modifier.size(30.dp).clip(RoundedCornerShape(9.dp))
                        .background(CourtQuestDesign.accentVivid).padding(2.dp))
                Image(painterResource(R.drawable.courtquest_signature_wordmark), "CourtQuest Pro",
                    modifier = Modifier.width(116.dp).height(36.dp))
            }
            Box(Modifier.heightIn(min = 40.dp), contentAlignment = Alignment.CenterStart) { CourtEyebrow("PLAYER LAB") }
        }
        Spacer(Modifier.height(8.dp))
        Text(title, color = CourtQuestDesign.textPrimary,
            style = if (compact) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displaySmall,
            modifier = Modifier.semantics { heading() })
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(10.dp))
        Box(Modifier.width(36.dp).height(3.dp).clip(RoundedCornerShape(2.dp))
            .background(CourtQuestDesign.accentVivid))
        Spacer(Modifier.height(10.dp))
        CourtRule()
    }
}
