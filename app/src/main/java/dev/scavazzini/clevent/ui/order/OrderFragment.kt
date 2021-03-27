package dev.scavazzini.clevent.ui.order

import android.nfc.Tag
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.scavazzini.clevent.R
import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.databinding.FragmentOrderBinding
import dev.scavazzini.clevent.io.NFCListener
import dev.scavazzini.clevent.ui.dialogs.NFCDialog
import dev.scavazzini.clevent.ui.order.OrderViewModel.OrderUiState.Error
import dev.scavazzini.clevent.ui.order.OrderViewModel.OrderUiState.Success
import dev.scavazzini.clevent.utilities.extensions.toCurrency
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class OrderFragment : Fragment(), NFCListener, View.OnClickListener, SearchView.OnQueryTextListener {

    private val viewModel: OrderViewModel by viewModels()
    private val navController by lazy { findNavController() }
    private val mNFCDialog: NFCDialog by lazy { NFCDialog(requireContext()) }

    private val mAdapter: ProductsOrderAdapter by lazy {
        ProductsOrderAdapter(viewModel.selectedProducts, quantityChangeListener = {
            updateOrderPrice()
        })
    }

    private lateinit var binding: FragmentOrderBinding
    private var searchView: SearchView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentOrderBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        initializeUI()

        viewModel.getProducts().observe(viewLifecycleOwner) { products ->
            mAdapter.setProducts(products)
            binding.productsEmptyList.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
        }

        lifecycleScope.launchWhenCreated {
            viewModel.orderUiState.collect {
                when (it) {
                    is Success -> showPurchaseSuccess(it.customer)
                    is Error -> {
                        mNFCDialog.apply {
                            if (it.messageId != null && it.messageId == R.string.not_enough_credits) {
                                withActionButton(getString(R.string.make_a_recharge)) {
                                    navController.navigate(OrderFragmentDirections.actionOrderToRecharge())
                                }
                            }
                            val messageRes = it.messageId ?: R.string.purchase_error_description
                            showError(getString(R.string.purchase_error), getString(messageRes))
                        }
                    }
                }
            }
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.order_menu, menu)

        val searchItem = menu.findItem(R.id.search_product)
        searchView = searchItem.actionView as SearchView
        searchView?.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.open_settings -> {
                navController.navigate(OrderFragmentDirections.actionOrderToSettings())
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initializeUI() {
        updateOrderPrice()

        binding.apply {
            productsList.adapter = mAdapter
            productsList.setHasFixedSize(true)
            confirmOrder.setOnClickListener(this@OrderFragment)
        }
    }

    private fun updateOrderPrice() {
        if (viewModel.selectedProducts.isNotEmpty()) {
            updateConfirmationButton(true, mAdapter.getSelectedPrice())
        } else {
            updateConfirmationButton(false)
        }
    }

    private fun updateConfirmationButton(enabled: Boolean, value: Int = 0) {
        if (enabled) {
            binding.confirmOrder.apply {
                isEnabled = true
                setBackgroundResource(R.color.colorPrimary)
                text = getString(R.string.confirm_order_value, value.toCurrency())
            }
        } else {
            binding.confirmOrder.apply {
                isEnabled = false
                setBackgroundResource(android.R.color.darker_gray)
                text = getString(R.string.confirm_order)
            }
        }
    }

    override fun onClick(view: View) {
        val orderPrice = mAdapter.getSelectedPrice()
        val productCount = mAdapter.getSelectedAmount()

        mNFCDialog.showWaitingForRead(getString(R.string.purchase_confirmation),
                resources.getQuantityString(R.plurals.nfc_action_order,
                        productCount, productCount, orderPrice.toCurrency()))
    }

    private fun showPurchaseSuccess(customer: Customer) {
        clearSearchView()
        mAdapter.clearSelected()
        updateConfirmationButton(enabled = false)
        mNFCDialog.apply {
            withActionButton(getString(R.string.view_receipt)) {
                val toReceiptDirection = OrderFragmentDirections.actionOrderToReceipt(customer)
                navController.navigate(toReceiptDirection)
            }
            showSuccess(getString(R.string.purchase_completed),
                    getString(R.string.order_successfully_placed))
        }
    }

    private fun clearSearchView() {
        searchView?.apply {
            setQuery("", false)
            isIconified = true
        }
    }

    override fun onTagRead(tag: Tag, customer: Customer) {
        if (!mNFCDialog.isWaitingForRead()) return
        viewModel.performPurchase(customer, tag)
    }

    override fun onInvalidTagRead(tag: Tag?) {
        if (!mNFCDialog.isWaitingForRead()) return
        mNFCDialog.showError(getString(R.string.purchase_error),
                getString(R.string.invalid_tag_error))
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onQueryTextChange(term: String): Boolean {
        mAdapter.filterList(term)
        return true
    }
}
