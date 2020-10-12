package com.ryan.github.koroutine.lite

interface Deferred<T> : Job {
    suspend fun await(): T
}