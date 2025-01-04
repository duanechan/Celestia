package com.coco.celestia.screens.coop.facility

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.model.ProductData
import java.util.Locale

@Composable
fun CoopInventoryDetails(
    productName: String,
    productViewModel: ProductViewModel
) {
    val productState by productViewModel.productState.observeAsState()
    val productData by productViewModel.productData.observeAsState(emptyList())

    LaunchedEffect(productName) {
        productViewModel.fetchProduct(productName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White1)
    ) {
        when (productState) {
            is ProductState.SUCCESS -> {
                if (productData.isNotEmpty()) {
                    ProductHeader(product = productData.first())
                    ProductTabs(product = productData.first())
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(White1),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Product not found")
                    }
                }
            }
            is ProductState.EMPTY -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Product not found")
                }
            }
            is ProductState.ERROR -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (productState as ProductState.ERROR).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            ProductState.LOADING -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun ProductHeader(product: ProductData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(White1)
    ) {
        Text(
            text = product.name.split(" ").joinToString(" ") { it.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            } },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PriceInfoColumn(
                title = "Selling Price",
                price = product.price,
                weightUnit = product.weightUnit
            )

            PriceInfoColumn(
                title = "Purchase Cost",
                price = product.purchasingCost,
                weightUnit = product.weightUnit
            )

            // placeholder for now for adding image idk how to do this
            Card(
                modifier = Modifier.size(100.dp),
                colors = CardDefaults.cardColors(containerColor = Green4)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("+ Add\nImage", textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun PriceInfoColumn(
    title: String,
    price: Double,
    weightUnit: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "PHP${price.format(2)}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "per ${weightUnit.lowercase()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProductTabs(product: ProductData) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("DETAILS", "TRANSACTIONS", "HISTORY")

    Column {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = White1,
            contentColor = Green1,
            indicator = { tabPositions ->
                Box(
                    Modifier
                        .tabIndicatorOffset(tabPositions[selectedTab])
                        .height(3.dp)
                        .background(Green1)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == index) Green1 else Color.Gray
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> DetailsTab(product)
            1 -> TransactionsTab()
            2 -> HistoryTab()
        }
    }
}

@Composable
private fun DetailsTab(product: ProductData) {
    val isProductInStore = remember(product) {
        product.isInStore
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize() // Use fillMaxSize to ensure the LazyColumn covers the entire screen
            .background(White2) // Match the background color with the card's containerColor
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Stock Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White1)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Green1
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Stock Summary",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    StockSummaryTable(product)
                }
            }
        }

        // Stock Status Card - for online products only
        if (!isProductInStore) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = White1)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Green1
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Stock Status",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        StockStatusGrid()
                    }
                }
            }
        }

        // Sales & Purchase Information Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White1)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Green1
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sales & Purchase Information",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Sales Information
                        Column(modifier = Modifier.weight(1f)) {
                            SalesInfoSection(product)
                        }

                        Divider(
                            modifier = Modifier
                                .width(1.dp)
                                .height(200.dp),
                            color = Color.LightGray
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            PurchaseInfoSection(product)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun StockSummaryTable(product: ProductData) {
    Column(modifier = Modifier.fillMaxWidth()) {
        StockSummaryRow("Current Stock",
            value = product.quantity.toString(),
            unit = product.weightUnit.lowercase()
        )
        StockSummaryRow("Opening Stock",
            value = product.openingStock.toString(),
            unit = product.weightUnit.lowercase()
        )
    }
}

@Composable
private fun StockSummaryRow(label: String, value: String, unit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White1)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$value $unit",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun StockStatusGrid() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StockStatusItem(
                quantity = "0",
                label = "To be Shipped",
                modifier = Modifier.weight(1f)
            )
            StockStatusItem(
                quantity = "0",
                label = "To be Received",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StockStatusItem(
    quantity: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "$quantity Qty",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
private fun SalesInfoSection(product: ProductData) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Selling Price",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = "PHP${product.price}",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "per ${product.weightUnit.lowercase()}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Description",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = product.description,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun PurchaseInfoSection(product: ProductData) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Purchase Cost",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = "PHP${product.purchasingCost}",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "per ${product.weightUnit.lowercase()}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Preferred Vendor",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = product.vendor,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun TransactionsTab() {
    Box(
        modifier = Modifier.fillMaxSize().background(White2),
        contentAlignment = Alignment.Center
    ) {
        Text("Transactions Coming Soon")
    }
}

@Composable
private fun HistoryTab() {
    Box(
        modifier = Modifier.fillMaxSize().background(White2),
        contentAlignment = Alignment.Center
    ) {
        Text("History Coming Soon")
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)