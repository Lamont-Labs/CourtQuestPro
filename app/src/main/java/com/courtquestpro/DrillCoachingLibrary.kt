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
        "d2" to DrillCoaching("Choose an imaginary shooter spot and a separate stopping mark outside the imagined landing area. Start several controlled steps away.", "Approach under control, shorten the final steps, raise your hands and stop at the mark without jumping forward.", "Finish balanced at the stopping mark with hands raised and feet outside the imagined landing space.", "If you fly past the target, decelerate sooner and allow more stopping space."),
        "d3" to DrillCoaching("Mark left, center and right positions on a dry clear surface. Start at center in a defensive stance.", "Imagine an opponent changing direction; slide left, return to center, slide right and return. Vary the rhythm while keeping hips low.", "Keep feet apart, chest up and each direction change balanced.", "If balance deteriorates, slow down and shorten slides before increasing pace."),
        "r1" to DrillCoaching("Locate the ball and the opponent's path on a clear court.", "Turn toward the opponent's path and establish legal position without pushing; then locate the rebound.", "Hold position with arms close; do not push or extend elbows.", "If you lose the rebound line, pause and re-establish legal position without contact."),
        "r2" to DrillCoaching("Begin beneath a safe rebound path with a passing target available.", "Secure the rebound with both hands, land, pivot and deliver a controlled outlet.", "Keep the ball protected and eyes up before passing.", "If the outlet is rushed, pause in a stable pivot before releasing."),
        "r3" to DrillCoaching("Choose a comfortable shooting spot with clear landing and rebound space.", "Take your own shot, read its flight and bounce, move under control and secure the ball with both hands.", "Track the ball, stay clear of other court users and regain balance before the next shot.", "If you overrun the bounce, slow your approach and take shorter adjustment steps."),
        "p1" to DrillCoaching("Face a sturdy permitted wall with clear space, feet set and hands behind the ball. Choose chest- and waist-height targets.", "Step toward the target and extend arms; for a bounce pass aim at the floor before the wall. Control the return before repeating.", "Finish with thumbs rotating downward and the pass on target.", "If passes float or bounce too high, adjust the release angle and step."),
        "p2" to DrillCoaching("Choose a sturdy permitted wall, a visible target and a clear lateral lane. Begin with a gentle test pass to check the return path.", "Pass gently toward the wall at an angle that returns into the clear lane, take a short lateral cut, show your hands and catch under control.", "Prioritize accurate timing and balanced footwork over throwing harder.", "If the return is unpredictable or the catch feels rushed, stop, reduce pass speed and shorten the cut."),
        "p3" to DrillCoaching("Set two reachable targets on a sturdy permitted wall and check both outlet lanes for clear space.", "Gather a gentle self-toss with both hands, scan both targets, pivot without traveling and pass through a clear lane. Control the return.", "Keep the pivot foot planted, head up and balance under control through the pass.", "If the outlet is late, scan earlier and keep the pivot foot planted before releasing.")
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
        "d2" to "Count a closeout only if you stop balanced at the mark outside the imagined landing space. Track eight approaches per side.",
        "d3" to "Count one full 30-second solo mirror round with controlled direction changes. Stop and reset if you lose footing or balance.",
        "r1" to "Count one controlled turn, legal position and rebound read. This drill does not require a jump or a caught ball.",
        "r2" to "Count one secure rebound, balanced landing and controlled outlet. Regain balance before pivoting.",
        "r3" to "Count each controlled two-hand rebound after your own shot, whether the shot scores or misses. Reset before shooting again.",
        "p1" to "Count passes that hit the chosen wall target after the correct pass pattern. Track ten chest passes and ten bounce passes separately.",
        "p2" to "Count a full wall pass, lateral cut and controlled return catch. A good first pass alone does not complete the sequence.",
        "p3" to "Count the outlet only after a secure gather, scan, legal pivot and accurate pass to the chosen wall target through a clear lane."
    )

    fun repCheck(drill: Drill): String? = repChecks[drill.id]
}
