package com.emarsys.sample.predict

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emarsys.predict.api.model.Product
import com.emarsys.sample.R
import com.emarsys.sample.ui.style.cardWithPointEightWidth
import com.emarsys.sample.ui.style.columnWithPointEightWidth
import com.emarsys.sample.ui.style.rowWithPointEightWidth


@Composable
fun RecommendedProductsCard(products: List<Product>) {
    products.forEach { product ->
        Card(modifier = Modifier.cardWithPointEightWidth(), elevation = 5.dp) {
            Column(modifier = Modifier.columnWithPointEightWidth()) {
                Row(modifier = Modifier.rowWithPointEightWidth()) {
                    Text(text = "${stringResource(id = R.string.product)}: ${product.productId}, ${stringResource(id = R.string.title)}: ${product.title}")
                }
            }
        }
    }
}