package com.ryan.github.koroutine.lite.dispatcher

object Dispatchers {
    val Default by lazy {
        DispatcherContext(DefaultDispatcher)
    }

    val Main by lazy {
        DispatcherContext(HandlerDispatcher)
    }
}