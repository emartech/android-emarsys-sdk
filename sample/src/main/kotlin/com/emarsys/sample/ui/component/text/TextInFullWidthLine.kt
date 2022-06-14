package com.emarsys.sample.ui.component.text

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emarsys.sample.ui.style.rowWithMaxWidth

@Composable
fun TextWithFullWidthLine(text: String) {
    Row(
        modifier = Modifier.rowWithMaxWidth().padding(start = 8.dp)
    ) {
        Text(text = text)
    }
}