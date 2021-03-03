package dev.scavazzini.clevent.ui.receipt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.scavazzini.clevent.data.models.Product
import dev.scavazzini.clevent.databinding.ProductItemReceiptBinding

class ProductsReceiptAdapter :
        RecyclerView.Adapter<ProductsReceiptAdapter.ProductViewHolder>() {

    private val products: MutableList<Map.Entry<Product, Int>> = arrayListOf()

    fun updateProducts(products: Map<Product, Int>) {
        this.products.addAll(products.entries)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(ProductItemReceiptBinding.inflate(LayoutInflater
                .from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position].key
        val quantity = products[position].value
        holder.bind(product, quantity)
    }

    override fun getItemCount(): Int {
        return products.size
    }

    class ProductViewHolder(private val binding: ProductItemReceiptBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product, quantity: Int) {
            binding.product = product
            binding.quantity = quantity
        }
    }
}
