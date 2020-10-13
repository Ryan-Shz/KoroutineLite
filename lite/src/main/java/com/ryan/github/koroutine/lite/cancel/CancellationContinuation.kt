package com.ryan.github.koroutine.lite.cancel

import com.ryan.github.koroutine.lite.core.CancellationException
import com.ryan.github.koroutine.lite.core.Job
import com.ryan.github.koroutine.lite.core.OnCancelBlock
import java.lang.IllegalStateException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.Continuation
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED

class CancellationContinuation<T>(private val continuation: Continuation<T>) :
    Continuation<T> by continuation {

    private val state = AtomicReference<CancelState>(CancelState.InComplete)
    private val cancelHandlers = CopyOnWriteArrayList<OnCancelBlock>()

    val isCompleted: Boolean
        get() = state.get() is CancelState.Complete<*>

    private val isActive: Boolean
        get() = state.get() == CancelState.InComplete

    override fun resumeWith(result: Result<T>) {
        state.updateAndGet { prev ->
            when (prev) {
                CancelState.InComplete -> {
                    continuation.resumeWith(result)
                    CancelState.Complete(result.getOrNull(), result.exceptionOrNull())
                }
                is CancelState.Complete<*> -> throw IllegalStateException("Already completed.")
                CancelState.Cancelled -> {
                    CancellationException("Cancelled.").let {
                        continuation.resumeWith(Result.failure(it))
                        CancelState.Complete(null, it)
                    }
                }
            }
        }
    }

    fun getResult(): Any? {
        installCancelHandler()
        return when (val currentState = state.get()) {
            CancelState.InComplete -> COROUTINE_SUSPENDED
            is CancelState.Complete<*> -> currentState.exception?.let { throw it }
                ?: currentState.value
            is CancelState.Cancelled -> throw CancellationException("Continuation is cancelled.")
        }
    }

    fun invokeOnCancel(onCancel: OnCancelBlock) {
        cancelHandlers += onCancel
    }

    fun cancel() {
        if (!isActive) return
        val parent = continuation.context[Job] ?: return
        parent.cancel()
    }

    private fun installCancelHandler() {
        if (!isActive) return
        val parent = continuation.context[Job] ?: return
        parent.invokeOnCancel {
            doCancel()
        }
    }

    private fun doCancel() {
        state.updateAndGet { prev ->
            when (prev) {
                CancelState.InComplete -> {
                    CancelState.Cancelled
                }
                is CancelState.Complete<*>,
                is CancelState.Cancelled -> prev
            }
        }
        cancelHandlers.forEach(OnCancelBlock::invoke)
        cancelHandlers.clear()
    }
}

suspend inline fun <T> suspendCancellableCoroutine(
    crossinline block: (CancellationContinuation<T>) -> Unit
): T = suspendCoroutineUninterceptedOrReturn { continuation: Continuation<T> ->
    val cancellationContinuation = CancellationContinuation(continuation)
    block(cancellationContinuation)
    cancellationContinuation.getResult()
}