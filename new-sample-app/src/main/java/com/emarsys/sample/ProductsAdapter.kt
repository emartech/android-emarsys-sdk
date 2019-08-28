package com.emarsys.sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.emarsys.predict.api.model.Product
import com.emarsys.sample.ProductsAdapter.ViewHolder
import kotlinx.android.synthetic.main.product_view.view.*

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
        holder.productImage.text = products[position].imageUrl.toString()
    }

    fun addItems(products: List<Product>) {
        this.products = products as MutableList<Product>
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productId: TextView = view.product_id
        val productTitle: TextView = view.product_title
        val productImage: TextView = view.product_image
    }
}