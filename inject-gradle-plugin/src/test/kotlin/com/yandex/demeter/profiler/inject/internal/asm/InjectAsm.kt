package com.yandex.demeter.profiler.inject.internal.asm

object InjectAsm {
    data class Event(val startTimeNs: Long, val className: String, val parameterClassNames: String)
    val events = mutableListOf<Event>()

    @JvmStatic
    fun log(startTimeNs: Long, className: String, parameterClassNames: String) {
        events += Event(startTimeNs, className, parameterClassNames)
    }
}