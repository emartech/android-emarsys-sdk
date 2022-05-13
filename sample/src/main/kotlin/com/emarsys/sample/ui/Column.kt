package com.emarsys.sample.ui.style

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

fun Modifier.columnWithMaxWidth(): Modifier {
    return this
        .fillMaxWidth(1f)
        .padding(2.dp)
}

fun Modifier.columnWithPointEightWidth(): Modifier {
    return this
        .fillMaxWidth(.8f)
        .padding(2.dp)
}