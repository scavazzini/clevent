package dev.scavazzini.clevent.feature.order.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.scavazzini.clevent.core.data.model.CurrencyValue
import dev.scavazzini.clevent.core.data.model.Product


@Composable
fun NfcHandlerBottomSheetProductListContent(
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
@Preview
private fun NfcHandlerBottomSheetContentWithListPreview() {
    MaterialTheme {
        NfcHandlerBottomSheetProductListContent(
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
