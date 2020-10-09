package com.ryan.github.koroutine.lite

import com.ryan.github.koroutine.lite.dispatcher.Dispatcher
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque
import kotlin.coroutines.CoroutineContext

typealias EventTask = () -> Unit

class BlockingQueueDispatcher : LinkedBlockingDeque<EventTask>(), Dispatcher {

    override fun dispatch(block: () -> Unit) {
        offer(block)
    }

}

class BlockingCoroutine<T>(context: CoroutineContext, val eventQueue: BlockingDeque<EventTask>) :
    AbstractCoroutine<T>(context) {
    fun joinBlocking(): T {
        while (isActive) {
            eventQueue.take().invoke()
        }
        return (state.get() as CoroutineState.Complete<T>).let {
            it.value ?: throw it.exception!!
        }
    }
}