package com.ryan.github.koroutine.lite

import kotlin.coroutines.CoroutineContext

class CoroutineName(private val name: String) : CoroutineContext.Element {

    companion object Key : CoroutineContext.Key<CoroutineName>

    override val key: CoroutineContext.Key<*>
        get() = Key

    override fun toString(): String {
        return name
    }
}