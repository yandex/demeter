package com.yandex.test.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class TestCoroutineMultipleLaunches {
    private val scope = CoroutineScope(Dispatchers.Default)

    fun doWork() {
        scope.launch {
            println("first")
        }
        scope.launch {
            println("second")
        }
        scope.async {
            "third"
        }
    }
}
