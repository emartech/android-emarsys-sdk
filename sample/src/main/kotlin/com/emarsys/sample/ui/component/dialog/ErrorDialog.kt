package com.emarsys.sample.ui.component.toast

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@Composable
fun ErrorDialog(message: String, isVisible: MutableState<Boolean>) {
    AlertDialog(
        onDismissRequest = { isVisible.value = false },
        title = { Text("ERROR") },
        text = { Text(text = message) },
        confirmButton = {
            Button(onClick = { isVisible.value = false }) {
                Text(text = "OK")
            }
        }
    )
}