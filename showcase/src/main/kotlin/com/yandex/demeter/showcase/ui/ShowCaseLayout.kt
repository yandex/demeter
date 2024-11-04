package com.yandex.demeter.showcase.ui

import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.random.Random

@Suppress("MagicNumber")
@Composable
fun ShowCaseLayout(showCaseViewFactory: ShowCaseView.Factory) {
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