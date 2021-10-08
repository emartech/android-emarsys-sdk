package com.emarsys.sample.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.emarsys.Emarsys
import com.emarsys.predict.api.model.CartItem
import com.emarsys.predict.api.model.RecommendationLogic
import com.emarsys.sample.SampleCartItem
import com.emarsys.sample.adapters.ProductsAdapter
import com.emarsys.sample.databinding.FragmentPredictBinding
import com.emarsys.sample.extensions.showSnackBar
import kotlin.random.Random

class PredictFragment : Fragment() {

    private var cartContent = mutableListOf<SampleCartItem>()
    private var _binding : FragmentPredictBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentPredictBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.productsRecycleView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.productsRecycleView.adapter = ProductsAdapter()

        binding.buttonTrackItemView.setOnClickListener {
            val itemView = binding.itemId.text.toString()
            if (itemView.isNotEmpty()) {
                Emarsys.predict.trackItemView(itemView)
                view.showSnackBar("Track of:$itemView = OK")
            }
        }

        binding.buttonTrackCategoryView.setOnClickListener {
            val categoryView = binding.categoryView.text.toString()
            if (categoryView.isNotEmpty()) {
                Emarsys.predict.trackCategoryView(categoryView)
                view.showSnackBar("Track of:$categoryView = OK")
            }
        }

        binding.buttonSearchTerm.setOnClickListener {
            val searchTerm = binding.searchTerm.text.toString()
            if (searchTerm.isNotEmpty()) {
                Emarsys.predict.trackSearchTerm(searchTerm)
                view.showSnackBar("Track of:$searchTerm = OK")
            }
        }

        binding.buttonAddToCart.setOnClickListener {
            val newCartItem = generateCartItem()
            binding.cartItems.text?.append("$newCartItem,")
            cartContent.add(newCartItem)
        }

        binding.buttonTrackCartItems.setOnClickListener {
            if (cartContent.isNotEmpty()) {
                Emarsys.predict.trackCart(cartContent as List<CartItem>)
                view.showSnackBar("Tracking cart: OK")
            }
        }

        binding.buttonTrackPurchase.setOnClickListener {
            val item = binding.orderId.text.toString()
            if (item.isNotEmpty()) {
                Emarsys.predict.trackPurchase(item, cartContent as List<CartItem>)
                view.showSnackBar("Track purchase of $item: OK")
            }
        }

        binding.buttonRecommend.setOnClickListener {
            val searchTerm = binding.searchTermForRecommend.text.toString()
            if (searchTerm.isNotEmpty()) {
                Emarsys.predict.recommendProducts(RecommendationLogic.search(searchTerm)) {
                    if (it.result != null) {
                        val products = it.result ?: listOf()
                        (binding.productsRecycleView.adapter as ProductsAdapter).addItems(products)
                    }
                }
            }
        }
    }

    private fun generateCartItem(): SampleCartItem {
        return SampleCartItem(getRandomItemId(), Random.nextInt(99).toDouble(), (Random.nextInt(99)).toDouble())
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
