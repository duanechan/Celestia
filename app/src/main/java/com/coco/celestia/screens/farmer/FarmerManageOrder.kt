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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.UserData
import com.coco.celestia.viewmodel.ProductViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FarmerManageOrder(
    navController: NavController,
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel
) {
    val userData by userViewModel.userData.observeAsState()
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    val productData by productViewModel.productData.observeAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()

    // Track which view is active (Order Status or Order Request)
    var isOrderStatusView by remember { mutableStateOf(true) }

    // Dropdown state
    val categoryOptions = productData?.map { it.name }
    var selectedCategory by remember { mutableStateOf("") }
    var expandedCategory by remember { mutableStateOf(false) }

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
                .padding(top = 60.dp)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFFF2E3DB))
        ) {
            // Display the buttons for "Order Status" and "Order Request"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Dropdown (Lettuce, Onion, Carrot)
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    TextField(
                        readOnly = true,
                        value = selectedCategory,
                        onValueChange = {},
                        placeholder = { Text("All") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expandedCategory)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .menuAnchor()
                            .background(
                                color = Color(0xFFEAE7DC),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                BorderStroke(1.dp, Color(0xFF4A2B0E)),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All") },
                            onClick = {
                                selectedCategory = ""
                                expandedCategory = false
                            }
                        )
                        categoryOptions?.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    selectedCategory = option
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }
            }

            if (isOrderStatusView) {
                // Show Order Status view
                when (orderState) {
                    is OrderState.LOADING -> {
                        Text("Loading orders...")
                    }
                    is OrderState.ERROR -> {
                        Text("Failed to load orders: ${(orderState as OrderState.ERROR).message}")
                    }
                    is OrderState.EMPTY -> {
                        Text("No orders available.")
                    }
                    is OrderState.SUCCESS -> {
                        val filteredOrders = if (selectedCategory.isNotEmpty()) {
                            orderData.filter { order ->
                                order.orderData.name.equals(selectedCategory, ignoreCase = true)
                            }
                        } else {
                            orderData
                        }

                        if (filteredOrders.isEmpty()) {
                            Text("No orders available.")
                        } else {
                            var orderCount = 1
                            filteredOrders.forEach { order ->
                                if (userData == null) {
                                    CircularProgressIndicator()
                                } else {
                                    ManageOrderCards(orderCount, order, userData!!)
                                    orderCount++
                                }
                            }
                        }
                    }
                }
            } else {
                // Show Order Request view
                FarmerManageRequest(
                    navController = navController,
                    userData = userData,
                    orderData = orderData,
                    orderState = orderState,
                    selectedCategory = selectedCategory
                )
            }
        }
    }
}

@Composable
fun FarmerManageRequest(
    navController: NavController,
    userData: UserData?,
    orderData: List<OrderData>,
    orderState: OrderState,
    selectedCategory: String
) {
    when (orderState) {
        is OrderState.LOADING -> { Text("Loading pending orders...") }
        is OrderState.ERROR -> { Text("Failed to load orders: ${(orderState as OrderState.ERROR).message}") }
        is OrderState.EMPTY -> { Text("No pending orders available.") }
        is OrderState.SUCCESS -> {
            val pendingOrders = orderData.filter { it.status == "PENDING" }
            val filteredOrders = if (selectedCategory.isNotEmpty()) {
                orderData.filter { order ->
                    order.orderData.name == selectedCategory
                }
            } else {
                pendingOrders
            }
            if (filteredOrders.isEmpty()) {
                Text("No pending orders available.")
            } else {
                var orderCount = 1
                filteredOrders.forEach { order ->
                    if (userData == null) {
                        CircularProgressIndicator()
                    } else {
                        RequestCards(navController, orderCount, order, userData!!)
                        orderCount++
                    }
                }
            }
        }
    }
}

@Composable
fun ManageOrderCards(orderCount: Int, order: OrderData, user: UserData) {
    var expanded by remember { mutableStateOf(false) }
    val clientName = "${user.firstname} ${user.lastname}"
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
                    .clickable { expanded = !expanded }
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize()
                ) {
                    // Order Number Box
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.White, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = orderCount.toString(),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

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
        "ACCEPTED" -> Color(0xFF4CAF50) // Green
        "PENDING" -> Color(0xFFE0A83B) // Yellow
        "REJECTED" -> Color(0xFFA2453D) // Red
        else -> Color.Gray
    }

    val icon = when (orderStatus) {
        "ACCEPTED" -> Icons.Default.Check
        "PENDING" -> Icons.Default.Refresh
        "REJECTED" -> Icons.Default.Clear
        else -> Icons.Default.Star
    }

    Card(
        modifier = Modifier.size(75.dp, 125.dp),
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
            Icon(
                imageVector = icon,
                contentDescription = orderStatus,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = orderStatus,
                fontSize = 7.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun RequestCards(
    navController: NavController,
    orderCount: Int,
    order: OrderData,
    user: UserData
) {
    val clientName = "${user.firstname} ${user.lastname}"
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
                        .fillMaxSize()
                ) {
                    // Order Number Box
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color.White, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = orderCount.toString(),
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

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
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}



