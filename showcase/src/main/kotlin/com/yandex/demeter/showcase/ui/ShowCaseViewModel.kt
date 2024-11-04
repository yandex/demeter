package com.yandex.demeter.showcase.ui

import com.yandex.demeter.showcase.di.scope.PerShowCase
import kotlinx.coroutines.sync.Mutex
import java.lang.reflect.Proxy
import javax.inject.Inject
import kotlin.random.Random

@PerShowCase
class ShowCaseViewModel @Inject constructor(
    val bigObject: BigObject,
)

class BigObject @Inject constructor(
    val innerClass: InnerClass,
) {
    init {
        Thread.sleep(randomLong())
        reflectCall()
    }

    private fun reflectCall() {
        val proxy: Mutex = Proxy.newProxyInstance(
            Mutex::class.java.classLoader,
            arrayOf<Class<*>>(Mutex::class.java)
        ) { _, _, _ -> null } as Mutex
        proxy.unlock()
    }

    class InnerClass @Inject constructor()
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun randomLong(): Long {
    return Random.nextLong(until = 200)
}
