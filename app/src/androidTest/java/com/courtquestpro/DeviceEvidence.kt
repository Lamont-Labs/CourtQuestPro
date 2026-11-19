package com.courtquestpro

import android.app.Activity
import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry

internal fun captureDeviceFrame(activity: Activity, name: String) {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    instrumentation.waitForIdleSync()
    instrumentation.uiAutomation.waitForIdle(100, 2500)
    val image = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
    try {
        activity.openFileOutput("$name.png", Activity.MODE_PRIVATE).use { output ->
            check(image.compress(Bitmap.CompressFormat.PNG, 100, output))
        }
    } finally {
        image.recycle()
    }
}
