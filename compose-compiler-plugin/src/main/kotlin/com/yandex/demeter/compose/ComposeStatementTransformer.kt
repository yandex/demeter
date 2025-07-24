package com.yandex.demeter.compose

import com.yandex.demeter.DemeterLogger
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.transformStatement
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classFqName
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.superTypes

class ComposeStatementTransformer(
    private val logger: DemeterLogger,
    private val irBlockBodyBuilder: IrBlockBodyBuilder,
    private val registerTrackerFunction: IrSimpleFunctionSymbol,
    private val logFunction: IrSimpleFunctionSymbol,
    private val recomposeNotifyFunction: IrSimpleFunctionSymbol,
    private val skipNotifyFunction: IrSimpleFunctionSymbol,
    private val function: IrFunction,
    private val currentFileName: String?,
    private val parentVariables: MutableList<IrVariable> = mutableListOf(),
    private val depth: Int = 0
) : IrElementTransformerVoidWithContext() {
    private var currentStateVariables = mutableListOf<IrVariable>()

    override fun visitBlock(expression: IrBlock): IrExpression {
        logger.d("inspect block: ${expression.statements}", depth)
        return super.visitBlock(expression)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        transformRecomposeCall(expression)?.let { return it }
        transformSkipToGroupEnd(expression)?.let { return it }
        return super.visitCall(expression)
    }

    override fun visitVariable(declaration: IrVariable): IrStatement {
        logger.d("found variable: ${declaration.name.asString()} is ${
            declaration.type.superTypes().map { it.classFqName?.asString() }
        }, ${declaration.symbol.owner.origin}",
            depth
        )

        if (declaration.type.isStateVariable() && declaration.symbol.owner.origin != IrDeclarationOrigin.IR_TEMPORARY_VARIABLE) {
            logger.d("add variable: ${declaration.name.asString()} is ${
                declaration.type.superTypes().map { it.classFqName?.asString() }
            }, ${declaration.symbol.owner.origin}",
                depth
            )
            parentVariables.add(declaration)
        }

        return super.visitVariable(declaration)
    }

    override fun visitWhen(expression: IrWhen): IrExpression {
        expression.branches.forEach { branch ->
            when (val result = branch.result) {
                is IrCall -> {
                    logger.d(
                        "[result] found call: ${result.symbol.owner.fqNameWhenAvailable?.asString()}",
                        depth
                    )
                }

                is IrBlock -> {
                    logger.d(
                        "[result] found block: ${result.statements.map { it }}",
                        depth
                    )

                    val newStatements = mutableListOf<IrStatement>()

                    result.statements.forEach { statement ->
                        statement.transformStatement(
                            ComposeStatementTransformer(
                                logger,
                                irBlockBodyBuilder,
                                registerTrackerFunction,
                                logFunction,
                                recomposeNotifyFunction,
                                skipNotifyFunction,
                                function,
                                currentFileName,
                                currentStateVariables,
                                depth + 1
                            )
                        )

                        newStatements.add(statement)
                    }

                    logger.d("[variable] list to add: $currentStateVariables")

                    currentStateVariables.forEach {
                        irBlockBodyBuilder.irTrack(it)?.let { call ->
                            logger.d(
                                "[variable] add new statement: ${it.type}",
                                depth
                            )

                            newStatements.add(call)
                        }
                    }

                    currentStateVariables.clear()

                    result.statements.clear()
                    result.statements.addAll(newStatements)
                }
            }
        }

        return super.visitWhen(expression)
    }

    private fun IrType.isStateVariable(): Boolean {
        return superTypes().any { it.classFqName?.asString()?.contains("State") == true } ||
            classFqName?.asString() == "androidx.compose.animation.core.Animatable"
    }

    private fun transformRecomposeCall(expression: IrCall): IrExpression? {
        return if (expression.symbol.owner.fqNameWhenAvailable.toString()
                .contains("androidx.compose.runtime.ScopeUpdateScope.updateScope")
        ) {
            logger.d(
                "[transform] found updateScope: ${expression.symbol.owner} ${expression.symbol.owner.fqNameWhenAvailable.toString()} in ${function.name.asString()} (${currentFileName.orEmpty()})",
                depth
            )

            irBlockBodyBuilder.irRecompose(expression)?.let { recomposeExpression ->
                irBlockBodyBuilder.irBlock {
                    +expression
                    +recomposeExpression
                }
            }
        } else {
            null
        }
    }

    private fun transformSkipToGroupEnd(expression: IrCall): IrExpression? {
        return if (expression.symbol.owner.fqNameWhenAvailable.toString()
                .contains("androidx.compose.runtime.Composer.skipToGroupEnd")
        ) {
            logger.d(
                "[transform] found skipToGroupEnd: ${expression.symbol.owner} ${expression.symbol.owner.fqNameWhenAvailable.toString()} in ${function.name.asString()} (${currentFileName.orEmpty()})",
                depth
            )

            irBlockBodyBuilder.irSkip(expression)?.let { skipExpression ->
                irBlockBodyBuilder.irBlock {
                    +expression
                    +skipExpression
                }
            }
        } else {
            null
        }
    }

    private fun IrBuilderWithScope.irRecompose(
        stateCall: IrCall
    ): IrExpression? {
        val composer = function.getComposer() ?: return null

        logger.i("Registered recomposition for ${stateCall.symbol.owner.name} in ${function.name.asString()} (${currentFileName.orEmpty()})")

        return irCall(recomposeNotifyFunction).also { call ->
            call.putValueArgument(0, irGet(composer))
            call.putValueArgument(1, irString(function.name.asString()))
            call.putValueArgument(2, irString(currentFileName.orEmpty()))
        }
    }

    private fun IrBuilderWithScope.irSkip(
        stateCall: IrCall
    ): IrExpression? {
        val composer = function.getComposer() ?: return null

        logger.i("Registered skip for ${stateCall.symbol.owner.name} in ${function.name.asString()} (${currentFileName.orEmpty()})")

        return irCall(skipNotifyFunction).also { call ->
            call.putValueArgument(0, irGet(composer))
            call.putValueArgument(1, irString(function.name.asString()))
            call.putValueArgument(2, irString(currentFileName.orEmpty()))
        }
    }

    private fun IrBuilderWithScope.irTrack(
        stateVariable: IrVariable
    ): IrExpression? {
        val composer = function.getComposer() ?: return null

        logger.i("Registered tracker for ${stateVariable.name.asString()} in ${function.name.asString()} (${currentFileName.orEmpty()})")

        return irCall(registerTrackerFunction).also { call ->
            call.putValueArgument(0, irGet(stateVariable))
            call.putValueArgument(1, irGet(composer))
            call.putValueArgument(2, irString(function.name.asString()))
            call.putValueArgument(3, irString(stateVariable.name.asString()))
            call.putValueArgument(4, irString(currentFileName.orEmpty()))
        }
    }

    private fun IrFunction.getComposer(): IrValueParameter? {
        return function.valueParameters
            .firstOrNull { it.name.asString() == "\$composer" }
            ?: run {
                logger.e("Not found composer in variables: ${function.valueParameters.map { it.name.asString() }}")
                return null
            }
    }
}
