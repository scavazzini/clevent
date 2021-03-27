package dev.scavazzini.clevent.ui.settings

import android.nfc.Tag
import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import dev.scavazzini.clevent.R
import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.io.NFCListener
import dev.scavazzini.clevent.ui.dialogs.NFCDialog
import dev.scavazzini.clevent.ui.settings.SettingsViewModel.EraseUiState.Error
import dev.scavazzini.clevent.ui.settings.SettingsViewModel.EraseUiState.Success
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat(), NFCListener {

    private val viewModel: SettingsViewModel by viewModels()
    private val nfcDialog: NFCDialog by lazy { NFCDialog(requireContext()) }
    private val syncPreference: Preference? by lazy { findPreference("sync_now") }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        viewModel.lastSync.observe(this, ::updateLastSync)

        syncPreference?.apply {
            setOnPreferenceClickListener {
                viewModel.sync()
                true
            }
        }

        findPreference<Preference?>("erase_tag")?.setOnPreferenceClickListener {
            showEraseDialog()
            true
        }

        lifecycleScope.launchWhenCreated {
            viewModel.eraseUiState.collect {
                when (it) {
                    is Success -> nfcDialog.apply {
                        withActionButton(getString(R.string.erase_another_one), ::showEraseDialog)
                        showSuccess(getString(R.string.tag_erased), getString(R.string.tag_successfully_erased))
                    }
                    is Error -> nfcDialog.showError(getString(R.string.erase_failed),
                            getString(R.string.erase_failed_description))
                }
            }
        }
    }

    private fun updateLastSync(message: String) {
        syncPreference?.apply {
            summary = message
        }
    }

    private fun showEraseDialog() {
        nfcDialog.showWaitingForRead(getString(R.string.erase_tag_title),
                getString(R.string.nfc_action_erase))
    }

    override fun onTagRead(tag: Tag, customer: Customer) {
        if (nfcDialog.isWaitingForRead()) viewModel.eraseTag(tag)
    }

    override fun onInvalidTagRead(tag: Tag?) {
        if (!nfcDialog.isWaitingForRead()) return

        if (tag !is Tag) {
            nfcDialog.showError(getString(R.string.erase_failed),
                    getString(R.string.erase_failed_description))
            return
        }

        viewModel.eraseTag(tag)
    }
}
