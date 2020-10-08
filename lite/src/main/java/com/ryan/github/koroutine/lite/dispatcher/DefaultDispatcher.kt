package com.ryan.github.koroutine.lite.dispatcher

import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

object DefaultDispatcher : Dispatcher {

    private val threadGroup = ThreadGroup("DefaultDispatcher")
    private val threadId = AtomicInteger(0)
    private val processors = Runtime.getRuntime().availableProcessors()

    private val executor = Executors.newFixedThreadPool(2 * processors) {
        Thread(threadGroup, it, "${threadGroup.name}-worker-${threadId.getAndIncrement()}").apply {
            isDaemon = true
        }
    }

    override fun dispatch(block: () -> Unit) {
        executor.execute(block)
    }
}