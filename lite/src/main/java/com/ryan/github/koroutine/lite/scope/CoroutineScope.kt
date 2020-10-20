package com.ryan.github.koroutine.lite.scope

import com.ryan.github.koroutine.lite.core.AbstractCoroutine
import com.ryan.github.koroutine.lite.core.Job
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.coroutines.suspendCoroutine

interface CoroutineScope {
    val scopeContext: CoroutineContext
}

internal class ContextScope(context: CoroutineContext) : CoroutineScope {
    override val scopeContext: CoroutineContext = context
}

operator fun CoroutineScope.plus(context: CoroutineContext): CoroutineScope {
    return ContextScope(scopeContext + context)
}

fun CoroutineScope.cancel() {
    val job = scopeContext[Job] ?: error("Scope cannot be cancelled because it does not has job.")
    job.cancel()
}

suspend fun <R> coroutineScope(block: suspend CoroutineScope.() -> R): R = suspendCoroutine {
    val coroutine = ScopeCoroutine(it.context, it)
    block.startCoroutine(coroutine, coroutine)
}

internal open class ScopeCoroutine<T>(
    context: CoroutineContext,
    private val continuation: Continuation<T>
) : AbstractCoroutine<T>(context) {
    override fun resumeWith(result: Result<T>) {
        super.resumeWith(result)
        continuation.resumeWith(result)
    }
}