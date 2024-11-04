@file:OptIn(ExperimentalCompilerApi::class)

package com.yandex.demeter

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

class DemeterComposeCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = "demeter-compose-compiler-plugin"

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption(
            optionName = "enabled",
            valueDescription = "<true|false>",
            description = "apply Composables if it is not specified",
            required = false
        )
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        when (option.optionName) {
            "enabled" -> configuration.put(ENABLED, value.toBoolean())
        }
    }

    companion object {
        val ENABLED = CompilerConfigurationKey<Boolean>("Enable Composable Applier")
    }
}
