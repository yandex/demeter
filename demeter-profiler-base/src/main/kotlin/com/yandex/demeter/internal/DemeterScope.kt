package com.yandex.demeter.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.newSingleThreadContext

@OptIn(DelicateCoroutinesApi::class)
internal val demeterGlobalScope: CoroutineScope = CoroutineScope(
    SupervisorJob() + newSingleThreadContext("DemeterScope")
)
