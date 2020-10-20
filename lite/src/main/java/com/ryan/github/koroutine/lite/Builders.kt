package com.ryan.github.koroutine.lite

import com.ryan.github.koroutine.lite.core.*
import com.ryan.github.koroutine.lite.dispatcher.DispatcherContext
import com.ryan.github.koroutine.lite.dispatcher.Dispatchers
import com.ryan.github.koroutine.lite.scope.CoroutineScope
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

private var coroutineIndex = AtomicInteger(0)

fun CoroutineScope.launch(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
): Job {
    val completion = StandardCoroutine(newCoroutineContext(context))
    block.startCoroutine(completion, completion)
    return completion
}

fun CoroutineScope.newCoroutineContext(context: CoroutineContext): CoroutineContext {
    val combined =
        scopeContext + context + CoroutineName("coroutine#${coroutineIndex.getAndIncrement()}")
    return if (combined !== Dispatchers.Default && combined[ContinuationInterceptor] == null)
        combined + Dispatchers.Default else combined
}

fun <T> runBlocking(context: CoroutineContext = EmptyCoroutineContext, block: suspend () -> T): T {
    val dispatcher = BlockingQueueDispatcher()
    val completion = BlockingCoroutine<T>(
        context + DispatcherContext(dispatcher),
        dispatcher
    )
    block.startCoroutine(completion)
    return completion.joinBlocking()
}

fun <T> CoroutineScope.async(
    context: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> T
): Deferred<T> {
    val completion = DeferredCoroutine<T>(newCoroutineContext(context))
    block.startCoroutine(completion, completion)
    return completion
}