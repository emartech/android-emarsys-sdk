package com.emarsys.sample.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.emarsys.Emarsys
import com.emarsys.predict.api.model.Product
import com.emarsys.sample.R
import com.emarsys.sample.adapters.ProductsAdapter.ViewHolder

class ProductsAdapter : RecyclerView.Adapter<ViewHolder>() {
    private var products = mutableListOf<Product>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.product_view, parent, false))
    }

    override fun getItemCount(): Int {
        return products.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.productId.text = products[position].productId
        holder.productTitle.text = products[position].title

        holder.productImage.load(products[position].imageUrl.toString()) {
            placeholder(R.drawable.placeholder)
            crossfade(true)
            error(R.drawable.placeholder)
        }

        holder.cardView.setOnClickListener {
            Emarsys.predict.trackRecommendationClick(products[position])
        }
    }

    fun addItems(products: List<Product>) {
        this.products = products as MutableList<Product>
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.card)
        val productId: TextView = view.findViewById(R.id.product_id)
        val productTitle: TextView = view.findViewById(R.id.product_title)
        val productImage: ImageView = view.findViewById(R.id.product_image)
    }
}