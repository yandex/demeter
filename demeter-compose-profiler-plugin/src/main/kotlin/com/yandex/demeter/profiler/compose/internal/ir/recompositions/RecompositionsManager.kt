package com.yandex.demeter.profiler.compose.internal.ir.recompositions

import androidx.compose.runtime.Composer
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.profiler.compose.internal.data.ComposeMetricHolder.recompositionsNotifier

internal data class ObjectRecomposition(
    val composableFunctionName: String,
    val fileNameWithPackage: String,
) {
    override fun toString(): String =
        "$fileNameWithPackage#$composableFunctionName"
}

internal interface RecompositionNotifier {
    fun recomposed(recomposition: ObjectRecomposition)

    fun skipped(recomposition: ObjectRecomposition)
}

@InternalDemeterApi
fun notifyRecomposition(
    composer: Composer,
    composableFunctionName: String,
    fileNameWithPackage: String,
) {
    recompositionsNotifier.recomposed(
        ObjectRecomposition(
            composableFunctionName,
            fileNameWithPackage
        )
    )
}

@InternalDemeterApi
fun notifySkip(
    composer: Composer,
    composableFunctionName: String,
    fileNameWithPackage: String,
) {
    recompositionsNotifier.skipped(
        ObjectRecomposition(
            composableFunctionName,
            fileNameWithPackage
        )
    )
}
