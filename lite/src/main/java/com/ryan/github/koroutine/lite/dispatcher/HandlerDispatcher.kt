package com.ryan.github.koroutine.lite.dispatcher

import android.os.Handler
import android.os.Looper

object HandlerDispatcher : Dispatcher {

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun dispatch(block: () -> Unit) {
        mainHandler.post(block)
    }
}