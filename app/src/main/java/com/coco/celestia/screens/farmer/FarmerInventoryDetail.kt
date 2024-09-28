package com.coco.celestia.screens.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import com.coco.celestia.viewmodel.OrderData
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel

@Composable
fun FarmerInventoryDetail(navController: NavController, productName: String) {

    val farmerProductViewModel: ProductViewModel = viewModel()
    val productData by farmerProductViewModel.productData.observeAsState(emptyList())
    val productState by farmerProductViewModel.productState.observeAsState(ProductState.LOADING)
    val orderViewModel: OrderViewModel = viewModel()
    val allOrders by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)

    LaunchedEffect(Unit) {
        if (productData.isEmpty()) {
            farmerProductViewModel.fetchProducts(
                filter = "",
                role = "Farmer"
            )
        }
        // Fetch all orders; adjust parameters as needed
        orderViewModel.fetchAllOrders(
            filter = "",       // No filter initially
            isPending = false, // Fetch all statuses
            role = "Farmer"
        )
    }

    // Main container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2E3DB))
            .verticalScroll(rememberScrollState()) // Make the content scrollable
    ) {
        when (productState) {
            ProductState.LOADING -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            ProductState.SUCCESS -> {
                // Find the product by name
                val product = productData.find { it.name.equals(productName, ignoreCase = true) }
                if (product != null) {
                    // Display the inventory details for the found product
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp) // Add padding for better layout
                    ) {
                        Spacer(modifier = Modifier.height(70.dp))
                        Text(
                            text = "Product Name: ${product.name}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Quantity: ${product.quantity}",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Type: ${product.type}",
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(30.dp)) // Space before the table

                        // Handle different states of order data
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
                                // Filter orders for the specific product
                                val filteredOrders = allOrders.filter {
                                    it.orderData.name.equals(productName, ignoreCase = true)
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
                    // Product is not found
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
                // Handle empty state
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
}

@Composable
fun OrderTable(orders: List<OrderData>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 1.dp)
    ) {
        // Table Header
        Spacer(modifier = Modifier.height(100.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF957541))
                .padding(8.dp)
        ) {
            Text(
                text = "Order ID",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
            Text(
                text = "Status",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
            Text(
                text = "Qty",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 16.sp
            )
        }

        // Divider
        Spacer(modifier = Modifier.height(5.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Gray)
        )

        // Table Body with fixed height and scrollable content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp) // Set the fixed height of the table
                .verticalScroll(rememberScrollState()) // Enable vertical scrolling
        ) {
            Column {
                if (orders.isNotEmpty()) {
                    orders.forEach { order ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFDAAC63))
                                .padding(55.dp)
                        ) {
                            Text(
                                text = order.orderId.substring(
                                    5,
                                    9
                                ), // Extract first 4 characters after prefix
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Left
                            )
                            Text(
                                text = order.status,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = order.orderData.quantity.toString(),
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




