package com.yandex.demeter.showcase.ui

import androidx.compose.animation.Animatable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun TestLayout(input: Int) {
    val testLayoutInput: MutableState<String> = remember(input) { mutableStateOf("Value: $input") }

    Column {
        Column {
            Column(Modifier.clickable { testLayoutInput.value += "!" }) {
                Text("TestLayout: ${testLayoutInput.value}")
            }
        }
    }
}

@Suppress("MagicNumber")
@Composable
fun SimpleLayout() {
    val test = remember { mutableIntStateOf(1) }
    val counter = remember { mutableIntStateOf(2) }

    val color = remember { Animatable(Color.Gray) }
    LaunchedEffect(counter.intValue) {
        if (counter.intValue % 2f == 0f) {
            color.animateTo(Color.Red)
        } else {
            color.animateTo(Color.Yellow)
        }
    }

    Column {
        Button(onClick = { test.intValue++ }) {
            Column {
                Text("Test: ${test.intValue}", color = color.value)
            }
        }

        if (test.intValue > 3) {
            TestLayout(test.intValue)
        }

        Button(onClick = { counter.intValue++ }) {
            Column {
                Text("Count: ${counter.intValue}")
            }
        }
    }
}