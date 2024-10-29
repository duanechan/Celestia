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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.Dp
import com.coco.celestia.ui.theme.*

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

    var isOrderStatusView by remember { mutableStateOf(true) }
    var selectedStatus by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("") }
    var filterMenuExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        orderViewModel.fetchAllOrders(filter = "", role = "Farmer")
        productViewModel.fetchProducts(filter = "", role = "Farmer")
        userViewModel.fetchUser(uid)
    }

    Spacer(modifier = Modifier.width(30.dp))

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(color = BgColor)
                .padding(top = 100.dp, bottom = 50.dp)
                .verticalScroll(rememberScrollState())
                .semantics { testTag = "android:id/farmerManageOrderColumn" }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .semantics { testTag = "android:id/farmerManageOrderSearchRow" },
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search orders...", color = Cocoa) },
                    modifier = Modifier
                        .weight(1f)
                        .background(color = Apricot, shape = RoundedCornerShape(16.dp))
                        .border(BorderStroke(1.dp, color = Cocoa), shape = RoundedCornerShape(16.dp))
                        .semantics { testTag = "android:id/searchBar" },
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

                IconButton(
                    onClick = { navController.navigate(Screen.FarmerTransactions.route) },
                    modifier = Modifier
                        .background(color = Apricot, shape = RoundedCornerShape(16.dp))
                        .border(BorderStroke(1.dp, color = Cocoa), shape = RoundedCornerShape(16.dp))
                ) {
                    Icon(painter = painterResource(id = R.drawable.transactions), contentDescription = "Transactions", tint = Cocoa)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box {
                    IconButton(
                        onClick = { filterMenuExpanded = true },
                        modifier = Modifier
                            .background(color = Apricot, shape = RoundedCornerShape(16.dp))
                            .border(BorderStroke(1.dp, color = Cocoa), shape = RoundedCornerShape(16.dp))
                            .semantics { testTag = "android:id/filterButton" }
                    ) {
                        Icon(painter = painterResource(id = R.drawable.filter), contentDescription = "Filter", tint = Cocoa)
                    }

                    DropdownMenu(
                        expanded = filterMenuExpanded,
                        onDismissRequest = { filterMenuExpanded = false },
                        modifier = Modifier.background(color = LightApricot, shape = RoundedCornerShape(8.dp))
                    ) {
                        if (isOrderStatusView) {
                            listOf("All", "Preparing", "Incomplete", "Delivering", "Completed", "Rejected").forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status, color = Cocoa) },
                                    onClick = {
                                        selectedStatus = status
                                        filterMenuExpanded = false
                                    }
                                )
                            }
                        } else {
                            val categoryOptions = productData?.map { it.name } ?: emptyList()
                            DropdownMenuItem(
                                text = { Text("All Products", color = Cocoa) },
                                onClick = {
                                    selectedCategory = ""
                                    filterMenuExpanded = false
                                }
                            )
                            categoryOptions.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category, color = Cocoa) },
                                    onClick = {
                                        selectedCategory = category
                                        filterMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { isOrderStatusView = true },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isOrderStatusView) GoldenYellow else Brown1),
                    modifier = Modifier.weight(1f).semantics { testTag = "android:id/orderStatusButton" }
                ) {
                    Text("Order Status", color = Cocoa)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { isOrderStatusView = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (!isOrderStatusView) GoldenYellow else Brown1),
                    modifier = Modifier.weight(1f).semantics { testTag = "android:id/orderRequestButton" }
                ) {
                    Text("Order Requests", color = Cocoa)
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            if (isOrderStatusView) {
                when (orderState) {
                    is OrderState.LOADING -> {
                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is OrderState.ERROR -> {
                        Text("Failed to load orders: ${(orderState as OrderState.ERROR).message}", color = Color.Red, modifier = Modifier.padding(16.dp))
                    }
                    is OrderState.EMPTY -> {
                        Text("No orders available.", modifier = Modifier.padding(16.dp))
                    }
                    is OrderState.SUCCESS -> {
                        val filteredOrders = orderData.filter { order ->
                            order.orderId.contains(searchQuery, ignoreCase = true) &&
                                    (selectedStatus == "All" || order.status.equals(selectedStatus, ignoreCase = true)) &&
                                    order.status != "PENDING"
                        }

                        if (filteredOrders.isEmpty()) {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("No matching orders available.")
                            }
                        } else {
                            filteredOrders.forEach { order ->
                                if (userData == null) {
                                    CircularProgressIndicator()
                                } else {
                                    Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                            ManageOrderCards(navController, order)
                                            OrderStatusCard(orderStatus = order.status, orderId = order.orderId)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                FarmerManageRequest(
                    navController = navController,
                    userData = userData,
                    orderData = orderData,
                    orderState = orderState,
                    searchQuery = searchQuery,
                    selectedCategory = selectedCategory
                )
            }
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
fun FarmerManageRequest(
    navController: NavController,
    userData: UserData?,
    orderData: List<OrderData>,
    orderState: OrderState,
    searchQuery: String,
    selectedCategory: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
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
                val filteredOrders = orderData
                    .filter { order ->
                        order.status.equals("PENDING", ignoreCase = true)
                    }
                    .filter { order ->
                        order.orderId.contains(searchQuery, ignoreCase = true) &&
                                (selectedCategory.isEmpty() || order.orderData.name.equals(selectedCategory, ignoreCase = true))
                    }

                if (filteredOrders.isEmpty()) {
                    Text("No pending orders available.")
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        filteredOrders.forEach { order ->
                            if (userData == null) {
                                CircularProgressIndicator()
                            } else {
                                RequestCards(navController, order)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManageOrderCards(
    navController: NavController,
    order: OrderData,
    cardWidth: Dp = 240.dp,
    cardHeight: Dp = 180.dp
) {
    val clientName = order.client
    val orderId = order.orderId.substring(6, 10).uppercase()

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .semantics { testTag = "android:id/manageOrderRow_${order.orderId}" }
    ) {
        Card(
            modifier = Modifier
                .width(cardWidth)
                .height(cardHeight)
                .semantics { testTag = "android:id/manageOrderCard+${order.orderId}" },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(PaleGold, GoldenYellow)
                        )
                    )
                    .fillMaxSize()
                    .clickable {
                        navController.navigate(Screen.FarmerOrderDetails.createRoute(order.orderId))
                    }
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Order Info
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxHeight()
                            .semantics { testTag = "android:id/orderInfoColumn_${order.orderId}" }
                    ) {
                        Text(
                            text = "Order ID: $orderId",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Cocoa,
                            modifier = Modifier.semantics { testTag = "android:id/orderIdText_${order.orderId}" }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Client Name: $clientName",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Cocoa,
                            modifier = Modifier.semantics { testTag = "android:id/clientNameText_${order.orderId}" }
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Arrow Forward",
                        tint = Cocoa,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterVertically)
                            .semantics { testTag = "navigateIcon_${order.orderId}" }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
fun OrderStatusCard(
    orderStatus: String,
    orderId: String,
    cardWidth: Dp = 100.dp,
    cardHeight: Dp = 180.dp,
    showText: Boolean = true
) {
    val backgroundColor = when (orderStatus) {
        "PREPARING" -> Brown1
        "INCOMPLETE" -> Tangerine
        "REJECTED" -> Copper3
        "DELIVERING" -> Blue
        "COMPLETED" -> SageGreen
        else -> Color.Gray
    }

    val iconPainter: Painter? = when (orderStatus) {
        "PREPARING" -> painterResource(id = R.drawable.preparing)
        "INCOMPLETE" -> painterResource(id = R.drawable.incomplete)
        "DELIVERING" -> painterResource(id = R.drawable.deliveryicon)
        else -> null
    }

    val iconVector: ImageVector? = when (orderStatus) {
        "REJECTED" -> Icons.Default.Clear
        "COMPLETED" -> Icons.Default.CheckCircle
        else -> Icons.Default.Warning
    }

    Card(
        modifier = Modifier
            .size(cardWidth, cardHeight)
            .semantics { testTag = "android:id/orderStatusCard_$orderId" },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .semantics { testTag = "android:id/orderStatusColumn_$orderId" },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (iconPainter != null) {
                Icon(
                    painter = iconPainter,
                    contentDescription = orderStatus,
                    modifier = Modifier
                        .size(35.dp)
                        .semantics { testTag = "android:id/statusIcon_$orderId" },
                    tint = Cocoa
                )
            } else if (iconVector != null) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = orderStatus,
                    tint = Cocoa,
                    modifier = Modifier
                        .size(35.dp)
                        .semantics { testTag = "android:id/statusIcon_$orderId" }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (showText) {
                Text(
                    text = orderStatus,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cocoa,
                    modifier = Modifier.semantics { testTag = "android:id/statusText_$orderId" }
                )
            }
        }
    }
}

@Composable
fun RequestCards(
    navController: NavController,
    order: OrderData,
) {
    val clientName = order.client
    val orderId = order.orderId.substring(6, 10).uppercase()

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .semantics { testTag = "android:id/requestRow_${order.orderId}" }
    ) {
        Card(
            modifier = Modifier
                .weight(1f)
                .height(175.dp)
                .semantics { testTag = "android:id/requestCard_${order.orderId}" },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(PaleGold, GoldenYellow)
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
                        modifier = Modifier
                            .fillMaxHeight()
                            .semantics { testTag = "android:id/requestInfoColumn_${order.orderId}" }
                    ) {
                        Text(
                            text = "Order ID: $orderId",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Cocoa,
                            modifier = Modifier.semantics { testTag = "android:id/requestOrderIdText_${order.orderId}" }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Client Name: $clientName",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Cocoa,
                            modifier = Modifier.semantics { testTag = "android:id/requestClientNameText_${order.orderId}" }
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Arrow Forward",
                        tint = Cocoa,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterVertically)
                            .semantics { testTag = "android:id/requestNavigateIcon_${order.orderId}" }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}