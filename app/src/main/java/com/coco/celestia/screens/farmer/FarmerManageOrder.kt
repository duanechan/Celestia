@file:Suppress("IMPLICIT_CAST_TO_ANY")
package com.coco.celestia.screens.farmer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.UserData
import com.coco.celestia.viewmodel.ProductViewModel
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.painter.Painter

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FarmerManageOrder(
    navController: NavController,
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel
) {
    // Observing LiveData from ViewModels
    val userData by userViewModel.userData.observeAsState()
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    val productData by productViewModel.productData.observeAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()

    // State to track the active view (Order Status or Order Requests)
    var isOrderStatusView by remember { mutableStateOf(true) }

    // Filter states
    val categoryOptions = productData?.map { it.name } ?: emptyList()
    var selectedCategory by remember { mutableStateOf("") }
    var filterMenuExpanded by remember { mutableStateOf(false) }

    // Search bar state
    var searchQuery by remember { mutableStateOf("") }

    // Fetch data when the composable is first launched
    LaunchedEffect(Unit) {
        orderViewModel.fetchAllOrders(
            filter = "",
            role = "Farmer"
        )
        productViewModel.fetchProducts(
            filter = "",
            role = "Farmer"
        )
        userViewModel.fetchUser(uid)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 80.dp)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF2E3DB))
        ) {
            // Search and Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search TextField
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search orders...") },
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = Color(0xFFEAE7DC),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            BorderStroke(1.dp, Color(0xFF4A2B0E)),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))
                Box {
                    IconButton(
                        onClick = { filterMenuExpanded = true },
                        modifier = Modifier
                            .background(
                                color = Color(0xFFEAE7DC),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                BorderStroke(1.dp, Color(0xFF4A2B0E)),
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.filter),
                            contentDescription = "Filter",
                            tint = Color(0xFF4A2B0E)
                        )
                    }

                    // Dropdown Menu for Category Filtering
                    DropdownMenu(
                        expanded = filterMenuExpanded,
                        onDismissRequest = { filterMenuExpanded = false },
                        modifier = Modifier
                            .background(Color(0xFFEAE7DC), shape = RoundedCornerShape(8.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories") },
                            onClick = {
                                selectedCategory = ""
                                filterMenuExpanded = false
                            }
                        )
                        categoryOptions.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    filterMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Order Status and Order Requests
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { isOrderStatusView = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isOrderStatusView) Color(0xFF957541) else Color(0xFFBDBDBD)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Order Status", color = Color.White)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { isOrderStatusView = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isOrderStatusView) Color(0xFF957541) else Color(0xFFBDBDBD)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Order Requests", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            if (isOrderStatusView) {
                // Show Order Status View
                when (orderState) {
                    is OrderState.LOADING -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is OrderState.ERROR -> {
                        Text(
                            "Failed to load orders: ${(orderState as OrderState.ERROR).message}",
                            color = Color.Red,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    is OrderState.EMPTY -> {
                        Text(
                            "No orders available.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    is OrderState.SUCCESS -> {
                        val filteredOrders = orderData.filter { order ->
                            (selectedCategory.isEmpty() || order.orderData.any { it.name == selectedCategory }) &&
                                    order.orderId.contains(searchQuery, ignoreCase = true)
                        }

                        if (filteredOrders.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No orders match your search and filter criteria.")
                            }
                        } else {
                            filteredOrders.forEach { order ->
                                if (userData == null) {
                                    CircularProgressIndicator()
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                    ) {
                                        ManageOrderCards(navController, order)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Show Order Requests View
                FarmerManageRequest(
                    navController = navController,
                    userData = userData,
                    orderData = orderData,
                    orderState = orderState,
                    selectedCategory = selectedCategory,
                    searchQuery = searchQuery
                )
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun FarmerManageRequest(
    navController: NavController,
    userData: UserData?,
    orderData: List<OrderData>,
    orderState: OrderState,
    selectedCategory: String,
    searchQuery: String
) {
    when (orderState) {
        is OrderState.LOADING -> {
            Text("Loading pending orders...")
        }
        is OrderState.ERROR -> {
            Text("Failed to load orders: ${orderState.message}")
        }
        is OrderState.EMPTY -> {
            Text("No pending orders available.")
        }
        is OrderState.SUCCESS -> {
            // Filter orders by pending status
            val pendingOrders = orderData.filter { it.status == "PENDING" }
            val filteredOrders = pendingOrders.filter { order ->
                (selectedCategory.isEmpty() || order.orderData.any { it.name == selectedCategory }) &&
                        order.orderId.contains(searchQuery, ignoreCase = true)
            }

            if (filteredOrders.isEmpty()) {
                Text("No pending orders available.")
            } else {
                filteredOrders.forEach { order ->
                    if (userData == null) {
                        CircularProgressIndicator()
                    } else {
                        RequestCards(navController, order)
                    }
                }
            }
        }
    }
}

@Composable
fun ManageOrderCards(navController: NavController, order: OrderData) {
    val clientName = order.client
    val orderId = order.orderId.substring(5, 9).uppercase()
    val orderStatus = order.status

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .height(125.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF957541),  // Start of gradient
                                Color(0xFF693F27)   // End of gradient
                            )
                        )
                    )
                    .fillMaxSize()
                    .clickable {
                        navController.navigate(Screen.FarmerOrderDetails.createRoute(order.orderId))
                    }
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Order Info
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            text = "Order ID: $orderId",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Client Name: $clientName",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Arrow Forward",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))
        // Status
        OrderStatusCard(orderStatus)
    }
}

@Composable
fun OrderStatusCard(orderStatus: String) {
    val backgroundColor = when (orderStatus) {
        "PREPARING" -> Color(0xFFB06520)
        "PENDING" -> Color(0xFFE0A83B)
        "REJECTED" -> Color(0xFFA2453D)
        else -> Color.Gray
    }

    val iconPainter = when (orderStatus) {
        "PREPARING" -> painterResource(id = R.drawable.preparing) // Use preparing.png
        "PENDING" -> Icons.Default.Refresh
        "REJECTED" -> Icons.Default.Clear
        else -> Icons.Default.Warning
    }

    Card(
        modifier = Modifier.size(80.dp, 125.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (orderStatus == "PREPARING") {
                Icon(
                    painter = iconPainter as Painter,
                    contentDescription = orderStatus,
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
            } else {
                Icon(
                    imageVector = iconPainter as ImageVector,
                    contentDescription = orderStatus,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = orderStatus,
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun RequestCards(
    navController: NavController,
    order: OrderData,
) {
    val clientName = order.client
    val orderId = order.orderId.substring(5, 9).uppercase()

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .height(125.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF957541),  // Start of gradient
                                Color(0xFF693F27)   // End of gradient
                            )
                        )
                    )
                    .fillMaxSize()
                    .clickable {
                        navController.navigate(Screen.FarmerRequestDetails.createRoute(order.orderId))
                    }
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Order Info
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Text(
                            text = "Order ID: $orderId",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Client Name: $clientName",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Arrow Forward",
                        tint = Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterVertically)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}