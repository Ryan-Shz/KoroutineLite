package com.ryan.github.koroutine.lite.core

import com.ryan.github.koroutine.lite.exception.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

class StandardCoroutine(context: CoroutineContext) : AbstractCoroutine<Unit>(context) {
    override fun handleJobException(exception: Throwable): Boolean {
        context[CoroutineExceptionHandler]?.handleException(context, exception)
            ?: Thread.currentThread().uncaughtExceptionHandler.uncaughtException(
                Thread.currentThread(),
                exception
            )
        return true
    }
}