package com.yandex.test.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class TestCoroutineAsync {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun doWork() {
        scope.async {
            "result"
        }
    }
}
