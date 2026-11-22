package com.courtquestpro

import android.provider.Settings
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*

/** A single save acknowledgement; animation-off always has the same static check. */
@Composable
internal fun CourtSavedStamp(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val animate = Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    if (animate) {
        val composition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.training_saved))
        LottieAnimation(composition.value, iterations = 1, restartOnPlay = false, modifier = modifier.size(64.dp))
    } else Icon(Icons.Default.CheckCircle, null, tint = CourtQuestDesign.accentVivid, modifier = modifier.size(64.dp))
}
