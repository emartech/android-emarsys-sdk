package com.emarsys.sample.inbox

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.size.OriginalSize

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ShowImage(imageUrl: String) {
    Image(
        painter = rememberImagePainter(imageUrl,
            builder = {
                size(OriginalSize)
            }
        ),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(1f),
        contentScale = ContentScale.FillWidth
    )
}