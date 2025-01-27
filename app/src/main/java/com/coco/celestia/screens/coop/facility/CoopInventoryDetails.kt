package com.coco.celestia.screens.coop.facility

import android.annotation.SuppressLint
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.TransactionState
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.model.Constants
import com.coco.celestia.viewmodel.model.FacilityData
import com.coco.celestia.viewmodel.model.PriceUpdate
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.TransactionData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CoopInventoryDetails(
    navController: NavController,
    productName: String,
    productViewModel: ProductViewModel,
    transactionViewModel: TransactionViewModel,
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
                    ProductTabs(product = productData.first(), transactionViewModel = transactionViewModel)
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

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun ProductHeader(
    product: ProductData,
    navController: NavController,
    productViewModel: ProductViewModel,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var productImage by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(product.productId) {
        isLoading = true
        ImageService.fetchProductImage(product.productId) { uri ->
            productImage = uri
            isLoading = false
        }

        onDispose { }
    }

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
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
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
                                navController.navigate(Screen.EditProductInventory.createRoute(product.productId))
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
                                productViewModel.updateActiveStatus(product.productId, !product.isActive)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = product.productId,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )

                Text(
                    text = product.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = mintsansFontFamily
                )
            }

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
                    price = product.totalPurchases,
                    weightUnit = product.weightUnit
                )

                Card(
                    modifier = Modifier.size(100.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading || productImage == null) {
                            Image(
                                painter = painterResource(R.drawable.product_icon),
                                contentDescription = "Loading",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = rememberImagePainter(productImage),
                                contentDescription = product.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Delete Product") },
                text = { Text("Are you sure you want to delete ${product.name}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            productViewModel.deleteProduct(product.productId)
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
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            fontFamily = mintsansFontFamily
        )
        Text(
            text = "per ${weightUnit.lowercase()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            fontFamily = mintsansFontFamily
        )
    }
}

@Composable
private fun ProductTabs(
    product: ProductData,
    transactionViewModel: TransactionViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("DETAILS", "TRANSACTIONS", "PRICE HISTORY")

    Log.d("ProductTabs", "Product: ${product.productId}, PriceHistory: ${product.priceHistory}")

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
                            color = if (selectedTab == index) Green1 else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = mintsansFontFamily
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> Details(product)
            1 -> TransactionsTab(
                transactionViewModel = transactionViewModel,
                productId = product.productId
            )
            2 -> HistoryTab(product)
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
            .fillMaxSize()
            .background(White2)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Description Card
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
                            text = "Description",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = mintsansFontFamily
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = product.description,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = mintsansFontFamily
                        )
                    }
                }
            }
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
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontFamily = mintsansFontFamily
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
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = mintsansFontFamily
                            )
                        }

                        StockStatusGrid(product)
                    }
                }
            }
        }

        // Collection Method Card - for online products only
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f) // Allocates space for this group
                            ){
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Green1
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Collection Method",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = mintsansFontFamily
                                )
                            }
                            Text(
                                text = "Available In",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = mintsansFontFamily
                            )
                        }


                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Pick Up",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = mintsansFontFamily
                            )
                            Text(
                                text = "Pick Up Location here", //TODO: PLACEHOLDER
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = mintsansFontFamily
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Delivery",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = mintsansFontFamily
                            )
                            Text(
                                text = "Couriers here or etc", //TODO: PLACEHOLDER
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = mintsansFontFamily
                            )
                        }

                    }
                }
            }
        }

        // Payment Method Card - for online products only
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f) // Allocates space for this group
                            ){
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Green1
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Payment Method",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = mintsansFontFamily
                                )
                            }
                            Text(
                                text = "Available In",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = mintsansFontFamily
                            )
                        }


                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Cash",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = mintsansFontFamily
                            )
                            Text(
                                text = "Instruction here or blank", //TODO: PLACEHOLDER
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = mintsansFontFamily
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "G-Cash",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontFamily = mintsansFontFamily
                            )
                            Text(
                                text = "G-Cash Number/s here", //TODO: PLACEHOLDER
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = mintsansFontFamily
                            )
                        }

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
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = mintsansFontFamily
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

        // Notes Card
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
                            text = "Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontFamily = mintsansFontFamily
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = product.notes,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = mintsansFontFamily
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }

    }
}

// TODO: USE THESE LATER
@Composable
private fun CollectionMethodCard(facilityData: FacilityData) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (facilityData.isPickupEnabled) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = Constants.COLLECTION_PICKUP,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
                Text(
                    text = facilityData.pickupLocation,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = mintsansFontFamily
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (facilityData.isDeliveryEnabled) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = Constants.COLLECTION_DELIVERY,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
                Text(
                    text = facilityData.deliveryDetails,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = mintsansFontFamily
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodCard(facilityData: FacilityData) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (facilityData.isCashEnabled) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = Constants.PAYMENT_CASH,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
                Text(
                    text = facilityData.cashInstructions,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = mintsansFontFamily
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (facilityData.isGcashEnabled) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = Constants.PAYMENT_GCASH,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
                Text(
                    text = facilityData.gcashNumbers,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = mintsansFontFamily
                )
            }
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
        if (!product.isInStore) {
            StockSummaryRow(
                label = "Committed Stock",
                value = product.committedStock.toString(),
                unit = product.weightUnit.lowercase()
            )
        }

        StockSummaryRow("Reorder Point",
            value = product.reorderPoint.toString(),
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
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            fontFamily = mintsansFontFamily
        )
        Text(
            text = "$value $unit",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            fontFamily = mintsansFontFamily
        )
    }
}

@Composable
private fun StockStatusGrid(product: ProductData) {
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
                quantity = product.committedStock.toInt().toString(),
                label = "Ordered Stock",
                modifier = Modifier.weight(1f)
            )
            StockStatusItem(
                quantity = getStockStatus(product),
                label = "Stock Status",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun getStockStatus(product: ProductData): String {
    val availableStock = product.quantity - product.committedStock
    return if (availableStock > product.reorderPoint) {
        "Sufficient"
    } else {
        "Low"
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
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            fontFamily = mintsansFontFamily
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            fontFamily = mintsansFontFamily
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
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            fontFamily = mintsansFontFamily
        )
        Text(
            text = product.totalQuantitySold.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontFamily = mintsansFontFamily
        )
        Text(
            text = product.weightUnit.lowercase(),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontFamily = mintsansFontFamily
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Total Amount",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            fontFamily = mintsansFontFamily
        )
        Text(
            text = "PHP 1,000.00", //TODO: PLACEHOLDER
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = mintsansFontFamily
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
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            fontFamily = mintsansFontFamily
        )
        Text(
            text = "50", //TODO: PLACEHOLDER
            style = MaterialTheme.typography.titleLarge,
            fontFamily = mintsansFontFamily
        )
        Text(
            text = product.weightUnit.lowercase(),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            fontFamily = mintsansFontFamily
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Total Cost of Purchases",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            fontFamily = mintsansFontFamily
        )
        Text(
            text = "PHP${product.totalPurchases}",
            style = MaterialTheme.typography.titleLarge,
            fontFamily = mintsansFontFamily
        )
    }
}

@Composable
fun TransactionsTab(
    transactionViewModel: TransactionViewModel,
    productId: String
) {
    val transactionState by transactionViewModel.transactionState.observeAsState(TransactionState.LOADING)
    val transactionData by transactionViewModel.transactionData.observeAsState(hashMapOf())

    LaunchedEffect(productId) {
        transactionViewModel.fetchAllTransactions(filter = productId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White2),
        contentAlignment = Alignment.Center
    ) {
        when (val state = transactionState) {
            TransactionState.LOADING -> {
                CircularProgressIndicator()
            }
            is TransactionState.ERROR -> {
                Text(
                    text = state.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            TransactionState.EMPTY -> {
                Text(
                    text = "No transactions found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TransactionState.SUCCESS -> {
                val productTransactions = transactionData.values.flatten()
                    .filter { it.productId == productId }

                if (productTransactions.isEmpty()) {
                    Text(
                        text = "No transactions found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(productTransactions) { transaction ->
                            TransactionsCard(transaction)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionsCard(transaction: TransactionData) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = transaction.transactionId,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1,
                    fontFamily = mintsansFontFamily
                )
                Text(
                    text = transaction.date,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = mintsansFontFamily
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier.size(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(
                                id = when (transaction.type) {
                                    "Online Sale", "In-Store Sale" -> R.drawable.subtract
                                    else -> R.drawable.addition
                                }
                            ),
                            contentDescription = transaction.type,
                            modifier = Modifier.size(
                                when (transaction.type) {
                                    "Online Sale", "In-Store Sale" -> 14.dp
                                    else -> 16.dp
                                }
                            ),
                            contentScale = ContentScale.Fit,
                            colorFilter = ColorFilter.tint(
                                when (transaction.type) {
                                    "Online Sale", "In-Store Sale" -> MaterialTheme.colorScheme.error
                                    else -> Green2
                                }
                            )
                        )
                    }
                    Text(
                        text = transaction.type,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = mintsansFontFamily
                    )
                }
                Text(
                    text = transaction.status,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (transaction.status) {
                        "Completed", "COMPLETED" -> Green1
                        "Failed" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontFamily = mintsansFontFamily
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = transaction.productName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (transaction.description.isNotEmpty()) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = mintsansFontFamily
                )
            }
        }
    }
}

@Composable
private fun HistoryTab(product: ProductData) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White2),
        contentAlignment = Alignment.Center
    ) {
        if (product.priceHistory.isNullOrEmpty()) {
            Text(
                text = "No price history available",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val sortedHistory = remember(product.priceHistory) {
                product.priceHistory.sortedWith(compareByDescending<PriceUpdate> { priceUpdate ->
                    try {
                        LocalDateTime.parse(
                            priceUpdate.dateTime,
                            DateTimeFormatter.ofPattern("MMMM d, yyyy h:mma")
                        )
                    } catch (e: Exception) {
                        LocalDateTime.MIN
                    }
                }.thenByDescending {
                    if (it.previousPrice > 0.0) it.previousPrice else Double.MIN_VALUE
                })
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = sortedHistory,
                    key = { "${it.dateTime}_${it.price}_${it.previousPrice}" }
                ) { priceUpdate ->
                    HistoryCard(priceUpdate, product.weightUnit)
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun HistoryCard(priceUpdate: PriceUpdate, weightUnit: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White1),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PHP ${String.format("%.2f", priceUpdate.price)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        fontFamily = mintsansFontFamily
                    )
                    if (priceUpdate.previousPrice > 0.0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Previous: PHP ${String.format("%.2f", priceUpdate.previousPrice)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontFamily = mintsansFontFamily
                        )
                    }
                }
                Text(
                    text = "per ${weightUnit.lowercase()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = priceUpdate.dateTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontFamily = mintsansFontFamily
                )
                Text(
                    text = "Updated by: ${priceUpdate.updatedBy}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontFamily = mintsansFontFamily
                )
            }
        }
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)