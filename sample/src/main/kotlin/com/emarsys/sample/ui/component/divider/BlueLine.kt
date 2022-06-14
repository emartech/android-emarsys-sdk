package com.emarsys.sample.ui.component.divider

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emarsys.sample.ui.theme.Blue100

@Composable
fun DividerWithBackgroundColor() {
    Divider(
        Modifier
            .padding(4.dp)
            .fillMaxWidth(1f),
        color = Blue100
    )
}