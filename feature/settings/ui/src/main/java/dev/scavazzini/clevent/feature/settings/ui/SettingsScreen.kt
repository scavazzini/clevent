package dev.scavazzini.clevent.feature.settings.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.scavazzini.clevent.core.ui.theme.CleventTheme
import dev.scavazzini.clevent.crypto.KeyInfo
import dev.scavazzini.clevent.nfc.NfcHandlerPrompt

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    val openFileLauncher = rememberLauncherForActivityResult(GetContent()) { uri ->
        uri?.let { viewModel.importSecretKey(it) }
    }

    val saveFileLauncher = rememberLauncherForActivityResult(
        CreateDocument(mimeType = "application/octet-stream")
    ) { uri ->
        uri?.let { viewModel.downloadSecretKey(it) }
    }

    val state by viewModel.uiState.collectAsState()

    NfcHandlerPrompt(
        title = stringResource(R.string.settings_erase_tag_title),
        description = stringResource(R.string.settings_erase_tag_description),
        readingStatus = state.nfcReadingStatus,
        readingMessage = state.nfcReadingMessage,
        onDismiss = viewModel::onCancelErase,
        onTagRead = viewModel::eraseTag,
    )

    SettingsScreenContent(
        state = state,
        onSyncClick = viewModel::sync,
        onEraseClick = viewModel::onEraseTagClick,
        onGenerateSecretKeyClick = viewModel::generateSecretKey,
        onImportSecretKeyClick = { openFileLauncher.launch("*/*") },
        onDownloadSecretKeyClick = { saveFileLauncher.launch("${state.secretKeyInfo?.id}.aes") },
        onDeleteSecretKeyClick = viewModel::deleteSecretKey,
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsScreenContent(
    state: SettingsViewModel.SettingsUiState,
    modifier: Modifier = Modifier,
    onSyncClick: () -> Unit = { },
    onEraseClick: () -> Unit = { },
    onGenerateSecretKeyClick: () -> Unit = { },
    onImportSecretKeyClick: () -> Unit = { },
    onDownloadSecretKeyClick: () -> Unit = { },
    onDeleteSecretKeyClick: () -> Unit = { },
) {
    val lastSyncDescription = state.lastSync?.let { lastSyncRes ->
        stringResource(lastSyncRes, *state.lastSyncArgs.toTypedArray())
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = modifier,
    ) {
        SettingsHeader(title = stringResource(R.string.settings_title))
        SettingsButtonText(
            title = stringResource(R.string.settings_sync_now_button),
            description = if (lastSyncDescription != null) AnnotatedString(lastSyncDescription) else null,
            loading = state.isSyncing ?: false,
            icon = Icons.Filled.Sync,
            clickable = state.isSyncing != true,
            onClick = onSyncClick,
        )
        SettingsButtonText(
            title = stringResource(R.string.settings_secret_key_title),
            description = state.secretKeyInfo.toAnnotatedString(),
            clickable = false,
            icon = Icons.Filled.Security,
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (state.secretKeyInfo == null) {
                    IconTextButton(
                        text = stringResource(R.string.settings_secret_key_generate_button),
                        icon = Icons.Filled.Add,
                        onClick = onGenerateSecretKeyClick,
                    )
                    IconTextButton(
                        text = stringResource(R.string.settings_secret_key_import_button),
                        icon = Icons.Filled.FileUpload,
                        onClick = onImportSecretKeyClick,
                    )
                } else {
                    IconTextButton(
                        text = stringResource(R.string.settings_secret_key_delete_button),
                        icon = Icons.Filled.Delete,
                        onClick = onDeleteSecretKeyClick,
                    )
                    IconTextButton(
                        text = stringResource(R.string.settings_secret_key_download_button),
                        icon = Icons.Filled.FileDownload,
                        onClick = onDownloadSecretKeyClick,
                    )
                }
            }
        }
        SettingsButtonText(
            title = stringResource(R.string.settings_erase_tag_title),
            description = AnnotatedString(stringResource(R.string.settings_erase_tag_description)),
            icon = Icons.Filled.Nfc,
            clickable = true,
            onClick = onEraseClick,
        )
    }
}

@Composable
private fun KeyInfo?.toAnnotatedString(): AnnotatedString {
    if (this == null) {
        return AnnotatedString(
            text = stringResource(R.string.settings_secret_key_not_set),
            spanStyle = SpanStyle(
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
            ),
        )
    }

    return AnnotatedString(stringResource(R.string.settings_secret_key_info, algorithm, id, size))
}

@Composable
private fun IconTextButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        contentPadding = PaddingValues(),
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(modifier = Modifier.width(2.dp))
        Text(text)
    }
}

@Composable
private fun SettingsHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        style = MaterialTheme.typography.headlineLarge,
        modifier = modifier.padding(top = 16.dp),
    )
}

@Composable
private fun SettingsButtonText(
    title: String,
    description: AnnotatedString? = null,
    loading: Boolean = false,
    icon: ImageVector,
    clickable: Boolean,
    onClick: (() -> Unit)? = {},
    footerContent: @Composable () -> Unit = {},
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = clickable,
                role = Role.Button,
                onClick = { onClick?.invoke() },
            ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp),
        ) {
            SettingsButtonIcon(
                icon = icon,
                loading = loading,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(Modifier.animateContentSize()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                )
                description?.let {
                    Text(
                        text = it,
                        style = TextStyle(color = Color.Gray),
                    )
                }
                footerContent()
            }
        }
    }
}

@Composable
private fun SettingsButtonIcon(
    icon: ImageVector,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (loading) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.tertiary
    }

    val iconColor = if (loading) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    Box(
        modifier = modifier.size(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            tint = iconColor,
            contentDescription = null,
            modifier = Modifier
                .background(
                    color = backgroundColor,
                    shape = CircleShape,
                )
                .padding(10.dp)
                .fillMaxSize(),
        )
        if (loading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

}

@Preview
@Composable
private fun SettingsScreenContentPreview() {
    CleventTheme {
        SettingsScreenContent(
            state = SettingsViewModel.SettingsUiState(),
        )
    }
}
