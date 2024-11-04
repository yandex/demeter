package com.yandex.test

class TestClassWithSomeThrows(value: Int) {

    init {
        if (value == 0) {
            throw IllegalStateException("0")
        }
        when (value) {
            1 -> throw IllegalStateException("1")
            2 -> throw IllegalStateException("2")
            3 -> throw IllegalStateException("3")
        }
    }
}