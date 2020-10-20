package com.ryan.github.koroutine.lite.sample

import com.ryan.github.koroutine.lite.CoroutineName
import com.ryan.github.koroutine.lite.delay
import com.ryan.github.koroutine.lite.exception.coroutineExceptionHandler
import com.ryan.github.koroutine.lite.launch
import com.ryan.github.koroutine.lite.scope.GlobalScope
import com.ryan.github.koroutine.lite.scope.coroutineScope
import com.ryan.github.koroutine.lite.utils.log
import java.lang.ArithmeticException

suspend fun main() {
    val handler = coroutineExceptionHandler { context, exception ->
        println("${context[CoroutineName]}, $exception")
    }
    val job = GlobalScope.launch(handler) {
        log("start")
        delay(2000)
        log("currentScope1: $this")
        getCurrentScope()
        throw ArithmeticException("div 0")
    }
    log("isActive: ${job.isActive}")
    job.join()
    log("done!")
}

private suspend fun getCurrentScope() {
    coroutineScope {
        log("currentScope2: $this")
    }
}