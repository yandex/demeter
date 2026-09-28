package sample.demeter

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.profiler.inject.InjectDemeterPlugin
import com.yandex.demeter.profiler.inject.internal.asm.InjectAsm
import com.yandex.demeter.profiler.inject.internal.data.InjectMetricsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

@OptIn(InternalDemeterApi::class)
class MainActivity : Activity() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = TextView(this)
        setContentView(view)
        val reports = CopyOnWriteArrayList<Map<String, Any>>()
        InjectDemeterPlugin { reports += it }.init(scope)
        repeat(2) { PreferencesProvider(Leaf(), it) }
        Child()
        InjectAsm.log(System.nanoTime(), "missing.Legacy", Any::class.java)
        scope.launch {
            repeat(100) {
                if (reports.size < 7) delay(50)
            }
            check(reports.size == 7) { "Expected 7 metrics, got $reports" }
            check(reports.count { it["className"] == "sample.demeter.PreferencesProvider" } == 2)
            check(reports.all { it["ms"] as Long >= 0 })
            val metrics = InjectMetricsRepository.initializedMetrics.values
            check(metrics.map { it.simpleName }.containsAll(listOf("PreferencesProvider", "PreferencesProvider №1", "Base", "Child", "Legacy")))
            check(metrics.first { it.simpleName == "PreferencesProvider" }.args.single().simpleName == "Leaf")
            check(metrics.first { it.simpleName == "PreferencesProvider №1" }.args.single().simpleName == "Leaf №1")
            val result = "PASS: 7 inject events after R8; original names, dependencies, inheritance and legacy ABI"
            view.text = result
            Log.i("DemeterInjectValidation", result)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}

class Leaf @Inject constructor()
class PreferencesProvider @Inject constructor(val leaf: Leaf, val number: Int)
open class Base @Inject constructor()
class Child @Inject constructor() : Base()