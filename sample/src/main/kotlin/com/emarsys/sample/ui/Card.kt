package com.emarsys.sample.ui.style

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

fun Modifier.cardWithFullWidth(): Modifier {
    return this
        .fillMaxWidth(1f)
        .padding(4.dp)
}

fun Modifier.cardWithPointEightWidth(): Modifier {
    return this
        .fillMaxWidth(.8f)
        .padding(4.dp)
}