package com.emarsys.sample.ui.component.screen

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import coil.annotation.ExperimentalCoilApi

abstract class DetailScreen {

    abstract val context: Context

    @ExperimentalCoilApi
    @ExperimentalComposeUiApi
    @Composable
    abstract fun Detail(paddingValues: PaddingValues)
}