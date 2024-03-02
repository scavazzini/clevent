package dev.scavazzini.clevent.ui.core.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.scavazzini.clevent.R
import dev.scavazzini.clevent.data.core.model.CurrencyValue
import dev.scavazzini.clevent.data.core.model.Product
import dev.scavazzini.clevent.ui.core.theme.CleventTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class NfcBottomSheetReadingState {
    WAITING,
    SUCCESS,
    ERROR,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcModalBottomSheet(
    title: String,
    description: String,
    sheetState: SheetState,
    nfcReadingState: NfcBottomSheetReadingState,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = { },
    content: @Composable () -> Unit = { },
) {
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        windowInsets = WindowInsets.navigationBars,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        NfcModalBottomSheetContent(
            title = title,
            description = description,
            sheetState = sheetState,
            nfcReadingState = nfcReadingState,
            modifier = modifier,
            onDismiss = onDismiss,
            scope = scope,
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NfcModalBottomSheetContent(
    title: String,
    description: String,
    sheetState: SheetState,
    nfcReadingState: NfcBottomSheetReadingState,
    modifier: Modifier = Modifier,
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
            colors = nfcReadingState.getColors(),
            modifier = Modifier.size(72.dp),
        ) {
            Image(
                imageVector = nfcReadingState.getIcon(),
                contentDescription = null,
                colorFilter = ColorFilter.tint(nfcReadingState.getPrimaryColor()),
                modifier = Modifier.size(32.dp)
            )
        }
        nfcReadingState.getMessage()?.let { message ->
            Text(
                text = stringResource(message),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                fontStyle = FontStyle.Italic,
                color = nfcReadingState.getPrimaryColor(),
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

@Composable
private fun ProductListDivider() {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 1.dp,
    )
}

@Composable
private fun ProductListItem(productEntry: Map.Entry<Product, Int>, modifier: Modifier) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 0.dp)
            .fillMaxWidth(),
    ) {
        val leftText = "${productEntry.value}x ${productEntry.key.name}"
        val rightText = CurrencyValue(productEntry.key.price * productEntry.value).toString()

        Text(leftText, modifier = Modifier.weight(1f))
        Text(
            text = rightText,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
fun BottomSheetProductListContent(
    products: Map<Product, Int> = emptyMap(),
    maxFullyVisible: Int = 4,
) {
    if (products.isEmpty()) {
        return
    }

    val productList = remember(products) { products.entries.toList() }

    var singleItemHeight by remember { mutableStateOf(0.dp) }
    val verticalMargin = 8.dp

    val localDensity = LocalDensity.current
    val quantityFullyVisible = products.size.coerceAtMost(maxFullyVisible)
    val itemsHeight = singleItemHeight * quantityFullyVisible
    val verticalMargins = verticalMargin * 2
    val marginsSeparator = verticalMargin * (quantityFullyVisible - 1)
    val heightOverflow = if (products.size > quantityFullyVisible) singleItemHeight / 2 else 0.dp

    val height = itemsHeight + marginsSeparator + verticalMargins + heightOverflow

    Column {
        ProductListDivider()
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(verticalMargin),
            contentPadding = PaddingValues(vertical = verticalMargin),
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
        ) {
            items(productList, key = { it.key.id }) {
                ProductListItem(
                    productEntry = it,
                    modifier = Modifier.onGloballyPositioned {
                        singleItemHeight = with(localDensity) { it.size.height.toDp() }
                    }
                )
            }
        }
        ProductListDivider()
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = verticalMargin)
                .fillMaxWidth(),
            text = CurrencyValue(products.entries.sumOf { it.value * it.key.price }).toString(),
            textAlign = TextAlign.Right,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun NfcBottomSheetReadingState.getIcon(): ImageVector {
    return when (this) {
        NfcBottomSheetReadingState.WAITING -> Icons.Filled.Nfc
        NfcBottomSheetReadingState.SUCCESS -> Icons.Filled.Check
        NfcBottomSheetReadingState.ERROR -> Icons.Filled.WarningAmber
    }
}

private fun NfcBottomSheetReadingState.getMessage(): Int? {
    return when (this) {
        NfcBottomSheetReadingState.WAITING -> R.string.nfc_modal_waiting_message
        NfcBottomSheetReadingState.ERROR -> R.string.nfc_modal_error_message
        else -> null
    }
}

private fun NfcBottomSheetReadingState.getColors(): List<Color> {
    return when (this) {
        NfcBottomSheetReadingState.WAITING -> listOf(
            Color.Black,
            Color.DarkGray,
            Color.Gray,
            Color.LightGray,
        )

        NfcBottomSheetReadingState.SUCCESS -> listOf(Color(0xFF13C666))
        NfcBottomSheetReadingState.ERROR -> listOf(Color(0xFFF54E4A))
    }
}

private fun NfcBottomSheetReadingState.getPrimaryColor() = this.getColors()[0]

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
private fun NfcModalBottomSheetContentWithListPreview() {
    MaterialTheme {
        NfcModalBottomSheetContent(
            title = "Confirm your Purchase",
            description = "Check the list below for compatibility before complete the purchase.",
            sheetState = rememberModalBottomSheetState(),
            nfcReadingState = NfcBottomSheetReadingState.WAITING,
        ) {
            BottomSheetProductListContent(
                products = mapOf(
                    Pair(Product(1, "Product 1", 600, ""), 3),
                    Pair(Product(2, "Product 2", 650, ""), 1),
                    Pair(Product(3, "Product 3", 350, ""), 2),
                    Pair(Product(4, "Product 4", 800, ""), 2),
                    Pair(Product(5, "Product 5", 1000, ""), 5),
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
private fun NfcModalBottomSheetContentSuccessPreview() {
    MaterialTheme {
        NfcModalBottomSheetContent(
            title = "Confirm your Purchase",
            description = "Check the list below before complete your purchase.",
            sheetState = rememberModalBottomSheetState(),
            nfcReadingState = NfcBottomSheetReadingState.SUCCESS,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
private fun NfcModalBottomSheetContentErrorPreview() {
    CleventTheme {
        NfcModalBottomSheetContent(
            title = "Confirm your Purchase",
            description = "Check the list below before complete your purchase.",
            sheetState = rememberModalBottomSheetState(),
            nfcReadingState = NfcBottomSheetReadingState.ERROR,
        )
    }
}
