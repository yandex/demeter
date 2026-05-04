package com.yandex.test.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TestCoroutineLaunch {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun doWork() {
        scope.launch {
            println("working")
        }
    }
}
