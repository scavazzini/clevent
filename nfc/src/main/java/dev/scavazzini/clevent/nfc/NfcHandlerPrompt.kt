@file:OptIn(ExperimentalMaterial3Api::class)

package dev.scavazzini.clevent.nfc

import android.content.Intent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import dev.scavazzini.clevent.nfc.component.NfcHandlerBottomSheet
import dev.scavazzini.clevent.nfc.component.NfcHandlerReadingStatus

@Composable
fun NfcHandlerPrompt(
    title: String,
    description: String,
    readingStatus: NfcHandlerReadingStatus,
    onDismiss: () -> Unit,
    onTagRead: (Intent) -> Unit,
    modifier: Modifier = Modifier,
    readingMessage: String? = null,
    sheetContent: @Composable () -> Unit = { },
) {
    val sheetState = rememberModalBottomSheetState()
    val renderBottomSheet = readingStatus != NfcHandlerReadingStatus.HALTED

    LaunchedEffect(readingStatus) {
        when (readingStatus) {
            NfcHandlerReadingStatus.HALTED -> sheetState.hide()
            else -> sheetState.expand()
        }
    }

    OnTagDiscoveredHandler { newIntent ->
        if (readingStatus == NfcHandlerReadingStatus.LISTENING) {
            onTagRead(newIntent)
        }
    }

    if (renderBottomSheet) {
        NfcHandlerBottomSheet(
            title = title,
            description = description,
            readingStatus = readingStatus,
            sheetState = sheetState,
            onDismiss = onDismiss,
            modifier = modifier,
            readingMessage = readingMessage,
            content = sheetContent,
        )
    }
}
