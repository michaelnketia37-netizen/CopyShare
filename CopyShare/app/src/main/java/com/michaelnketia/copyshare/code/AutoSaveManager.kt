package com.michaelnketia.copyshare.code

import android.os.Handler
import android.os.Looper

class AutoSaveManager(
    private val saveAction: () -> Unit
) {

    private val handler = Handler(Looper.getMainLooper())

    private val runnable = object : Runnable {
        override fun run() {
            saveAction.invoke()
            handler.postDelayed(this, 30000)
        }
    }

    fun start() {
        handler.postDelayed(runnable, 30000)
    }

    fun stop() {
        handler.removeCallbacks(runnable)
    }
}