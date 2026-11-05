package com.courtquestpro

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.max
import kotlin.random.Random

private val CourtGreen = Color(0xFFFF783D)
private val CourtLight = Color(0xFF322722)
private val Ink = Color(0xFFF7F3EF)
private val Orange = Color(0xFFFFA35C)
private val Muted = Color(0xFFB7ACA6)

data class Drill(
    val id: String, val category: String, val title: String, val level: String,
    val minutes: Int, val instructions: String, val target: String, val videoQuery: String
)

data class Slot(val id: String, val drillId: String, var complete: Boolean = false)
data class DrillResult(val date: String, val slotId: String, val drillId: String, val completed: Int, val attempted: Int)
data class MatchRecord(
    val date: String, val player: String, val opponent: String,
    val playerScore: Int, val opponentScore: Int, val winner: String, val xp: Int
)
data class SaveState(
    var playerName: String = "",
    var difficulty: String = "Intermediate",
    var totalXp: Int = 0,
    var currentStreak: Int = 0,
    var longestStreak: Int = 0,
    var lastStreakDate: String = "",
    var activeDate: String = "",
    var slots: MutableList<Slot> = mutableListOf(),
    var championshipDone: Boolean = false,
    var playerScore: Int = 0,
    var opponentScore: Int = 0,
    var pointHistory: MutableList<Boolean>? = null,
    var matchPlayer: String = "",
    var matchOpponent: String = "",
    var matches: MutableList<MatchRecord> = mutableListOf(),
    var awardedKeys: MutableList<String> = mutableListOf(),
    var drillResults: MutableList<DrillResult>? = null,
    var drillDrafts: MutableList<DrillDraft>? = null,
    var showWarmupDone: Boolean = false
)

private val drills = listOf(
    Drill("s1","Shooting","Five-Spot Form Shooting","Beginner",6,"Shoot from five comfortable spots. Reset your feet and hold your follow-through after each shot.","Make 3 shots from each spot.","basketball five spot shooting drill"),
    Drill("s2","Shooting","Catch, Set, Shoot","Intermediate",6,"Start balanced, receive a pass or self-toss, square to the hoop, and shoot. Retrieve the ball safely.","Complete 15 controlled attempts.","basketball catch and shoot shooting drill"),
    Drill("s3","Shooting","Move Into Midrange","Advanced",6,"Start at a wing, make a controlled change of direction, stop under control, then shoot.","Complete 10 balanced attempts from both sides.","basketball midrange footwork drill"),
    Drill("b1","Ball Handling","Stationary Crossover Control","Beginner",6,"Stay in an athletic stance and cross the ball below your waist. Keep your eyes up and use a controlled pace.","30 clean crossovers, then switch direction.","basketball stationary crossover drill"),
    Drill("b2","Ball Handling","Change-of-Pace Lane","Intermediate",6,"Dribble from baseline to half court slowly, accelerate for a few steps, then decelerate under control.","Complete 6 trips without losing control.","basketball change of pace dribbling drill"),
    Drill("b3","Ball Handling","Combo Into Drive","Expert",6,"Combine two controlled moves, protect the ball, then accelerate toward an open lane. Keep the space clear.","Complete 8 clean combinations on each hand.","basketball combo dribble attack drill"),
    Drill("f1","Finishing","Right and Left Layups","Beginner",6,"Approach at a comfortable speed, use the correct footwork, and finish gently off the backboard with each hand.","Make 5 layups per side.","basketball right left hand layup drill"),
    Drill("f2","Finishing","Change Direction Finish","Intermediate",6,"Approach a cone or court line, change direction without slipping, and finish with the hand farthest from the imaginary defender.","Complete 8 finishes on each side.","basketball change direction finishing drill"),
    Drill("f3","Finishing","Reverse Finish Footwork","Advanced",6,"Practice the path under the basket at a controlled pace. Protect the ball and finish on the far side of the rim.","Complete 6 controlled reverse layups per side.","basketball reverse layup drill"),
    Drill("d1","Defense","Defensive Slide Intervals","Beginner",6,"Stay balanced with knees bent. Slide without crossing your feet, then rest and reset. Use a dry, non-slippery surface.","4 rounds of 20 seconds with 20 seconds rest.","basketball defensive slide drill"),
    Drill("d2","Defense","Closeout and Contest","Intermediate",6,"Approach under control, shorten your steps near the shooter, raise your hands, and avoid body contact or jumping into them.","Complete 8 controlled closeouts each side.","basketball closeout defense drill"),
    Drill("d3","Defense","Mirror Footwork","Expert",6,"With a partner, mirror lateral movements at a safe distance. Stay balanced and stop if either player loses footing.","4 rounds of 30 seconds.","basketball mirror defense footwork drill"),
    Drill("r1","Rebounding","Box-Out Footwork","Beginner",6,"Find the ball, turn toward the opponent's path, establish legal position without pushing, then locate the rebound.","Practice 10 controlled box-out reps.","basketball box out rebounding drill"),
    Drill("r2","Rebounding","Secure and Outlet","Intermediate",6,"Jump only when safe, secure the ball with two hands, land balanced, pivot, and make a controlled outlet pass.","Complete 10 rebound-to-outlet reps.","basketball rebound outlet pass drill"),
    Drill("r3","Rebounding","Partner Shot and Rebound","Advanced",6,"A partner takes a comfortable shot. Read the bounce, move into safe position, secure the ball, and reset.","Complete 10 rebounds, alternating roles.","basketball partner rebounding drill"),
    Drill("p1","Passing","Target Chest and Bounce Passes","Beginner",6,"Aim at a partner's chest for a chest pass or the floor point that lets a bounce pass reach their waist.","Complete 10 accurate passes of each type.","basketball chest bounce pass drill"),
    Drill("p2","Passing","Pass and Cut","Intermediate",6,"Pass to a partner, cut decisively into open space, show your hands, and receive under control.","Complete 10 pass-and-cut sequences.","basketball pass and cut drill"),
    Drill("p3","Passing","Rebound Outlet Decisions","Advanced",6,"Secure a simulated rebound, scan for a partner, pivot without traveling, and choose a clear passing lane.","Complete 8 accurate outlet passes.","basketball outlet passing drill")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CourtQuestApp { url -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } }
    }
}

@Composable
fun CourtQuestApp(openUrl: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val gson = remember { Gson() }
    val prefs = remember { context.getSharedPreferences("court_quest_pro", 0) }
    var save by remember {
        mutableStateOf(
            runCatching {
                val raw = prefs.getString("state", null)
                if (raw == null) SaveState() else gson.fromJson(raw, SaveState::class.java) ?: SaveState()
            }.getOrDefault(SaveState())
        )
    }
    fun persist() { prefs.edit().putString("state", gson.toJson(save)).apply() }
    fun refresh() { save = gson.fromJson(gson.toJson(save), SaveState::class.java); persist() }

    var screen by remember { mutableStateOf("home") }
    var selectedSlot by remember { mutableIntStateOf(-1) }
    var spinning by remember { mutableStateOf(false) }
    var playerNameInput by remember { mutableStateOf(save.playerName) }
    var opponentInput by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var recap by remember { mutableStateOf<MatchRecord?>(null) }

    val today = LocalDate.now(ZoneId.systemDefault()).toString()
    LaunchedEffect(today) {
        if (save.activeDate != today) {
            save.drillDrafts = mutableListOf()
            save.activeDate = today
            save.slots = makeDailySlots(save.difficulty).toMutableList()
            save.championshipDone = false
            save.playerScore = 0
            save.opponentScore = 0
            save.pointHistory = mutableListOf()
            save.showWarmupDone = false
            refresh()
        } else if (save.slots.isEmpty()) {
            save.slots = makeDailySlots(save.difficulty).toMutableList()
            save.drillDrafts = mutableListOf()
            refresh()
        } else {
            val retained = DrillDraftRecovery.prune(save.drillDrafts.orEmpty(), today, save.slots) { id ->
                drills.firstOrNull { it.id == id }?.minutes?.coerceAtLeast(1)?.times(60)
            }
            if (retained != save.drillDrafts.orEmpty()) {
                save.drillDrafts = retained.toMutableList()
                refresh()
            }
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = CourtGreen, onPrimary = Color(0xFF1B1411), secondary = Orange,
            background = Color(0xFF121315), surface = Color(0xFF212124), onSurface = Ink
        )
    ) {
        Scaffold(
            containerColor = Color(0xFF121315),
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF17191C), tonalElevation = 0.dp) {
                    NavigationBarItem(selected = screen == "home", onClick = { screen = "home" }, icon = { Icon(Icons.Default.SportsBasketball, "Court dashboard") }, label = { Text("COURT", fontWeight = FontWeight.Bold, fontSize = 10.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF151515), selectedTextColor = Orange, indicatorColor = CourtGreen, unselectedIconColor = Muted, unselectedTextColor = Muted))
                    NavigationBarItem(selected = screen == "career", onClick = { screen = "career" }, icon = { Icon(Icons.Default.Insights, "Player development") }, label = { Text("PROGRESS", fontWeight = FontWeight.Bold, fontSize = 10.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF151515), selectedTextColor = Orange, indicatorColor = CourtGreen, unselectedIconColor = Muted, unselectedTextColor = Muted))
                    NavigationBarItem(selected = screen == "settings", onClick = { screen = "settings" }, icon = { Icon(Icons.Default.Tune, "Player settings") }, label = { Text("SETUP", fontWeight = FontWeight.Bold, fontSize = 10.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF151515), selectedTextColor = Orange, indicatorColor = CourtGreen, unselectedIconColor = Muted, unselectedTextColor = Muted))
                }
            }
        ) { padding ->
            AnimatedContent(targetState = screen, label = "screen-transition", modifier = Modifier.padding(padding)) { target ->
                when (target) {
                    "home" -> HomeScreen(
                        save = save, onSpin = {
                            if (save.slots.all { it.complete }) screen = "champSetup"
                            else {
                                val candidates = save.slots.indices.filter { !save.slots[it].complete }
                                selectedSlot = candidates.firstOrNull { index ->
                                    save.drillDrafts.orEmpty().any { draft ->
                                        draft.date == save.activeDate &&
                                            draft.slotId == save.slots[index].id &&
                                            draft.drillId == save.slots[index].drillId
                                    }
                                } ?: candidates.random()
                                screen = "drill"
                            }
                        },
                        onStartChamp = { screen = "champSetup" },
                        onCareer = { screen = "career" }
                    )
                    "drill" -> {
                        val slot = save.slots.getOrNull(selectedSlot)
                        val drill = drills.firstOrNull { it.id == slot?.drillId } ?: drills.first()
                        DrillScreen(
                            drill = drill, slotNumber = selectedSlot + 1, complete = slot?.complete == true,
                            previousResults = save.drillResults.orEmpty().filter { it.drillId == drill.id },
                            draft = slot?.let {
                                DrillDraftRecovery.recover(save.drillDrafts.orEmpty(), save.activeDate,
                                    it, drill.minutes.coerceAtLeast(1) * 60)
                            } ?: DrillDraft(save.activeDate, slot?.id ?: "", drill.id,
                                remainingSeconds = drill.minutes.coerceAtLeast(1) * 60),
                            onDraft = { updated ->
                                val currentSlot = save.slots.getOrNull(selectedSlot)
                                if (currentSlot != null && save.activeDate == today &&
                                    DrillDraftRecovery.valid(updated, today, currentSlot,
                                        drill.minutes.coerceAtLeast(1) * 60)) {
                                save.drillDrafts = (save.drillDrafts.orEmpty().filterNot {
                                    it.date == updated.date && it.slotId == updated.slotId
                                } + updated).toMutableList()
                                persist()
                                }
                            },
                            onVideo = { openUrl("https://www.youtube.com/results?search_query=" + Uri.encode(drill.videoQuery)) },
                            onComplete = { completed, attempted ->
                                if (selectedSlot in save.slots.indices && !save.slots[selectedSlot].complete) {
                                    val result = DrillResult(save.activeDate, save.slots[selectedSlot].id, drill.id, completed, attempted)
                                    save.drillResults = (save.drillResults.orEmpty() + result).toMutableList()
                                    save.slots[selectedSlot].complete = true
                                    save.drillDrafts = save.drillDrafts.orEmpty().filterNot {
                                        it.date == save.activeDate && it.slotId == save.slots[selectedSlot].id
                                    }.toMutableList()
                                    award(save, "drill_${save.activeDate}_${save.slots[selectedSlot].id}", 50)
                                    if (save.slots.all { it.complete }) award(save, "six_${save.activeDate}", 100)
                                    refresh()
                                }
                                screen = "home"
                            },
                            onBack = { screen = "home" }
                        )
                    }
                    "champSetup" -> ChampionshipSetup(
                        playerName = playerNameInput, opponentName = opponentInput, error = error,
                        onPlayerName = { playerNameInput = it }, onOpponentName = { opponentInput = it },
                        onStart = {
                            if (playerNameInput.trim().isBlank()) error = "Enter your name first."
                            else if (opponentInput.trim().isBlank()) error = "Enter your opponent's name."
                            else {
                                save.playerName = playerNameInput.trim()
                                save.matchPlayer = save.playerName
                                save.matchOpponent = opponentInput.trim()
                                save.playerScore = 0; save.opponentScore = 0
                                save.pointHistory = mutableListOf()
                                save.championshipDone = false
                                refresh()
                                error = ""
                                screen = "score"
                            }
                        },
                        onBack = { screen = "home" }
                    )
                    "score" -> ScoreScreen(
                        player = save.matchPlayer, opponent = save.matchOpponent,
                        playerScore = save.playerScore, opponentScore = save.opponentScore,
                        onPoint = { playerPoint ->
                            if (save.playerScore < 11 && save.opponentScore < 11) {
                                val updated = Scorekeeper.point(
                                    Scorekeeper.Score(save.playerScore, save.opponentScore, save.pointHistory),
                                    playerPoint
                                )
                                save.playerScore = updated.player
                                save.opponentScore = updated.opponent
                                save.pointHistory = updated.history?.toMutableList()
                                if (save.playerScore >= 11 || save.opponentScore >= 11) {
                                    val winner = if (save.playerScore > save.opponentScore) save.matchPlayer else save.matchOpponent
                                    val won = winner == save.matchPlayer
                                    val matchXp = 100 + if (won) 50 else 0
                                    award(save, "champ_${save.activeDate}", 100)
                                    if (won) award(save, "win_${save.activeDate}", 50)
                                    save.championshipDone = true
                                    val record = MatchRecord(save.activeDate, save.matchPlayer, save.matchOpponent,
                                        save.playerScore, save.opponentScore, winner, matchXp)
                                    save.matches.add(0, record)
                                    updateStreak(save, LocalDate.parse(save.activeDate), LocalDate.now(ZoneId.systemDefault()).toString())
                                    recap = record
                                    refresh()
                                    screen = "recap"
                                } else refresh()
                            }
                        },
                        onUndo = {
                            val updated = Scorekeeper.undo(
                                Scorekeeper.Score(save.playerScore, save.opponentScore, save.pointHistory)
                            )
                            if (updated.player != save.playerScore || updated.opponent != save.opponentScore) {
                                save.playerScore = updated.player
                                save.opponentScore = updated.opponent
                                save.pointHistory = updated.history?.toMutableList()
                                refresh()
                            }
                        },
                        onBack = { screen = "home" }
                    )
                    "recap" -> recap?.let { MatchRecapScreen(it, save.totalXp, onHome = { screen = "home" }, onCareer = { screen = "career" }) } ?: HomeScreen(save, { screen = "drill" }, { screen = "champSetup" }, { screen = "career" })
                    "career" -> CareerScreen(save, onHome = { screen = "home" })
                    "settings" -> SettingsScreen(save.difficulty, onDifficulty = { save.difficulty = it; refresh() }, onSaveName = {
                        save.playerName = it.trim(); playerNameInput = save.playerName; refresh()
                    })
                    else -> HomeScreen(save, { screen = "drill" }, { screen = "champSetup" }, { screen = "career" })
                }
            }
        }
    }
}

private fun makeDailySlots(level: String): List<Slot> {
    val categoryNames = listOf("Shooting", "Ball Handling", "Finishing", "Defense", "Rebounding", "Passing")
    val chosen = mutableListOf<Drill>()
    repeat(6) {
        val category = categoryNames.random()
        val options = drills.filter { it.category == category && it.id !in chosen.map { d -> d.id } }
        chosen.add((options.ifEmpty { drills.filter { d -> d.id !in chosen.map { it.id } } }).random())
    }
    return chosen.mapIndexed { i, d -> Slot("${LocalDate.now()}_${i}_${Random.nextInt(100000)}", d.id) }
}
private fun award(s: SaveState, key: String, xp: Int) {
    if (key !in s.awardedKeys) { s.awardedKeys.add(key); s.totalXp += xp }
}
private fun updateStreak(s: SaveState, sessionDate: LocalDate, today: String) {
    if (sessionDate.toString() != today || !s.slots.all { it.complete }) return
    val previous = s.lastStreakDate
    if (previous == sessionDate.toString()) return
    s.currentStreak = if (previous == sessionDate.minusDays(1).toString()) s.currentStreak + 1 else 1
    s.longestStreak = max(s.longestStreak, s.currentStreak)
    s.lastStreakDate = sessionDate.toString()
}

@Composable
private fun AppHeader(title: String, subtitle: String = "") {
    Column(
        Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF382118), Color(0xFF1D1D21), Color(0xFF121315))))
            .padding(start = 22.dp, end = 22.dp, top = 27.dp, bottom = 25.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(35.dp).clip(RoundedCornerShape(11.dp)).background(CourtGreen), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.SportsBasketball, null, tint = Color(0xFF171719), modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text("COURTQUEST", color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.5.sp)
            Spacer(Modifier.weight(1f))
            Text("PLAYER LAB", color = Orange, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        }
        Spacer(Modifier.height(27.dp))
        Text(title, color = Ink, fontSize = 34.sp, fontWeight = FontWeight.Black, lineHeight = 37.sp)
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(9.dp))
            Text(subtitle, color = Muted, fontSize = 12.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun CourtSectionTitle(number: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(number, color = CourtGreen, fontSize = 11.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.width(10.dp))
        Text(title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
    }
}

@Composable
private fun HomeScreen(save: SaveState, onSpin: () -> Unit, onStartChamp: () -> Unit, onCareer: () -> Unit) {
    val done = save.slots.count { it.complete }
    val resumable = save.slots.count { slot ->
        !slot.complete && save.drillDrafts.orEmpty().any { draft ->
            draft.date == save.activeDate && draft.slotId == slot.id && draft.drillId == slot.drillId
        }
    }
    val progress by animateFloatAsState(if (save.slots.isEmpty()) 0f else done / 6f, animationSpec = tween(650), label = "progress")
    LazyColumn(Modifier.fillMaxSize()) {
        item { AppHeader("${save.playerName.ifBlank { "Vincent" }}'s court.", "BUILD YOUR GAME  /  ONE SESSION AT A TIME") }
        item {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("TOTAL XP", save.totalXp.toString(), Modifier.weight(1f))
                    StatCard("RANK", rankFor(save.totalXp), Modifier.weight(1f))
                    StatCard("STREAK", "${save.currentStreak} DAYS", Modifier.weight(1f))
                }
                CourtSectionTitle("01 /", "DAILY WORKOUT")
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1D21)), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("TODAY'S TRAINING", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                Text("$done / 6 drills", fontSize = 29.sp, fontWeight = FontWeight.Black, color = Ink)
                            }
                            Box(Modifier.size(56.dp).clip(CircleShape).background(CourtLight), contentAlignment = Alignment.Center) {
                                Text("${(progress * 100).toInt()}%", color = Orange, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = CourtGreen, trackColor = CourtLight)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onSpin, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp)) {
                            Icon(Icons.Default.Casino, null); Spacer(Modifier.width(8.dp))
                            Text(if (done == 6) "TRAINING COMPLETE · PLAY 1-ON-1" else if (resumable > 0) "RESUME TRAINING" else "START NEXT DRILL", fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(if (resumable > 0) "$resumable unfinished session(s) ready to resume" else "6 skill sessions · your progress is saved automatically", color = Muted, fontSize = 12.sp)
                    }
                }
                CourtSectionTitle("02 /", "GAME DAY")
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF38251F)), shape = RoundedCornerShape(22.dp)) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(50.dp).clip(CircleShape).background(Orange), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.EmojiEvents, null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("GAME DAY  /  1-ON-1", fontWeight = FontWeight.ExtraBold, color = Ink)
                            Text("Real 1-on-1. First to 11. At 10–10, next point wins.", fontSize = 12.sp, color = Muted)
                        }
                        IconButton(onClick = onStartChamp) { Icon(Icons.Default.ChevronRight, null, tint = Ink) }
                    }
                }
                CourtSectionTitle("03 /", "SKILL DEVELOPMENT")
                val categories = listOf("Shooting" to Icons.Default.SportsBasketball, "Ball Handling" to Icons.Default.Bolt, "Finishing" to Icons.Default.Sports, "Defense" to Icons.Default.Shield, "Rebounding" to Icons.Default.ArrowUpward, "Passing" to Icons.Default.SwapHoriz)
                categories.chunked(2).forEach { pair ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        pair.forEach { (name, icon) ->
                            Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color(0xFF212124)), shape = RoundedCornerShape(16.dp)) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(icon, null, tint = CourtGreen)
                                    Spacer(Modifier.width(8.dp))
                                    Text(name, color = Ink, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                OutlinedButton(onClick = onCareer, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Insights, null); Spacer(Modifier.width(8.dp)); Text("View player development") }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color(0xFF212124)), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(vertical = 13.dp, horizontal = 10.dp)) {
            Text(label, color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(Modifier.height(5.dp))
            Text(value, color = Ink, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun DrillSessionTimer(minutes: Int, initialRemaining: Int, onRemaining: (Int) -> Unit) {
    val initialSeconds = minutes.coerceAtLeast(1) * 60
    var remaining by remember(minutes) { mutableIntStateOf(initialRemaining.coerceIn(0, initialSeconds)) }
    var running by remember(minutes) { mutableStateOf(false) }
    LaunchedEffect(running, remaining) {
        if (running && remaining > 0) {
            delay(1000L)
            remaining = (remaining - 1).coerceAtLeast(0)
            onRemaining(remaining)
            if (remaining == 0) running = false
        }
    }
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF212124)), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (remaining == 0) "TRAINING CLOCK · FINISHED" else if (running) "TRAINING CLOCK · RUNNING" else "TRAINING CLOCK · PAUSED", color = Orange, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Text("%02d:%02d".format(remaining / 60, remaining % 60),
                color = Ink, fontSize = 46.sp, fontWeight = FontWeight.ExtraBold)
            LinearProgressIndicator(
                progress = { remaining.toFloat() / initialSeconds },
                modifier = Modifier.fillMaxWidth().height(7.dp).clip(CircleShape),
                color = Orange, trackColor = CourtLight
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { if (remaining == 0) { remaining = initialSeconds; onRemaining(remaining) }; running = !running },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(if (running) Icons.Default.Pause else Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (running) "PAUSE" else if (remaining == 0) "RESTART" else if (remaining < initialSeconds) "RESUME" else "START")
                }
                OutlinedButton(
                    onClick = { running = false; remaining = initialSeconds; onRemaining(remaining) },
                    shape = RoundedCornerShape(12.dp)
                ) { Text("RESET") }
            }
            Text(
                if (remaining == 0) "Time complete. Record the drill only after completing its target."
                else "Timer runs while this drill is open. Pause for recovery as needed.",
                color = Muted, fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DrillScreen(drill: Drill, slotNumber: Int, complete: Boolean, previousResults: List<DrillResult>, draft: DrillDraft, onDraft: (DrillDraft) -> Unit, onVideo: () -> Unit, onComplete: (Int, Int) -> Unit, onBack: () -> Unit) {
    var completedCount by remember(drill.id, slotNumber) { mutableIntStateOf(draft.successful) }
    var attemptedCount by remember(drill.id, slotNumber) { mutableIntStateOf(draft.attempted) }
    val best = previousResults.maxByOrNull { it.completed }
    val measurement = measurementFor(drill.category)
    val requiredCount = DrillTargets.minimum(drill.id)
    var targetConfirmed by remember(drill.id, slotNumber) { mutableStateOf(draft.targetConfirmed) }
    var timerSeconds by remember(drill.id, slotNumber) { mutableIntStateOf(draft.remainingSeconds) }
    fun saveDraft() {
        onDraft(draft.copy(successful = completedCount, attempted = attemptedCount,
            remainingSeconds = timerSeconds, targetConfirmed = targetConfirmed))
    }
    val meetsCount = DrillTargets.eligible(drill.id, completedCount, attemptedCount)
    LazyColumn(Modifier.fillMaxSize()) {
        item { AppHeader("Training / $slotNumber of 6", drill.category.uppercase()) }
        item {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF212124)), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Text(drill.category.uppercase(), color = CourtGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                        Spacer(Modifier.height(7.dp))
                        Text(drill.title, fontSize = 29.sp, fontWeight = FontWeight.Black, color = Ink, lineHeight = 33.sp)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Pill("${drill.minutes} MIN")
                            Pill(drill.level.uppercase())
                        }
                        Spacer(Modifier.height(18.dp))
                        Text("HOW TO DO IT", fontWeight = FontWeight.ExtraBold, color = Ink)
                        Spacer(Modifier.height(7.dp))
                        Text(drill.instructions, color = Muted, lineHeight = 22.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("YOUR TARGET", fontWeight = FontWeight.ExtraBold, color = Ink)
                        Spacer(Modifier.height(6.dp))
                        Text(drill.target, color = CourtGreen, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(16.dp))
                        Text("SAFETY CHECK", fontWeight = FontWeight.ExtraBold, color = Ink)
                        Spacer(Modifier.height(5.dp))
                        Text("Use a dry, clear court. Warm up first, keep movements controlled, and stop if the surface is slippery or something hurts.", color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
                    }
                }
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF212124)), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("02 / LIVE PERFORMANCE", color = Orange, fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.3.sp)
                        Text("Log each attempt as it happens", color = Muted, fontSize = 12.sp)
                        Text("$completedCount / ${requiredCount ?: 0} TARGET", color = Ink, fontSize = 26.sp, fontWeight = FontWeight.Black)
                        LinearProgressIndicator(
                            progress = { if (requiredCount != null && requiredCount > 0) (completedCount.toFloat() / requiredCount).coerceIn(0f, 1f) else 0f },
                            modifier = Modifier.fillMaxWidth().height(9.dp).clip(CircleShape),
                            color = CourtGreen, trackColor = CourtLight
                        )
                        Text(
                            "${measurement.successLabel} / ${measurement.attemptLabel}",
                            color = Ink, fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { if (completedCount > 0) { completedCount--; attemptedCount--; saveDraft() } }) { Text("−") }
                            Text("$completedCount", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            Button(onClick = { completedCount++; attemptedCount++; saveDraft() }) { Text("+ " + measurement.successButton) }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { if (attemptedCount > completedCount) { attemptedCount--; saveDraft() } }) { Text("−") }
                            Text("$attemptedCount · ${measurement.attemptLabel}", color = Ink, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            OutlinedButton(onClick = { attemptedCount++; saveDraft() }) { Text("+ MISSED / OTHER") }
                        }
                        if (best != null) Text("PERSONAL BEST  ·  ${best.completed} successful / ${best.attempted} attempts", color = Muted, fontSize = 12.sp)
                        Text(measurement.description + " Each successful action also counts as an attempt. Use missed / other for attempts without success. Progress is saved as you go.", color = Muted, fontSize = 12.sp)
                        Text("TARGET CHECK · ${requiredCount ?: 0} successful actions minimum", color = if (meetsCount) Orange else Muted, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = targetConfirmed, onCheckedChange = { targetConfirmed = it; saveDraft() })
                            Text("I completed the full written target, including required sides, intervals, rest, and safe technique.", color = Ink, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        }
                        Text(
                            when {
                                complete -> "THIS DRILL IS ALREADY COMPLETE"
                                meetsCount && targetConfirmed -> "READY TO FINISH · SAVE YOUR RECORDED RESULTS"
                                meetsCount -> "TARGET REACHED · CONFIRM THE FULL DRILL TO FINISH"
                                else -> "KEEP WORKING · REACH THE TARGET TO UNLOCK COMPLETION"
                            },
                            color = if (!complete && meetsCount && targetConfirmed) CourtGreen else Muted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                DrillSessionTimer(drill.minutes, timerSeconds) { timerSeconds = it; saveDraft() }
                Button(onClick = onVideo, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(15.dp)) {
                    Icon(Icons.Default.PlayCircle, null); Spacer(Modifier.width(8.dp)); Text("WATCH VIDEO DEMONSTRATION")
                }
                Button(onClick = { onComplete(completedCount, attemptedCount) }, enabled = !complete && meetsCount && targetConfirmed, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = if (complete) Muted else CourtGreen), shape = RoundedCornerShape(15.dp)) {
                    Icon(Icons.Default.CheckCircle, null); Spacer(Modifier.width(8.dp)); Text(if (complete) "SLOT COMPLETED" else "SAVE RESULTS & COMPLETE · +50 XP", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to dashboard") }
            }
        }
    }
}
@Composable private fun Pill(text: String) {
    Surface(color = CourtLight, shape = RoundedCornerShape(50)) { Text(text, Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = CourtGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun ChampionshipSetup(playerName: String, opponentName: String, error: String, onPlayerName: (String) -> Unit, onOpponentName: (String) -> Unit, onStart: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        AppHeader("Championship setup", "Two players. One court. First to 11.")
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Enter both names to start your match.", color = Muted)
            OutlinedTextField(value = playerName, onValueChange = onPlayerName, modifier = Modifier.fillMaxWidth(), label = { Text("Your name") }, singleLine = true, leadingIcon = { Icon(Icons.Default.Person, null) }, shape = RoundedCornerShape(14.dp))
            OutlinedTextField(value = opponentName, onValueChange = onOpponentName, modifier = Modifier.fillMaxWidth(), label = { Text("Opponent's name (required)") }, singleLine = true, leadingIcon = { Icon(Icons.Default.Group, null) }, shape = RoundedCornerShape(14.dp))
            if (error.isNotBlank()) Text(error, color = Color(0xFFB3261E), fontWeight = FontWeight.SemiBold)
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF302722)), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("GAME RULES", color = Ink, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(6.dp))
                    Text("• First player to 11 points wins\n• At 10–10: sudden death, next point wins\n• Score honestly after each point\n• Undo is available for accidental taps", color = Muted, lineHeight = 22.sp)
                }
            }
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(15.dp)) { Text("ENTER THE CHAMPIONSHIP", fontWeight = FontWeight.ExtraBold) }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
        }
    }
}

@Composable
private fun ScoreScreen(player: String, opponent: String, playerScore: Int, opponentScore: Int, onPoint: (Boolean) -> Unit, onUndo: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        AppHeader("Championship", if (playerScore == 10 && opponentScore == 10) "SUDDEN DEATH · NEXT POINT WINS" else "FIRST TO 11 WINS")
        Spacer(Modifier.height(16.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ScorePanel(player, playerScore, CourtGreen, Modifier.weight(1f))
            ScorePanel(opponent, opponentScore, Orange, Modifier.weight(1f))
        }
        Spacer(Modifier.height(20.dp))
        Text(if (playerScore == 10 && opponentScore == 10) "SUDDEN DEATH" else "TAP THE SCORER AFTER EACH POINT", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, color = if (playerScore == 10 && opponentScore == 10) Orange else Muted, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onPoint(true) }, modifier = Modifier.weight(1f).height(112.dp), shape = RoundedCornerShape(20.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Add, null, modifier = Modifier.size(32.dp)); Text("+1", fontSize = 29.sp, fontWeight = FontWeight.ExtraBold); Text(player.take(14), fontSize = 12.sp) }
            }
            Button(onClick = { onPoint(false) }, modifier = Modifier.weight(1f).height(112.dp), colors = ButtonDefaults.buttonColors(containerColor = Orange), shape = RoundedCornerShape(20.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.Add, null, modifier = Modifier.size(32.dp)); Text("+1", fontSize = 29.sp, fontWeight = FontWeight.ExtraBold); Text(opponent.take(14), fontSize = 12.sp) }
            }
        }
        Spacer(Modifier.height(18.dp))
        OutlinedButton(onClick = onUndo, modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(50.dp), shape = RoundedCornerShape(14.dp)) { Icon(Icons.Default.Undo, null); Spacer(Modifier.width(8.dp)); Text("UNDO LAST POINT") }
        Spacer(Modifier.height(8.dp))
        Text("Only tap after a point has actually been scored.", modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), color = Muted, fontSize = 12.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("Exit scorekeeper") }
    }
}
@Composable
private fun ScorePanel(name: String, score: Int, color: Color, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color(0xFF212124)), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.fillMaxWidth().padding(vertical = 22.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(color))
            Spacer(Modifier.height(9.dp))
            Text(name, fontSize = 15.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Ink, maxLines = 2)
            Spacer(Modifier.height(12.dp))
            Text(score.toString(), fontSize = 64.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
private fun MatchRecapScreen(record: MatchRecord, totalXp: Int, onHome: () -> Unit, onCareer: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        AppHeader("Final buzzer", "Championship complete")
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF302722)), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EmojiEvents, null, tint = Orange, modifier = Modifier.size(54.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("${record.winner} WINS!", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Ink, textAlign = TextAlign.Center)
                    Text("${record.playerScore}  —  ${record.opponentScore}", fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, color = CourtGreen)
                    Text("Final score", color = Muted)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("MATCH XP", "+${record.xp}", Modifier.weight(1f))
                StatCard("TOTAL XP", totalXp.toString(), Modifier.weight(1f))
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF212124)), shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("MATCH RECAP", fontWeight = FontWeight.ExtraBold, color = Ink)
                    Spacer(Modifier.height(7.dp))
                    Text("${record.player} vs ${record.opponent}. ${record.winner} took the win ${max(record.playerScore, record.opponentScore)}–${kotlin.math.min(record.playerScore, record.opponentScore)}. Keep showing up, work on one skill at a time, and bring that energy to the next run.", color = Muted, lineHeight = 22.sp)
                }
            }
            Button(onClick = onHome, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("BACK TO HOME") }
            OutlinedButton(onClick = onCareer, modifier = Modifier.fillMaxWidth()) { Text("VIEW CAREER STATS") }
        }
    }
}

@Composable
private fun CareerScreen(save: SaveState, onHome: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        item { AppHeader("${save.playerName.ifBlank { "Vincent" }} · Development", "RECORDED WORK  /  MEASURABLE PROGRESS") }
        item {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("TOTAL XP", save.totalXp.toString(), Modifier.weight(1f))
                    StatCard("RANK", rankFor(save.totalXp), Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("CURRENT STREAK", save.currentStreak.toString(), Modifier.weight(1f))
                    StatCard("LONGEST STREAK", save.longestStreak.toString(), Modifier.weight(1f))
                }
                Text("PLAYER DEVELOPMENT", color = Ink, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                val recorded = save.drillResults.orEmpty()
                if (recorded.isEmpty()) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF212124))) {
                        Column(Modifier.padding(18.dp)) {
                            Text("YOUR BASELINE STARTS HERE", color = Orange, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text("Complete a drill and record attempts to start building a real performance history. XP alone is not a skill rating.", color = Muted, fontSize = 13.sp)
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard("RECORDED DRILLS", recorded.size.toString(), Modifier.weight(1f))
                        StatCard("TOTAL ATTEMPTS", recorded.sumOf { it.attempted }.toString(), Modifier.weight(1f))
                    }
                    CourtSectionTitle("01 /", "TRAINING RECORD")
                    val validResults = recorded.filter { it.attempted > 0 && it.completed >= 0 && it.completed <= it.attempted }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard("TRAINING DAYS", validResults.map { it.date }.distinct().size.toString(), Modifier.weight(1f))
                        StatCard("DRILLS PRACTICED", validResults.map { it.drillId }.distinct().size.toString(), Modifier.weight(1f))
                    }
                    Text("Training days and drill variety measure activity, not skill.", color = Muted, fontSize = 12.sp)
                    CourtSectionTitle("02 /", "SKILL BREAKDOWN")
                    val categories = listOf("Shooting", "Ball Handling", "Finishing", "Defense", "Rebounding", "Passing")
                    categories.forEach { category ->
                        val results = recorded.filter { result ->
                            drills.any { it.id == result.drillId && it.category == category }
                        }
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF212124)), shape = RoundedCornerShape(18.dp)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(category.uppercase(), color = Ink, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                                    Text("${results.size} SESSIONS", color = Orange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                if (results.isEmpty()) {
                                    Text("No recorded results yet", color = Muted, fontSize = 12.sp)
                                } else {
                                    val attempts = results.sumOf { it.attempted }
                                    val successful = results.sumOf { it.completed }
                                    Text("$successful successful / $attempts attempts", color = Ink, fontWeight = FontWeight.SemiBold)
                                    if (attempts > 0) {
                                        LinearProgressIndicator(
                                            progress = { (successful.toFloat() / attempts).coerceIn(0f, 1f) },
                                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                                            color = Orange, trackColor = CourtLight
                                        )
                                        Text("${successful * 100 / attempts}% recorded success rate · not a skill rating", color = Muted, fontSize = 12.sp)
                                    }
                                    val best = results.maxByOrNull { it.completed }
                                    if (best != null) {
                                        val drillName = drills.firstOrNull { it.id == best.drillId }?.title ?: category
                                        Text("Best recorded: ${best.completed} / ${best.attempted} · $drillName", color = Muted, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                    CourtSectionTitle("03 /", "SAME-DRILL TRENDS")
                    val distinctDrills = recorded.map { it.drillId }.distinct()
                    distinctDrills.forEach { id ->
                        val title = drills.firstOrNull { it.id == id }?.title ?: id
                        val trend = DrillTrends.calculate(id, recorded)
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF212124)), shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(title, color = Ink, fontWeight = FontWeight.Bold)
                                if (trend == null) {
                                    Text("Record this drill again to establish a trend.", color = Muted, fontSize = 12.sp)
                                } else {
                                    val firstPct = (trend.firstRate * 100).toInt()
                                    val latestPct = (trend.latestRate * 100).toInt()
                                    val delta = trend.changePoints
                                    val direction = when {
                                        delta > 0.00001 -> "INCREASE"
                                        delta < -0.00001 -> "DECREASE"
                                        else -> "NO CHANGE"
                                    }
                                    Text("$firstPct% → $latestPct% · $direction", color = Orange, fontWeight = FontWeight.Bold)
                                    Text("${trend.sessions} recorded sessions · ${"%.1f".format(kotlin.math.abs(delta))} percentage-point difference", color = Muted, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    Text("Trends compare the earliest and latest valid session for the same drill. Results may reflect changing conditions or small samples; they do not establish a skill rating.", color = Muted, fontSize = 12.sp)
                    Text("Different drills have different difficulty and targets. These totals are descriptive; compare the same drill over time before drawing improvement conclusions.", color = Muted, fontSize = 12.sp)
                }
                CourtSectionTitle("04 /", "MATCH HISTORY")
                if (save.matches.isEmpty()) {
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF212124))) {
                        Text("Your championship results will show up here after your first game.", Modifier.padding(18.dp), color = Muted)
                    }
                } else save.matches.forEach { m ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF212124)), shape = RoundedCornerShape(16.dp)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${m.player} vs ${m.opponent}", color = Ink, fontWeight = FontWeight.Bold)
                                Text("${m.playerScore}–${m.opponentScore} · ${m.date}", color = Muted, fontSize = 12.sp)
                                Text("Winner: ${m.winner}", color = CourtGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text("+${m.xp} XP", color = CourtGreen, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
                OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) { Text("Back to home") }
            }
        }
    }
}

@Composable
private fun SettingsScreen(difficulty: String, onDifficulty: (String) -> Unit, onSaveName: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        AppHeader("Settings", "Make the app work for your game.")
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("DIFFICULTY LEVEL", fontWeight = FontWeight.ExtraBold, color = Ink)
            listOf("Beginner", "Intermediate", "Advanced", "Expert", "Elite").forEach { level ->
                Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(if (level == difficulty) CourtLight else Color(0xFF212124)).clickable { onDifficulty(level) }.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(level, fontWeight = FontWeight.Bold, color = Ink)
                        Text(difficultyDescription(level), color = Muted, fontSize = 12.sp)
                    }
                    RadioButton(selected = level == difficulty, onClick = { onDifficulty(level) })
                }
            }
            Divider()
            Text("PLAYER NAME", fontWeight = FontWeight.ExtraBold, color = Ink)
            OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Update saved name") }, singleLine = true)
            Button(onClick = { if (name.isNotBlank()) { onSaveName(name); name = "" } }, modifier = Modifier.fillMaxWidth()) { Text("SAVE NAME") }
            Text("Your progress is saved locally on this phone. Videos require internet.", color = Muted, fontSize = 12.sp)
        }
    }
}

private fun rankFor(xp: Int): String = when {
    xp >= 6000 -> "Legend"
    xp >= 4000 -> "MVP"
    xp >= 2500 -> "All-Star"
    xp >= 1500 -> "Gold"
    xp >= 800 -> "Silver"
    xp >= 300 -> "Bronze"
    else -> "Rookie"
}
private fun difficultyDescription(level: String) = when(level) {
    "Beginner" -> "Learn technique and movement basics."
    "Intermediate" -> "Build consistency and coordination."
    "Advanced" -> "Add movement, speed, and decisions."
    "Expert" -> "Combine skills under greater pressure."
    else -> "Challenging game-like drills, controlled safely."
}
