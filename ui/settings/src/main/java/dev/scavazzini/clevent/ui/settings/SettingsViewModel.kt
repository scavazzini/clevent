package dev.scavazzini.clevent.ui.settings

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
import dev.scavazzini.clevent.data.core.repository.ProductRepository
import dev.scavazzini.clevent.data.core.workers.SyncProductsWorker
import dev.scavazzini.clevent.data.core.workers.SyncProductsWorker.Companion.SYNC_PRODUCTS_WORK_NAME
import dev.scavazzini.clevent.domain.core.FormatDateToStringUseCase
import dev.scavazzini.clevent.domain.core.crypto.KeyInfo
import dev.scavazzini.clevent.domain.settings.CreateSecretKeyUseCase
import dev.scavazzini.clevent.domain.settings.DeleteSecretKeyUseCase
import dev.scavazzini.clevent.domain.settings.DownloadSecretKeyUseCase
import dev.scavazzini.clevent.domain.settings.EraseTagUseCase
import dev.scavazzini.clevent.domain.settings.GetSecretKeyInfoUseCase
import dev.scavazzini.clevent.domain.settings.ImportSecretKeyUseCase
import dev.scavazzini.clevent.ui.core.components.NfcBottomSheetReadingState
import kotlinx.coroutines.Dispatchers
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
            eraseTagUseCase(intent)

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

    private fun Long?.parse(): String {
        if (this == null) {
            return "-"
        }
        return formatDateToStringUseCase(Date(this))
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
        val isSyncing: Boolean? = false,
        val secretKeyInfo: KeyInfo? = null,
    ) {
        fun isReadyToErase(): Boolean {
            return sheetState == NfcBottomSheetReadingState.WAITING && showSheet
        }
    }

}
