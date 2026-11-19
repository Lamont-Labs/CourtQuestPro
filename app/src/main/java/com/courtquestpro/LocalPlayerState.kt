package com.courtquestpro

import com.google.gson.Gson

/** A damaged save is a load failure, preserving the original data for recovery. */
internal fun readLocalPlayerState(gson: Gson, raw: String?): SaveState {
    if (raw == null) return SaveState()
    return requireNotNull(gson.fromJson(raw, SaveState::class.java)) { "Missing saved player data" }
}
