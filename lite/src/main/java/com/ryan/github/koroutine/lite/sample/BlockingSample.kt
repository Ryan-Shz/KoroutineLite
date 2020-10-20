package com.ryan.github.koroutine.lite.sample

import com.ryan.github.koroutine.lite.delay
import com.ryan.github.koroutine.lite.launch
import com.ryan.github.koroutine.lite.runBlocking
import com.ryan.github.koroutine.lite.scope.GlobalScope
import com.ryan.github.koroutine.lite.utils.log

fun main() = runBlocking {
    log(1)
    val job = GlobalScope.launch {
        log(2)
        delay(100)
        log(3)
    }
    log(4)
    job.join()
    log(5)
    delay(1000)
    log(6)
}
