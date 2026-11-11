package com.courtquestpro

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
private fun GameHeader(title: String, detail: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(onClick = onBack, modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to Court")
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.semantics { heading() })
            Text(detail, color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
internal fun ChampionshipSetup(
    playerName: String, opponentName: String, error: String,
    onPlayerName: (String) -> Unit, onOpponentName: (String) -> Unit,
    onStart: () -> Unit, onBack: () -> Unit
) {
    val focus = LocalFocusManager.current
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 640.dp).fillMaxSize().imePadding()) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                GameHeader("Set the matchup.", "1-on-1 · First to 11", onBack)
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    CourtEyebrow("THE PLAYERS")
                    Text("Your game. Your scoreboard.", color = CourtQuestDesign.textPrimary,
                        style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(playerName, onPlayerName, modifier = Modifier.fillMaxWidth(),
                        label = { Text("Your name") }, singleLine = true,
                        isError = error.isNotBlank() && playerName.isBlank(),
                        leadingIcon = { Icon(Icons.Default.PersonOutline, null) },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next))
                    OutlinedTextField(opponentName, onOpponentName, modifier = Modifier.fillMaxWidth(),
                        label = { Text("Opponent's name (required)") }, singleLine = true,
                        isError = error.isNotBlank() && opponentName.isBlank(),
                        leadingIcon = { Icon(Icons.Default.PersonOutline, null) },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }))
                    if (error.isNotBlank()) {
                        Text(error, color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                    }
                    CourtRule()
                    CourtEyebrow("HOW IT PLAYS")
                    GameRule("01", "First to 11", "Record one point for the player who scored.")
                    GameRule("02", "Tied at 10? Next point wins.", "Sudden death keeps the finish simple.")
                    GameRule("03", "A tap you didn't mean? Undo it.", "Your score saves after every point.")
                    Spacer(Modifier.height(8.dp))
                }
            }
            Column(Modifier.fillMaxWidth().background(CourtQuestDesign.background)
                .padding(horizontal = 20.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CourtQuestPrimaryButton(onClick = { focus.clearFocus(); onStart() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Start game", Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun GameRule(number: String, title: String, detail: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(number, color = CourtQuestDesign.accent, style = MaterialTheme.typography.headlineSmall)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleMedium)
            Text(detail, color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
internal fun ScoreScreen(
    player: String, opponent: String, playerScore: Int, opponentScore: Int,
    onPoint: (Boolean) -> Unit, onUndo: () -> Unit, onBack: () -> Unit,
    canUndo: Boolean = playerScore + opponentScore > 0, lastScorer: String? = null
) {
    val suddenDeath = playerScore == 10 && opponentScore == 10
    val fontScale = LocalDensity.current.fontScale
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 680.dp).fillMaxSize().verticalScroll(rememberScrollState())) {
            GameHeader("1-on-1.", "First to 11 · One point at a time", onBack)
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                GameScoreboard(player, opponent, playerScore, opponentScore,
                    if (suddenDeath) "Sudden death" else "Live game")
                Text(if (suddenDeath) "Next point wins." else "Tap the player who scored.",
                    color = if (suddenDeath) CourtQuestDesign.accent else CourtQuestDesign.textSecondary,
                    style = MaterialTheme.typography.bodyMedium)
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    if (maxWidth / fontScale < 240.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            PointButton(player, true, { onPoint(true) }, Modifier.fillMaxWidth(), wide = true)
                            PointButton(opponent, false, { onPoint(false) }, Modifier.fillMaxWidth(), wide = true)
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PointButton(player, true, { onPoint(true) }, Modifier.weight(1f))
                            PointButton(opponent, false, { onPoint(false) }, Modifier.weight(1f))
                        }
                    }
                }
                OutlinedButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.AutoMirrored.Filled.Undo, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Undo last point", style = MaterialTheme.typography.labelLarge)
                }
                if (lastScorer != null) {
                    Text("Last point: $lastScorer", color = CourtQuestDesign.textSecondary,
                        style = MaterialTheme.typography.bodySmall)
                }
                CourtRule()
                Text("Score saved. You can leave and resume from Court today.",
                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) { Text("Back to Court") }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun PointButton(name: String, primary: Boolean, onClick: () -> Unit, modifier: Modifier, wide: Boolean = false) {
    Button(onClick = onClick, modifier = modifier.heightIn(min = 112.dp)
        .semantics { contentDescription = "Record point for $name" }, shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) CourtQuestDesign.accentVivid else CourtQuestDesign.surface,
            contentColor = if (primary) CourtQuestDesign.iconForeground else CourtQuestDesign.textPrimary),
        border = if (primary) null else BorderStroke(1.dp, CourtQuestDesign.controlOutline)) {
        if (wide) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("+1", style = MaterialTheme.typography.headlineLarge)
                Text(name, Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            }
        } else {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("+1", style = MaterialTheme.typography.headlineLarge)
                Text(name, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun GameScoreboard(player: String, opponent: String, playerScore: Int, opponentScore: Int, status: String) {
    val fontScale = LocalDensity.current.fontScale
    Surface(shape = RoundedCornerShape(24.dp), color = CourtQuestDesign.surface) {
        Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(
            CourtQuestDesign.warmSurface, CourtQuestDesign.surface))).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CourtEyebrow("THE SCORE", Modifier.weight(1f))
                CourtStatus(status)
            }
            BoxWithConstraints(Modifier.fillMaxWidth().semantics {
                liveRegion = LiveRegionMode.Polite
                stateDescription = "$player $playerScore, $opponent $opponentScore"
            }) {
                if (maxWidth / fontScale < 220.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ScoreLane(player, playerScore, "YOU", true, Modifier.fillMaxWidth(), wide = true)
                        CourtRule()
                        ScoreLane(opponent, opponentScore, "OPPONENT", false, Modifier.fillMaxWidth(), wide = true)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        ScoreLane(player, playerScore, "YOU", true, Modifier.weight(1f))
                        ScoreLane(opponent, opponentScore, "OPPONENT", false, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreLane(name: String, score: Int, role: String, primary: Boolean, modifier: Modifier, wide: Boolean = false) {
    val scoreColor = if (primary) CourtQuestDesign.accent else CourtQuestDesign.textPrimary
    if (wide) {
        Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CourtEyebrow(role, color = CourtQuestDesign.textSecondary)
                Text(name, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleMedium)
            }
            Text(score.toString(), color = scoreColor, style = MaterialTheme.typography.displayMedium)
        }
    } else {
        Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            CourtEyebrow(role, color = CourtQuestDesign.textSecondary)
            Text(name, color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleMedium)
            Text(score.toString(), color = scoreColor,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 88.sp, lineHeight = 92.sp))
        }
    }
}

@Composable
internal fun MatchRecapScreen(record: MatchRecord, totalXp: Int, onHome: () -> Unit, onCareer: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 640.dp).fillMaxSize().verticalScroll(rememberScrollState())) {
            GameHeader("Final buzzer.", gameDate(record.date), onHome)
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CheckCircle, null, tint = CourtQuestDesign.accent, modifier = Modifier.size(20.dp))
                    CourtEyebrow("GAME RECORDED")
                }
                Text("${record.winner} won.", color = CourtQuestDesign.textPrimary,
                    style = MaterialTheme.typography.displaySmall)
                GameScoreboard(record.player, record.opponent, record.playerScore, record.opponentScore, "Final")
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    CourtMetric("Game XP", "+${record.xp}", Modifier.weight(1f), accent = true)
                    CourtMetric("Total XP", totalXp.toString(), Modifier.weight(1f))
                }
                CourtRule()
                Text("Your players, final score and XP are saved in Progress.",
                    color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodyMedium)
                CourtQuestPrimaryButton(onClick = onCareer, modifier = Modifier.fillMaxWidth()) {
                    Text("View game history", Modifier.weight(1f))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(20.dp))
                }
                TextButton(onClick = onHome, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("Back to Court") }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
internal fun RecordedGameCard(record: MatchRecord, modifier: Modifier = Modifier) {
    CourtQuestPremiumSurface(modifier.fillMaxWidth(), PaddingValues(20.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CourtEyebrow(gameDate(record.date))
            RecordedScoreRow(record.player, record.playerScore, record.playerScore > record.opponentScore)
            RecordedScoreRow(record.opponent, record.opponentScore, record.opponentScore > record.playerScore)
            CourtRule()
            Text("${record.winner} won", color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleSmall)
            Text("+${record.xp} XP · Final", color = CourtQuestDesign.textSecondary, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RecordedScoreRow(name: String, score: Int, won: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(name, Modifier.weight(1f), color = CourtQuestDesign.textPrimary, style = MaterialTheme.typography.titleMedium)
        Text(score.toString(), color = if (won) CourtQuestDesign.accent else CourtQuestDesign.textSecondary,
            style = MaterialTheme.typography.headlineMedium)
    }
}

private fun gameDate(date: String): String = runCatching {
    LocalDate.parse(date).format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
}.getOrDefault(date)
