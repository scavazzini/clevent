package dev.scavazzini.clevent.ui.settings

import android.nfc.Tag
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.scavazzini.clevent.data.repositories.ProductRepository
import dev.scavazzini.clevent.io.NFCWriter
import dev.scavazzini.clevent.utilities.Preferences
import dev.scavazzini.clevent.utilities.extensions.formatted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
        private val productRepository: ProductRepository,
        private val nfcWriter: NFCWriter,
        preferences: Preferences,
) : ViewModel() {

    private val _eraseUiState: MutableStateFlow<EraseUiState> = MutableStateFlow(EraseUiState.Empty)
    val eraseUiState: StateFlow<EraseUiState> = _eraseUiState

    val lastSync: LiveData<String>
        get() = _lastSync

    private val _lastSync: MutableLiveData<String> = MutableLiveData(preferences.lastSync.parse())

    fun sync() = viewModelScope.launch(Dispatchers.IO) {
        _lastSync.postValue("Syncing...")

        productRepository.sync().also { synced ->
            _lastSync.postValue(if (synced is Long) synced.parse() else "Failed to sync.")
        }
    }

    fun eraseTag(tag: Tag) = viewModelScope.launch {
        _eraseUiState.value = EraseUiState.Loading

        try {
            nfcWriter.erase(tag)
            _eraseUiState.value = EraseUiState.Success

        } catch (e: Exception) {
            _eraseUiState.value = EraseUiState.Error
        }
    }

    sealed class EraseUiState {
        object Loading : EraseUiState()
        object Success : EraseUiState()
        object Error : EraseUiState()
        object Empty : EraseUiState()
    }

    private fun Long.parse(): String {
        if (this > 0) {
            return "Last sync: ${Date(this).formatted()}"
        }
        return ""
    }

}
