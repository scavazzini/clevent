package dev.scavazzini.clevent.feature.receipt.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.scavazzini.clevent.core.data.model.CurrencyValue
import dev.scavazzini.clevent.core.data.model.Customer
import dev.scavazzini.clevent.core.data.model.Product
import dev.scavazzini.clevent.core.ui.components.PrimaryButton
import dev.scavazzini.clevent.core.ui.theme.CleventTheme
import dev.scavazzini.clevent.feature.receipt.ui.components.QrCodeModalBottomSheet
import dev.scavazzini.clevent.nfc.OnTagDiscoveredHandler
import kotlinx.coroutines.launch

@Composable
fun ReceiptScreen(
    viewModel: ReceiptViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val density = LocalDensity.current
    val qrCodeSize = 240.dp
    val qrCodeSizePx = with(density) { qrCodeSize.toPx().toInt() }

    OnTagDiscoveredHandler {
        coroutineScope.launch { viewModel.onNfcTagRead(it) }
    }

    ReceiptScreenContent(
        state = state,
        onShareButtonTapped = viewModel::share,
        onQrCodeButtonTapped = { viewModel.showQrCode(qrCodeSizePx) },
        modifier = modifier.fillMaxWidth(),
        qrCodeSize = qrCodeSize,
        onDismissQrCode = viewModel::dismissQrCode,
    )
}

@Composable
private fun ReceiptScreenContent(
    state: ReceiptViewModel.ReceiptUiState,
    onShareButtonTapped: () -> Unit,
    onQrCodeButtonTapped: () -> Unit,
    modifier: Modifier = Modifier,
    qrCodeSize: Dp = 240.dp,
    onDismissQrCode: () -> Unit = { },
) {
    Column(modifier.fillMaxWidth()) {
        ReceiptHeader(
            state = state,
            onShareButtonTapped = onShareButtonTapped,
            onQrCodeButtonTapped = onQrCodeButtonTapped,
            modifier = Modifier.fillMaxWidth(),
        )
        ReceiptList(
            tagState = state.tagState,
            modifier = Modifier.weight(1f),
        )
    }

    if (state.showQrCodeSheet && state.qrCode != null) {
        QrCodeModalBottomSheet(
            qrCode = state.qrCode.asImageBitmap(),
            qrCodeSize = qrCodeSize,
            title = stringResource(R.string.receipt_qrcode_modal_title),
            description = stringResource(R.string.receipt_qrcode_modal_description),
            close = stringResource(R.string.receipt_qrcode_modal_close),
            onDismiss = onDismissQrCode,
        )
    }
}

@Composable
private fun ReceiptHeader(
    state: ReceiptViewModel.ReceiptUiState,
    onShareButtonTapped: () -> Unit,
    onQrCodeButtonTapped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 24.dp),
    ) {
        ReceiptBalanceText(
            value = CurrencyValue(state.tagState?.customer?.balance ?: 0),
        )
        Text(
            text = stringResource(R.string.receipt_available_balance),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.displaySmall,
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val buttonColors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.secondaryContainer,
            )
            PrimaryButton(
                onClick = onShareButtonTapped,
                text = stringResource(R.string.receipt_share_button),
                colors = buttonColors,
                state = state.shareButtonState,
                modifier = Modifier.size(width = 125.dp, height = 40.dp),
            )
            PrimaryButton(
                onClick = onQrCodeButtonTapped,
                text = stringResource(R.string.receipt_qrcode_button),
                colors = buttonColors,
                state = state.qrCodeButtonState,
                modifier = Modifier.size(width = 125.dp, height = 40.dp),
            )
        }
    }
}

@Composable
fun ReceiptBalanceText(
    value: CurrencyValue,
    modifier: Modifier = Modifier,
) {
    Text(
        textAlign = TextAlign.Center,
        modifier = modifier,
        text = value.toAnnotatedString(),
        color = MaterialTheme.colorScheme.secondaryContainer,
    )
}

@Composable
private fun ReceiptList(
    tagState: ReceiptViewModel.TagState?,
    modifier: Modifier = Modifier,
) {
    Crossfade(
        targetState = tagState,
        label = "ReceiptListFade",
        modifier = modifier.padding(16.dp),
    ) { state ->
        when {
            state == null -> ProductsIdleList()
            state.customer != null -> ProductsList(state.customer)
            state.error != null -> ProductsErrorList(error = state.error)
        }
    }
}

@Composable
private fun ProductsList(
    customer: Customer,
    modifier: Modifier = Modifier,
) {
    val products = remember(customer.products) { customer.products.entries.toList() }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.receipt_transaction_list_title),
            style = MaterialTheme.typography.titleMedium,
        )
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.weight(1f),
        ) {
            items(products, key = { it.key.id }) { item ->
                ReceiptItem(
                    quantity = item.value,
                    product = item.key,
                )
            }
        }
        HorizontalDivider()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.receipt_transaction_list_total),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = CurrencyValue(customer.total).toString(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
fun ProductsIdleList(
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize(),
    ) {
        Icon(
            imageVector = Icons.Filled.Nfc,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(120.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.receipt_empty_state_instructions),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
        )
    }
}

@Composable
private fun ProductsErrorList(
    error: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize(),
    ) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(120.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(error),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
        )
    }
}

@Composable
private fun ReceiptItem(
    quantity: Int,
    product: Product,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(vertical = 8.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)) {
                        withStyle(style = SpanStyle(color = Color.Black)) {
                            append("${quantity}x ")
                        }
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append(CurrencyValue(product.price).toString())
                        }
                    }
                },
            )
        }
        Text(
            text = CurrencyValue(product.price * quantity).toString(),
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Preview
@Composable
private fun ReceiptScreenContentPreview() {
    val customer = Customer(
        balance = 12050,
        products = mapOf(
            Product(1, "Irish Stout, 568ml", 600, "Beer") to 3,
            Product(2, "India Pale Ale (IPA), 330ml", 450, "Beer") to 1,
            Product(3, "Imperial Porter, 568ml", 650, "Beer") to 2,
        )
    )
    CleventTheme {
        ReceiptScreenContent(
            state = ReceiptViewModel.ReceiptUiState(
                tagState = ReceiptViewModel.TagState(customer),
            ),
            onShareButtonTapped = { },
            onQrCodeButtonTapped = { },
        )
    }
}

@Preview
@Composable
private fun ReceiptScreenEmptyContentPreview() {
    CleventTheme {
        ReceiptScreenContent(
            state = ReceiptViewModel.ReceiptUiState(),
            onShareButtonTapped = { },
            onQrCodeButtonTapped = { },
        )
    }
}
