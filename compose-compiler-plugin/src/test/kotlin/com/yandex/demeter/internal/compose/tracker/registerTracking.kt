package com.yandex.demeter.internal.compose.tracker

import androidx.compose.runtime.Composer

fun <S : Any> registerTracking(
    state: S,
    composer: Composer,
    composableFunctionName: String,
    stateName: String,
    fileNameWithPackage: String,
): S = state.also {
    println("registered tracking for $state")
}
