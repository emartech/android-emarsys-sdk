package com.emarsys.sample.ui.component.screen

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import coil.annotation.ExperimentalCoilApi

abstract class DetailScreen {

    abstract val context: Context
    abstract val application: Application

    @ExperimentalCoilApi
    @ExperimentalComposeUiApi
    @Composable
    open fun Detail(paddingValues: PaddingValues) {
    }
}