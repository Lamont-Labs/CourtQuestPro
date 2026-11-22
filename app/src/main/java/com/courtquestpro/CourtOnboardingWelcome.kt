package com.courtquestpro

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
internal fun CourtOnboardingWelcome() {
    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        CourtArenaHero(
            R.drawable.court_arena_player,
            Modifier.fillMaxWidth().height(300.dp * LocalDensity.current.fontScale)
                .testTag("onboarding-arena-hero")
        ) {
            Image(
                painterResource(R.drawable.courtquest_signature_wordmark),
                contentDescription = "CourtQuest Pro",
                modifier = Modifier.align(Alignment.TopStart).padding(20.dp).fillMaxWidth(.68f),
                contentScale = ContentScale.FillWidth
            )
            Row(
                Modifier.align(Alignment.BottomStart).padding(horizontal = 20.dp, vertical = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Icon(Icons.Default.SportsBasketball, null, tint = CourtQuestDesign.accent, modifier = Modifier.size(21.dp))
                Text("YOUR NEXT REP STARTS HERE", color = CourtQuestDesign.textPrimary,
                    style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(CourtQuestDesign.spacingMd)) {
            Text(
                "The work is\nyours.",
                modifier = Modifier.semantics { heading() },
                color = CourtQuestDesign.textPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.displaySmall
            )
            Text(
                "A personal training room for the player you are becoming. Set your level, choose a focus, and build a routine that fits your week.",
                color = CourtQuestDesign.textSecondary,
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(2.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, null, tint = CourtQuestDesign.accent, modifier = Modifier.size(18.dp))
                Text("Your results stay saved on this phone.", color = CourtQuestDesign.textSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
