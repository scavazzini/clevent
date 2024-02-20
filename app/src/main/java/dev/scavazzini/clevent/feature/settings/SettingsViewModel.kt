package dev.scavazzini.clevent.feature.settings

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.scavazzini.clevent.R
import dev.scavazzini.clevent.data.repositories.ProductRepository
import dev.scavazzini.clevent.io.NFCReader
import dev.scavazzini.clevent.io.NFCWriter
import dev.scavazzini.clevent.ui.components.NfcBottomSheetReadingState
import dev.scavazzini.clevent.utilities.Preferences
import dev.scavazzini.clevent.utilities.extensions.formatted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val nfcWriter: NFCWriter,
    private val nfcReader: NFCReader,
    preferences: Preferences,
) : ViewModel() {

    private val _uiState: MutableStateFlow<SettingsUiState> = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        viewModelScope.launch {
            _uiState.update {
                _uiState.value.copy(
                    lastSync = R.string.settings_last_sync,
                    lastSyncArgs = listOf(preferences.lastSync.parse()),
                )
            }
        }
    }

    fun sync() = viewModelScope.launch(Dispatchers.IO) {
        _uiState.update {
            _uiState.value.copy(lastSync = R.string.settings_last_sync_loading)
        }

        productRepository.sync("products.json").also { syncedIn ->
            if (syncedIn == null) {
                return@launch _uiState.update {
                    _uiState.value.copy(lastSync = R.string.settings_last_sync_error)
                }
            }

            _uiState.update {
                _uiState.value.copy(
                    lastSync = R.string.settings_last_sync,
                    lastSyncArgs = listOf(syncedIn.parse()),
                )
            }
        }
    }

    fun onEraseTagClick() {
        _uiState.update {
            it.copy(
                sheetState = NfcBottomSheetReadingState.WAITING,
                showSheet = true,
                title = R.string.settings_erase_tag_title,
                description = R.string.settings_erase_tag_description,
            )
        }
    }

    fun onCancelErase() {
        _uiState.update {
            it.copy(showSheet = false)
        }
    }

    fun eraseTag(intent: Intent) = viewModelScope.launch {
        if (!_uiState.value.isReadyToErase()) {
            return@launch
        }

        try {
            val (tag) = nfcReader.extract(intent)

            nfcWriter.erase(tag)

            _uiState.value = _uiState.value.copy(
                sheetState = NfcBottomSheetReadingState.SUCCESS,
                title = R.string.settings_erase_tag_modal_success_title,
                description = R.string.settings_erase_tag_modal_success_description,
            )
            delay(1500)
            _uiState.value = _uiState.value.copy(showSheet = false)

        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                sheetState = NfcBottomSheetReadingState.ERROR,
                title = R.string.settings_erase_tag_modal_error_title,
                description = R.string.settings_erase_tag_modal_error_description,
            )
            delay(1500)
            _uiState.value = _uiState.value.copy(
                sheetState = NfcBottomSheetReadingState.WAITING,
                title = R.string.settings_erase_tag_title,
                description = R.string.settings_erase_tag_description,
            )
        }
    }

    private fun Long.parse(): String {
        if (this <= 0) {
            return "-"
        }
        return Date(this).formatted()
    }

    data class SettingsUiState(
        val sheetState: NfcBottomSheetReadingState = NfcBottomSheetReadingState.WAITING,
        val showSheet: Boolean = false,
        val title: Int? = null,
        val titleArgs: List<String> = emptyList(),
        val description: Int? = null,
        val descriptionArgs: List<String> = emptyList(),
        val lastSync: Int? = null,
        val lastSyncArgs: List<String> = emptyList(),
    ) {
        fun isReadyToErase(): Boolean {
            return sheetState == NfcBottomSheetReadingState.WAITING && showSheet
        }
    }

}
