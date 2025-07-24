package com.yandex.test

class TestClassWithSomeExitsInMethod {

    fun testMethod(value: Int): Any? {
        if (value == 0) {
            return 0
        }
        if (value == 1) {
            throw IllegalStateException("1")
        }
        if (value == 2) {
            return null
        }
        if (value == 3) {
            return value
        }
        if (value == 4) {
            return SomeClass(value)
        }
        when (value) {
            5 -> return 5
            6 -> throw IllegalStateException("6")
            7 -> return null
            8 -> return value
            9 -> return SomeClass(value)
        }
        val square = value * value
        val someClass = SomeClass(square)
        return SomeClass(someClass.value)
    }

    class SomeClass(val value: Int)
}