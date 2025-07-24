package com.yandex.demeter.profiler.compose.internal.data.model

internal data class ComposeName(
    val funName: String,
    val stateName: String,
    val fileNameWithPackage: String,
) {
    override fun toString(): String = "$stateName ($fileNameWithPackage: $funName)"
}
