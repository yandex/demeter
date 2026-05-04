package com.yandex.demeter.showcase.ui

import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.yandex.demeter.showcase.coroutine.CoroutineShowCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

@Suppress("MagicNumber")
@Composable
fun ShowCaseLayout(showCaseViewFactory: ShowCaseView.Factory) {
    val coroutineShowCase = remember { CoroutineShowCase() }
    var syncViewCounter by remember { mutableIntStateOf(0) }
    var asyncViewCounter by remember { mutableIntStateOf(0) }
    var simpleCounter by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        SimpleLayout()

        Button(modifier = Modifier.fillMaxWidth(), onClick = { syncViewCounter++ }) {
            Text(text = "Click to create View with random delay")
        }

        Button(modifier = Modifier.fillMaxWidth(), onClick = { asyncViewCounter++ }) {
            Text(text = "Click to create View with random delay asynchronously")
        }

        Button(modifier = Modifier.fillMaxWidth(), onClick = { simpleCounter++ }) {
            Text(text = "Click to increment: $simpleCounter")
        }

        CoroutineShowCaseGroup(coroutineShowCase)

        for (i in 0..<syncViewCounter) {
            AndroidView(factory = {
                showCaseViewFactory.create(Random.nextLong(1000))
            })
        }

        for (i in 0..<asyncViewCounter) {
            AndroidView(factory = {
                lateinit var view: View
                runBlocking(Dispatchers.IO) {
                    view = showCaseViewFactory.create(Random.nextLong(1000))
                }
                view
            })
        }
    }
}

@Composable
private fun CoroutineShowCaseGroup(coroutineShowCase: CoroutineShowCase) {
    var expanded by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (expanded) "▼" else "▶",
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Coroutine Demos",
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CoroutineButton("Sequential launches") { coroutineShowCase.sequentialLaunches() }
                CoroutineButton("Nested launches") { coroutineShowCase.nestedLaunches() }
                CoroutineButton("Async/Await pattern") { coroutineShowCase.asyncAwaitPattern() }
                CoroutineButton("Dispatcher switching") { coroutineShowCase.dispatcherSwitching() }
                CoroutineButton("Cancellation demo") { coroutineShowCase.cancellationDemo() }
                CoroutineButton("SupervisorScope demo") { coroutineShowCase.supervisorScopeDemo() }
                CoroutineButton("Flow collection") { coroutineShowCase.flowCollection() }
                CoroutineButton("Multiple flow subscribers") { coroutineShowCase.multipleFlowSubscribers() }
                CoroutineButton("StateFlow transform") { coroutineShowCase.stateFlowTransform() }
                CoroutineButton("Timeout demo") { coroutineShowCase.timeoutDemo() }
                CoroutineButton("Retry pattern") { coroutineShowCase.retryPattern() }
            }
        }
    }
}

@Composable
private fun CoroutineButton(
    label: String,
    onClick: () -> Job,
) {
    var activeJobs by remember { mutableIntStateOf(0) }

    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            val job = onClick()
            activeJobs++
            job.invokeOnCompletion { activeJobs-- }
        },
    ) {
        if (activeJobs > 0) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = MaterialTheme.colors.onPrimary,
                strokeWidth = 2.dp,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = label)
    }
}
