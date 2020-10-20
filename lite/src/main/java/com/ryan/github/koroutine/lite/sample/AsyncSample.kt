package com.ryan.github.koroutine.lite.sample

import com.ryan.github.koroutine.lite.async
import com.ryan.github.koroutine.lite.delay
import com.ryan.github.koroutine.lite.scope.GlobalScope
import com.ryan.github.koroutine.lite.utils.log

suspend fun main() {
    log(1)
    val deferred = GlobalScope.async {
        log(2)
        delay(1000)
        log(3)
        "Hello"
    }
    log(4)
    val result = deferred.await()
    println("result: $result")
    log(5)
}