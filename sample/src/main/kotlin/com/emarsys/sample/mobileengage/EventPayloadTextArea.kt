package com.emarsys.sample.ui.component.textfield

import androidx.compose.foundation.layout.height
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emarsys.sample.ui.style.rowWithPointEightWidth

@ExperimentalComposeUiApi
@Composable
fun EventPayloadTextArea(
    fieldToEdit: MutableState<String>,
    label: String = ""
) {
    OutlinedTextField(
        modifier = Modifier
            .rowWithPointEightWidth()
            .height(120.dp),
        label = { Text(text = label) },
        value = fieldToEdit.value,
        onValueChange = { fieldToEdit.value = it },
        maxLines = 3
    )
}