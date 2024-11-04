@file:OptIn(ExperimentalCompilerApi::class)

package com.yandex.demeter

import com.yandex.demeter.DemeterComposeCommandLineProcessor.Companion.ENABLED
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.codegen.extensions.ExpressionCodegenExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration

class DemeterComposeCompilerRegistrar : CompilerPluginRegistrar() {

    override val supportsK2 = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val messageCollector = configuration.get(
            CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
            MessageCollector.NONE
        )
        messageCollector.report(
            CompilerMessageSeverity.WARNING,
            "Demeter Compose Compiler Plugin applied: ${configuration.get(ENABLED, true)}"
        )

        if (configuration.get(ENABLED, true)) {
            IrGenerationExtension.registerExtension(
                DemeterComposeIrGenerationExtension(messageCollector)
            )
        }
    }
}
