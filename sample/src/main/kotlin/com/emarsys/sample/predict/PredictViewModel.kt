package com.emarsys.sample.predict

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.emarsys.Emarsys
import com.emarsys.predict.api.model.Product
import com.emarsys.predict.api.model.RecommendationLogic
import com.emarsys.sample.R
import com.emarsys.sample.predict.cart.SampleCartItem
import java.util.*

class PredictViewModel : ViewModel() {
    val itemView = mutableStateOf("")
    val categoryView = mutableStateOf("")
    val searchTerm = mutableStateOf("")
    val orderId = mutableStateOf("")
    val product = mutableStateOf(Product("test+ID1", "testTitle", "https://emarsys.com", "RELATED", "AAAA"))
    val recommendedProducts = mutableStateListOf<Product>()
    val sampleCart = mutableStateListOf<SampleCartItem>()
    val recommendationLogic = mutableStateOf(RecommendationLogic.search(searchTerm.value))
    private val errorVisible = mutableStateOf(false)
    private val errorMessage = mutableStateOf("")

    fun trackItem(context: Context, confirmSuccess: () -> Unit) {
        if (isTrackingInfoPresent(this.itemView, context)) {
            Emarsys.predict.trackItemView(itemId = this.itemView.value)
            recommendationLogic.value = RecommendationLogic.related(this.itemView.value)
            confirmSuccess()
        }
    }

    fun trackCategory(context: Context, confirmSuccess: () -> Unit) {
        if (isTrackingInfoPresent(this.categoryView, context)) {
            Emarsys.predict.trackCategoryView(categoryPath = this.categoryView.value)
            recommendationLogic.value = RecommendationLogic.category(this.categoryView.value)
            confirmSuccess()
        }
    }

    fun trackSearch(context: Context, confirmSuccess: () -> Unit) {
        if (isTrackingInfoPresent(this.searchTerm, context)) {
            Emarsys.predict.trackSearchTerm(searchTerm = this.searchTerm.value)
            recommendationLogic.value = RecommendationLogic.search(this.searchTerm.value)
            confirmSuccess()
        }
    }

    fun trackCart(context: Context, confirmSuccess: () -> Unit) {
        if (this.sampleCart.isNotEmpty()) {
            Emarsys.predict.trackCart(this.sampleCart)
            recommendationLogic.value = RecommendationLogic.cart(this.sampleCart)
            confirmSuccess()
        } else {
            this.errorMessage.value = context.getString(R.string.track_information_needed)
            this.errorVisible.value = true
        }
    }

    fun trackPurchase(context: Context, confirmSuccess: () -> Unit) {
        if (isTrackingInfoPresent(this.orderId, context)) {
            Emarsys.predict.trackPurchase(this.orderId.value, this.sampleCart)
            recommendationLogic.value = RecommendationLogic.alsoBought()
            confirmSuccess()
        }
    }

    fun addToCart() {
        sampleCart.add(generateCartItem())
    }

    private fun isTrackingInfoPresent(trackInfo: MutableState<String>, context: Context): Boolean {
        return if (trackInfo.value.isNotEmpty()) {
            true
        } else {
            this.errorMessage.value = context.getString(R.string.track_information_needed)
            this.errorVisible.value = true
            false
        }
    }

    private fun generateCartItem(): SampleCartItem {
        return SampleCartItem(
            itemId = Random().nextInt().toString()+"+testId",
            price = Random().nextDouble(),
            quantity = Random().nextDouble()
        )
    }

    fun getErrorMessage(): String {
        return this.errorMessage.value
    }

    fun getErrorVisibleField(): MutableState<Boolean> {
        return this.errorVisible
    }

    fun isErrorVisible(): Boolean {
        return this.errorVisible.value
    }
}