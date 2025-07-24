package com.yandex.demeter.compose

import com.yandex.demeter.DemeterLogger
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal class IrTrackComposableState(
    private val pluginContext: IrPluginContext,
    private val logger: DemeterLogger,
) {
    private val trackerFunction = pluginContext.referenceFunctions(
        callableId = CallableId(
            FqName("com.yandex.demeter.profiler.compose.internal.ir.tracker"),
            Name.identifier("registerTracking")
        )
    ).single()

    private val recomposeNotifyFunction = pluginContext.referenceFunctions(
        callableId = CallableId(
            FqName("com.yandex.demeter.profiler.compose.internal.ir.recompositions"),
            Name.identifier("notifyRecomposition")
        )
    ).single()

    private val skipNotifyFunction = pluginContext.referenceFunctions(
        callableId = CallableId(
            FqName("com.yandex.demeter.profiler.compose.internal.ir.recompositions"),
            Name.identifier("notifySkip")
        )
    ).single()

    private val logFunction = pluginContext.referenceFunctions(
        callableId = CallableId(
            FqName("kotlin.io"),
            Name.identifier("println")
        )
    ).single {
        val parameters = it.owner.valueParameters
        parameters.size == 1 && parameters[0].type == pluginContext.irBuiltIns.anyNType
    }

    fun irBuildBody(
        function: IrFunction,
        body: IrBody,
        currentFileName: String?
    ): IrBody {
        val statements = (body as? IrBlockBody)?.statements ?: return body

        return DeclarationIrBuilder(pluginContext, function.symbol).irBlockBody {
            statements.forEach { statement ->
                statement.accept(
                    ComposeStatementTransformer(
                        logger,
                        this,
                        trackerFunction,
                        logFunction,
                        recomposeNotifyFunction,
                        skipNotifyFunction,
                        function,
                        currentFileName
                    ),
                    null
                )

                logger.d(statement.dump())
            }

            +statements
        }
    }
}
