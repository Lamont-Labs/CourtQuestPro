package com.courtquestpro

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.max
import kotlin.random.Random

private val CourtGreen = CourtQuestDesign.accentVivid
private val CourtLight = Color(0xFF322722)
private val Ink = CourtQuestDesign.textPrimary
private val Orange = CourtQuestDesign.accent
private val Muted = CourtQuestDesign.textSecondary

data class Drill(
    val id: String, val category: String, val title: String, val level: String,
    val minutes: Int, val instructions: String, val target: String, val videoQuery: String,
    val minimumSuccesses: Int = 0
)

data class Slot(val id: String, val drillId: String, var complete: Boolean = false)
data class DrillResult(val date: String, val slotId: String, val drillId: String, val completed: Int, val attempted: Int,
    val practice: Boolean = false, val drillSnapshot: Drill? = null)
data class MatchRecord(
    val date: String, val player: String, val opponent: String,
    val playerScore: Int, val opponentScore: Int, val winner: String, val xp: Int
)
data class SaveState(
    var playerName: String = "",
    var difficulty: String = "Intermediate",
    var weeklyPlan: String = "Balanced",
    var trainingDays: MutableList<Int>? = null,
    var weeklyActivities: MutableList<String>? = null,
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
    var showWarmupDone: Boolean = false,
    var customDrills: MutableList<UserDrill>? = null,
    var favoriteDrillIds: MutableList<String>? = null,
    var practiceSession: PracticeSession? = null
)

internal val drills = listOf(
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

@OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
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
            }.getOrDefault(SaveState()),
            // refresh publishes a deep copy of an already-mutated save.
            // Referential equality keeps preferences and scores observable.
            referentialEqualityPolicy()
        )
    }
    fun persist() { prefs.edit().putString("state", gson.toJson(save)).apply() }
    fun refresh() { save = gson.fromJson(gson.toJson(save), SaveState::class.java); persist() }

    val catalog = TrainingLibrary.catalog(save)
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun notice(message: String) { scope.launch { snackbar.showSnackbar(message) } }

    val legacyProfileExists = remember { prefs.contains("state") }
    var onboardingComplete by remember {
        mutableStateOf(prefs.getBoolean("onboarding_complete", legacyProfileExists))
    }
    // Pin the first-install decision before daily state writes create a "state" entry.
    LaunchedEffect(Unit) {
        if (!prefs.contains("onboarding_complete")) {
            prefs.edit().putBoolean("onboarding_complete", legacyProfileExists).apply()
        }
    }
    var onboardingStep by remember { mutableIntStateOf(prefs.getInt("onboarding_step", 0).coerceIn(0, 4)) }
    var onboardingName by remember { mutableStateOf(prefs.getString("onboarding_name", save.playerName) ?: "") }
    var onboardingLevel by remember {
        mutableStateOf((prefs.getString("onboarding_level", save.difficulty) ?: "Intermediate")
            .takeIf { it in listOf("Beginner", "Intermediate", "Advanced") } ?: "Intermediate")
    }
    var onboardingFocus by remember {
        mutableStateOf((prefs.getString("onboarding_focus", save.weeklyPlan) ?: "Balanced")
            .takeIf { it in listOf("Balanced", "Shooting", "Ball Handling", "Finishing") } ?: "Balanced")
    }
    var onboardingDays by remember {
        mutableStateOf(
            (prefs.getString("onboarding_days", null)?.split(",")?.mapNotNull { it.toIntOrNull() }
                ?: save.trainingDays ?: listOf(1, 3, 5)).filter { it in 1..7 }.distinct().sorted()
        )
    }
    fun persistOnboardingDraft() {
        prefs.edit()
            .putInt("onboarding_step", onboardingStep)
            .putString("onboarding_name", onboardingName)
            .putString("onboarding_level", onboardingLevel)
            .putString("onboarding_focus", onboardingFocus)
            .putString("onboarding_days", onboardingDays.joinToString(","))
            .apply()
    }
    fun finishOnboarding() {
        save.playerName = onboardingName.trim().take(40)
        save.difficulty = onboardingLevel
        save.weeklyPlan = onboardingFocus
        save.trainingDays = onboardingDays.toMutableList()
        save.slots = makeDailySlots(save.difficulty, save.weeklyPlan, TrainingLibrary.available(save)).toMutableList()
        save.drillDrafts = mutableListOf()
        refresh()
        prefs.edit().putBoolean("onboarding_complete", true)
            .remove("onboarding_step").remove("onboarding_name").remove("onboarding_level")
            .remove("onboarding_focus").remove("onboarding_days").apply()
        onboardingComplete = true
    }
    var screen by rememberSaveable { mutableStateOf("home") }
    var librarySlotId by rememberSaveable { mutableStateOf<String?>(null) }
    var libraryFocusId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingDrillId by rememberSaveable { mutableStateOf<String?>(null) }
    var editorCategory by rememberSaveable { mutableStateOf("Shooting") }
    var selectedSlot by rememberSaveable { mutableIntStateOf(-1) }
    var playerNameInput by rememberSaveable { mutableStateOf(save.playerName) }
    var opponentInput by rememberSaveable { mutableStateOf("") }
    var error by rememberSaveable { mutableStateOf("") }
    var recap by remember { mutableStateOf<MatchRecord?>(null) }
    var recordedDrillId by rememberSaveable { mutableStateOf<String?>(null) }
    val recordedDrill = save.drillResults.orEmpty().lastOrNull { it.slotId == recordedDrillId }
    var recordedXp by rememberSaveable { mutableIntStateOf(0) }

    val today = LocalDate.now(ZoneId.systemDefault()).toString()
    fun openMatch() {
        error = ""
        screen = if (save.hasResumableMatch(today)) "score" else "champSetup"
    }
    fun openTraining() {
        if (save.slots.all { it.complete }) openMatch()
        else {
            val candidates = save.slots.indices.filter { !save.slots[it].complete }
            selectedSlot = candidates.firstOrNull { index ->
                save.drillDrafts.orEmpty().any { draft ->
                    draft.date == save.activeDate && draft.slotId == save.slots[index].id &&
                        draft.drillId == save.slots[index].drillId
                }
            } ?: candidates.random()
            screen = "drill"
        }
    }
    BackHandler(enabled = onboardingComplete && screen != "home") { screen = "home" }
    LaunchedEffect(today) {
        if (save.activeDate != today) {
            save.drillDrafts = mutableListOf()
            save.activeDate = today
            save.slots = makeDailySlots(save.difficulty, save.weeklyPlan, TrainingLibrary.available(save)).toMutableList()
            save.championshipDone = false
            save.playerScore = 0
            save.opponentScore = 0
            save.pointHistory = mutableListOf()
            save.matchPlayer = ""
            save.matchOpponent = ""
            save.showWarmupDone = false
            if (screen == "drill") screen = "home"
            refresh()
        } else if (save.slots.isEmpty()) {
            save.slots = makeDailySlots(save.difficulty, save.weeklyPlan, TrainingLibrary.available(save)).toMutableList()
            save.drillDrafts = mutableListOf()
            refresh()
        } else {
            val retained = DrillDraftRecovery.prune(save.drillDrafts.orEmpty(), today, save.slots) { id ->
                catalog.firstOrNull { it.id == id }?.minutes?.coerceAtLeast(1)?.times(60)
            }
            if (retained != save.drillDrafts.orEmpty()) {
                save.drillDrafts = retained.toMutableList()
                refresh()
            }
        }
    }

    fun openLibrary(slotId: String? = null) {
        librarySlotId = slotId
        libraryFocusId = null
        screen = "library"
    }
    @Composable fun HomeContent() {
        HomeScreen(save, { openTraining() }, { openMatch() }, { screen = "career" },
            { name -> save.playerName = name.trim(); playerNameInput = save.playerName; refresh() },
            { plan -> save.weeklyPlan = plan; refresh() },
            { day -> save.trainingDays = toggleTrainingDay(save.trainingDays, day); refresh() },
            { day, kind -> save.weeklyActivities = updateWeeklyActivity(save.weeklyActivities, day, kind); refresh() },
            onLibrary = { openLibrary() }, onSwap = { openLibrary(it) },
            onShuffle = {
                val changed = TrainingLibrary.shuffle(save)
                if (changed > 0) { refresh(); notice("$changed untouched drills shuffled.") }
                else notice("No suitable alternatives yet. Started and completed drills stay protected.")
            }, onResumePractice = { screen = "practice" })
    }

    CourtQuestTheme {
        Scaffold(
            modifier = Modifier.semantics { testTagsAsResourceId = true },
            snackbarHost = { SnackbarHost(snackbar) },
            containerColor = Color(0xFF121315),
            bottomBar = {
                if (onboardingComplete && screen in listOf("home", "library", "career", "gameHistory", "settings")) NavigationBar(containerColor = CourtQuestDesign.surface, tonalElevation = 0.dp) {
                    NavigationBarItem(selected = screen == "home", onClick = { screen = "home" }, icon = { Icon(Icons.Default.SportsBasketball, "Court dashboard") }, label = { Text("Court", fontWeight = FontWeight.Bold, fontSize = 12.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF151515), selectedTextColor = Orange, indicatorColor = CourtGreen, unselectedIconColor = Muted, unselectedTextColor = Muted))
                    NavigationBarItem(selected = screen == "library", onClick = { openLibrary() }, icon = { Icon(Icons.Default.FitnessCenter, "Drill library") }, label = { Text("Drills", fontWeight = FontWeight.Bold, fontSize = 12.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = CourtQuestDesign.iconForeground, selectedTextColor = Orange, indicatorColor = CourtGreen, unselectedIconColor = Muted, unselectedTextColor = Muted))
                    NavigationBarItem(selected = screen in listOf("career", "gameHistory"), onClick = { screen = "career" }, icon = { Icon(Icons.Default.Insights, "Player development") }, label = { Text("Progress", fontWeight = FontWeight.Bold, fontSize = 12.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF151515), selectedTextColor = Orange, indicatorColor = CourtGreen, unselectedIconColor = Muted, unselectedTextColor = Muted))
                    NavigationBarItem(selected = screen == "settings", onClick = { screen = "settings" }, icon = { Icon(Icons.Default.Tune, "Player settings") }, label = { Text("Setup", fontWeight = FontWeight.Bold, fontSize = 12.sp) }, colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF151515), selectedTextColor = Orange, indicatorColor = CourtGreen, unselectedIconColor = Muted, unselectedTextColor = Muted))
                }
            }
        ) { padding ->
            if (!onboardingComplete) {
                CourtOnboarding(
                    modifier = Modifier.padding(padding).consumeWindowInsets(padding),
                    step = onboardingStep,
                    name = onboardingName,
                    level = onboardingLevel,
                    focus = onboardingFocus,
                    days = onboardingDays,
                    onName = { onboardingName = it.take(40); persistOnboardingDraft() },
                    onLevel = { onboardingLevel = it; persistOnboardingDraft() },
                    onFocus = { onboardingFocus = it; persistOnboardingDraft() },
                    onDay = { day ->
                        onboardingDays = if (day in onboardingDays) onboardingDays - day else (onboardingDays + day).sorted()
                        persistOnboardingDraft()
                    },
                    onBack = { onboardingStep = (onboardingStep - 1).coerceAtLeast(0); persistOnboardingDraft() },
                    onNext = {
                        if (onboardingStep < 4) { onboardingStep++; persistOnboardingDraft() }
                        else finishOnboarding()
                    }
                )
            } else AnimatedContent(targetState = screen, label = "screen-transition", modifier = Modifier.padding(padding)) { target ->
                when (target) {
                    "home" -> HomeContent()
                    "library" -> DrillLibraryScreen(save, librarySlotId,
                        onNew = {
                            editingDrillId = null
                            editorCategory = librarySlotId?.let { id -> save.slots.firstOrNull { it.id == id }?.drillId }
                                ?.let { id -> catalog.firstOrNull { it.id == id }?.category } ?: "Shooting"
                            screen = "drillEditor"
                        }, onEdit = { editingDrillId = it; screen = "drillEditor" },
                        onArchive = { TrainingLibrary.archive(save, it); refresh(); notice("Drill archived. Saved results are preserved.") },
                        onFavorite = { TrainingLibrary.toggleFavorite(save, it); refresh() },
                        onPractice = { id ->
                            if (save.practiceSession?.drillId == id || TrainingLibrary.startPractice(save, id, today)) {
                                refresh(); screen = "practice"
                            } else notice("Resume or finish your saved practice first.")
                        }, onUse = { id ->
                            val drill = catalog.firstOrNull { it.id == id }
                            val slot = drill?.let { TrainingLibrary.swappableSlot(save, it, librarySlotId) }
                            if (slot != null && TrainingLibrary.swap(save, slot.id, id)) {
                                refresh(); screen = "home"; notice("Daily drill replaced. Your recorded work is protected.")
                            } else notice("Choose an untouched slot and a suitable drill.")
                        }, onResume = { screen = "practice" }, onHome = { screen = "home" }, initialDrillId = libraryFocusId)
                    "drillEditor" -> CustomDrillEditor(catalog.firstOrNull { it.id == editingDrillId }, editorCategory,
                        save.difficulty.takeIf { it in TrainingLibrary.levels } ?: "Beginner", onSave = { input ->
                            val result = TrainingLibrary.saveCustom(save, input, editingDrillId)
                            if (result == null) TrainingLibrary.validation(input) ?: "This drill is no longer available to edit."
                            else { libraryFocusId = result.id; refresh(); screen = "library"; notice("Custom drill saved."); null }
                        }, onBack = { screen = "library" })
                    "practice" -> {
                        val session = save.practiceSession
                        val drill = catalog.firstOrNull { it.id == session?.drillId }
                        if (session != null && drill != null) {
                            DrillScreen(drill, 0, false, save.drillResults.orEmpty().filter { it.drillId == drill.id }, session.draft,
                                onDraft = { if (TrainingLibrary.updatePracticeDraft(save, it)) persist() },
                                onVideo = { openUrl("https://www.youtube.com/results?search_query=" + Uri.encode(drill.videoQuery)) },
                                onComplete = { _, _ ->
                                    val result = TrainingLibrary.finishPractice(save, session.id, today)
                                    if (result != null) { refresh(); recordedDrillId = result.slotId; recordedXp = 0; screen = "drillResult" }
                                }, onBack = { screen = "home" }, practice = true,
                                onDiscard = { if (TrainingLibrary.discardPractice(save, session.id)) { refresh(); openLibrary() } })
                        } else LaunchedEffect(Unit) { openLibrary() }
                    }
                    "drill" -> {
                        val slot = save.slots.getOrNull(selectedSlot)
                        val drill = catalog.firstOrNull { it.id == slot?.drillId } ?: drills.first()
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
                                if (save.activeDate == today && selectedSlot in save.slots.indices && !save.slots[selectedSlot].complete &&
                                    save.slots[selectedSlot].drillId == drill.id && DrillTargets.eligible(drill, completed, attempted)) {
                                    val xpBefore = save.totalXp
                                    val result = DrillResult(save.activeDate, save.slots[selectedSlot].id, drill.id, completed, attempted, drillSnapshot = drill)
                                    save.drillResults = (save.drillResults.orEmpty() + result).toMutableList()
                                    save.slots[selectedSlot].complete = true
                                    save.drillDrafts = save.drillDrafts.orEmpty().filterNot {
                                        it.date == save.activeDate && it.slotId == save.slots[selectedSlot].id
                                    }.toMutableList()
                                    award(save, "drill_${save.activeDate}_${save.slots[selectedSlot].id}", 50)
                                    if (save.slots.all { it.complete }) award(save, "six_${save.activeDate}", 100)
                                    refresh()
                                    recordedDrillId = result.slotId
                                    recordedXp = save.totalXp - xpBefore
                                    screen = "drillResult"
                                }
                            },
                            onBack = { screen = "home" }
                        )
                    }
                    "drillResult" -> {
                        val result = recordedDrill
                        if (result != null) {
                            val drill = result.drillSnapshot ?: catalog.firstOrNull { it.id == result.drillId } ?: drills.first()
                            DrillResultScreen(drill, result, recordedXp, save.slots.count { it.complete },
                                onContinue = { if (result.practice) openLibrary() else openTraining() }, onProgress = { screen = "career" },
                                onHome = { screen = "home" })
                        } else LaunchedEffect(Unit) { screen = "home" }
                    }
                    "champSetup" -> ChampionshipSetup(
                        playerName = playerNameInput, opponentName = opponentInput, error = error,
                        onPlayerName = { playerNameInput = it.take(40); error = "" },
                        onOpponentName = { opponentInput = it.take(40); error = "" },
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
                                    val won = save.playerScore > save.opponentScore
                                    val winner = if (won) save.matchPlayer else save.matchOpponent
                                    val previousXp = save.totalXp
                                    award(save, "champ_${save.activeDate}", 100)
                                    if (won) award(save, "win_${save.activeDate}", 50)
                                    val matchXp = save.totalXp - previousXp
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
                        onBack = { screen = "home" },
                        canUndo = !save.pointHistory.isNullOrEmpty(),
                        lastScorer = save.pointHistory?.lastOrNull()?.let { if (it) save.matchPlayer else save.matchOpponent }
                    )
                    "recap" -> (recap ?: save.matches.firstOrNull())?.let { MatchRecapScreen(it, save.totalXp, onHome = { screen = "home" }, onCareer = { screen = "gameHistory" }) } ?: HomeContent()
                    "gameHistory" -> CareerScreen(save, onHome = { screen = "home" }, initiallyShowGames = true)
                    "career" -> CareerScreen(save, onHome = { screen = "home" })
                    "settings" -> SettingsScreen(save.playerName, save.difficulty, onDifficulty = { save.difficulty = it; refresh() }, onSaveName = {
                        save.playerName = it.trim(); playerNameInput = save.playerName; refresh()
                    })
                    else -> HomeContent()
                }
            }
        }
    }
}

private fun updateWeeklyActivity(current: MutableList<String>?, day: Int, kind: String): MutableList<String> {
    if (day !in 1..7 || kind !in listOf("Practice", "Game")) return current ?: mutableListOf()
    val retained = current.orEmpty().filterNot { it.startsWith("$day:") }
    val existing = current.orEmpty().any { it == "$day:$kind" }
    return (retained + if (existing) emptyList() else listOf("$day:$kind")).toMutableList()
}

private fun toggleTrainingDay(current: MutableList<Int>?, day: Int): MutableList<Int> {
    val days = current?.filter { it in 1..7 }?.distinct() ?: listOf(1, 3, 5)
    return (if (day in days) days.filterNot { it == day } else days + day).sorted().toMutableList()
}

private fun makeDailySlots(level: String, weeklyPlan: String = "Balanced", catalog: List<Drill> = drills): List<Slot> {
    val categories = listOf("Shooting", "Ball Handling", "Finishing", "Defense", "Rebounding", "Passing")
    val focus = weeklyPlan.takeIf { it in categories }
    val levels = listOf("Beginner", "Intermediate", "Advanced", "Expert", "Elite")
    val requestedIndex = levels.indexOf(level).coerceAtLeast(0)
    val dayIndex = LocalDate.now().dayOfYear
    val chosen = categories.map { category ->
        val options = catalog.filter { it.category == category }
        // A focus prioritizes the selected difficulty; other categories
        // use a lighter tier when available. Never escalate above the setting.
        val targetIndex = if (focus == null || category == focus) requestedIndex
            else (requestedIndex - 1).coerceAtLeast(0)
        val eligible = options.filter { levels.indexOf(it.level) in 0..targetIndex }
        val closest = eligible.maxOfOrNull { levels.indexOf(it.level) }
        val candidates = eligible.filter { levels.indexOf(it.level) == closest }
        candidates.ifEmpty { options.filter { it.level == "Beginner" } }
            .ifEmpty { options }
            .let { it[dayIndex % it.size] }
    }
    return chosen.shuffled().mapIndexed { i, drill ->
        Slot("${LocalDate.now()}_${i}_${Random.nextInt(100000)}", drill.id)
    }
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
private fun CourtOnboardingChoice(
    title: String, description: String, selected: Boolean, onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
            .heightIn(min = 88.dp)
            .semantics { this.selected = selected },
        shape = RoundedCornerShape(19.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) CourtQuestDesign.warmSurface else CourtQuestDesign.elevatedSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) CourtQuestDesign.accent else CourtQuestDesign.chipBorder
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                Text(description, color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
            }
            Spacer(Modifier.width(10.dp))
            Icon(
                if (selected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (selected) CourtGreen else Muted
            )
        }
    }
}

@Composable
private fun CourtOnboarding(
    modifier: Modifier = Modifier,
    step: Int, name: String, level: String, focus: String, days: List<Int>,
    onName: (String) -> Unit, onLevel: (String) -> Unit, onFocus: (String) -> Unit,
    onDay: (Int) -> Unit, onBack: () -> Unit, onNext: () -> Unit
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(step) { scrollState.scrollTo(0) }
    BackHandler(enabled = step > 0, onBack = onBack)

    Column(
        modifier.fillMaxSize()
            .imePadding()
            .padding(horizontal = CourtQuestDesign.spacingXl)
            .padding(top = CourtQuestDesign.spacingXl)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CourtQuestDesign.spacingSm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "COURTQUEST PRO", Modifier.weight(1f),
                    color = CourtQuestDesign.accent, fontSize = 11.sp,
                    lineHeight = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
                )
                Text(
                    "STEP ${step + 1} OF 5",
                    color = CourtQuestDesign.textSecondary, fontSize = 11.sp,
                    lineHeight = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.7.sp
                )
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                repeat(5) { index ->
                    Box(
                        Modifier.weight(1f).height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (index <= step) CourtQuestDesign.accentVivid
                                else CourtQuestDesign.elevatedSurface
                            )
                    )
                }
            }
        }

        Column(
            Modifier.weight(1f).fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(top = 20.dp, bottom = CourtQuestDesign.spacingLg)
        ) {
            if (step > 0) {
                Text(
                    listOf("Make it yours.", "Find your level.", "Choose your focus.", "Build your routine.")[step - 1],
                    modifier = Modifier.semantics { heading() },
                    color = Ink, fontSize = 32.sp, lineHeight = 37.sp, fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(CourtQuestDesign.spacingMd))
                Text(
                    listOf(
                        "What should we call you? You can change this anytime.",
                        "Choose the level that best matches your current experience.",
                        "Your focus shapes the drills selected for your daily sessions.",
                        "Pick your usual training days. You can adjust them later."
                    )[step - 1], color = Muted, fontSize = 15.sp, lineHeight = 22.sp
                )
                Spacer(Modifier.height(CourtQuestDesign.spacingXl))
            }
            when (step) {
                0 -> CourtOnboardingWelcome()
                1 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF242428))
                    ) {
                        Column(Modifier.fillMaxWidth().padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Text("PLAYER IDENTITY", color = Orange, fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.1.sp)
                            OutlinedTextField(
                                value = name, onValueChange = onName,
                                label = { Text("Player name", color = CourtQuestDesign.textSecondary) },
                                placeholder = {
                                    Text("What should we call you?", color = CourtQuestDesign.textSecondary)
                                },
                                supportingText = {
                                    Text("Optional. You can change this later.", color = CourtQuestDesign.textSecondary)
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words,
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CourtQuestDesign.accent,
                                    unfocusedBorderColor = CourtQuestDesign.chipBorder,
                                    focusedTextColor = CourtQuestDesign.textPrimary,
                                    unfocusedTextColor = CourtQuestDesign.textPrimary,
                                    cursorColor = CourtQuestDesign.accent
                                )
                            )
                            Text(
                                if (name.isBlank()) "Your dashboard will use a generic player greeting."
                                else "Your dashboard will welcome ${name.trim()}.",
                                color = Muted, fontSize = 12.sp, lineHeight = 18.sp
                            )
                        }
                    }
                }
                2 -> {
                    listOf(
                        "Beginner" to "Build confidence, fundamentals, and repeatable technique.",
                        "Intermediate" to "Strengthen consistency, control, and game-ready skills.",
                        "Advanced" to "Challenge precision, pace, and execution under pressure."
                    ).forEach { (option, description) ->
                        CourtOnboardingChoice(
                            title = option, description = description,
                            selected = level == option, onClick = { onLevel(option) }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }
                3 -> {
                    listOf(
                        "Balanced" to "Develop a well-rounded game across multiple skills.",
                        "Shooting" to "Prioritize mechanics, accuracy, and shot repetition.",
                        "Ball Handling" to "Build dribble control, coordination, and confidence.",
                        "Finishing" to "Work on controlled scoring around the basket."
                    ).forEach { (option, description) ->
                        CourtOnboardingChoice(
                            title = option, description = description,
                            selected = focus == option, onClick = { onFocus(option) }
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                }
                4 -> {
                    Text("YOUR TRAINING DAYS", color = Orange, fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 1.1.sp)
                    Spacer(Modifier.height(8.dp))
                    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    val fullDayNames = listOf(
                        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
                    )
                    val chipSpacing = CourtQuestDesign.spacingSm
                    val minimumChipWidth = 24.dp + (42f * LocalDensity.current.fontScale).dp
                    BoxWithConstraints(Modifier.fillMaxWidth()) {
                        val columns = (
                            (maxWidth.value + chipSpacing.value) /
                                (minimumChipWidth.value + chipSpacing.value)
                            ).toInt().coerceIn(1, 4)
                        Column(verticalArrangement = Arrangement.spacedBy(chipSpacing)) {
                            dayLabels.withIndex().chunked(columns).forEach { rowDays ->
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(chipSpacing)
                                ) {
                                    rowDays.forEach { (index, day) ->
                                        val dayNumber = index + 1
                                        FilterChip(
                                            selected = dayNumber in days,
                                            onClick = { onDay(dayNumber) },
                                            label = { Text(day, maxLines = 1) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 48.dp)
                                                .semantics { contentDescription = "Train on ${fullDayNames[index]}" },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = CourtQuestDesign.elevatedSurface,
                                                labelColor = CourtQuestDesign.textSecondary,
                                                selectedContainerColor = CourtQuestDesign.warmSurface,
                                                selectedLabelColor = CourtQuestDesign.textPrimary
                                            ),
                                            border = FilterChipDefaults.filterChipBorder(
                                                enabled = true,
                                                selected = dayNumber in days,
                                                borderColor = CourtQuestDesign.chipBorder,
                                                selectedBorderColor = CourtQuestDesign.accent,
                                                borderWidth = 1.dp,
                                                selectedBorderWidth = 1.dp
                                            )
                                        )
                                    }
                                    repeat(columns - rowDays.size) { Spacer(Modifier.weight(1f)) }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (days.isEmpty()) "No scheduled days — you can still train anytime."
                        else "SCHEDULED: " + dayLabels
                            .filterIndexed { index, _ -> index + 1 in days }.joinToString(" · "),
                        color = Muted, fontSize = 12.sp, lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(22.dp))
                    CourtQuestPremiumSurface(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("YOUR STARTING PLAN", color = Orange, fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold, letterSpacing = 1.1.sp)
                            Text(if (name.isBlank()) "Basketball player" else name.trim(),
                                color = Ink, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                            Text("$level  ·  $focus", color = Ink, fontSize = 14.sp)
                            Text(
                                if (days.isEmpty()) "No recurring training days selected"
                                else "${days.size} training ${if (days.size == 1) "day" else "days"} per week",
                                color = Muted, fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Column(
            Modifier.fillMaxWidth().padding(bottom = CourtQuestDesign.spacingXl),
            verticalArrangement = Arrangement.spacedBy(CourtQuestDesign.spacingSm)
        ) {
            if (step == 0) {
                Text(
                    "Works offline. Your plan is saved on this device.",
                    color = CourtQuestDesign.textSecondary, fontSize = 12.sp, lineHeight = 17.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            CourtQuestPrimaryButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (step == 4) "ENTER MY COURT" else "CONTINUE",
                    fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp
                )
            }
            if (step > 0) {
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) {
                    Icon(
                        Icons.Default.ArrowBack, contentDescription = null,
                        tint = Muted, modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(CourtQuestDesign.spacingSm))
                    Text("PREVIOUS STEP", color = Muted)
                }
            }
        }
    }
}

internal fun rankFor(xp: Int): String = when {
    xp >= 6000 -> "Legend"
    xp >= 4000 -> "MVP"
    xp >= 2500 -> "All-Star"
    xp >= 1500 -> "Gold"
    xp >= 800 -> "Silver"
    xp >= 300 -> "Bronze"
    else -> "Rookie"
}
internal fun difficultyDescription(level: String) = when(level) {
    "Beginner" -> "Learn technique and movement basics."
    "Intermediate" -> "Build consistency and coordination."
    "Advanced" -> "Add movement, speed, and decisions."
    "Expert" -> "Combine skills under greater pressure."
    else -> "Challenging game-like drills, controlled safely."
}
