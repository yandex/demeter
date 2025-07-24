package com.yandex.demeter

interface Reporter {
  fun report(payload: Map<String, Any>)
}