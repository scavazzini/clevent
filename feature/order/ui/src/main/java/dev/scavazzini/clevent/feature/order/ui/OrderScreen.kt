package dev.scavazzini.clevent.feature.order.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.scavazzini.clevent.core.data.model.CurrencyValue
import dev.scavazzini.clevent.core.data.model.Product
import dev.scavazzini.clevent.core.ui.components.PrimaryButton
import dev.scavazzini.clevent.core.ui.components.PrimaryButtonState
import dev.scavazzini.clevent.core.ui.theme.CleventTheme
import dev.scavazzini.clevent.feature.order.ui.component.NfcHandlerBottomSheetProductListContent
import dev.scavazzini.clevent.nfc.NfcHandlerPrompt

@Composable
fun OrderScreen(
    viewModel: OrderViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.orderUiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val searchFieldValue by viewModel.searchFieldValue.collectAsState()
    val products by viewModel.products.collectAsState()
    val productsOnCart by viewModel.productsOnCart.collectAsState()

    NfcHandlerPrompt(
        title = stringResource(R.string.order_confirm_title),
        description = stringResource(R.string.order_confirm_description),
        readingStatus = state.nfcReadingStatus,
        readingMessage = state.nfcReadingMessage,
        onDismiss = viewModel::cancelOrder,
        onTagRead = viewModel::performPurchase,
        sheetContent = { NfcHandlerBottomSheetProductListContent(productsOnCart) },
    )

    OrderScreenContent(
        products = products,
        categories = categories,
        onIncreaseQuantity = viewModel::increase,
        onDecreaseQuantity = viewModel::decrease,
        onConfirmOrderButtonTapped = viewModel::confirmOrder,
        onClearOrderButtonTapped = viewModel::clearOrder,
        modifier = modifier.fillMaxWidth(),
        state = state,
        onCategoryClick = viewModel::onCategoryChange,
        searchFieldValue = searchFieldValue,
        onSearchFieldValueChange = viewModel::onSearchFieldValueChange,
    )
}

@Composable
private fun OrderScreenContent(
    products: Map<Product, Int>,
    categories: List<Pair<String, Boolean>>,
    onIncreaseQuantity: (product: Product) -> Unit,
    onDecreaseQuantity: (product: Product) -> Unit,
    onConfirmOrderButtonTapped: () -> Unit,
    onClearOrderButtonTapped: () -> Unit,
    modifier: Modifier = Modifier,
    state: OrderViewModel.OrderUiState = OrderViewModel.OrderUiState(),
    onCategoryClick: (String?) -> Unit = { },
    searchFieldValue: String = "",
    onSearchFieldValueChange: (String) -> Unit = { },
) {
    Column(modifier.fillMaxWidth()) {
        CategoryTabs(
            categories = categories,
            onSelectedCategoryChange = onCategoryClick,
        )
        ProductList(
            products = products,
            onIncreaseQuantity = onIncreaseQuantity,
            onDecreaseQuantity = onDecreaseQuantity,
            onConfirmOrderButtonTapped = onConfirmOrderButtonTapped,
            onClearOrderButtonTapped = onClearOrderButtonTapped,
            buttonState = state.confirmOrderButtonState,
            buttonValue = state.orderValue,
            modifier = Modifier.weight(1f),
            searchFieldValue = searchFieldValue,
            onSearchFieldValueChange = onSearchFieldValueChange,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryTabs(
    categories: List<Pair<String, Boolean>>,
    onSelectedCategoryChange: (selected: String?) -> Unit,
) {
    val selected = categories.find { it.second }
    val selectedIndex = categories.indexOf(selected) + 1

    SecondaryTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Tab(
            selected = selected == null,
            onClick = {
                if (selected != null) {
                    onSelectedCategoryChange(null)
                }
            },
        ) {
            Text(
                text = stringResource(R.string.order_category_all),
                fontWeight = if (selected == null) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.padding(vertical = 16.dp),
            )
        }

        categories.forEach { category ->
            Tab(
                selected = category.second,
                onClick = {
                    if (category != selected) {
                        onSelectedCategoryChange(category.first)
                    }
                },
            ) {
                Text(
                    text = category.first,
                    fontWeight = if (category.second) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun ProductList(
    products: Map<Product, Int>,
    onIncreaseQuantity: (product: Product) -> Unit,
    onDecreaseQuantity: (product: Product) -> Unit,
    onConfirmOrderButtonTapped: () -> Unit,
    onClearOrderButtonTapped: () -> Unit,
    modifier: Modifier = Modifier,
    buttonState: PrimaryButtonState = PrimaryButtonState.ENABLED,
    buttonValue: Int = 0,
    searchFieldValue: String = "",
    onSearchFieldValueChange: (String) -> Unit = { },
) {
    val productList = remember(products) { products.entries.toList() }
    val localDensity = LocalDensity.current
    var listBottomPadding by remember { mutableIntStateOf(0) }

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = modifier,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                top = 16.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp + with(localDensity) { listBottomPadding.toDp() },
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                SearchBar(
                    value = searchFieldValue,
                    onValueChange = onSearchFieldValueChange,
                    modifier = Modifier.padding(bottom = 8.dp),
                    trailingIcon = if (buttonState == PrimaryButtonState.ENABLED) {
                        {
                            ClearOrderButton(onClick = onClearOrderButtonTapped)
                        }
                    } else null,
                )
            }
            items(productList, key = { it.key.id }) { item ->
                ListItem(
                    product = item.key,
                    quantity = item.value,
                    onIncreaseQuantity = { onIncreaseQuantity(item.key) },
                    onDecreaseQuantity = { onDecreaseQuantity(item.key) },
                )
            }
        }
        val buttonText = if (buttonValue <= 0)
            stringResource(R.string.order_confirm_order_button)
        else
            stringResource(
                R.string.order_confirm_order_with_value_button,
                CurrencyValue(buttonValue).toString()
            )

        PrimaryButton(
            text = buttonText,
            onClick = onConfirmOrderButtonTapped,
            state = buttonState,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .onGloballyPositioned { listBottomPadding = it.size.height },
        )
    }
}

@Composable
private fun SearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val focusManager = LocalFocusManager.current

    val clearInput = {
        focusManager.clearFocus()
        onValueChange("")
    }

    TextField(
        value = value,
        onValueChange = onValueChange,
        shape = ShapeDefaults.Medium,
        label = {
            Text(
                text = stringResource(R.string.order_search_field_label),
                style = MaterialTheme.typography.bodySmall,
            )
        },
        trailingIcon = {
            when {
                trailingIcon != null -> trailingIcon()
                value.isNotEmpty() -> {
                    IconButton(
                        onClick = clearInput,
                        modifier = Modifier,
                        content = {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.order_search_field_clear_button),
                            )
                        }
                    )
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { focusManager.clearFocus() }
        ),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            errorContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun ClearOrderButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
        ),
        onClick = onClick,
        modifier = modifier,
    ) {
        Text(stringResource(id = R.string.order_clear_order_button))
    }
}

@Composable
private fun ListItem(
    product: Product,
    quantity: Int,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
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
                text = CurrencyValue(product.price).toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (quantity > 0) {
                ChangeQuantityButton(
                    image = Icons.Filled.RemoveCircle,
                    contentDescription = "-1",
                    onClick = onDecreaseQuantity,
                )
                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            ChangeQuantityButton(
                image = Icons.Filled.AddCircle,
                contentDescription = "+1",
                onClick = onIncreaseQuantity,
            )
        }
    }
}

@Composable
private fun ChangeQuantityButton(
    image: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            imageVector = image,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = contentDescription,
        )
    }
}

@Preview
@Composable
private fun OrderScreenContentPreview() {
    val products = mapOf(
        Product(1, "Irish Stout, 568ml", 600, "Beer") to 3,
        Product(2, "India Pale Ale (IPA), 330ml", 450, "Beer") to 1,
        Product(3, "Imperial Porter, 568ml", 650, "Beer") to 2,
    )
    CleventTheme {
        OrderScreenContent(
            products = products,
            categories = listOf("Beer" to false, "Food" to false),
            onIncreaseQuantity = { },
            onDecreaseQuantity = { },
            onConfirmOrderButtonTapped = { },
            onClearOrderButtonTapped = { },
        )
    }
}
