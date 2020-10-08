package com.ryan.github.koroutine.lite.sample

import com.ryan.github.koroutine.lite.delay
import com.ryan.github.koroutine.lite.launch
import com.ryan.github.koroutine.lite.utils.log
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun main() {
    val job = launch {
        log("start")
        val result = hello()
        log(result)
        delay(2000)
    }
    job.invokeOnCompletion {
        log("onCompleted: $it")
    }
    log("isActive: ${job.isActive}")
    job.join()
    log("done!")
}

suspend fun hello() = suspendCoroutine<Int> {
    thread(isDaemon = true) {
        Thread.sleep(1000)
        it.resume(10086)
    }
}