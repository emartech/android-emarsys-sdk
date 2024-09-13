package com.emarsys.sample.predict

import android.content.Context
import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import coil.annotation.ExperimentalCoilApi
import com.emarsys.Emarsys
import com.emarsys.sample.R
import com.emarsys.sample.predict.cart.SampleCartItemCard
import com.emarsys.sample.ui.component.button.StyledTextButton
import com.emarsys.sample.ui.component.divider.GreyLine
import com.emarsys.sample.ui.component.screen.DetailScreen
import com.emarsys.sample.ui.component.text.TitleText
import com.emarsys.sample.ui.component.toast.ErrorDialog
import com.emarsys.sample.ui.component.toast.customTextToast
import com.emarsys.sample.ui.style.columnWithMaxWidth
import com.emarsys.sample.ui.style.rowWithMaxWidth
import com.emarsys.sample.ui.style.rowWithPointEightWidth

class PredictScreen(
    override val context: Context
) : DetailScreen() {
    private val viewModel = PredictViewModel()

    @OptIn(ExperimentalCoilApi::class)
    @ExperimentalComposeUiApi
    @Composable
    override fun Detail(paddingValues: PaddingValues) {
        val bottomPadding = remember { mutableStateOf(paddingValues.calculateBottomPadding()) }
        val focusManager = LocalFocusManager.current
        val successToast = {
            customTextToast(context, context.getString(R.string.track_success))
        }

        if (viewModel.isErrorVisible()) {
            ErrorDialog(
                message = viewModel.getErrorMessage(),
                isVisible = viewModel.getErrorVisibleField()
            )
        }
        LazyColumn(
            modifier = Modifier
                .columnWithMaxWidth()
                .padding(bottom = bottomPadding.value)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            item {
                TrackableTerm(
                    title = stringResource(id = R.string.track_item_title),
                    placeHolder = stringResource(id = R.string.track_item_label),
                    fieldToEdit = viewModel.itemView
                ) {
                    viewModel.trackItem(context) {
                        successToast()
                    }
                }
            }
            item { GreyLine() }
            item {
                TrackableTerm(
                    title = stringResource(id = R.string.track_category_title),
                    placeHolder = stringResource(id = R.string.track_category_label),
                    fieldToEdit = viewModel.categoryView
                ) {
                    viewModel.trackCategory(context) {
                        successToast()
                    }
                }
            }
            item { GreyLine() }
            item {
                TrackableTerm(
                    title = stringResource(id = R.string.track_search_title),
                    placeHolder = stringResource(id = R.string.track_search_label),
                    fieldToEdit = viewModel.searchTerm
                ) {
                    viewModel.trackSearch(context) {
                        successToast()
                    }
                }
            }
            item { GreyLine() }
            item {
                TitleText(titleText = "Cart")
            }
            item {
                Row(
                    modifier = Modifier.rowWithMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StyledTextButton(buttonText = stringResource(id = R.string.add_item_to_cart)) {
                        viewModel.addToCart()
                    }
                    StyledTextButton(buttonText = stringResource(id = R.string.track_cart)) {
                        viewModel.trackCart(context) {
                            successToast()
                        }
                    }
                }
            }
            if (viewModel.sampleCart.isNotEmpty()) {
                item { TitleText(stringResource(id = R.string.items_in_cart)) }
                item {
                    viewModel.sampleCart.forEach { sampleItem ->
                        SampleCartItemCard(sampleItem = sampleItem)
                    }
                }
            }
            item {
                TrackableTerm(
                    title = stringResource(id = R.string.track_order_title),
                    placeHolder = stringResource(id = R.string.track_order_label),
                    fieldToEdit = viewModel.orderId
                ) {
                    viewModel.trackPurchase(context) {
                        successToast()
                    }
                }
            }
            item { GreyLine() }
            item {
                TextButton(onClick = {
                    Emarsys.predict.trackRecommendationClick(viewModel.product.value)
                }) {
                    Text(stringResource(id = R.string.track_recommendation_click))
                }
            }
            item { GreyLine() }
            item {
                TitleText(titleText = stringResource(id = R.string.recommend_text))
            }
            item {
                Row(
                    modifier = Modifier.rowWithPointEightWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    StyledTextButton(buttonText = stringResource(id = R.string.recommend_button_label)) {
                        onRecommendationClicked()
                    }
                }
            }
            if (viewModel.recommendedProducts.isNotEmpty()) {
                item { TitleText(titleText = stringResource(id = R.string.recommendation_title)) }
                item { RecommendedProductsCard(products = viewModel.recommendedProducts) }
            }
        }
    }

    private fun onRecommendationClicked() {
        Emarsys.predict.recommendProducts(
            logic = viewModel.recommendationLogic.value
        ) { tryResult ->
            try {
                tryResult.result?.let { viewModel.recommendedProducts.addAll(it) }
            } catch (e: Exception) {
                Log.e("ERROR", "Recommendation error.")
            }
        }
    }
}
