package dev.scavazzini.clevent.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.ShoppingCart
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
import dev.scavazzini.clevent.R
import dev.scavazzini.clevent.ui.OnNewIntentHandler
import dev.scavazzini.clevent.ui.components.NfcModalBottomSheet
import dev.scavazzini.clevent.ui.theme.CleventTheme

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
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        SettingsHeader(
            icon = Icons.Filled.ShoppingCart,
            title = stringResource(R.string.settings_products_header),
        )
        SettingsButtonText(
            title = stringResource(R.string.settings_sync_now_button),
            description = lastSyncDescription,
            onClick = onSyncClick,
        )
        SettingsHeader(
            icon = Icons.Filled.Nfc,
            title = stringResource(R.string.settings_tags_header),
        )
        SettingsButtonText(
            title = stringResource(R.string.settings_erase_tag_title),
            description = stringResource(R.string.settings_erase_tag_description),
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
fun SettingsHeader(
    icon: ImageVector,
    title: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(top = 16.dp)
    ) {
        Icon(
            imageVector = icon,
            tint = MaterialTheme.colorScheme.secondary,
            contentDescription = null,
        )
        Text(
            text = title,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

@Composable
fun SettingsButtonText(
    title: String,
    description: String? = null,
    onClick: () -> Unit,
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable(
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        Column {
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
