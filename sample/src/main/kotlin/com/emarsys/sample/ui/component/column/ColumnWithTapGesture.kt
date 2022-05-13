package com.emarsys.sample.ui.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import com.emarsys.sample.ui.style.columnWithMaxWidth

@Composable
fun ColumnWithTapGesture(paddingValues: MutableState<Dp>, content: @Composable () -> Unit) {
    val focusManager = LocalFocusManager.current
    val scrollState = ScrollState(0)

    Column(
        modifier = Modifier
            .columnWithMaxWidth()
            .padding(bottom = paddingValues.value)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
            .verticalScroll(scrollState, true),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}