package dev.scavazzini.clevent.feature.settings.ui

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.scavazzini.clevent.core.data.repository.ProductRepository
import dev.scavazzini.clevent.core.data.workers.SyncProductsWorker
import dev.scavazzini.clevent.core.data.workers.SyncProductsWorker.Companion.SYNC_PRODUCTS_WORK_NAME
import dev.scavazzini.clevent.core.domain.FormatDateToStringUseCase
import dev.scavazzini.clevent.crypto.KeyInfo
import dev.scavazzini.clevent.feature.settings.domain.CreateSecretKeyUseCase
import dev.scavazzini.clevent.feature.settings.domain.DeleteSecretKeyUseCase
import dev.scavazzini.clevent.feature.settings.domain.DownloadSecretKeyUseCase
import dev.scavazzini.clevent.feature.settings.domain.EraseTagUseCase
import dev.scavazzini.clevent.feature.settings.domain.GetSecretKeyInfoUseCase
import dev.scavazzini.clevent.feature.settings.domain.ImportSecretKeyUseCase
import dev.scavazzini.clevent.nfc.component.NfcHandlerReadingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val productRepository: ProductRepository,
    private val eraseTagUseCase: EraseTagUseCase,
    private val formatDateToStringUseCase: FormatDateToStringUseCase,
    private val getSecretKeyInfoUseCase: GetSecretKeyInfoUseCase,
    private val createSecretKeyUseCase: CreateSecretKeyUseCase,
    private val importSecretKeyUseCase: ImportSecretKeyUseCase,
    private val downloadSecretKeyUseCase: DownloadSecretKeyUseCase,
    private val deleteSecretKeyUseCase: DeleteSecretKeyUseCase,
) : AndroidViewModel(application) {

    private var eraseJob: Job? = null

    private val _uiState: MutableStateFlow<SettingsUiState> = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    private val syncProductsWorkInfoFlow = WorkManager.getInstance(application).getWorkInfosFlow(
        WorkQuery.fromUniqueWorkNames(SYNC_PRODUCTS_WORK_NAME)
    )

    init {
        viewModelScope.launch {
            updateSecretKeyInfoUiState()

            syncProductsWorkInfoFlow.collect { workInfoList ->
                val workState = workInfoList.getOrNull(0)?.state ?: return@collect

                _uiState.update { _uiState.value.copy(isSyncing = !workState.isFinished) }

                when (workState) {
                    WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING -> _uiState.update {
                        _uiState.value.copy(lastSync = R.string.settings_last_sync_loading)
                    }

                    WorkInfo.State.FAILED -> _uiState.update {
                        _uiState.value.copy(lastSync = R.string.settings_last_sync_error)
                    }

                    else -> _uiState.update {
                        val lastSync = productRepository.getLastSync().parse()

                        _uiState.value.copy(
                            lastSync = R.string.settings_last_sync,
                            lastSyncArgs = listOf(lastSync),
                        )
                    }
                }
            }
        }
    }

    private suspend fun updateSecretKeyInfoUiState() {
        try {
            _uiState.update {
                _uiState.value.copy(secretKeyInfo = getSecretKeyInfoUseCase())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.update { _uiState.value.copy(secretKeyInfo = null) }
        }
    }

    fun sync() = viewModelScope.launch(Dispatchers.IO) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncProductsRequest = OneTimeWorkRequest.Builder(SyncProductsWorker::class.java)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(application)
            .enqueueUniqueWork(
                SYNC_PRODUCTS_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                syncProductsRequest,
            )
    }

    fun generateSecretKey() = viewModelScope.launch {
        try {
            createSecretKeyUseCase()
            updateSecretKeyInfoUiState()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun importSecretKey(uri: Uri) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            try {
                application.contentResolver.openInputStream(uri).use {
                    val fileBytes = it?.readBytes()
                        ?: throw Exception("File bytes could not be read.")

                    importSecretKeyUseCase(fileBytes)
                    updateSecretKeyInfoUiState()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun downloadSecretKey(uri: Uri) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            try {
                val keyBytes = downloadSecretKeyUseCase()?.encoded
                    ?: throw Exception("Secret key bytes could not be read.")

                application.contentResolver.openOutputStream(uri).use {
                    it?.write(keyBytes)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteSecretKey() = viewModelScope.launch {
        try {
            deleteSecretKeyUseCase()
            updateSecretKeyInfoUiState()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onEraseTagClick() {
        _uiState.update {
            it.copy(
                nfcReadingStatus = NfcHandlerReadingStatus.LISTENING,
                nfcReadingMessage = null,
            )
        }
    }

    fun onCancelErase() {
        eraseJob?.cancel()
        eraseJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    nfcReadingStatus = NfcHandlerReadingStatus.HALTED,
                )
            }
        }
    }

    fun eraseTag(intent: Intent) {
        eraseJob?.cancel()
        eraseJob = viewModelScope.launch {
            try {
                eraseTagUseCase(intent)

                _uiState.value = _uiState.value.copy(
                    nfcReadingStatus = NfcHandlerReadingStatus.SUCCESS,
                    nfcReadingMessage = application.getString(
                        R.string.settings_erase_tag_modal_success_description,
                    ),
                )
                delay(1500)
                _uiState.value = _uiState.value.copy(
                    nfcReadingStatus = NfcHandlerReadingStatus.HALTED,
                )

            } catch (e: Exception) {
                _uiState.value.updateNfcStateToError(
                    message = application.getString(
                        R.string.settings_erase_tag_modal_error_description
                    ),
                )
            }
        }
    }

    private fun Long?.parse(): String {
        if (this == null) {
            return "-"
        }
        return formatDateToStringUseCase(Date(this))
    }

    data class SettingsUiState(
        val nfcReadingStatus: NfcHandlerReadingStatus = NfcHandlerReadingStatus.HALTED,
        val nfcReadingMessage: String? = null,
        val lastSync: Int? = null,
        val lastSyncArgs: List<String> = emptyList(),
        val isSyncing: Boolean? = false,
        val secretKeyInfo: KeyInfo? = null,
    )

    private suspend fun SettingsUiState.updateNfcStateToError(message: String) {
        _uiState.value = copy(
            nfcReadingStatus = NfcHandlerReadingStatus.ERROR,
            nfcReadingMessage = application.getString(
                R.string.settings_erase_tag_modal_error_description
            ),
        )
        delay(1500)
        _uiState.value.updateNfcStateToListening()
    }

    private fun SettingsUiState.updateNfcStateToListening() {
        _uiState.value = copy(
            nfcReadingStatus = NfcHandlerReadingStatus.LISTENING,
            nfcReadingMessage = null,
        )
    }

}
