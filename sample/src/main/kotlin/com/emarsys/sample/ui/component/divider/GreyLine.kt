package com.emarsys.sample.ui.component.divider

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GreyLine() {
    Divider(
        Modifier
            .padding(top = 10.dp, bottom = 10.dp)
            .fillMaxWidth(.8f),
        color = Color.Gray.copy(0.6f)
    )
}