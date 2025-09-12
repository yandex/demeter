@file:OptIn(ExperimentalCompilerApi::class)

package com.yandex.demeter

import com.yandex.demeter.DemeterComposeCommandLineProcessor.Companion.ENABLED
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration

class DemeterComposeCompilerRegistrar : CompilerPluginRegistrar() {

    override val supportsK2 = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val messageCollector = configuration.get(
            CommonConfigurationKeys.MESSAGE_COLLECTOR_KEY,
            MessageCollector.NONE
        )
        messageCollector.report(
            CompilerMessageSeverity.WARNING,
            "Demeter Compose Compiler Plugin applied: ${configuration[ENABLED]}"
        )

        if (configuration.get(ENABLED, false)) {
            IrGenerationExtension.registerExtension(
                DemeterComposeIrGenerationExtension(messageCollector)
            )
        }
    }
}
