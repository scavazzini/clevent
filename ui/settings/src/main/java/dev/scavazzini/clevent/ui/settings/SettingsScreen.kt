package dev.scavazzini.clevent.ui.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.scavazzini.clevent.ui.core.OnNewIntentHandler
import dev.scavazzini.clevent.ui.core.components.NfcModalBottomSheet
import dev.scavazzini.clevent.ui.core.theme.CleventTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    OnNewIntentHandler { viewModel.eraseTag(it) }

    val state by viewModel.uiState.collectAsState()

    SettingsScreenContent(
        state = state,
        onSyncClick = viewModel::sync,
        onEraseClick = viewModel::onEraseTagClick,
        onDismiss = viewModel::onCancelErase,
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreenContent(
    state: SettingsViewModel.SettingsUiState,
    modifier: Modifier = Modifier,
    onSyncClick: () -> Unit = { },
    onEraseClick: () -> Unit = { },
    onDismiss: () -> Unit = { },
    sheetState: SheetState = rememberModalBottomSheetState(),
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
            description = lastSyncDescription,
            loading = state.isSyncing ?: false,
            icon = Icons.Filled.Sync,
            onClick = onSyncClick,
        )
        SettingsButtonText(
            title = stringResource(R.string.settings_erase_tag_title),
            description = stringResource(R.string.settings_erase_tag_description),
            icon = Icons.Filled.Nfc,
            onClick = onEraseClick,
        )
    }

    if (state.showSheet) {
        val title = state.title?.let { titleRes ->
            stringResource(titleRes, *state.titleArgs.toTypedArray())
        }

        val description = state.description?.let { descriptionRes ->
            stringResource(descriptionRes, *state.descriptionArgs.toTypedArray())
        }

        NfcModalBottomSheet(
            onDismiss = onDismiss,
            title = title ?: "",
            description = description ?: "",
            sheetState = sheetState,
            nfcReadingState = state.sheetState,
        )
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
    description: String? = null,
    loading: Boolean = false,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = !loading,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun SettingsScreenContentPreview() {
    CleventTheme {
        SettingsScreenContent(
            state = SettingsViewModel.SettingsUiState(),
        )
    }
}
