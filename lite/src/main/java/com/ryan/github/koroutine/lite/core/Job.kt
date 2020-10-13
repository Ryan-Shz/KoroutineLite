package com.ryan.github.koroutine.lite.core

import java.util.concurrent.CancellationException
import kotlin.coroutines.CoroutineContext

typealias OnCompleteBlock = (Result<*>) -> Unit
typealias CancellationException = CancellationException
typealias OnCancelBlock = () -> Unit

interface Job : CoroutineContext.Element {

    companion object Key : CoroutineContext.Key<Job>

    override val key: CoroutineContext.Key<*>
        get() = Key

    // 协程是在运行状态
    val isActive: Boolean

    // 传递一个回调函数，协程执行完成后回调它
    fun invokeOnCompletion(onComplete: OnCompleteBlock): Disposable

    // 移除回调函数
    fun remove(disposable: Disposable)

    // 类似线程的join函数
    suspend fun join()

    // 取消协程执行
    fun cancel()

    // 添加取消回调
    fun invokeOnCancel(onCancel: OnCancelBlock): Disposable
}