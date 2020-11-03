package com.ryan.github.koroutine.lite.sample

import com.ryan.github.koroutine.lite.generator.generator

fun main() {
    val generator = generator { start: Int ->
        for (i in 0..5) {
            yield(start + i)
        }
    }

    val seq = generator(10)

    for (i in seq) {
        println(i)
    }
}