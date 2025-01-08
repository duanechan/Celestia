package com.coco.celestia.screens.coop.facility

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.navigation.NavController
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.model.ProductData
import java.util.Locale

//TODO: Add product ID, Format: PID-Year-Month-Day-Hours-Minutes-Seconds-Count
//TODO: Add Date and timestamp, timestamp para maiwasan natin ung race conditions

@Composable
fun CoopInventoryDetails(
    navController: NavController,
    productName: String,
    productViewModel: ProductViewModel,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit
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
                    ProductHeader(
                        product = productData.first(),
                        navController = navController,
                        productViewModel = productViewModel,
                        onEvent = onEvent
                    )
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
private fun ProductHeader(
    product: ProductData,
    navController: NavController,
    productViewModel: ProductViewModel,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(White1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "PID-YYYYMMDDHHMMSS-Count", //TODO: Add product ID, Format: Year-Month-Day-Hours-Minutes-Seconds-Count
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.semantics { testTag = "android:id/ProductOptionsButton" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                navController.navigate(Screen.EditProductInventory.createRoute(product.name))
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit"
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showDeleteConfirmation = true
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete"
                                )
                            }
                        )

                        DropdownMenuItem(
                            text = { Text(if (product.isActive) "Mark as Inactive" else "Mark as Active") },
                            onClick = {
                                productViewModel.updateActiveStatus(product.name, !product.isActive)
                                onEvent(
                                    Triple(
                                        ToastStatus.SUCCESSFUL,
                                        if (product.isActive) "Product marked as inactive" else "Product marked as active",
                                        System.currentTimeMillis()
                                    )
                                )
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (product.isActive) Icons.Default.Clear else Icons.Default.CheckCircle,
                                    contentDescription = if (product.isActive) "Mark as Inactive" else "Mark as Active"
                                )
                            }
                        )
                    }
                }
            }
            Text(
                text = "Date Added: Date and Timestamp", //TODO: Add Date and timestamp, timestamp para maiwasan natin ung race conditions
                style = MaterialTheme.typography.bodySmall,
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

        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete Product") },
                text = { Text("Are you sure you want to delete ${product.name}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            productViewModel.deleteProduct(product.name)
                            onEvent(
                                Triple(
                                    ToastStatus.SUCCESSFUL,
                                    "Product deleted successfully",
                                    System.currentTimeMillis()
                                )
                            )
                            showDeleteConfirmation = false
                            navController.popBackStack()
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteConfirmation = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
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
    val tabs = listOf("DETAILS", "TRANSACTIONS", "PRICE HISTORY")

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
            0 -> Details(product)
            1 -> TransactionsTab()
            2 -> HistoryTab()
        }
    }
}

@Composable
private fun Details(product: ProductData) {
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
            text = "Sales",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = "100", //TODO: Change to Total Quantity Sold
//            text = "PHP${product.price}",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "${product.weightUnit.lowercase()}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Total Amount",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = "PHP 1,000.00",
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
            text = "Purchases",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = "50",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "${product.weightUnit.lowercase()}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Total Cost of Purchases",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = "PHP${product.purchasingCost}", //TODO: Change to Total Purchases, not purchasing cost
            style = MaterialTheme.typography.titleLarge
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sample
            items(3) { index ->
                TransactionsCard()
            }
        }
    }
}

@Composable
fun TransactionsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White1),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SO Number / PO Number",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )
                Text(
                    text = "Date of Sale/Purchase",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quantity Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Price Per Unit: {put here}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Total Amount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total Quantity:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "+/- Quantity", //TODO: Pag Sales deducted ung quantity, pag Purchase added ung quantity
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

        }
    }
}

@Composable
private fun HistoryTab() {
    Box(
        modifier = Modifier.fillMaxSize().background(White2),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sample
            items(3) { index ->
                HistoryCard()
            }
        }
    }
}

@Composable
fun HistoryCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White1),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Date",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Green1
                )
                Text(
                    text = "Price per unit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quantity Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "January 8, 2025",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "PHP 23.00",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)