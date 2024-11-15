package com.coco.celestia.screens.farmer.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.coco.celestia.ui.theme.mintsansFontFamily
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.font.FontWeight.Companion.Medium
import com.coco.celestia.screens.farmer.dialogs.EditQuantityDialog
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FarmerItemViewModel
import com.coco.celestia.viewmodel.model.ItemData
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.util.UUID
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.model.TransactionData

@Composable
fun FarmerItemDetails(navController: NavController, productName: String) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    val farmerProductViewModel: ProductViewModel = viewModel()
    val farmerItemViewModel: FarmerItemViewModel = viewModel()
    val productData by farmerProductViewModel.productData.observeAsState(emptyList())
    val itemData by farmerItemViewModel.itemData.observeAsState(emptyList())
    val productState by farmerProductViewModel.productState.observeAsState(ProductState.LOADING)
    val orderViewModel: OrderViewModel = viewModel()
    val allOrders by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    var showEditDialog by remember { mutableStateOf(false) }
    var productQuantity by remember { mutableIntStateOf(0) }
    var productPricePerKg by remember { mutableDoubleStateOf(0.0) }
    var farmerName by remember { mutableStateOf("") }
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    val formattedDateTime = currentDateTime.format(formatter).toString()
    val transactionViewModel: TransactionViewModel = viewModel()
    val productDateAdded by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))) }

    LaunchedEffect(Unit) {
        farmerItemViewModel.getItems(uid = uid)
        if (productData.isEmpty()) {
            farmerProductViewModel.fetchProducts(filter = "", role = "Farmer")
        }
        orderViewModel.fetchAllOrders(filter = "", role = "Farmer")

        if (uid.isNotEmpty()) {
            farmerName = farmerItemViewModel.fetchFarmerName(uid)
        }
    }

    LaunchedEffect(productName) {
        farmerProductViewModel.fetchProducts(filter = "", role = "Farmer")
    }

    LaunchedEffect(itemData) {
        val availableItem = itemData.find { it.name.equals(productName, ignoreCase = true) }
        productQuantity = availableItem?.quantity ?: 0
        productPricePerKg = availableItem?.priceKg ?: 0.0
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BgColor)
            .verticalScroll(rememberScrollState())
            .semantics { testTag = "android:id/farmerItemsDetailsScreen" }
    ) {
        when (productState) {
            ProductState.LOADING -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .semantics { testTag = "android:id/loadingIndicator" }
                )
            }
            ProductState.SUCCESS -> {
                val product = productData.find { it.name.equals(productName, ignoreCase = true) }
                val selectedItemData = itemData.find { it.name.equals(productName, ignoreCase = true) }
                val selectedItemAsItemData = selectedItemData?.let {
                    ItemData(
                        name = it.name,
                        farmerName = farmerName,
                        items = listOf(it).toMutableList()
                    )
                }

                if (product != null && selectedItemAsItemData != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                    ) {
                        ProductDetailsCard(
                            itemData = selectedItemAsItemData,
                            productQuantity = productQuantity,
                            productPricePerKg = productPricePerKg,
                            onEditClick = { showEditDialog = true },
                            uid = uid,
                            farmerItemViewModel = farmerItemViewModel,
                            farmerName = farmerName,
                            productDateAdded = productDateAdded
                        )

                        when (orderState) {
                            OrderState.LOADING -> {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .semantics { testTag = "android:id/orderLoadingIndicator" }
                                )
                            }
                            OrderState.EMPTY -> {
                                Text(
                                    text = "No orders available for this product.",
                                    fontSize = 16.sp,
                                    color = Cocoa.copy(alpha = 0.8f),
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(16.dp)
                                        .semantics { testTag = "android:id/noOrdersText" }
                                )
                            }
                            OrderState.SUCCESS -> {
                                val filteredOrders = allOrders.filter {
                                    it.orderData.name.equals(productName, ignoreCase = true)
                                }

                                if (filteredOrders.isEmpty()) {
                                    Text(
                                        text = "No orders available for this product.",
                                        fontSize = 16.sp,
                                        color = Cocoa.copy(alpha = 0.8f),
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .padding(16.dp)
                                            .semantics { testTag = "noOrdersForProductText" }
                                    )
                                } else {
                                    OrderTable(orders = filteredOrders, farmerName = farmerName)
                                }
                            }
                            is OrderState.ERROR -> {
                                Text(
                                    text = "Error loading orders",
                                    fontSize = 16.sp,
                                    color = Color.Red,
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(16.dp)
                                        .semantics { testTag = "android:id/orderErrorText" }
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Product not found",
                        fontSize = 16.sp,
                        color = Color.Red,
                        fontWeight = Bold,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .semantics { testTag = "android:id/productNotFoundText" }
                    )
                }
            }
            ProductState.EMPTY -> {
                Text(
                    text = "No products available",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    fontWeight = Bold,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .semantics { testTag = "android:id/noProductsText" }
                )
            }
            is ProductState.ERROR -> {
                Text(
                    text = "Error loading products",
                    fontSize = 16.sp,
                    color = Color.Red,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .semantics { testTag = "android:id/productErrorText" }
                )
            }
        }
    }

    if (showEditDialog) {
        EditQuantityDialog(
            productName = productName,
            currentQuantity = productQuantity,
            currentPrice = productPricePerKg,
            onDismiss = { showEditDialog = false },
            onConfirm = { newQuantity, newPrice ->
                val quantityDifference = newQuantity - productQuantity
                farmerItemViewModel.updateItemQuantity(productName, quantityDifference)
                farmerItemViewModel.updateItemPrice(productName, newPrice)
                transactionViewModel.recordTransaction(
                    uid = uid,
                    transaction = TransactionData(
                        transactionId = "Transaction-${UUID.randomUUID()}",
                        type = "ProductUpdated",
                        date = formattedDateTime,
                        description = "$productName quantity updated to ${quantityDifference}kg."
                    )
                )
                transactionViewModel.recordTransaction(
                    uid = uid,
                    transaction = TransactionData(
                        transactionId = "Transaction-${UUID.randomUUID()}",
                        type = "ProductUpdated",
                        date = formattedDateTime,
                        description = "$productName price updated to ₱$newPrice."
                    )
                )
                showEditDialog = false
            }
        )
    }
}

@Composable
fun ProductDetailsCard(
    itemData: ItemData,
    productQuantity: Int,
    productPricePerKg: Double,
    onEditClick: () -> Unit,
    farmerItemViewModel: FarmerItemViewModel,
    uid: String,
    farmerName: String,
    modifier: Modifier = Modifier,
    productDateAdded: String
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        Card(
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "android:id/productCard_${itemData.name}" },
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Yellow4, Sand)
                        )
                    )
            ) {
                ProductDetailsContent(
                    itemData = itemData,
                    productQuantity = productQuantity,
                    productPricePerKg = productPricePerKg,
                    productDateAdded = productDateAdded,
                    onEditClick = onEditClick
                )
            }
        }
    }
}

@Composable
private fun ProductDetailsContent(
    itemData: ItemData,
    productQuantity: Int,
    productPricePerKg: Double,
    productDateAdded: String,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(start = 16.dp, top = 10.dp, end = 16.dp)
    ) {
        // Product Name
        Text(
            text = itemData.name,
            fontSize = 40.sp,
            fontWeight = Bold,
            textAlign = TextAlign.Center,
            color = Cocoa,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "android:id/productNameText" }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Product Added Date
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .background(Sand2, RoundedCornerShape(8.dp))
                .border(1.dp, Cocoa.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(vertical = 8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Date Added",
                    fontSize = 14.sp,
                    fontWeight = Medium,
                    color = Cocoa,
                    modifier = Modifier.semantics { testTag = "android:id/productAddedTitleText" }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = productDateAdded,
                    fontSize = 16.sp,
                    fontWeight = Bold,
                    color = Cocoa,
                    modifier = Modifier.semantics { testTag = "android:id/productAddedValueText" }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 4.dp)
                        .background(Sand2, RoundedCornerShape(8.dp))
                        .border(1.dp, Cocoa.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Quantity",
                            fontSize = 14.sp,
                            fontWeight = Medium,
                            color = Cocoa,
                            modifier = Modifier.semantics { testTag = "android:id/quantityTitleText" }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$productQuantity kg",
                            fontSize = 16.sp,
                            fontWeight = Bold,
                            color = Cocoa,
                            modifier = Modifier.semantics { testTag = "android:id/quantityValueText" }
                        )
                    }
                }

                // Price Box
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                        .background(Sand2, RoundedCornerShape(8.dp))
                        .border(1.dp, Cocoa.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Price",
                            fontSize = 14.sp,
                            fontWeight = Medium,
                            color = Cocoa,
                            modifier = Modifier.semantics { testTag = "android:id/priceTitleText" }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₱$productPricePerKg/kg",
                            fontSize = 16.sp,
                            fontWeight = Bold,
                            color = Cocoa,
                            modifier = Modifier.semantics { testTag = "android:id/priceValueText" }
                        )
                    }
                }
            }
        }

        // Actions
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Edit Button
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier
                        .size(35.dp)
                        .semantics { testTag = "android:id/editQuantityButton" }
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit Quantity",
                        tint = Cocoa
                    )
                }
            }
        }
    }
}

@Composable
fun OrderTable(
    orders: List<OrderData>,
    rowHeight: Dp = 80.dp,
    tableHeight: Dp = 450.dp,
    farmerName: String
) {
    val filteredOrders = orders.filter { order ->
        order.fulfilledBy.any { it.farmerName == farmerName }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp)
            .semantics { testTag = "android:id/orderTable" }
    ) {
        // Table Header
        Spacer(modifier = Modifier.height(35.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Sand)
                .padding(20.dp)
                .semantics { testTag = "android:id/orderTableHeader" }
        ) {
            Text(
                text = "Order ID",
                modifier = Modifier.weight(1f),
                fontWeight = Bold,
                fontFamily = mintsansFontFamily,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = Cocoa
            )
            Text(
                text = "Status",
                modifier = Modifier.weight(1f),
                fontWeight = Bold,
                fontFamily = mintsansFontFamily,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                color = Cocoa
            )
            Text(
                text = "Qty",
                modifier = Modifier.weight(1f),
                fontWeight = Bold,
                fontFamily = mintsansFontFamily,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
            color = Cocoa
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Sand2)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(tableHeight)
                .verticalScroll(rememberScrollState())
                .semantics { testTag = "android:id/orderList" }
                .background(SoftOrange)  // Added background here
        ) {
            if (filteredOrders.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    filteredOrders.forEach { order ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(50.dp)
                                .semantics { testTag = "android:id/orderRow_${order.orderId}" }
                        ) {
                            Text(
                                text = order.orderId.substring(6, 10),
                                modifier = Modifier.weight(1f),
                                fontFamily = mintsansFontFamily,
                                textAlign = TextAlign.Left,
                                fontWeight = Bold,
                                color = Cocoa
                            )
                            Text(
                                text = order.status,
                                modifier = Modifier.weight(1f),
                                fontFamily = mintsansFontFamily,
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp,
                                fontWeight = Bold,
                                color = Cocoa
                            )
                            Text(
                                text = order.orderData.quantity.toString(),
                                modifier = Modifier.weight(1f),
                                fontFamily = mintsansFontFamily,
                                textAlign = TextAlign.Right,
                                fontWeight = Bold,
                                color = Cocoa
                            )
                        }
                        Divider(
                            color = Sand2,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    val totalRows = (tableHeight / rowHeight).toInt()
                    val blankRows = totalRows - filteredOrders.size

                    repeat(blankRows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(50.dp)
                                .semantics { testTag = "android:id/blankRow_$it" }
                        ) {
                            Text(
                                text = "---",
                                color = Cocoa.copy(alpha = 0.7f),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Left
                            )
                            Text(
                                text = "---",
                                color = Cocoa.copy(alpha = 0.7f),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "---",
                                color = Cocoa.copy(alpha = 0.7f),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Right
                            )
                        }
                        Divider(
                            color = Sand2,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No orders available",
                        modifier = Modifier
                            .padding(10.dp)
                            .semantics { testTag = "android:id/noOrdersAvailableText" },
                        textAlign = TextAlign.Center,
                        color = Cocoa.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

