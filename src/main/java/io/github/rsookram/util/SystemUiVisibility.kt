package io.github.rsookram.util

import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController

fun Window.enterImmersiveMode() {
    setDecorFitsSystemWindows(false)

    val insetsController = insetsController ?: return

    insetsController.systemBarsBehavior =
        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    insetsController.hide(WindowInsets.Type.systemBars())
}
