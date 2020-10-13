package com.ryan.github.koroutine.lite.exception

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

interface CoroutineExceptionHandler : CoroutineContext.Element {

    companion object Key : CoroutineContext.Key<CoroutineExceptionHandler>

    fun handleException(context: CoroutineContext, exception: Throwable)
}

inline fun coroutineExceptionHandler(crossinline block: (CoroutineContext, Throwable) -> Unit): CoroutineExceptionHandler {
    return object : AbstractCoroutineContextElement(CoroutineExceptionHandler),
        CoroutineExceptionHandler {
        override fun handleException(context: CoroutineContext, exception: Throwable) {
            block.invoke(context, exception)
        }
    }
}