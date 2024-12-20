package com.coco.celestia.screens.farmer

import android.util.Log
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
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FarmerItemViewModel

@Composable
fun FarmerManageOrder(
    navController: NavController,
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel,
) {
    val userData by userViewModel.userData.observeAsState()
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    val productData by productViewModel.productData.observeAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val farmerItemViewModel: FarmerItemViewModel = viewModel()

    var isOrderStatusView by remember { mutableStateOf(true) }
    var selectedStatus by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("") }
    var filterMenuExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var farmerName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        orderViewModel.fetchAllOrders(filter = "", role = "Farmer")
        productViewModel.fetchProducts(filter = "", role = "Farmer")
        userViewModel.fetchUser(uid)
        if (uid.isNotEmpty()) {
            farmerName = farmerItemViewModel.fetchFarmerName(uid)
        }
    }

    Spacer(modifier = Modifier.width(30.dp))

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(color = BgColor)
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
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            tint = Cocoa
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .background(color = Apricot, shape = RoundedCornerShape(16.dp))
                        .border(BorderStroke(1.dp, color = Cocoa), shape = RoundedCornerShape(16.dp))
                        .semantics { testTag = "android:id/searchBar" },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { navController.navigate(Screen.FarmerTransactions.route) },
                    modifier = Modifier
                        .background(color = Apricot, shape = RoundedCornerShape(16.dp))
                        .border(BorderStroke(1.dp, color = Cocoa), shape = RoundedCornerShape(16.dp))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.transactions),
                        contentDescription = "Transactions",
                        tint = Cocoa
                    )
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
                        Icon(
                            painter = painterResource(id = R.drawable.filter),
                            contentDescription = "Filter",
                            tint = Cocoa
                        )
                    }

                    DropdownMenu(
                        expanded = filterMenuExpanded,
                        onDismissRequest = { filterMenuExpanded = false },
                        modifier = Modifier
                            .background(color = LightApricot, shape = RoundedCornerShape(8.dp))
                            .heightIn(max = 250.dp)

                    ) {
                        if (isOrderStatusView) {
                            listOf("All", "Accepted", "Planting", "Harvesting", "Delivering", "Completed", "Rejected", "Cancelled").forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status, color = Cocoa) },
                                    onClick = {
                                        selectedStatus = status
                                        filterMenuExpanded = false
                                    }
                                )
                            }
                        } else {
                            DropdownMenuItem(
                                text = { Text("All Products", color = Cocoa) },
                                onClick = {
                                    selectedCategory = ""
                                    filterMenuExpanded = false
                                }
                            )
                            productData?.map { it.name }?.forEach { category ->
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
                    modifier = Modifier
                        .weight(1f)
                        .semantics { testTag = "android:id/orderStatusButton" }
                ) {
                    Text("Order Status", color = Cocoa)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { isOrderStatusView = false },
                    colors = ButtonDefaults.buttonColors(containerColor = if (!isOrderStatusView) GoldenYellow else Brown1),
                    modifier = Modifier
                        .weight(1f)
                        .semantics { testTag = "android:id/orderRequestButton" }
                ) {
                    Text("Order Requests", color = Cocoa)
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            if (isOrderStatusView) {
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
                        Text("No orders available.", modifier = Modifier.padding(16.dp))
                    }
                    is OrderState.SUCCESS -> {
                        val filteredOrders = orderData.filter { order ->
                            val matchesSearchQuery = order.orderId.contains(searchQuery, ignoreCase = true)
                            val matchesStatus = when (selectedStatus){
                                "All" -> true
                                "Completed" -> order.status == "COMPLETED" || order.status == "RECEIVED"
                                "Rejected" -> order.status == "REJECTED"
                                "Cancelled" -> order.status == "CANCELLED"
                                "Accepted" -> order.status == "ACCEPTED"
                                "Planting" -> order.status == "PLANTING"
                                "Harvesting" -> order.status == "HARVESTING"
                                "Delivering" -> order.status == "DELIVERING"
                                else -> order.status.equals(selectedStatus, ignoreCase = true)
                            }
                            val isFulfilledByFarmer = order.fulfilledBy.any { it.farmerName == farmerName}
                            matchesSearchQuery && matchesStatus &&
                                    (isFulfilledByFarmer || order.status in listOf("REJECTED", "CANCELLED"))
                        }
                        filteredOrders.forEach { order ->
                            Log.d("filter", order.status)
                        }
                        if (filteredOrders.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No matching orders available.")
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
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            ManageOrderCards(navController, order, farmerName)
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
                        order.status.equals("PENDING", ignoreCase = true) ||
                                (order.status == "PARTIALLY_FULFILLED" &&
                                        order.orderData.quantity - order.partialQuantity != 0)
                    }
                    .filter { order ->
                        order.orderId.contains(searchQuery, ignoreCase = true) &&
                                (selectedCategory.isEmpty() || order.orderData.name.equals(selectedCategory, ignoreCase = true))
                    }
                Log.d("orders", filteredOrders.toString())
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
    farmerName: String,
    cardHeight: Dp = 150.dp,
    showStatus: Boolean = true
) {
    val clientName = order.client
    val orderId = order.orderId.substring(6, 10).uppercase()
    var displayStatus by remember { mutableStateOf("") }
    val fulfilledByFarmer = order.fulfilledBy.find { it.farmerName == farmerName }

    if (order.status == "PARTIALLY_FULFILLED") {
        if (fulfilledByFarmer != null) {
            displayStatus = fulfilledByFarmer.status
        }
    } else {
        displayStatus = if (order.status == "RECEIVED") "COMPLETED" else order.status
    }

    val backgroundColor = when (displayStatus) {
        "ACCEPTED" -> SageGreen
        "PLANTING" -> Tangerine
        "REJECTED" -> Copper.copy(alpha = 0.4f)
        "DELIVERING" -> Green
        "COMPLETED" -> SageGreen.copy(alpha = 0.7f)
        "CANCELLED" -> Copper3
        "HARVESTING" -> Brown1
        "HARVESTING_MEAT" -> Brown1
        else -> Color.Gray
    }

    val iconPainter: Painter? = when (displayStatus) {
        "ACCEPTED" -> painterResource(id = R.drawable.preparing)
        "PLANTING" -> painterResource(id = R.drawable.plant)
        "HARVESTING" -> painterResource(id = R.drawable.harvest_basket)
        "HARVESTING_MEAT" -> painterResource(id = R.drawable.cow_animal)
        "DELIVERING" -> painterResource(id = R.drawable.deliveryicon)
        "CANCELLED" -> painterResource(id = R.drawable.cancelled)
        else -> null
    }

    val iconVector: ImageVector = when (displayStatus) {
        "REJECTED" -> Icons.Default.Clear
        "COMPLETED" -> Icons.Default.CheckCircle
        else -> Icons.Default.Warning
    }

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .semantics { testTag = "android:id/manageOrderRow_${order.orderId}" }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {
                        if (showStatus) {
                            Box(
                                modifier = Modifier
                                    .background(backgroundColor, shape = RoundedCornerShape(16.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .semantics { testTag = "android:id/orderStatusRow_${order.orderId}" }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (iconPainter != null) {
                                        Icon(
                                            painter = iconPainter,
                                            contentDescription = displayStatus,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .semantics { testTag = "android:id/statusIcon_${order.orderId}" },
                                            tint = Cocoa
                                        )
                                    } else {
                                        Icon(
                                            imageVector = iconVector,
                                            contentDescription = displayStatus,
                                            tint = Cocoa,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .semantics { testTag = "android:id/statusIcon_${order.orderId}" }
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Text(
                                        text = displayStatus.replace("_", " "),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Cocoa,
                                        modifier = Modifier
                                            .semantics { testTag = "android:id/statusText_${order.orderId}" }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, start = 10.dp)
                                .semantics { testTag = "android:id/orderInfoColumn_${order.orderId}" },
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Order ID: $orderId",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Cocoa,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.semantics { testTag = "android:id/orderIdText_${order.orderId}" }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Client Name: $clientName",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Cocoa,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.semantics { testTag = "android:id/clientNameText_${order.orderId}" }
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Arrow Icon",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(end = 8.dp)
                            .align(Alignment.CenterVertically),
                        tint = Cocoa
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}

@Composable
fun RequestCards(
    navController: NavController,
    order: OrderData,
) {
    val clientName = order.client
    val orderId = order.orderId.substring(6, 10).uppercase()
    val displayStatus = when (order.status) {
        "PENDING" -> "PENDING"
        "PARTIALLY_FULFILLED" -> "PARTIALLY FULFILLED"
        else -> "Unknown"
    }

    val backgroundColor = when (order.status) {
        "PENDING" -> Sand2
        "PARTIALLY_FULFILLED" -> Tangerine
        else -> Color.Gray
    }

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .semantics { testTag = "android:id/requestRow_${order.orderId}" }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
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
                        .padding(20.dp)
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start
                    ) {

                        Box(
                            modifier = Modifier
                                .background(backgroundColor, shape = RoundedCornerShape(16.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .semantics { testTag = "android:id/requestStatusBox_${order.orderId}" }
                        ) {
                            Text(
                                text = displayStatus,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Cocoa,
                                modifier = Modifier
                                    .semantics { testTag = "android:id/requestStatusText_${order.orderId}" }
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, start = 10.dp)
                                .semantics { testTag = "android:id/requestInfoColumn_${order.orderId}" },
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Order ID: $orderId",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Cocoa,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.semantics { testTag = "android:id/requestOrderIdText_${order.orderId}" }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Client Name: $clientName",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Cocoa,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.semantics { testTag = "android:id/requestClientNameText_${order.orderId}" }
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Arrow Forward",
                        modifier = Modifier
                            .size(40.dp)
                            .padding(end = 8.dp)
                            .align(Alignment.CenterVertically),
                        tint = Cocoa
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
    }
}