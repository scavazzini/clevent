package dev.scavazzini.clevent.ui.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import dev.scavazzini.clevent.data.models.Product
import dev.scavazzini.clevent.databinding.ProductItemOrderBinding

class ProductsOrderAdapter(private val selectedProducts: MutableMap<Product, Int>,
                           var quantityChangeListener: () -> Unit = {}) :
        RecyclerView.Adapter<ProductsOrderAdapter.ProductViewHolder>() {

    private val products: MutableList<Product> = arrayListOf()
    private val filteredProducts: MutableList<Product> = arrayListOf()

    fun setProducts(products: List<Product>) {
        this.products.clear()
        this.products.addAll(products)

        filteredProducts.clear()
        filteredProducts.addAll(products)

        selectedProducts.entries.removeAll {
            it.key !in products
        }

        quantityChangeListener()
        notifyDataSetChanged()
    }

    fun clearSelected() {
        filteredProducts.clear()
        filteredProducts.addAll(products)
        selectedProducts.clear()
        notifyDataSetChanged()
    }

    fun filterList(term: String) {
        filteredProducts.clear()
        filteredProducts.addAll(products.filter {
            it.name.trim().contains(term.trim(), ignoreCase = true)
        })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        return ProductViewHolder(ProductItemOrderBinding
                .inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val productToBind = filteredProducts[position]
        holder.bind(productToBind, selectedProducts[productToBind] ?: 0)
    }

    override fun getItemCount(): Int {
        return filteredProducts.size
    }

    fun getSelectedPrice(): Int {
        return selectedProducts.map { it.key.price * it.value }.sum()
    }

    fun getSelectedAmount(): Int {
        var units = 0
        for (quantity in selectedProducts.values) {
            units += quantity
        }
        return units
    }

    inner class ProductViewHolder(private val binding: ProductItemOrderBinding)
        : RecyclerView.ViewHolder(binding.root) {

        private var quantity
            get() = binding.quantity
            set(value) {
                binding.quantity = value
                if (value <= 0) {
                    binding.quantity = 0
                    selectedProducts.remove(binding.product)
                } else {
                    binding.product?.also {
                        selectedProducts[it] = value
                    }
                }
                quantityChangeListener()
            }

        init {
            quantity = 0
            binding.buttonMinus.setOnClickListener { quantity-- }
            binding.buttonPlus.setOnClickListener { quantity++ }
        }

        fun bind(product: Product, quantity: Int) {
            binding.product = product
            this.quantity = quantity
            this.binding.executePendingBindings()
        }
    }
}
