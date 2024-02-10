package dev.scavazzini.clevent.order

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
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import dev.scavazzini.clevent.ui.components.PrimaryButton

@Composable
fun OrderScreen(
    products: Map<Product, Int>,
    categories: List<String>,
    selectedCategory: Int,
    modifier: Modifier = Modifier,
) {
    OrderScreenContent(
        products = products,
        categories = categories,
        selectedCategory = selectedCategory,
        onConfirmOrderButtonTapped = { },
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun OrderScreenContent(
    products: Map<Product, Int>,
    categories: List<String>,
    selectedCategory: Int,
    onConfirmOrderButtonTapped: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        CategoryTabs(
            categories = categories,
            selected = selectedCategory,
            onSelectedCategoryChange = { },
        )
        ProductList(
            products = products,
            onConfirmOrderButtonTapped = onConfirmOrderButtonTapped,
            modifier = Modifier.weight(1f),
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
    onConfirmOrderButtonTapped: () -> Unit,
    modifier: Modifier = Modifier,
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
                ListItem(item.key)
            }
        }
        PrimaryButton(
            text = stringResource(R.string.order_confirm_order_button),
            onClick = onConfirmOrderButtonTapped,
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
            ChangeQuantityButton(
                image = Icons.Filled.RemoveCircle,
                contentDescription = "-1",
                onClick = { },
            )
            Text(
                text = 0.toString(),
                fontSize = 16.sp,
            )
            ChangeQuantityButton(
                image = Icons.Filled.AddCircle,
                contentDescription = "+1",
                onClick = { },
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

@Preview
@Composable
private fun OrderScreenContentPreview() {
    val products = mapOf(
        Product(1, "Irish Stout, 568ml", 600) to 3,
        Product(2, "India Pale Ale (IPA), 330ml", 450) to 1,
        Product(3, "Imperial Porter, 568ml", 650) to 2,
    )
    OrderScreenContent(
        products = products,
        categories = listOf("All", "Beer", "Food"),
        selectedCategory = 0,
        onConfirmOrderButtonTapped = { },
    )
}
