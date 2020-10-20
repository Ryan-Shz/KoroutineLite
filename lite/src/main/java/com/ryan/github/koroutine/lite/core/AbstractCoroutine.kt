package com.ryan.github.koroutine.lite.core

import com.ryan.github.koroutine.lite.scope.CoroutineScope
import java.lang.IllegalStateException
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.*

abstract class AbstractCoroutine<T>(
    context: CoroutineContext
) : Job, Continuation<T>, CoroutineScope {

    // 当前协程的状态，可能多线程操作，使用Atomic来保证线程安全
    protected val state = AtomicReference<CoroutineState>()

    override val context = context + this

    // 协程是否正在运行中
    override val isActive: Boolean
        get() = state.get() is CoroutineState.InComplete

    override val scopeContext: CoroutineContext
        get() = context

    protected val parentJob = context[Job]

    private var parentCancelDisposable: Disposable? = null

    init {
        // 初始化协程状态为正在执行中
        state.set(CoroutineState.InComplete())
        parentCancelDisposable = parentJob?.invokeOnCancel { cancel() }
    }

    // resumeWith调用时，表示协程已经执行完成
    override fun resumeWith(result: Result<T>) {
        // 更新当前状态为已完成的CoroutineState.Complete
        val newState = state.updateAndGet { prev ->
            when (prev) {
                // 如果是Cancelling状态，表示之前调用过cancel，但协程也要有正常的状态流转到结束(返回结果或者异常)
                is CoroutineState.Cancelling,
                is CoroutineState.InComplete -> {
                    CoroutineState.Complete(result.getOrNull(), result.exceptionOrNull()).from(prev)
                }
                is CoroutineState.Complete<*> -> {
                    throw IllegalStateException("already completion!")
                }
            }
        }
        // 尝试处理异常
        (newState as CoroutineState.Complete<*>).exception?.let(this::tryHandleException)
        // 通知所有回调当前协程以执行完成
        newState.notifyCompletion(result)
        // 通知完之后清空回调
        newState.clear()

        parentCancelDisposable?.dispose()
    }

    private fun tryHandleException(exception: Throwable): Boolean {
        // CancellationException是特殊异常，用来标识协程被取消
        if (exception is CancellationException) {
            return false
        }
        return (parentJob as? AbstractCoroutine<*>)
            ?.handleChildException(exception)
            ?.takeIf { it }
            ?: handleJobException(exception)
    }

    protected open fun handleJobException(exception: Throwable): Boolean {
        return false
    }

    protected open fun handleChildException(e: Throwable): Boolean {
        cancel()
        return tryHandleException(e)
    }

    override fun invokeOnCompletion(onComplete: OnCompleteBlock): Disposable {
        return doOnCompleted(onComplete)
    }

    override fun remove(disposable: Disposable) {
        state.updateAndGet { prev ->
            when (prev) {
                is CoroutineState.InComplete -> CoroutineState.InComplete().from(prev)
                    .without(disposable)
                is CoroutineState.Cancelling -> CoroutineState.Cancelling().from(prev)
                    .without(disposable)
                is CoroutineState.Complete<*> -> prev
            }
        }
    }

    override suspend fun join() {
        when (state.get()) {
            is CoroutineState.Cancelling,
            is CoroutineState.InComplete -> return joinSuspend()
            is CoroutineState.Complete<*> -> {
                val currentCallingJobState = coroutineContext[Job]?.isActive ?: return
                if (!currentCallingJobState) {
                    throw CancellationException("Coroutine is cancelled.")
                }
                return
            }
        }
    }

    // 这是一个挂起函数，返回值是
    private suspend fun joinSuspend() = suspendCoroutine<Unit> { continuation ->
        doOnCompleted {
            continuation.resume(Unit)
        }
    }

    // 保存外部传进来的block回调到State中，以便于在协程执行结束时可以回调它
    protected fun doOnCompleted(callback: OnCompleteCallback<T>): Disposable {
        // 创建Disposable，保存外部传进来的结束回调：block
        val disposable = CompletionHandlerDisposable(this, callback)
        // 将创建好的Disposable对象保存到State中
        val newState = state.updateAndGet { prev ->
            when (prev) {
                is CoroutineState.InComplete -> CoroutineState.InComplete().from(prev)
                    .with(disposable)
                is CoroutineState.Cancelling -> CoroutineState.Cancelling().from(prev)
                    .with(disposable)
                is CoroutineState.Complete<*> -> prev
            }
        }
        // 看看当前状态是否是已完成，如果是，则表示需要直接回调
        (newState as? CoroutineState.Complete<T>)?.let {
            callback(if (it.value != null) Result.success(it.value) else Result.failure(it.exception!!))
        }
        return disposable
    }

    // 保存外部传进来的block回调到State中，以便于在协程取消时回调它
    override fun invokeOnCancel(onCancel: OnCancelBlock): Disposable {
        val disposable = CancellationHandlerDisposable(this, onCancel)
        val newState = state.updateAndGet { prev ->
            when (prev) {
                // 协程还在执行中，将disposable保存到state中
                is CoroutineState.InComplete -> CoroutineState.InComplete().from(prev)
                    .with(disposable)
                // 协程已经取消了或者已经完成，返回之前的状态就行了
                is CoroutineState.Cancelling,
                is CoroutineState.Complete<*> -> prev
            }
        }
        // 如果协程已经是取消状态，则直接调用这个回调
        if (newState is CoroutineState.Cancelling) {
            onCancel()
        }
        return disposable
    }

    // 取消协程
    // 1. 如果协程正在执行中，则将当前的状态修改为Cancelling
    // 2. 修改为Cancelling之后，回调所有之前通过invokeOnCancel传进来的回调
    override fun cancel() {
        // 如果协程正在执行中，则将当前的状态修改为Cancelling
        val newState = state.updateAndGet { prev ->
            when (prev) {
                is CoroutineState.InComplete -> CoroutineState.Cancelling().from(prev)
                is CoroutineState.Complete<*>,
                is CoroutineState.Cancelling -> prev
            }
        }
        // 回调所有之前通过invokeOnCancel传进来的回调
        if (newState is CoroutineState.Cancelling) {
            newState.notifyCancellation()
        }

        parentCancelDisposable?.dispose()
    }
}