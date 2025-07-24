package com.yandex.demeter.plugin

import com.android.build.api.instrumentation.FramesComputationMode
import org.objectweb.asm.Opcodes

const val ASM_API_VERSION: Int =
    Opcodes.ASM9

val DEMETER_FRAMES_COMPUTATION_MODE: FramesComputationMode =
    FramesComputationMode.COMPUTE_FRAMES_FOR_INSTRUMENTED_METHODS
