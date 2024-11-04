package com.yandex.demeter.internal

import com.yandex.demeter.annotations.InternalDemeterApi

@InternalDemeterApi
enum class WarningLevel(
    val level: Int,
    val threshold: Long
) {
    Zero(0, 0),
    First(1, 16),
    Second(2, 50),
    Third(3, 150),
    ;

    companion object {
        @JvmStatic
        fun getLevel(time: Long): WarningLevel {
            return entries.toTypedArray().lastOrNull { it.threshold < time }
                ?: Zero
        }
    }
}
