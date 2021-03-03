package dev.scavazzini.clevent.ui.recharge

import android.nfc.Tag
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.scavazzini.clevent.R
import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.databinding.FragmentRechargeBinding
import dev.scavazzini.clevent.io.NFCListener
import dev.scavazzini.clevent.ui.dialogs.NFCDialog
import dev.scavazzini.clevent.utilities.extensions.toCurrency
import dev.scavazzini.clevent.utilities.extensions.toIntCurrency
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class RechargeFragment : Fragment(), NFCListener {

    private val viewModel: RechargeViewModel by viewModels()
    private val navController by lazy { findNavController() }
    private val mNFCDialog: NFCDialog by lazy { NFCDialog(requireContext()) }
    private lateinit var binding: FragmentRechargeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentRechargeBinding.inflate(inflater, container, false)
        initializeUI()
        return binding.root
    }

    private fun initializeUI() {
        binding.rechargeValue.hint = Currency.getInstance(Locale.getDefault()).symbol

        binding.rechargeButton.setOnClickListener {
            val value = binding.rechargeValue.text.toString().toIntCurrency()
            mNFCDialog.showWaitingForRead(getString(R.string.recharge_confirmation),
                    getString(R.string.nfc_action_recharge, value.toCurrency()))
        }

        binding.rechargeValue.doAfterTextChanged {
            try {
                val value = it.toString().toDouble()
                if (value > 0) {
                    enableRechargeButton()
                } else {
                    disableRechargeButton()
                }
            } catch (e: Exception) {
                disableRechargeButton()
            }
        }
    }

    private fun enableRechargeButton() {
        binding.rechargeButton.setBackgroundResource(R.color.colorPrimary)
        binding.rechargeButton.isEnabled = true
    }

    private fun disableRechargeButton() {
        binding.rechargeButton.setBackgroundResource(R.color.darkGray)
        binding.rechargeButton.isEnabled = false
    }

    private fun performRecharge(customer: Customer, tag: Tag) = lifecycleScope.launch {
        try {
            val value = binding.rechargeValue.text.toString().toIntCurrency()
            viewModel.recharge(tag, customer, value)
            showRechargeSuccess(customer)

        } catch (e: IllegalArgumentException) {
            e.message?.let { mNFCDialog.showError(getString(R.string.recharge_failed_title), it) }

        } catch (e: Exception) {
            mNFCDialog.showError(getString(R.string.recharge_failed_title),
                    getString(R.string.recharge_failed_description))
        }
    }

    private fun showRechargeSuccess(customer: Customer) {
        binding.rechargeValue.text.clear()
        mNFCDialog.apply {
            withActionButton(getString(R.string.view_receipt)) {
                val toReceiptDirection = RechargeFragmentDirections.actionRechargeToReceipt(customer)
                navController.navigate(toReceiptDirection)
            }
            showSuccess(getString(R.string.recharge_completed),
                    getString(R.string.recharge_successfully_made, customer.balance.toCurrency()))
        }
    }

    override fun onTagRead(tag: Tag, customer: Customer) {
        if (!mNFCDialog.isWaitingForRead()) return
        performRecharge(customer, tag)
    }

    override fun onInvalidTagRead() {
        if (!mNFCDialog.isWaitingForRead()) return
        mNFCDialog.showError(getString(R.string.recharge_failed_description),
                getString(R.string.invalid_tag_error))
    }
}
