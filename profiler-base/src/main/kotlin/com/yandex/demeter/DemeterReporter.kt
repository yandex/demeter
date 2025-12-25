package com.yandex.demeter

fun interface DemeterReporter {
  fun report(payload: Map<String, Any>)
}
