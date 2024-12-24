@file:OptIn(ExperimentalMaterial3Api::class)

package dev.scavazzini.clevent.nfc.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.scavazzini.clevent.nfc.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class NfcHandlerReadingStatus {
    HALTED,
    LISTENING,
    SUCCESS,
    ERROR,
}

@Composable
internal fun NfcHandlerBottomSheet(
    title: String,
    description: String,
    sheetState: SheetState,
    readingStatus: NfcHandlerReadingStatus,
    modifier: Modifier = Modifier,
    readingMessage: String? = null,
    onDismiss: () -> Unit = { },
    content: @Composable () -> Unit = { },
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        contentWindowInsets = { WindowInsets.navigationBars },
        sheetState = sheetState,
        modifier = modifier,
    ) {
        NfcHandlerBottomSheetContent(
            title = title,
            description = description,
            sheetState = sheetState,
            readingStatus = readingStatus,
            modifier = modifier,
            readingMessage = readingMessage,
            onDismiss = onDismiss,
            content = content,
        )
    }
}

@Composable
private fun NfcHandlerBottomSheetContent(
    title: String,
    description: String,
    sheetState: SheetState,
    readingStatus: NfcHandlerReadingStatus,
    modifier: Modifier = Modifier,
    readingMessage: String? = null,
    onDismiss: () -> Unit = { },
    scope: CoroutineScope = rememberCoroutineScope(),
    content: @Composable () -> Unit = { },
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
    ) {
        Text(
            text = title,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = description,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
        )

        content()

        Spacer(modifier = Modifier.height(8.dp))
        CircularAnimatedIcon(
            colors = readingStatus.getCircularIconColors(),
            modifier = Modifier.size(72.dp),
        ) {
            Image(
                imageVector = readingStatus.getCircularIconImage(),
                contentDescription = null,
                colorFilter = ColorFilter.tint(readingStatus.getCircularIconPrimaryColor()),
                modifier = Modifier.size(32.dp)
            )
        }

        val readStatusMessage = readingMessage
            ?: readingStatus.getDefaultMessage()

        readStatusMessage?.let { message ->
            Text(
                text = message,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
                fontStyle = FontStyle.Italic,
                color = readingStatus.getCircularIconPrimaryColor(),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Button(
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onBackground
            ),
            onClick = {
                scope.launch {
                    sheetState.hide()
                    onDismiss()
                }
            },
        ) {
            Text(stringResource(R.string.nfc_modal_cancel_button))
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

private fun NfcHandlerReadingStatus.getCircularIconImage(): ImageVector {
    return when (this) {
        NfcHandlerReadingStatus.LISTENING -> Icons.Filled.Nfc
        NfcHandlerReadingStatus.SUCCESS -> Icons.Filled.Check
        NfcHandlerReadingStatus.ERROR -> Icons.Filled.WarningAmber
        NfcHandlerReadingStatus.HALTED -> Icons.Filled.Nfc
    }
}

@Composable
private fun NfcHandlerReadingStatus.getDefaultMessage(): String? {
    return when (this) {
        NfcHandlerReadingStatus.LISTENING -> stringResource(R.string.nfc_modal_waiting_message)
        NfcHandlerReadingStatus.ERROR -> stringResource(R.string.nfc_modal_error_message)
        else -> null
    }
}

private fun NfcHandlerReadingStatus.getCircularIconColors(): List<Color> {
    return when (this) {
        NfcHandlerReadingStatus.LISTENING -> listOf(
            Color.Black,
            Color.DarkGray,
            Color.Gray,
            Color.LightGray,
        )

        NfcHandlerReadingStatus.SUCCESS -> listOf(Color(0xFF13C666))
        NfcHandlerReadingStatus.ERROR -> listOf(Color(0xFFF54E4A))
        NfcHandlerReadingStatus.HALTED -> listOf(Color.Black)
    }
}

private fun NfcHandlerReadingStatus.getCircularIconPrimaryColor(): Color {
    return this.getCircularIconColors().first()
}

@Composable
@Preview
private fun NfcHandlerBottomSheetContentSuccessPreview() {
    MaterialTheme {
        NfcHandlerBottomSheetContent(
            title = "Confirm your Purchase",
            description = "Check the list below before complete your purchase.",
            sheetState = rememberModalBottomSheetState(),
            readingStatus = NfcHandlerReadingStatus.SUCCESS,
        )
    }
}

@Composable
@Preview
private fun NfcHandlerBottomSheetContentErrorPreview() {
    MaterialTheme {
        NfcHandlerBottomSheetContent(
            title = "Confirm your Purchase",
            description = "Check the list below before complete your purchase.",
            sheetState = rememberModalBottomSheetState(),
            readingStatus = NfcHandlerReadingStatus.ERROR,
        )
    }
}
