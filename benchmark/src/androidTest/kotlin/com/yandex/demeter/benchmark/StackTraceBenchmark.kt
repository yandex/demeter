package com.yandex.demeter.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.sync.Mutex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.OrderWith
import org.junit.runner.RunWith
import org.junit.runner.manipulation.Alphanumeric
import java.lang.reflect.Proxy
import java.util.stream.Stream

@RunWith(AndroidJUnit4::class)
@OrderWith(Alphanumeric::class)
class StackTraceBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun threadCurrentThread() {
        benchmarkRule.measureRepeated {
            Thread.currentThread()
        }
    }

    @Test
    fun threadStackTrace() {
        benchmarkRule.measureRepeated {
            Thread.currentThread().stackTrace
        }
    }

    @Test
    fun threadStackTraceCallerMethod() {
        var callerMethodStackFrame: StackTraceElement? = null
        benchmarkRule.measureRepeated {
            callerMethodStackFrame = Thread.currentThread().stackTrace[3]
        }
        assertEquals("java.lang.reflect.Method", callerMethodStackFrame!!.className)
        assertEquals("invoke", callerMethodStackFrame!!.methodName)
    }

    @Test
    fun throwableInit() {
        benchmarkRule.measureRepeated {
            Throwable()
        }
    }

    @Test
    fun throwableOnlyStackTrace() {
        var throwable: Throwable? = null
        benchmarkRule.measureRepeated {
            runWithTimingDisabled {
                throwable = Throwable()
            }
            throwable!!.stackTrace
        }
    }

    @Test
    fun throwableFillInStackTrace() {
        var throwable: Throwable? = null
        benchmarkRule.measureRepeated {
            runWithTimingDisabled {
                throwable = Throwable()
            }
            throwable!!.fillInStackTrace()
        }
    }

    @Test
    fun throwableStackTrace() {
        benchmarkRule.measureRepeated {
            Throwable().stackTrace
        }
    }

    @Test
    fun throwableStackTraceCallerMethod() {
        var callerMethodStackFrame: StackTraceElement? = null
        benchmarkRule.measureRepeated {
            callerMethodStackFrame = Throwable().stackTrace[1]
        }
        assertEquals("java.lang.reflect.Method", callerMethodStackFrame!!.className)
        assertEquals("invoke", callerMethodStackFrame!!.methodName)
    }

    @Test
    fun testThrowableAndStackTraceSeparately() {
        fun someFunction(block: () -> Unit) {
            block()
        }

        var insideInnerFunctionStackTrace: Array<StackTraceElement>? = null
        var actualStackTrace: Array<StackTraceElement>? = null
        val actualThrowable = Throwable()

        someFunction {
            insideInnerFunctionStackTrace = Throwable().stackTrace
            actualStackTrace = actualThrowable.stackTrace
        }

        assertNotEquals(
            "testThrowableAndStackTraceSeparately",
            insideInnerFunctionStackTrace!!.first().methodName
        )
        assertEquals("testThrowableAndStackTraceSeparately", actualStackTrace!!.first().methodName)
    }

    @Test
    fun testThrowableAndStackTraceSeparatelyAndReuse() {
        fun someFunction(block: () -> Unit) {
            block()
        }

        var actualStackTrace: Array<StackTraceElement>? = null
        val actualThrowable = Throwable()

        someFunction {
            actualStackTrace = actualThrowable.stackTrace
        }

        assertEquals(
            "testThrowableAndStackTraceSeparatelyAndReuse",
            actualStackTrace!!.first().methodName
        )

        someFunction {
            actualThrowable.fillInStackTrace()
        }
        actualStackTrace = actualThrowable.stackTrace

        assertNotEquals(
            "testThrowableAndStackTraceSeparatelyAndReuse",
            actualStackTrace!!.first().methodName
        )
    }

    @Test
    fun stackWalkerGetInstance() {
        try {
            Class.forName("java.lang.StackWalker")
        } catch (e: ClassNotFoundException) {
            return
        }
        benchmarkRule.measureRepeated {
            StackWalker.getInstance(StackWalker.Option.SHOW_HIDDEN_FRAMES)
        }
    }

    @Test
    fun stackWalkerStackTraceToArray() {
        try {
            Class.forName("java.lang.StackWalker")
        } catch (e: ClassNotFoundException) {
            return
        }
        val stackWalker = StackWalker.getInstance(StackWalker.Option.SHOW_HIDDEN_FRAMES)
        benchmarkRule.measureRepeated {
            stackWalker.walk(Stream<StackWalker.StackFrame>::toArray)
        }
    }

    @Test
    fun stackWalkerStackTraceToList() {
        try {
            Class.forName("java.lang.StackWalker")
        } catch (e: ClassNotFoundException) {
            return
        }
        val stackWalker = StackWalker.getInstance(StackWalker.Option.SHOW_HIDDEN_FRAMES)
        benchmarkRule.measureRepeated {
            stackWalker.walk(Stream<StackWalker.StackFrame>::toList)
        }
    }

    @Test
    fun stackWalkerStackTraceCallerMethod() {
        try {
            Class.forName("java.lang.StackWalker")
        } catch (e: ClassNotFoundException) {
            return
        }
        // why 10 and not 2 for example?
        // 10 does not crash our project.
        // Not 2 because https://cs.android.com/android/platform/superproject/main/+/main:libcore/ojluni/src/main/java/java/lang/StackStreamFactory.java;drc=4b2d43eb620eaa4750c21354e3880ea2234aa844;l=616
        val stackWalker = StackWalker.getInstance(setOf(StackWalker.Option.SHOW_HIDDEN_FRAMES), 10)
        var callerMethodStackFrame: StackWalker.StackFrame? = null
        benchmarkRule.measureRepeated {
            callerMethodStackFrame = stackWalker.walk { it.limit(2).skip(1).findFirst().get() }
        }
        assertEquals("java.lang.reflect.Method", callerMethodStackFrame!!.className)
        assertEquals("invoke", callerMethodStackFrame!!.methodName)
    }

    // not benchmark
    @Test
    fun stackWalkerStackTraceReflectCallerMethod() {
        try {
            Class.forName("java.lang.StackWalker")
        } catch (e: ClassNotFoundException) {
            return
        }
        val stackWalker = StackWalker.getInstance(setOf(StackWalker.Option.SHOW_HIDDEN_FRAMES), 10)
        val proxy: Mutex = Proxy.newProxyInstance(
            Mutex::class.java.classLoader,
            arrayOf<Class<*>>(Mutex::class.java)
        ) { _, _, _ ->
            stackWalker.walk { it.limit(2).skip(1).findFirst().get() }
            null
        } as Mutex
        proxy.unlock()
    }
}
