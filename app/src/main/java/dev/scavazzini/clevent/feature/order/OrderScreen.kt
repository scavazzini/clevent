package dev.scavazzini.clevent.feature.order

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SheetState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.scavazzini.clevent.R
import dev.scavazzini.clevent.data.models.CurrencyValue
import dev.scavazzini.clevent.data.models.Product
import dev.scavazzini.clevent.ui.OnNewIntentHandler
import dev.scavazzini.clevent.ui.components.BottomSheetProductListContent
import dev.scavazzini.clevent.ui.components.NfcModalBottomSheet
import dev.scavazzini.clevent.ui.components.PrimaryButton
import dev.scavazzini.clevent.ui.components.PrimaryButtonState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    viewModel: OrderViewModel,
    modifier: Modifier = Modifier,
) {
    OnNewIntentHandler { viewModel.performPurchase(it) }

    val state by viewModel.orderUiState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val products = viewModel.products

    var selectedCategory by remember(categories) { mutableIntStateOf(0) }

    OrderScreenContent(
        products = products,
        categories = categories,
        selectedCategory = selectedCategory,
        onIncreaseQuantity = viewModel::increase,
        onDecreaseQuantity = viewModel::decrease,
        onConfirmOrderButtonTapped = viewModel::confirmOrder,
        modifier = modifier.fillMaxWidth(),
        state = state,
        onCategoryClick = { selectedCategory = it },
        onDismiss = viewModel::cancelOrder,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderScreenContent(
    products: Map<Product, Int>,
    categories: List<String>,
    selectedCategory: Int,
    onIncreaseQuantity: (product: Product) -> Unit,
    onDecreaseQuantity: (product: Product) -> Unit,
    onConfirmOrderButtonTapped: () -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = { },
    state: OrderViewModel.OrderUiState = OrderViewModel.OrderUiState(),
    sheetState: SheetState = rememberModalBottomSheetState(),
    onCategoryClick: (Int) -> Unit = { },
) {
    Column(modifier.fillMaxWidth()) {
        CategoryTabs(
            categories = categories,
            selected = selectedCategory,
            onSelectedCategoryChange = onCategoryClick,
        )
        ProductList(
            products = products,
            category = categories[selectedCategory],
            onIncreaseQuantity = onIncreaseQuantity,
            onDecreaseQuantity = onDecreaseQuantity,
            onConfirmOrderButtonTapped = onConfirmOrderButtonTapped,
            buttonState = state.confirmOrderButtonState,
            buttonValue = state.orderValue,
            modifier = Modifier.weight(1f),
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
            content = { BottomSheetProductListContent(products.filter { it.value > 0 }) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryTabs(
    categories: List<String>,
    selected: Int,
    onSelectedCategoryChange: (selected: Int) -> Unit,
) {
    require(categories.isNotEmpty()) {
        "Categories must not be empty."
    }

    require(categories.indices.contains(selected)) {
        "Selected parameter must be within categories range."
    }

    SecondaryTabRow(
        selectedTabIndex = selected,
        containerColor = Color.White,
    ) {
        categories.forEachIndexed { index, category ->
            val isSelected = index == selected

            Tab(
                selected = isSelected,
                onClick = {
                    if (selected != index) {
                        onSelectedCategoryChange(index)
                    }
                },
            ) {
                Text(
                    text = category,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun ProductList(
    products: Map<Product, Int>,
    category: String,
    onIncreaseQuantity: (product: Product) -> Unit,
    onDecreaseQuantity: (product: Product) -> Unit,
    onConfirmOrderButtonTapped: () -> Unit,
    modifier: Modifier = Modifier,
    buttonState: PrimaryButtonState = PrimaryButtonState.ENABLED,
    buttonValue: Int = 0,
) {
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
            items(products.entries.toList()) { item ->
                if (item.key.category == category || category == "All") {
                    ListItem(
                        product = item.key,
                        quantity = item.value,
                        onIncreaseQuantity = { onIncreaseQuantity(item.key) },
                        onDecreaseQuantity = { onDecreaseQuantity(item.key) },
                    )
                }
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
                fontSize = 16.sp,
            )
            Text(
                text = CurrencyValue(product.price).toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xff3EB17A),
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
                    fontSize = 16.sp,
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
            contentDescription = contentDescription,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun OrderScreenContentPreview() {
    val products = mapOf(
        Product(1, "Irish Stout, 568ml", 600, "Beer") to 3,
        Product(2, "India Pale Ale (IPA), 330ml", 450, "Beer") to 1,
        Product(3, "Imperial Porter, 568ml", 650, "Beer") to 2,
    )
    OrderScreenContent(
        products = products,
        categories = listOf("All", "Beer", "Food"),
        selectedCategory = 0,
        onIncreaseQuantity = { },
        onDecreaseQuantity = { },
        onConfirmOrderButtonTapped = { },
    )
}
