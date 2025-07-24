package com.yandex.demeter.internal.compose.recompositions

import androidx.compose.runtime.Composer

fun notifyRecomposition(
    composer: Composer,
    composableFunctionName: String,
    fileNameWithPackage: String,
) {
    println("registered notifyRecomposition for $composableFunctionName")
}

fun notifySkip(
    composer: Composer,
    composableFunctionName: String,
    fileNameWithPackage: String,
) {
    println("registered notifySkip for $composableFunctionName")
}
