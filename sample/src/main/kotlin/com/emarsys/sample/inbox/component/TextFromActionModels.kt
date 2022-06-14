package com.emarsys.sample.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.emarsys.mobileengage.api.action.ActionModel
import com.emarsys.sample.ui.style.rowWithMaxWidth

@Composable
fun TextFromActionList(actionModels: List<ActionModel>) {
    Row(
        modifier = Modifier.rowWithMaxWidth().padding(start = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        actionModels.forEach { actionModel ->
            Text(text = "${actionModel.type} ", color = Color.Red)
        }
    }
}
