package com.ryan.github.koroutine.lite.core

import com.ryan.github.koroutine.lite.Deferred
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.suspendCoroutine

class DeferredCoroutine<T>(context: CoroutineContext) : AbstractCoroutine<T>(context), Deferred<T> {

    override suspend fun await(): T {
        return when (val currentState = state.get()) {
            is CoroutineState.Cancelling,
            is CoroutineState.InComplete -> awaitSuspend()
            is CoroutineState.Complete<*> -> {
                coroutineContext[Job]?.isActive?.takeIf { it }?.let {
                    throw CancellationException("Coroutine is Canceled.")
                }
                (currentState.value as T?) ?: throw currentState.exception!!
            }
        }
    }

    private suspend fun awaitSuspend() = suspendCoroutine<T> { continuation ->
        doOnCompleted { result ->
            continuation.resumeWith(result)
        }
    }
}