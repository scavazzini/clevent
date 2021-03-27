package dev.scavazzini.clevent.ui.receipt

import android.content.DialogInterface
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dev.scavazzini.clevent.R
import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.databinding.FragmentReceiptBinding
import dev.scavazzini.clevent.io.NFCListener
import dev.scavazzini.clevent.ui.dialogs.NFCDialog
import dev.scavazzini.clevent.ui.dialogs.QRCodeDialog
import dev.scavazzini.clevent.utilities.extensions.formatted
import dev.scavazzini.clevent.utilities.extensions.toReceiptString
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class ReceiptFragment : Fragment(), NFCListener, DialogInterface.OnDismissListener {

    private val viewModel: ReceiptViewModel by viewModels()
    private val navController by lazy { findNavController() }
    private val directionArguments by navArgs<ReceiptFragmentArgs>()
    private val mProductsReceiptAdapter: ProductsReceiptAdapter = ProductsReceiptAdapter()
    private val mNFCDialog: NFCDialog by lazy { NFCDialog(requireContext()) }
    private val mQRCodeDialog: QRCodeDialog by lazy { QRCodeDialog(requireContext()) }

    private lateinit var binding: FragmentReceiptBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        binding.productsList.setHasFixedSize(true)
        binding.productsList.adapter = mProductsReceiptAdapter
        mNFCDialog.setOnDismissListener(this)

        if (viewModel.customer is Customer) {
            showLoadedReceipt()
            return
        }

        getArgumentCustomer()?.let {
            loadReceipt(it)
            return
        }

        mNFCDialog.showWaitingForRead("Show receipt",
                getString(R.string.nfc_action_load_receipt))
    }

    private fun getArgumentCustomer(): Customer? {
        arguments?.getParcelable<Intent?>(NavController.KEY_DEEP_LINK_INTENT)?.apply {
            if (action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
                return viewModel.extractFromIntent(this)
            }
        }
        return directionArguments.customer
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.receipt_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.qr_code_receipt) {
            showQRCodeDialog()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showQRCodeDialog() {
        mQRCodeDialog.show()

        if (viewModel.qrCodeBitmap != null) {
            mQRCodeDialog.setBitmap(viewModel.qrCodeBitmap)
            return
        }
        viewModel.customer?.let {
            generateQRCode(it)
        }
    }

    private fun generateQRCode(customer: Customer) = lifecycleScope.launch {
        try {

            val receiptString = customer.toReceiptString(Calendar.getInstance(), requireContext())
            val qrCode = viewModel.generateQrCode(receiptString, 400, 400)
            viewModel.qrCodeBitmap = qrCode
            mQRCodeDialog.setBitmap(qrCode)

        } catch (e: Exception) {
            mQRCodeDialog.dismiss()
            Snackbar.make(requireView(), getString(R.string.failed_generate_qr_code),
                    Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onTagRead(tag: Tag, customer: Customer) {
        if (!mNFCDialog.isWaitingForRead()) return

        loadReceipt(customer)
    }

    private fun loadReceipt(customer: Customer) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.customer = customer
            viewModel.loadProductsData(customer.products.keys.toList())

            showLoadedReceipt()
            mNFCDialog.dismiss()
        }
    }

    private fun showLoadedReceipt() {
        binding.date = Calendar.getInstance().time.formatted()

        viewModel.customer?.let { customer ->
            mProductsReceiptAdapter.updateProducts(customer.products)

            if (customer.products.isEmpty()) {
                binding.productsEmptyList.visibility = View.VISIBLE
            }
        }

        binding.customer = viewModel.customer
    }

    override fun onInvalidTagRead(tag: Tag?) {
        if (!mNFCDialog.isWaitingForRead()) return

        mNFCDialog.showError(getString(R.string.receipt_error),
                getString(R.string.invalid_tag_error))
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (viewModel.customer == null) {
            navController.navigateUp()
        }
    }

}
