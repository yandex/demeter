package com.yandex.demeter

interface DemeterLogger {
    fun d(msg: String, depth: Int = 0)

    fun i(msg: String, depth: Int = 0)

    fun e(msg: String, depth: Int = 0)
}
