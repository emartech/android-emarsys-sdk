package com.emarsys.sample.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emarsys.sample.R
import com.emarsys.sample.ui.style.rowWithMaxWidth

@Composable
fun TextFromTags(tags: List<String>) {
    Row(
        modifier = Modifier
            .rowWithMaxWidth()
            .padding(start = 8.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(text = "${stringResource(id = R.string.tags)}: ")
        tags.forEach { tag ->
            Text(text = "$tag ")
        }
    }
}