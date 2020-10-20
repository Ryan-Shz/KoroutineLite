package com.ryan.github.koroutine.lite.scope

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.coroutines.suspendCoroutine

private class SupervisorCoroutine<T>(
    context: CoroutineContext,
    continuation: Continuation<T>
) : ScopeCoroutine<T>(context, continuation) {
    override fun handleChildException(e: Throwable): Boolean {
        return false
    }
}

suspend fun <R> supervisorScope(block: suspend CoroutineScope.() -> R): R = suspendCoroutine {
    val coroutine = SupervisorCoroutine(it.context, it)
    block.startCoroutine(coroutine, coroutine)
}