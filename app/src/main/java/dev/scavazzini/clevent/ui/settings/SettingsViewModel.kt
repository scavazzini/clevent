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
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
        private val productRepository: ProductRepository,
        private val nfcWriter: NFCWriter,
        preferences: Preferences,
) : ViewModel() {

    val lastSync: LiveData<String>
        get() = _lastSync

    private val _lastSync: MutableLiveData<String> = MutableLiveData(preferences.lastSync.parse())

    fun sync() = viewModelScope.launch(Dispatchers.IO) {
        _lastSync.postValue("Syncing...")

        productRepository.sync().also { synced ->
            _lastSync.postValue(if (synced is Long) synced.parse() else "Failed to sync.")
        }
    }

    suspend fun eraseTag(tag: Tag) {
        nfcWriter.erase(tag)
    }

    private fun Long.parse(): String {
        if (this > 0) {
            return "Last sync: ${Date(this).formatted()}"
        }
        return ""
    }

}
