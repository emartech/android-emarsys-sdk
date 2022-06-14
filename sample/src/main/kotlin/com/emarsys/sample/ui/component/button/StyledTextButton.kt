package com.emarsys.sample.ui.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp

@Composable
fun StyledTextButton(buttonText: String, onClick: () -> Unit) {
    TextButton(
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
        shape = RoundedCornerShape(30),
        onClick = onClick
    ) {
        Text(text = buttonText.toUpperCase(Locale.current))
    }
}