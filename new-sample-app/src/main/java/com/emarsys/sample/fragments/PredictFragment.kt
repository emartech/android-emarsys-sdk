package com.emarsys.sample.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.emarsys.Emarsys
import com.emarsys.predict.api.model.CartItem
import com.emarsys.sample.R
import com.emarsys.sample.SampleCartItem
import com.emarsys.sample.extensions.showSnackBar
import kotlinx.android.synthetic.main.fragment_predict.*
import kotlin.random.Random

class PredictFragment : Fragment() {

    private var cartContent = mutableListOf<SampleCartItem>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_predict, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonTrackItemView.setOnClickListener {
            val itemView = itemId.text.toString()
            if (itemView.isNotEmpty()) {
                Emarsys.Predict.trackItemView(itemView)
                view.showSnackBar("Track of:$itemView = OK")
            }
        }

        buttonTrackCategoryView.setOnClickListener {
            val categoryView = categoryView.text.toString()
            if (categoryView.isNotEmpty()) {
                Emarsys.Predict.trackCategoryView(categoryView)
                view.showSnackBar("Track of:$categoryView = OK")
            }
        }

        buttonSearchTerm.setOnClickListener {
            val searchTerm = searchTerm.text.toString()
            if (searchTerm.isNotEmpty()) {
                Emarsys.Predict.trackSearchTerm(searchTerm)
                view.showSnackBar("Track of:$searchTerm = OK")
            }
        }

        buttonAddToCart.setOnClickListener {
            val newCartItem = generateCartItem()
            cartItems.text?.append("$newCartItem,")
            cartContent.add(newCartItem)
        }

        buttonTrackCartItems.setOnClickListener {
            if (cartContent.isNotEmpty()) {
                Emarsys.Predict.trackCart(cartContent as List<CartItem>)
                view.showSnackBar("Tracking cart: OK")
            }
        }

        buttonTrackPurchase.setOnClickListener {
            val item = orderId.text.toString()
            if (item.isNotEmpty()) {
                Emarsys.Predict.trackPurchase(item, cartContent as List<CartItem>)
                view.showSnackBar("Track purchase of $item: OK")
            }
        }
    }

    private fun generateCartItem(): SampleCartItem {
        return SampleCartItem(Random.nextInt(99).toDouble(), (Random.nextInt(99)).toDouble(), getRandomItemId())
    }

    private fun getRandomItemId(): String {
        val identifiers = listOf(
                "2156",
                "2157",
                "2158",
                "2159",
                "2160",
                "2161",
                "2163",
                "2164",
                "2165",
                "2166",
                "2167",
                "2182",
                "2169",
                "2170",
                "2171",
                "2172",
                "2173",
                "2174",
                "2175",
                "2176",
                "2177",
                "2178",
                "2179",
                "2180",
                "2181",
                "2168",
                "2183",
                "2162",
                "2184"
        )
        val randomIndex = Random.nextInt(identifiers.size)
        return identifiers[randomIndex]
    }
}
