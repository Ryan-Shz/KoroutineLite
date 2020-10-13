package com.ryan.github.koroutine.lite

import com.ryan.github.koroutine.lite.cancel.suspendCancellableCoroutine
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private val executor = Executors.newScheduledThreadPool(1) { runnable ->
    Thread(runnable, "Delay-Scheduler").apply {
        isDaemon = true
    }
}

suspend fun delay(time: Long, unit: TimeUnit = TimeUnit.MILLISECONDS) =
    suspendCancellableCoroutine<Unit> { continuation ->
        executor.schedule({
            continuation.resume(Unit)
        }, time, unit)
    }