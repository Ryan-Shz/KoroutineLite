package com.ryan.github.koroutine.lite.core

import com.ryan.github.koroutine.lite.core.AbstractCoroutine
import kotlin.coroutines.CoroutineContext

class StandardCoroutine(context: CoroutineContext) : AbstractCoroutine<Unit>(context)