package com.ryan.github.koroutine.lite.cancel

sealed class CancelState {
    object InComplete : CancelState()
    class Complete<T>(val value: T? = null, val exception: Throwable? = null) : CancelState()
    object Cancelled: CancelState()
}