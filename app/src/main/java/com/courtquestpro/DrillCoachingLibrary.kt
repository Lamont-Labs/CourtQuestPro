package com.courtquestpro

/** Drill-specific instruction cues. These are text coaching references, not verified motion imagery. */
internal data class DrillCoaching(
    val setup: String,
    val movement: String,
    val checkpoint: String,
    val correction: String
)

internal object DrillCoachingLibrary {
    private val guides = mapOf(
        "s1" to DrillCoaching("Stand close enough to shoot without straining; mark five comfortable spots.", "Set feet, align the shooting elbow, extend smoothly and hold the wrist follow-through.", "Finish balanced with the shooting hand pointing toward the rim.", "If shots drift, slow down and check foot alignment before adding range."),
        "s2" to DrillCoaching("Choose a comfortable shooting spot with clear space; gently self-toss the ball in front of you.", "Catch your self-toss in the shooting pocket, set both feet, square to the rim and release without rushing.", "Land balanced after every catch-and-shoot attempt.", "If the ball dips too low, prepare hands earlier and shorten the catch-to-shot motion."),
        "s3" to DrillCoaching("Begin on the wing with a clear lane and a safe stopping area.", "Change direction, plant under control, square shoulders and rise into the jumper.", "Your final stop should be balanced before the ball leaves your hands.", "If momentum carries you sideways, reduce approach speed and rehearse the stop."),
        "b1" to DrillCoaching("Bend knees, widen stance and keep the ball below the waist.", "Push the ball across the body using the fingertips, alternating hands.", "Keep eyes forward and the off-hand ready to protect space.", "If the dribble rises high, lower your hips and reduce speed."),
        "b2" to DrillCoaching("Start at the baseline with an unobstructed path to half court.", "Dribble slowly, accelerate for several steps, then decelerate while retaining control.", "Change pace without losing the ball or leaning off balance.", "If you lose control at speed, shorten the acceleration phase."),
        "b3" to DrillCoaching("Choose two moves and identify an open driving lane.", "Chain the moves at controlled height, shield the ball and explode into space.", "Keep the final move purposeful and the first driving step decisive.", "If moves become loose, practice each move separately before combining."),
        "f1" to DrillCoaching("Start near the basket with room for an approach from either side.", "For a right-hand layup step right then left and jump off the left foot; reverse for the left hand.", "Aim softly at the backboard and land under control.", "If steps feel tangled, walk the two-step pattern without a ball first."),
        "f2" to DrillCoaching("Place a cone or choose a court marking as the change-of-direction point.", "Approach, plant safely, change direction and finish with the outside hand.", "Keep the ball away from the imaginary defender.", "If the plant slips or feels unstable, slow down and shorten the cut."),
        "f3" to DrillCoaching("Begin on one side of the rim and identify the safe path beneath it.", "Move under the basket, shield the ball with your body and finish on the far side.", "Keep eyes on the far-side target and protect the ball through the move.", "If you drift too far under the rim, rehearse the approach without shooting."),
        "d1" to DrillCoaching("Take an athletic stance with hips low and feet slightly wider than shoulders.", "Push laterally off the trailing foot; slide without crossing feet.", "Keep your head level and chest upright.", "If feet click together or cross, shorten each slide."),
        "d2" to DrillCoaching("Start several controlled steps away from a safe imaginary shooter position.", "Approach under control, shorten the final steps, raise your hands and stop before contact.", "Remain balanced at a safe distance; do not touch the shooter or enter their landing space.", "If you fly past the target, decelerate sooner and allow more stopping space."),
        "d3" to DrillCoaching("Face a partner at a safe distance on a clear surface.", "Mirror their lateral movement while keeping your hips low.", "Maintain spacing without crossing feet or reaching.", "If you fall behind, reduce speed and anticipate with shorter slides."),
        "r1" to DrillCoaching("Locate the ball and the opponent's path on a clear court.", "Turn toward the opponent's path and establish legal position without pushing; then locate the rebound.", "Hold position with arms close; do not push or extend elbows.", "If you lose the rebound line, pause and re-establish legal position without contact."),
        "r2" to DrillCoaching("Begin beneath a safe rebound path with a passing target available.", "Secure the rebound with both hands, land, pivot and deliver a controlled outlet.", "Keep the ball protected and eyes up before passing.", "If the outlet is rushed, pause in a stable pivot before releasing."),
        "r3" to DrillCoaching("Have a partner shoot from a comfortable distance with clear landing space.", "Read the shot, move to the rebound, secure it and alternate roles.", "Track the ball while staying aware of the other player's position.", "If you collide or overrun the bounce, reduce pace and widen spacing."),
        "p1" to DrillCoaching("Face a partner or safe wall target with feet set and hands behind the ball.", "Step toward the target and extend arms; for a bounce pass aim at the floor before the receiver.", "Finish with thumbs rotating downward and the pass on target.", "If passes float or bounce too high, adjust the release angle and step."),
        "p2" to DrillCoaching("Use a safe passing lane and a clearly visible partner target.", "Pass to the partner, cut decisively into open space, show your hands and receive under control.", "Prioritize accurate timing and balanced footwork over throwing harder.", "If the cut or catch feels rushed, reduce distance and reset the pass-and-cut timing."),
        "p3" to DrillCoaching("Set a safe partner target and leave a clear outlet lane.", "Secure the simulated rebound, scan for a partner, pivot without traveling and choose a clear lane.", "Keep the pivot foot planted, head up and balance under control through the pass.", "If the outlet is late, scan earlier and keep the pivot foot planted before releasing.")
    )

    fun forDrill(drill: Drill): DrillCoaching? = guides[drill.id]

    /** What to log, not a claim that the app can verify form or time spent. */
    private val repChecks = mapOf(
        "s1" to "Count made shots. Keep separate track of the three makes at each of your five spots.",
        "s2" to "Count a controlled self-toss, catch, set and shot even if it misses. A rushed or off-balance release is an other attempt.",
        "s3" to "Count a balanced stop and shot, not only a make. Track ten controlled attempts from each side.",
        "b1" to "Count a crossover once the receiving hand controls the bounce. Switch direction after thirty clean reps.",
        "b2" to "Count one full baseline-to-half-court trip with both speed changes and no loss of control.",
        "b3" to "Count one combination only after both moves and a controlled first driving step. Do eight per hand.",
        "f1" to "Count made layups; alternate or track sides separately so each hand gets five makes.",
        "f2" to "Count a finish only after a stable cut and a release with the hand away from the defender. Track eight per side.",
        "f3" to "Count a controlled reverse on the far side of the rim. Track six from each side; reset clear of the basket.",
        "d1" to "Count one completed 20-second slide interval, not every step. Rest 20 seconds and reset before the next round.",
        "d2" to "Count a closeout only if you finish balanced outside the shooter's landing space. Track eight approaches per side.",
        "d3" to "Count one full 30-second mirror round. Stop and reset if either player loses footing or spacing.",
        "r1" to "Count one controlled turn, legal position and rebound read. This drill does not require a jump or a caught ball.",
        "r2" to "Count one secure rebound, balanced landing and controlled outlet. Regain balance before pivoting.",
        "r3" to "Count each secured rebound and alternate shooter and rebounder. Wait until both players are clear before the next shot.",
        "p1" to "Count accurate passes. Track ten chest passes and ten bounce passes separately.",
        "p2" to "Count a full pass, cut and controlled return catch. A good first pass alone does not complete the sequence.",
        "p3" to "Count the outlet only after a secure gather, scan and accurate pass through a clear lane."
    )

    fun repCheck(drill: Drill): String? = repChecks[drill.id]
}
