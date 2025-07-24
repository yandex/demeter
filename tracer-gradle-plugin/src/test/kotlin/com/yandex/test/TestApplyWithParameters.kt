@file:Suppress("UNUSED_PARAMETER", "unused")

package com.yandex.test

open class BaseTestApplyWithParameters(label: String, title: String)

class TestApplyWithParameters(title: String, label: String) :
    BaseTestApplyWithParameters(label.apply {
        // no-op
    }, title) {
    fun fewMethodsForTest() {
        println("one")
        println("two")
        println("three")
    }
}
