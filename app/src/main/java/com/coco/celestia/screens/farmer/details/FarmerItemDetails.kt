package com.coco.celestia.screens.farmer.details

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import com.coco.celestia.screens.farmer.dialogs.EditQuantityDialog
import com.coco.celestia.ui.theme.*

@Composable
fun FarmerItemDetails(navController: NavController, productName: String) {
    val farmerProductViewModel: ProductViewModel = viewModel()
    val productData by farmerProductViewModel.productData.observeAsState(emptyList())
    val productState by farmerProductViewModel.productState.observeAsState(ProductState.LOADING)
    val orderViewModel: OrderViewModel = viewModel()
    val allOrders by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    var showEditDialog by remember { mutableStateOf(false) }
    var productQuantity by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        if (productData.isEmpty()) {
            farmerProductViewModel.fetchProducts(
                filter = "",
                role = "Farmer"
            )
        }
        orderViewModel.fetchAllOrders(
            filter = "",
            role = "Farmer"
        )
    }

    // Fetch updated product data whenever the productName changes
    LaunchedEffect(productName) {
        farmerProductViewModel.fetchProducts(
            filter = "",
            role = "Farmer"
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = BgColor)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .semantics { testTag = "android:id/farmerItemsDetailsScreen" }
        ) {
            when (productState) {
                ProductState.LOADING -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                        .align(Alignment.Center)
                        .semantics { testTag = "android:id/loadingIncidator" })
                }
                ProductState.SUCCESS -> {
                    val product = productData.find { it.name.equals(productName, ignoreCase = true) }
                    if (product != null) {
                        productQuantity = product.quantity
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(top = 30.dp)
                        ) {
                            Spacer(modifier = Modifier.height(20.dp))

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(270.dp)
                                    .semantics { testTag = "android:id/productCard_${product.name}," }
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
                                    Row(
                                        modifier = Modifier
                                            .padding(top = 60.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = product.name,
                                                fontSize = 60.sp,
                                                fontWeight = Bold,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .semantics { testTag = "android:id/productNameText" },
                                                textAlign = TextAlign.Center,
                                                color = Cocoa
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = "Quantity: $productQuantity kg",
                                                    fontSize = 20.sp,
                                                    textAlign = TextAlign.Center,
                                                    color = Cocoa,
                                                    modifier = Modifier.semantics { testTag = "android:id/productQuantityText" }
                                                )

                                                Spacer(modifier = Modifier.width(8.dp))

                                                IconButton(
                                                    onClick = { showEditDialog = true },
                                                    modifier = Modifier
                                                        .size(24.dp)
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
                            }

                            when (orderState) {
                                OrderState.LOADING -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .padding(top = 20.dp)
                                            .semantics { testTag = "android:id/orderLoadingIndicator" }
                                    )
                                }
                                OrderState.EMPTY -> {
                                    Text(
                                        text = "No orders available for this product.",
                                        fontSize = 16.sp,
                                        color = Color.Gray,
                                        modifier = Modifier
                                            .align(Alignment.CenterHorizontally)
                                            .padding(top = 20.dp)
                                            .semantics { testTag = "android:id/noOrdersText" }
                                    )
                                }
                                OrderState.SUCCESS -> {
                                    val filteredOrders = allOrders.filter { it.orderData.name.equals(productName, ignoreCase = true) }

                                    if (filteredOrders.isEmpty()) {
                                        Text(
                                            text = "No orders available for this product.",
                                            fontSize = 16.sp,
                                            color = Color.Gray,
                                            modifier = Modifier
                                                .align(Alignment.CenterHorizontally)
                                                .padding(top = 20.dp)
                                                .semantics { testTag = "noOrdersForProductText" }
                                        )
                                    } else {
                                        OrderTable(orders = filteredOrders)
                                    }
                                }
                                is OrderState.ERROR -> TODO()
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
                            .semantics { testTag = "noProductsText" }
                    )
                }
                is ProductState.ERROR -> {
                    Text(
                        text = "Error loading products",
                        fontSize = 16.sp,
                        fontWeight = Bold,
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .semantics { testTag = "android:id/errorLoadingProductsText" }
                    )
                }
            }
        }

        // Edit Quantity Dialog
        if (showEditDialog) {
            EditQuantityDialog(
                productName = productName,
                currentQuantity = productQuantity,
                onDismiss = { showEditDialog = false },
                onConfirm = { newQuantity ->
                    farmerProductViewModel.updateProductQuantity(productName, newQuantity - productQuantity)
                    productQuantity = newQuantity
                    showEditDialog = false
                }
            )
        }
    }
}

@Composable
fun OrderTable(orders: List<OrderData>, rowHeight: Dp = 80.dp, tableHeight: Dp = 450.dp) {
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

        // Divider
        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Gray)
        )

        // Table with orders
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(tableHeight)
                .verticalScroll(rememberScrollState())
                .semantics { testTag = "android:id/orderList" }
        ) {
            Column {
                if (orders.isNotEmpty()) {
                    orders.forEach { order ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = SoftOrange)
                                .padding(50.dp)
                                .semantics { testTag = "android:id/orderRow_${order.orderId}" }
                        ) {
                            Text(
                                text = order.orderId.substring(5, 9),
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
                            color = Color.White,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    val totalRows = (tableHeight / rowHeight).toInt()
                    val blankRows = totalRows - orders.size

                    repeat(blankRows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = SoftOrange)
                                .padding(50.dp)
                                .semantics { testTag = "android:id/blankRow_$it" }
                        ) {
                            Text(
                                text = "---",
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Left
                            )
                            Text(
                                text = "---",
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "---",
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Right
                            )
                        }
                        Divider(
                            color = Color.White,
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Text(
                        text = "No orders available",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .semantics { testTag = "android:id/noOrdersAvailableText" },
                        textAlign = TextAlign.Center,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}