package com.ryan.github.koroutine.lite.sample

import com.ryan.github.koroutine.lite.delay
import com.ryan.github.koroutine.lite.launch
import com.ryan.github.koroutine.lite.scope.GlobalScope
import com.ryan.github.koroutine.lite.utils.log

suspend fun main() {
    val job = GlobalScope.launch {
        log("start")
        val result = hello()
        log(result)
        delay(2000)
    }
    job.invokeOnCompletion {
        log("onCompleted: $it")
    }
    job.invokeOnCancel {
        log("onCancel")
    }
    log("isActive: ${job.isActive}")
    job.cancel()
    job.join()
    log("done!")
}