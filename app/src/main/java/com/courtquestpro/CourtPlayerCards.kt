package com.courtquestpro

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Cosmetic card identity only. Never generates competitive ratings or invented stats. */
internal enum class PlayerCardStyle(val label: String, val edition: String) {
    HOLOGRAPHIC("Holographic Elite", "ELITE SERIES"),
    CLASSIC("Court Classic", "COURT SERIES"),
    BATTLE("Battle Series", "FUTURE SERIES")
}

internal object PlayerCardSelection {
    private const val PREFS = "courtquest_player_card"
    private const val KEY = "selected_style_v1"
    private var current by mutableStateOf<PlayerCardStyle?>(null)
    fun read(context: Context): PlayerCardStyle = current ?:
        runCatching {
            PlayerCardStyle.valueOf(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY, PlayerCardStyle.HOLOGRAPHIC.name)!!)
        }.getOrDefault(PlayerCardStyle.HOLOGRAPHIC)

    fun write(context: Context, style: PlayerCardStyle): Boolean {
        val persisted = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY, style.name).commit()
        if (persisted) current = style
        return persisted
    }
}

@Composable
internal fun CourtPlayerCard(
    jersey: PlayerJersey,
    playerName: String,
    style: PlayerCardStyle,
    modifier: Modifier = Modifier
) {
    val background = when (style) {
        PlayerCardStyle.HOLOGRAPHIC -> listOf(Color(0xFF10172C), Color(0xFF3C2372), Color(0xFF087D93), Color(0xFF0C1021))
        PlayerCardStyle.CLASSIC -> listOf(Color(0xFF111922), Color(0xFF273949), Color(0xFF101820))
        PlayerCardStyle.BATTLE -> listOf(Color(0xFF090A0E), Color(0xFF4A1B09), Color(0xFF130D0B))
    }
    val accent = when (style) {
        PlayerCardStyle.HOLOGRAPHIC -> Color(0xFF7EE8FF)
        PlayerCardStyle.CLASSIC -> Color(0xFFD7DFE7)
        PlayerCardStyle.BATTLE -> Color(0xFFFF8A27)
    }
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier.aspectRatio(0.72f).clip(shape)
            .background(Brush.linearGradient(background))
            .border(BorderStroke(2.dp, Brush.linearGradient(listOf(accent, Color.White.copy(alpha = .75f), accent))), shape)
            .padding(12.dp).testTag("player-card-preview"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("COURTQUEST", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.8.sp)
            Text("01 / 03", color = accent, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }
        Text(style.edition, color = accent, fontSize = 10.sp, letterSpacing = 2.sp)
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            CourtJerseyArtwork(jersey, Modifier.fillMaxSize(), view = JerseyView.FRONT)
        }
        Text(playerName.ifBlank { jersey.name.ifBlank { "PLAYER" } }.uppercase(),
            color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp,
            maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text("#${jersey.number ?: "00"}  •  ${jersey.design.template.label.uppercase()}",
            color = accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Spacer(Modifier.height(5.dp))
        Box(Modifier.fillMaxWidth().height(3.dp).background(Brush.horizontalGradient(listOf(accent, Color.White, accent))))
    }
}

@Composable
internal fun PlayerCardStudio(jersey: PlayerJersey, playerName: String, onBack: () -> Unit) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf(PlayerCardSelection.read(context)) }
    var saved by remember { mutableStateOf(true) }
    Column(Modifier.fillMaxSize().background(CourtQuestDesign.background).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onBack) { Text("Back") }
            Text("PLAYER CARD STUDIO", color = Color.White, fontWeight = FontWeight.Black)
        }
        Text("Your saved Jersey Studio uniform appears on every card.",
            color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.bodyMedium)
        CourtPlayerCard(jersey, playerName, selected, Modifier.weight(1f).widthIn(max = 340.dp))
        PlayerCardStyle.entries.forEach { style ->
            val active = selected == style
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp)
                    .testTag("player-card-style-${style.name.lowercase()}")
                    .clickable { selected = style; saved = false },
                shape = RoundedCornerShape(12.dp),
                color = if (active) Color(0xFF573015) else Color(0xFF202125),
                border = BorderStroke(1.dp, if (active) Color(0xFFFF942F) else Color(0xFF454545))
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(style.label, color = Color.White)
                    if (active) Text("SELECTED", color = Color(0xFFFFA247), fontSize = 11.sp)
                }
            }
        }
        Button(
            onClick = { if (PlayerCardSelection.write(context, selected)) saved = true },
            enabled = !saved,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("player-card-save"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF912B), contentColor = Color.Black)
        ) { Text(if (saved) "Card saved" else "Save player card", fontWeight = FontWeight.Bold) }
    }
}
