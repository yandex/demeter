@file:Suppress("UNUSED_PARAMETER", "unused")

package com.yandex.test

open class BaseTestApply(label: String)

class TestApply : BaseTestApply("label".apply {
    // no-op
}) {
    fun fewMethodsForTest() {
        println("one")
        println("two")
        println("three")
    }
}
