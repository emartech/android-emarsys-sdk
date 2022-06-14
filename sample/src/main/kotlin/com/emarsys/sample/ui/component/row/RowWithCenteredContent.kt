package com.emarsys.sample.ui.component.row

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.emarsys.sample.ui.style.rowWithMaxWidth

@Composable
fun RowWithCenteredContent(content: @Composable () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.rowWithMaxWidth()
    ) {
        content()
    }
}