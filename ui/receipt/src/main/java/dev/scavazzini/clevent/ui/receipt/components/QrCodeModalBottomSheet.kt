package dev.scavazzini.clevent.ui.receipt.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.scavazzini.clevent.ui.receipt.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCodeModalBottomSheet(
    qrCode: ImageBitmap,
    title: String,
    description: String,
    close: String,
    modifier: Modifier = Modifier,
    qrCodeSize: Dp = 240.dp,
    onDismiss: () -> Unit = { },
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        windowInsets = WindowInsets.navigationBars,
        modifier = modifier,
    ) {
        QrCodeReceiptModalBottomSheetContent(
            qrCode = qrCode,
            qrCodeSize = qrCodeSize,
            title = title,
            description = description,
            close = close,
            modifier = modifier,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun QrCodeReceiptModalBottomSheetContent(
    qrCode: ImageBitmap,
    title: String,
    description: String,
    close: String,
    modifier: Modifier = Modifier,
    qrCodeSize: Dp = 240.dp,
    onDismiss: () -> Unit = { },
) {
    val scope = rememberCoroutineScope()

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
        Image(
            bitmap = qrCode,
            contentDescription = null,
            modifier = modifier.size(qrCodeSize),
        )
        Button(
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onBackground
            ),
            onClick = { scope.launch { onDismiss() } },
            content = { Text(close) },
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
@Preview
private fun QrCodeReceiptModalBottomSheetContentPreview() {
    MaterialTheme {
        QrCodeReceiptModalBottomSheetContent(
            qrCode = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565).asImageBitmap(),
            title = stringResource(R.string.receipt_qrcode_modal_title),
            description = stringResource(R.string.receipt_qrcode_modal_description),
            close = stringResource(R.string.receipt_qrcode_modal_close),
        )
    }
}
