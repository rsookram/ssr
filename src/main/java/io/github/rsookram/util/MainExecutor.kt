package io.github.rsookram.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor

object MainExecutor : Executor {

    private val handler = Handler(Looper.getMainLooper())

    override fun execute(command: Runnable) {
        handler.post(command)
    }
}
