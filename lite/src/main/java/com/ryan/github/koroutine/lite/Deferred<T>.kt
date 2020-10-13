package com.ryan.github.koroutine.lite

import com.ryan.github.koroutine.lite.core.Job

interface Deferred<T> : Job {
    suspend fun await(): T
}