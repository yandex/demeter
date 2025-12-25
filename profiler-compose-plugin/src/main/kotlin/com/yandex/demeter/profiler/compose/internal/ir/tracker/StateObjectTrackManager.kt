package com.yandex.demeter.profiler.compose.internal.ir.tracker

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.Transition
import androidx.compose.runtime.Composer
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.State
import androidx.compose.runtime.cache
import androidx.compose.runtime.snapshots.ObserverHandle
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.StateObject
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.profiler.compose.internal.core.TAG
import com.yandex.demeter.profiler.compose.internal.data.ComposeMetricHolder
import com.yandex.demeter.profiler.compose.internal.ir.tracker.StateObjectTrackManager.trackedStateChanges
import com.yandex.demeter.profiler.compose.internal.ir.tracker.StateObjectTrackManager.trackedStateObjects
import com.yandex.demeter.profiler.compose.internal.ir.tracker.StateObjectTrackManager.trackerNotifier
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

@InternalDemeterApi
data class StateObjectComposition(
    val state: StateObject,
    val composableFunctionName: String,
    val stateName: String,
    val fileNameWithPackage: String,
) {
    override fun toString(): String =
        "$stateName [$fileNameWithPackage#$composableFunctionName]#${state.javaClass.simpleName}"
}

@InternalDemeterApi
data class StateObjectChange(
    val prevValue: Any?,
    val newValue: Any?,
) {
    override fun toString(): String {
        return buildString {
            append("[")
            append(
                if (prevValue != null) {
                    "'$prevValue' -> "
                } else {
                    ""
                }
            )
            append("'$newValue']")
        }
    }
}

@InternalDemeterApi
interface StateObjectChangeNotifier {
    fun changed(composition: StateObjectComposition, change: StateObjectChange)

    fun forgotten(composition: StateObjectComposition)

    fun remembered(composition: StateObjectComposition, change: StateObjectChange)
}

@InternalDemeterApi
object StateObjectTrackManager {
    internal val trackerNotifier = ComposeMetricHolder.stateObjectNotifier

    private val started = AtomicBoolean(false)
    private var observerHandler: ObserverHandle? = null

    internal val trackedStateObjects = mutableMapOf<Int, StateObjectComposition>()
    internal val trackedStateChanges = mutableMapOf<Int, StateObjectChange>()

    internal fun ensureStarted() {
        if (started.compareAndSet(false, true)) {
            observerHandler = Snapshot.registerApplyObserver { stateObjects, _ ->
                stateObjects.forEach loop@{ stateObject ->
                    if (stateObject !is StateObject) return@loop

                    val state = trackedStateObjects[stateObject.hashCode()] ?: return@loop
                    val oldChange = trackedStateChanges[stateObject.hashCode()] ?: return@loop

                    val newChange = oldChange.copy(
                        prevValue = oldChange.newValue,
                        newValue = (stateObject as? State<*>)?.value
                    )

                    trackedStateChanges[stateObject.hashCode()] = newChange

                    if (newChange.prevValue == null) {
                        trackerNotifier.remembered(state, newChange)
                    } else {
                        trackerNotifier.changed(state, newChange)
                    }
                }
            }
        }
    }
}

@InternalDemeterApi
fun <S : Any> registerTracking(
    state: S,
    composer: Composer,
    composableFunctionName: String,
    stateName: String,
    fileNameWithPackage: String,
): S = state.also {
    val hash = state.asStateObject()?.hashCode() ?: state.hashCode()

    val register by lazy {
        object : RememberObserver {
            override fun onRemembered() {
                try {
                    val stateObject = state.asStateObject() ?: run {
                        Log.i(TAG, "Error to remember $this")
                        return
                    }

                    val savedState = trackedStateObjects.getOrPut(hash) {
                        StateObjectComposition(
                            stateObject,
                            composableFunctionName,
                            stateName,
                            fileNameWithPackage,
                        )
                    }

                    val savedChange = trackedStateChanges.compute(hash) { k, v ->
                        val rememberedValue = Snapshot.withoutReadObservation {
                            (stateObject as? State<*>)?.value
                        }

                        if (v == null || v.newValue != rememberedValue) {
                            StateObjectChange(
                                prevValue = v?.newValue,
                                newValue = rememberedValue
                            )
                        } else {
                            v
                        }
                    }

                    trackerNotifier.remembered(savedState, savedChange!!)
                } catch (unexpectedException: Exception) {
                    Log.e(TAG, "State value tracking registration failed", unexpectedException)
                }
            }

            override fun onForgotten() {
                trackedStateObjects[hash]?.let {
                    trackerNotifier.forgotten(it)
                }
                trackedStateObjects.remove(hash)
            }

            override fun onAbandoned() {
                // no-op
            }
        }
    }

    StateObjectTrackManager.ensureStarted()

    Log.d(TAG, "Saved hash: $hash, $stateName, $composableFunctionName: $composableFunctionName, fileName: $fileNameWithPackage")
    composer.startReplaceableGroup(hash)
    composer.cache(false) { register }
    composer.endReplaceableGroup()
}

private fun Any.asStateObject(): StateObject? {
    return when (this) {
        is StateObject -> this
        is Animatable<*, *> -> {
            val internalStateField = this::class.java.declaredFields.firstOrNull { field ->
                field.type == AnimationState::class.java
            }?.apply {
                isAccessible = true
            }
            val animationState = internalStateField?.get(this) as? AnimationState<*, *>
            animationState?.let { animationState::value.obtainStateObjectOrNull() }
        }

        is AnimationState<*, *> -> this::value.obtainStateObjectOrNull()
        is Transition<*>.TransitionAnimationState<*, *> -> this::value.obtainStateObjectOrNull()
        is InfiniteTransition.TransitionAnimationState<*, *> -> this::value.obtainStateObjectOrNull()
        else -> return null
    }
}

private fun KProperty0<*>.obtainStateObjectOrNull() = runCatching {
    val stateValue = apply { isAccessible = true }.getDelegate()
    stateValue as? StateObject
}.getOrNull()
