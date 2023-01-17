package com.emarsys.sample.inbox

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size

@OptIn(ExperimentalCoilApi::class)
@Composable
fun ShowImage(imageUrl: String) {
    Image(
        painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current).data(imageUrl).apply(block = fun ImageRequest.Builder.() {
                size(Size.ORIGINAL)
            }).build()
        ),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(1f),
        contentScale = ContentScale.FillWidth
    )
}