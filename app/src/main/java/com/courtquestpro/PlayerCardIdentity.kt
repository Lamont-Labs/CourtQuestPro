package com.courtquestpro

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/** Cosmetic identity only; deliberately independent of training and award state. */
internal enum class PlayerCardStyle(val label: String, val edition: String) {
    HOLOGRAPHIC("Holographic Elite", "PRISM / ELITE"),
    CLASSIC("Court Legends", "HERITAGE COLLECTION"),
    BATTLE("Battle Series", "BUILT FOR THE COURT"),
    OBSIDIAN("Obsidian Chrome","PRECISION / CHROME")
}

internal enum class PlayerCardPose(val label: String) {
    STANDING("Standing"), DRIBBLING("Dribbling"), ACTION("Action"),
    READY("Ready stance"), FOLLOWTHROUGH("Follow-through")
}

internal enum class PlayerCardFace(val label: String) {
    SHADOW("Shadowed"), BLANK("Featureless"), QUESTION("Question mark"), VISOR("Reflective visor")
}

internal data class PlayerCardIdentity(
    val style: PlayerCardStyle = PlayerCardStyle.HOLOGRAPHIC,
    val pose: PlayerCardPose = PlayerCardPose.STANDING,
    val face: PlayerCardFace = PlayerCardFace.SHADOW,
    val avatar: AvatarAppearance = AvatarAppearance()
) {
    companion object {
        fun restore(style: String?, pose: String?, face: String?, avatar: AvatarAppearance = AvatarAppearance()) = PlayerCardIdentity(
            PlayerCardStyle.entries.firstOrNull { it.name == style } ?: PlayerCardStyle.HOLOGRAPHIC,
            PlayerCardPose.entries.firstOrNull { it.name == pose } ?: PlayerCardPose.STANDING,
            PlayerCardFace.entries.firstOrNull { it.name == face } ?: PlayerCardFace.SHADOW, avatar
        )
    }
}

internal object PlayerCardSelection {
    private const val PREFS = "courtquest_player_card"
    // Keep the existing edition key: previously saved Court Classic becomes Court Legends.
    private const val STYLE = "selected_style_v1"
    private const val POSE = "selected_pose_v2"
    private const val FACE = "selected_face_v2"
    private var revision by mutableIntStateOf(0)

    fun identity(context: Context): PlayerCardIdentity {
        @Suppress("UNUSED_VARIABLE") val observed = revision
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return PlayerCardIdentity.restore(prefs.getString(STYLE, null),
            prefs.getString(POSE, null), prefs.getString(FACE, null),
            AvatarAppearance.restore(AvatarAppearance.keys.map { prefs.getString("avatar_${it}_v3",null) }))
    }

    fun read(context: Context): PlayerCardStyle = identity(context).style

    fun write(context: Context, style: PlayerCardStyle): Boolean =
        write(context, identity(context).copy(style = style))

    /** One atomic synchronous transaction; callers exit only after disk confirms the save. */
    fun write(context: Context, identity: PlayerCardIdentity): Boolean {
        val edit = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(STYLE, identity.style.name).putString(POSE, identity.pose.name)
            .putString(FACE, identity.face.name)
        AvatarAppearance.keys.zip(identity.avatar.values).forEach { (key,value) -> edit.putString("avatar_${key}_v3",value) }
        val persisted = edit.commit()
        if (persisted) revision++
        return persisted
    }
}
