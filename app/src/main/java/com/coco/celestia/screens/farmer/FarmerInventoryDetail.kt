package com.coco.celestia.screens.farmer

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
import com.coco.celestia.screens.farmer.dialogs.EditQuantityDialog

@Composable
fun FarmerInventoryDetail(navController: NavController, productName: String) {
    val farmerProductViewModel: ProductViewModel = viewModel()
    val productData by farmerProductViewModel.productData.observeAsState(emptyList())
    val productState by farmerProductViewModel.productState.observeAsState(ProductState.LOADING)
    val orderViewModel: OrderViewModel = viewModel()
    val allOrders by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    var showEditDialog by remember { mutableStateOf(false) } // State to show/hide dialog
    var productQuantity by remember { mutableStateOf(0) } // State to store the product's current quantity
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
        snackbarHost = { SnackbarHost(snackbarHostState) } // Pass the snackbarHostState here
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2E3DB))
                .verticalScroll(rememberScrollState())
                .padding(paddingValues) // Padding to avoid overlap
        ) {
            when (productState) {
                ProductState.LOADING -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                ProductState.SUCCESS -> {
                    val product = productData.find { it.name.equals(productName, ignoreCase = true) }
                    if (product != null) {
                        productQuantity = product.quantity // Set the initial quantity here
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
                                    .height(270.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFFE0A83B),
                                                    Color(0xFF7A5C20)
                                                )
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
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "Quantity: $productQuantity kg",
                                                fontSize = 20.sp,
                                                textAlign = TextAlign.Center,
                                                color = Color.White
                                            )
                                        }

                                        IconButton(
                                            onClick = { showEditDialog = true },
                                            modifier = Modifier
                                                .padding(top = 95.dp)
                                        ) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Edit Quantity", tint = Color.White)
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
                                    )
                                }
                                OrderState.SUCCESS -> {
                                    val filteredOrders = allOrders.filter {
                                        it.orderData.any { product -> product.name.equals(productName, ignoreCase = true) }
                                    }

                                    if (filteredOrders.isEmpty()) {
                                        Text(
                                            text = "No orders available for this product.",
                                            fontSize = 16.sp,
                                            color = Color.Gray,
                                            modifier = Modifier
                                                .align(Alignment.CenterHorizontally)
                                                .padding(top = 20.dp)
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
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                ProductState.EMPTY -> {
                    Text(
                        text = "No products available",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ProductState.ERROR -> {
                    Text(
                        text = "Error loading products",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
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
                    // Update the product quantity in the ViewModel
                    farmerProductViewModel.updateProductQuantity(productName, newQuantity)
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
    ) {
        // Table Header
        Spacer(modifier = Modifier.height(35.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF957541))
                .padding(20.dp)
        ) {
            Text(
                text = "Order ID",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                fontFamily = mintsansFontFamily,
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
            Text(
                text = "Status",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                fontFamily = mintsansFontFamily,
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
            Text(
                text = "Qty",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                fontFamily = mintsansFontFamily,
                textAlign = TextAlign.Center,
                fontSize = 16.sp
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
        ) {
            Column {
                if (orders.isNotEmpty()) {
                    orders.forEach { order ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFDAAC63))
                                .padding(50.dp)
                        ) {
                            Text(
                                text = order.orderId.substring(5, 9),
                                modifier = Modifier.weight(1f),
                                fontFamily = mintsansFontFamily,
                                textAlign = TextAlign.Left
                            )
                            Text(
                                text = order.status,
                                modifier = Modifier.weight(1f),
                                fontFamily = mintsansFontFamily,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = order.orderData[0].quantity.toString(),
                                modifier = Modifier.weight(1f),
                                fontFamily = mintsansFontFamily,
                                textAlign = TextAlign.Right
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
                                .background(Color(0xFFDAAC63))
                                .padding(50.dp)
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
                            .padding(8.dp),
                        textAlign = TextAlign.Center,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}




