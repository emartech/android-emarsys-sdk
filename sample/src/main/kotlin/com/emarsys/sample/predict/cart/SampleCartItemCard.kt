package com.emarsys.sample.predict.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.emarsys.sample.ui.style.cardWithPointEightWidth
import com.emarsys.sample.ui.style.rowWithPointEightWidth

@Composable
fun SampleCartItemCard(sampleItem: SampleCartItem) {
    Card(
        modifier = Modifier.cardWithPointEightWidth()
    ) {
        Row(
            modifier = Modifier
                .rowWithPointEightWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = sampleItem.toString(),
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}

