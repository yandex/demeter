package com.yandex.demeter.showcase.coroutine

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Demonstrates various coroutine patterns for the Coroutine Tracer plugin.
 * All launch/async calls in this class will be automatically instrumented
 * by the Demeter Coroutine Tracer Gradle plugin.
 */
class CoroutineShowCase(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
) {
    /**
     * Sequential launches — two coroutines launched one after another.
     */
    fun sequentialLaunches(): Job = scope.launch {
        launch {
            delay(100)
        }
        launch {
            delay(200)
        }
    }

    /**
     * Nested launches — parent coroutine launches children.
     * Creates a tree: parent -> child1, child2 -> grandchild
     */
    fun nestedLaunches(): Job = scope.launch {
        launch {
            delay(50)
            launch {
                delay(30)
            }
        }
        launch {
            delay(100)
        }
        delay(200)
    }

    /**
     * Async/await pattern — parallel work with result aggregation.
     */
    fun asyncAwaitPattern(): Job = scope.launch {
        val deferred1 = async(Dispatchers.IO) {
            delay(150)
            "result1"
        }
        val deferred2 = async(Dispatchers.IO) {
            delay(100)
            "result2"
        }
        val combined = "${deferred1.await()} + ${deferred2.await()}"
        println("Combined: $combined")
    }

    /**
     * Dispatcher switching — demonstrates coroutines on different threads.
     */
    fun dispatcherSwitching(): Job = scope.launch(Dispatchers.Main) {
        // Main thread work
        delay(50)

        launch(Dispatchers.IO) {
            // IO thread work
            delay(100)
        }

        launch(Dispatchers.Default) {
            // Default thread work
            delay(75)
        }
    }

    /**
     * Cancellation demo — launches a coroutine and cancels it.
     */
    fun cancellationDemo(): Job = scope.launch {
        val job = launch {
            launch {
                delay(1000)
            }
            launch {
                delay(2000)
            }
            delay(5000)
        }

        // Cancel after 200ms
        launch {
            delay(200)
            job.cancel()
        }
    }

    /**
     * SupervisorScope — one failing child doesn't cancel siblings.
     */
    fun supervisorScopeDemo(): Job = scope.launch {
        val exceptionHandler = CoroutineExceptionHandler { _, exception ->
            println("Caught exception in supervisorScopeDemo: ${exception.message}")
        }

        supervisorScope {
            launch(exceptionHandler) {
                delay(100)
                throw RuntimeException("Intentional failure for demo")
            }
            launch {
                delay(200)
                // This should complete despite sibling failure
            }
            launch {
                delay(150)
            }
        }
    }

    /**
     * Flow collection — collecting values from a flow inside a launched coroutine.
     */
    fun flowCollection(): Job = scope.launch {
        flow {
            emit(1)
            delay(80)
            emit(2)
            delay(80)
            emit(3)
        }.collect { value ->
            delay(50)
            println("Collected: $value")
        }
    }

    /**
     * Multiple flow subscribers — two coroutines collecting from the same SharedFlow.
     */
    fun multipleFlowSubscribers(): Job = scope.launch {
        val sharedFlow = MutableSharedFlow<Int>(replay = 0)
        val itemCount = 3

        // Subscriber 1 — fast consumer
        launch {
            sharedFlow.take(itemCount).collect { value ->
                delay(30)
                println("Subscriber1: $value")
            }
        }

        // Subscriber 2 — slow consumer on IO
        launch(Dispatchers.IO) {
            sharedFlow.take(itemCount).collect { value ->
                delay(100)
                println("Subscriber2: $value")
            }
        }

        // Producer — emits values
        launch {
            repeat(itemCount) { i ->
                delay(60)
                sharedFlow.emit(i)
            }
        }
    }

    /**
     * StateFlow with transformations — flatMapLatest + map chain.
     */
    fun stateFlowTransform(): Job = scope.launch {
        val stateFlow = MutableStateFlow(0)
        val updateCount = 4

        // Observer with transform chain
        launch(Dispatchers.Default) {
            stateFlow
                .drop(1)
                .take(updateCount)
                .map { it * 10 }
                .filter { it > 0 }
                .collect { value ->
                    delay(40)
                    println("Transformed: $value")
                }
        }

        // Updater
        launch {
            repeat(updateCount) { i ->
                delay(70)
                stateFlow.value = i + 1
            }
        }
    }

    /**
     * Timeout with withTimeoutOrNull — demonstrates a coroutine that times out.
     */
    fun timeoutDemo(): Job = scope.launch {
        val result = withTimeoutOrNull(150) {
            launch(Dispatchers.IO) {
                delay(100)
                println("Fast task done")
            }
            launch(Dispatchers.Default) {
                delay(300)
                println("Slow task — should be cancelled")
            }
            delay(200)
            "completed"
        }
        println("Timeout result: $result")
    }

    /**
     * Retry pattern — retries a failing operation on IO dispatcher.
     */
    fun retryPattern(): Job = scope.launch {
        launch(Dispatchers.IO) {
            repeat(3) { i ->
                val attempt = i + 1
                launch {
                    delay(50L * attempt)
                    if (attempt < 3) {
                        println("Attempt $attempt failed")
                    } else {
                        println("Attempt $attempt succeeded")
                    }
                }
            }
        }
    }

    fun destroy() {
        scope.cancel()
    }
}
