package com.ryan.github.koroutine.lite.core

sealed class CoroutineState {

    // 用来保存所有的Disposable，每个Disposable中保存了一个协程完成回调函数
    private var disposableList: DisposableList = DisposableList.Nil

    fun from(state: CoroutineState): CoroutineState {
        this.disposableList = state.disposableList
        return this
    }

    fun with(disposable: Disposable): CoroutineState {
        this.disposableList = DisposableList.Cons(disposable, this.disposableList)
        return this
    }

    fun without(disposable: Disposable): CoroutineState {
        this.disposableList = this.disposableList.remove(disposable)
        return this
    }

    // 通知所有的回调，协程执行完成了
    fun <T> notifyCompletion(result: Result<T>) {
        this.disposableList.loopOn<CompletionHandlerDisposable<T>> {
            it.onComplete(result)
        }
    }

    // 通知所有的回调，协程被取消了
    fun notifyCancellation() {
        disposableList.loopOn<CancellationHandlerDisposable> {
            it.onCancel()
        }
    }

    fun clear() {
        this.disposableList = DisposableList.Nil
    }

    // 表示执行中状态
    class InComplete : CoroutineState()

    // 表示已完成状态
    // 不同的状态可能要保存不同的信息，比如：已完成状态，需要保存执行结果或者异常信息
    // 所以我们用类继承的方式来创建不同的状态
    class Complete<T>(val value: T? = null, val exception: Throwable? = null) : CoroutineState()

    // 表示取消状态
    class Cancelling : CoroutineState()
}