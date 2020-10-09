package com.ryan.github.koroutine.lite

import java.lang.IllegalStateException
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.*

abstract class AbstractCoroutine<T>(
    override val context: CoroutineContext
) : Job, Continuation<T> {

    // 当前协程的状态，可能多线程操作，使用Atomic来保证线程安全
    protected val state = AtomicReference<CoroutineState>()

    // 协程是否正在运行中
    override val isActive: Boolean
        get() = state.get() is CoroutineState.InComplete

    init {
        // 初始化协程状态为正在执行中
        state.set(CoroutineState.InComplete())
    }

    override fun resumeWith(result: Result<T>) {
        // 更新当前状态为已完成的CoroutineState.Complete
        val newState = state.updateAndGet { prev ->
            when (prev) {
                is CoroutineState.InComplete -> {
                    CoroutineState.Complete(result.getOrNull(), result.exceptionOrNull()).from(prev)
                }
                is CoroutineState.Complete<*> -> {
                    throw IllegalStateException("already completion!")
                }
            }
        }
        // 通知所有回调当前协程以执行完成
        newState.notifyCompletion(result)
        // 通知完之后清空回调
        newState.clear()
    }

    override fun invokeOnCompletion(onComplete: OnCompleteBlock): Disposable {
        return doOnCompleted(onComplete)
    }

    override fun remove(disposable: Disposable) {
        state.updateAndGet { prev ->
            when (prev) {
                is CoroutineState.InComplete -> {
                    CoroutineState.InComplete().from(prev).without(disposable)
                }
                is CoroutineState.Complete<*> -> prev
            }
        }
    }

    override suspend fun join() {
        when (state.get()) {
            is CoroutineState.InComplete -> return joinSuspend()
            is CoroutineState.Complete<*> -> return
        }
    }

    // 这是一个挂起函数，返回值是
    private suspend fun joinSuspend() = suspendCoroutine<Unit> { continuation ->
        doOnCompleted {
            continuation.resume(Unit)
        }
    }

    // 保存外部传进来的block回调到State中，以便于在协程执行结束时可以回调它
    private fun doOnCompleted(callback: OnCompleteCallback<T>): Disposable {
        // 创建Disposable，保存外部传进来的结束回调：block
        val disposable = CompletionHandlerDisposable(this, callback)
        // 将创建好的Disposable对象保存到State中
        val newState = state.updateAndGet { prev ->
            when (prev) {
                is CoroutineState.InComplete -> {
                    CoroutineState.InComplete().from(prev).with(disposable)
                }
                is CoroutineState.Complete<*> -> prev
            }
        }
        // 看看当前状态是否是已完成，如果是，则表示需要直接回调
        (newState as? CoroutineState.Complete<T>)?.let {
            callback(if (it.value != null) Result.success(it.value) else Result.failure(it.exception!!))
        }
        return disposable
    }
}