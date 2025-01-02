package com.coco.celestia.screens.farmer

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.screens.farmer.dialogs.InSeasonProductListDialog
import com.coco.celestia.screens.farmer.dialogs.ProductListDialog
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.UserData
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FarmerItemViewModel
import com.coco.celestia.viewmodel.ItemState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.model.ProductData
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Month
import java.util.*

@Composable
fun FarmerDashboard(
    navController: NavController,
    userData: UserData?,
    orderData: List<OrderData>,
    orderState: OrderState,
    searchQuery: String,
    itemViewModel: FarmerItemViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel()
) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    val itemData by itemViewModel.itemData.observeAsState(emptyList())
    val itemState by itemViewModel.itemState.observeAsState(ItemState.LOADING)
    val products by productViewModel.productData.observeAsState(emptyList())
    val farmerItemViewModel: FarmerItemViewModel = viewModel()
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d yyyy", Locale.getDefault()) }
    val today = dateFormat.format(Date())
    val scrollState = rememberScrollState()
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 0..11 -> "Good Morning"
        in 12..17 -> "Good Afternoon"
        else -> "Good Evening"
    }

    var showInSeasonDialog by remember { mutableStateOf(false) }
    var showAllDialog by remember { mutableStateOf(false) }
    var farmerName by remember { mutableStateOf("") }

    LaunchedEffect(uid) {
        itemViewModel.getItems(uid = uid)
        productViewModel.fetchProducts(filter = "", role = "Farmer")
        itemViewModel.fetchFarmerName(uid)
    }

    LaunchedEffect (Unit) {
        if (uid.isNotEmpty()) {
            farmerName = farmerItemViewModel.fetchFarmerName(uid)
        }
    }

    when (itemState) {
        is ItemState.LOADING -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ItemState.EMPTY,
        is ItemState.SUCCESS -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgColor)
                    .verticalScroll(scrollState)
                    .padding(top = 10.dp, bottom = 10.dp)
            ) {
                // Greeting and date
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                        .background(LightApricot, shape = RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = today,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Start,
                            color = Cocoa
                        )

                        userData?.let { user ->
                            Text(
                                text = "$greeting, ${user.firstname} ${user.lastname}!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Start,
                                color = Cocoa,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                // orders overview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                        .background(LightApricot, shape = RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Orders Overview",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Cocoa
                            )
                            TextButton(
                                onClick = { showInSeasonDialog = true },
                                modifier = Modifier.padding(start = 8.dp)
                            ){}
                        }
                        FarmerOrderOverview(orders = orderData)
                    }
                }

                // Order Listings
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                        .background(LightApricot, shape = RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(start = 16.dp, top = 5.dp, end = 10.dp, bottom = 20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pending Order Requests",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Cocoa
                            )
                            TextButton(onClick = { navController.navigate("farmer_manage_order") }) {
                                Text(
                                    text = "See All",
                                    color = Cocoa,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        OrderStatusSection(
                            navController = navController,
                            orderData = orderData,
                            orderState = orderState,
                            searchQuery = searchQuery,
                            farmerName = farmerName
                        )
                    }
                }
            }
        }
        is ItemState.ERROR -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Error loading item data: ${(itemState as ItemState.ERROR).message}",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    if (showAllDialog) {
        ProductListDialog(
            items = itemData,
            onDismiss = { showAllDialog = false }
        )
    }
}

//TODO: to connect order status numbers to the actual orders. atm it is only a placeholder
@Composable
fun FarmerOrderOverview(orders: List<OrderData>) {
    val currentMonth = LocalDate.now().month

    val statusCounts = mapOf(
        "In Progress" to 5,
        "Pending" to 3,
        "Accepted" to 7,
        "Rejected" to 2
    )

    val statusIcons: Map<String, Any> = mapOf(
        "In Progress" to R.drawable.hourglass,
        "Pending" to Icons.Default.Refresh,
        "Accepted" to Icons.Default.CheckCircle,
        "Rejected" to Icons.Default.Close
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            statusCounts.forEach { (status, count) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    when (val icon = statusIcons[status]) {
                        is Int -> {
                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = "$status Icon",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        is ImageVector -> {
                            Icon(
                                imageVector = icon,
                                contentDescription = "$status Icon",
                                tint = when (status) {
                                    "Accepted" -> Color(0xFF4CAF50) // Green
                                    "Rejected" -> Color(0xFFF44336) // Red
                                    "In Progress" -> Color(0xFF000000)
                                    "Pending" -> Color(0xFFFFC107) // Orange
                                    else -> Color.Gray
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Default Icon",
                                tint = Color.Gray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status count
                    Text(
                        text = count.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    // Status label
                    Text(
                        text = status,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Current month label
        Text(
            text = currentMonth.name.lowercase().replaceFirstChar { it.uppercase() },
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Cocoa,
            modifier = Modifier.padding(top = 15.dp)
        )
    }
}

//adjust color of order box
@Composable
fun OrderStatusSection(
    navController: NavController,
    orderData: List<OrderData>,
    orderState: OrderState,
    searchQuery: String,
    farmerName: String
) {
    when (orderState) {
        is OrderState.LOADING -> {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is OrderState.ERROR -> {
            Text(
                "Failed to load orders: ${(orderState as OrderState.ERROR).message}",
                color = Color.Red,
                modifier = Modifier
                    .padding(16.dp)
            )
        }
        is OrderState.EMPTY -> {
            Text(
                "No orders available.",
                modifier = Modifier
                    .padding(16.dp)
            )
        }
        is OrderState.SUCCESS -> {
            val filteredOrders = orderData.filter { order ->
                order.status !in listOf("PENDING", "REJECTED", "COMPLETED", "CANCELLED") &&
                        order.orderId.contains(searchQuery, ignoreCase = true)
                order.fulfilledBy.any { it.farmerName == farmerName }
            }.take(2)

            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No orders found.", color = Cocoa.copy(alpha = 0.7f))
                }
            } else {
                Column {
                    filteredOrders.forEach { order ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, end = 5.dp)
                        ) {
                            ManageOrderCards(
                                navController = navController,
                                order = order,
                                farmerName = farmerName,
                                cardHeight = 100.dp,
                                showStatus = false
                            )
                        }
                    }
                }
            }
        }
    }
}