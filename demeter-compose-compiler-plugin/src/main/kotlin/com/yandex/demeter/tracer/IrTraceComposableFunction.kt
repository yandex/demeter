package com.yandex.demeter.tracer

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irCatch
import org.jetbrains.kotlin.backend.common.lower.irThrow
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irConcat
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irTemporary
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.addArgument
import org.jetbrains.kotlin.ir.expressions.impl.IrTryImpl
import org.jetbrains.kotlin.ir.util.statements
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

internal class IrTraceComposableFunction(
    private val pluginContext: IrPluginContext,
    private val messageCollector: MessageCollector,
) {
    private val typeNullableAny = pluginContext.irBuiltIns.anyNType
    private val typeUnit = pluginContext.irBuiltIns.unitType
    private val typeThrowable = pluginContext.irBuiltIns.throwableType

    private val classMonotonic = pluginContext.referenceClass(
        ClassId(
            FqName("kotlin.time"),
            relativeClassName = FqName("TimeSource.Monotonic"),
            false
        ),
    )!!

    private val funMarkNow = pluginContext.referenceFunctions(
        callableId = CallableId(
            classId = ClassId(
                FqName("kotlin.time"),
                relativeClassName = FqName("TimeSource.Monotonic"),
                false
            ),
            callableName = Name.identifier("markNow")
        )
    )
        .single()

    private val funElapsedNow = pluginContext.referenceFunctions(
        callableId = CallableId(
            classId = ClassId(
                FqName("kotlin.time"),
                relativeClassName = FqName("TimeMark"),
                false
            ),
            callableName = Name.identifier("elapsedNow")
        )
    )
        .single()

    private val logFunction = pluginContext.referenceFunctions(
        callableId = CallableId(
            FqName("kotlin.io"),
            Name.identifier("println")
        )
    ).single {
        val parameters = it.owner.valueParameters
        parameters.size == 1 && parameters[0].type == typeNullableAny
    }

    fun irBuildBody(
        function: IrFunction,
        body: IrBody
    ): IrBlockBody {
        return DeclarationIrBuilder(pluginContext, function.symbol).irBlockBody {
            +irEnterElapsedTime(function)

            val startTime = irTemporary(irCall(funMarkNow).also { call ->
                call.dispatchReceiver = irGetObject(classMonotonic)
            })

            val tryBlock = irBlock(resultType = function.returnType) {
                for (statement in body.statements) {
                    +statement
                }
                if (function.returnType == typeUnit) {
                    +irExitElapsedTime(function, startTime)
                }
            }.transform(
                DebugLogReturnTransformer(pluginContext, function, startTime),
                null
            )

            val throwable = buildVariable(
                scope.getLocalDeclarationParent(),
                startOffset,
                endOffset,
                IrDeclarationOrigin.CATCH_PARAMETER,
                Name.identifier("t"),
                typeThrowable
            )

            +IrTryImpl(startOffset, endOffset, tryBlock.type).also { irTry ->
                irTry.tryResult = tryBlock
                irTry.catches += irCatch(throwable, irBlock {
                    +irExitElapsedTime(function, startTime, irGet(throwable))
                    +irThrow(irGet(throwable))
                })
            }
        }
    }

    private fun IrBuilderWithScope.irEnterElapsedTime(
        function: IrFunction
    ): IrCall {
        val concat = irConcat()
        concat.addArgument(irString("DEMETERCOMPOSE: ⇢ ${function.name}("))
        for ((index, valueParameter) in function.valueParameters.withIndex()) {
            if (index > 0) concat.addArgument(irString(", "))
            concat.addArgument(irString("${valueParameter.name}="))
            concat.addArgument(irGet(valueParameter))
        }
        concat.addArgument(irString(")"))

        return irCall(logFunction).also { call ->
            call.putValueArgument(0, concat)
        }
    }

    private fun IrBuilderWithScope.irExitElapsedTime(
        function: IrFunction,
        startTime: IrValueDeclaration,
        result: IrExpression? = null
    ): IrCall {
        val concat = irConcat()
        concat.addArgument(irString("DEMETERCOMPOSE: ⇠ ${function.name} ["))
        concat.addArgument(irCall(funElapsedNow).also { call ->
            call.dispatchReceiver = irGet(startTime)
        })
        if (result != null) {
            concat.addArgument(irString("] = "))
            concat.addArgument(result)
        } else {
            concat.addArgument(irString("]"))
        }

        return irCall(logFunction).also { call ->
            call.putValueArgument(0, concat)
        }
    }

    /**
     * Converts
     * <code>
     *     return result
     * </code>
     * to
     * <code>
     *     val r = result
     *     function.invoke()
     *     return r
     *     </code>
     */
    inner class DebugLogReturnTransformer(
        private val pluginContext: IrPluginContext,
        private val function: IrFunction,
        private val startTime: IrVariable
    ) : IrElementTransformerVoidWithContext() {

        override fun visitReturn(expression: IrReturn): IrExpression {
            if (expression.returnTargetSymbol != function.symbol) {
                return super.visitReturn(expression)
            }

            return DeclarationIrBuilder(pluginContext, function.symbol).irBlock {
                val result = irTemporary(expression.value)
                +irExitElapsedTime(function, startTime, irGet(result))
                +expression.apply {
                    value = irGet(result)
                }
            }
        }
    }
}
