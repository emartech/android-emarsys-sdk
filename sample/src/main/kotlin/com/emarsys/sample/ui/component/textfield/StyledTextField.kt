package com.emarsys.sample.ui.component.textfield

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.emarsys.sample.ui.style.rowWithPointEightWidth

@ExperimentalComposeUiApi
@Composable
fun StyledTextField(
    fieldToEdit: MutableState<String>,
    label: String = ""
) {
    val keyBoardOptions: KeyboardOptions = if (label == "ContactFieldId") {
        KeyboardOptions(keyboardType = KeyboardType.Number)
    } else {
        KeyboardOptions.Default
    }

    OutlinedTextField(
        modifier = Modifier
            .rowWithPointEightWidth(),
        label = { Text(text = label) },
        value = fieldToEdit.value,
        onValueChange = {
            if (!it.contains("\n")) {
                fieldToEdit.value = it
            } else {
                fieldToEdit.value = it.trimIndent()
            }
        },
        keyboardOptions = keyBoardOptions,
        maxLines = 1,
        singleLine = true
    )
}