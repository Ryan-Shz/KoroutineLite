package com.ryan.github.koroutine.lite.sample

import com.ryan.github.koroutine.lite.CoroutineName
import com.ryan.github.koroutine.lite.delay
import com.ryan.github.koroutine.lite.exception.CoroutineExceptionHandler
import com.ryan.github.koroutine.lite.exception.coroutineExceptionHandler
import com.ryan.github.koroutine.lite.launch
import com.ryan.github.koroutine.lite.utils.log
import java.lang.ArithmeticException
import kotlin.concurrent.thread
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun main() {
    val handler = coroutineExceptionHandler { context, exception ->
        println("${context[CoroutineName]}, $exception")
    }
    val job = launch(handler) {
        log("start")
        delay(2000)
        throw ArithmeticException("div 0")
    }
    log("isActive: ${job.isActive}")
    job.join()
    log("done!")
}