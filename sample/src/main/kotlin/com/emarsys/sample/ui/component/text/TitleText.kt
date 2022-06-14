package com.emarsys.sample.ui.component.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TitleText(titleText : String) {
    Row(
        modifier = Modifier.fillMaxWidth(1f),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier
                .padding(top = 6.dp, bottom = 10.dp),
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            text = titleText,
        )
    }
}