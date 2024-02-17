package dev.scavazzini.clevent.feature.receipt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.scavazzini.clevent.R
import dev.scavazzini.clevent.data.models.CurrencyValue
import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.data.models.EMPTY_CUSTOMER
import dev.scavazzini.clevent.data.models.Product
import dev.scavazzini.clevent.ui.OnNewIntentHandler
import dev.scavazzini.clevent.ui.components.PrimaryButton
import dev.scavazzini.clevent.ui.components.PrimaryButtonState
import kotlinx.coroutines.launch

@Composable
fun ReceiptScreen(
    viewModel: ReceiptViewModel,
    modifier: Modifier = Modifier,
) {
    val customer by viewModel.customer.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    OnNewIntentHandler {
        coroutineScope.launch { viewModel.onNfcTagRead(it) }
    }

    ReceiptScreenContent(
        customer = customer,
        onShareButtonTapped = viewModel::share,
        onQrCodeButtonTapped = viewModel::generateQrCode,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun ReceiptScreenContent(
    customer: Customer,
    onShareButtonTapped: () -> Unit,
    onQrCodeButtonTapped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        ReceiptHeader(
            customer = customer,
            onShareButtonTapped = onShareButtonTapped,
            onQrCodeButtonTapped = onQrCodeButtonTapped,
            modifier = Modifier.fillMaxWidth(),
        )
        ReceiptList(
            customer = customer,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ReceiptHeader(
    customer: Customer,
    onShareButtonTapped: () -> Unit,
    onQrCodeButtonTapped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonState = if (customer == EMPTY_CUSTOMER)
        PrimaryButtonState.DISABLED
    else
        PrimaryButtonState.ENABLED

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 24.dp),
    ) {
        ReceiptBalanceText(
            value = CurrencyValue(customer.balance),
        )
        Text(
            text = stringResource(R.string.receipt_available_balance),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = Color(0xff55624C),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            val buttonColors = ButtonDefaults.buttonColors(
                containerColor = Color(0xffD9E7CB),
                contentColor = Color(0xff55624C),
                disabledContainerColor = Color(0xffEEEEEE),
                disabledContentColor = Color(0xffAAAAAA),
            )
            PrimaryButton(
                onClick = onShareButtonTapped,
                text = stringResource(R.string.receipt_share_button),
                colors = buttonColors,
                state = buttonState,
                modifier = Modifier.size(width = 125.dp, height = 40.dp),
            )
            PrimaryButton(
                onClick = onQrCodeButtonTapped,
                text = stringResource(R.string.receipt_qrcode_button),
                colors = buttonColors,
                state = buttonState,
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
    )
}

@Composable
private fun ReceiptList(
    customer: Customer,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(16.dp)) {
        Text(
            text = stringResource(R.string.receipt_transaction_list_title),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(customer.products.entries.toList()) { item ->
                ReceiptItem(
                    quantity = item.value,
                    product = item.key,
                )
            }
        }
        Divider(Modifier.padding(vertical = 8.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(R.string.receipt_transaction_list_total),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = CurrencyValue(customer.total).toString(),
                color = Color(0xff3EB17A),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
        }
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
                fontSize = 16.sp,
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)) {
                        withStyle(style = SpanStyle(color = Color.Black)) {
                            append("${quantity}x ")
                        }
                        withStyle(style = SpanStyle(color = Color(0xff3EB17A))) {
                            append(CurrencyValue(product.price).toString())
                        }
                    }
                },
            )
        }
        Text(
            text = CurrencyValue(product.price * quantity).toString(),
            color = Color(0xff3EB17A),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
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
    ReceiptScreenContent(
        customer = customer,
        onShareButtonTapped = { },
        onQrCodeButtonTapped = { },
    )
}
