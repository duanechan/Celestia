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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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

    val inSeasonProducts = products.filter { product ->
        val currentMonth = LocalDate.now().month
        val sanitizedStartSeason = product.startSeason.trim().uppercase(Locale.getDefault())
        val sanitizedEndSeason = product.endSeason.trim().uppercase(Locale.getDefault())

        val startMonth = try {
            Month.valueOf(sanitizedStartSeason)
        } catch (e: IllegalArgumentException) { return@filter false }

        val endMonth = try {
            Month.valueOf(sanitizedEndSeason)
        } catch (e: IllegalArgumentException) { return@filter false }

        when {
            startMonth.value <= endMonth.value -> {
                currentMonth.value in startMonth.value..endMonth.value
            }
            else -> {
                currentMonth.value >= startMonth.value || currentMonth.value <= endMonth.value
            }
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

                // In Season Products
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
                                text = "Order Progress Overview",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Cocoa
                            )
                            TextButton(
                                onClick = { showInSeasonDialog = true },
                                modifier = Modifier.padding(start = 8.dp)
                            ){}
//                            {
//                                Text("See All", color = Cocoa, fontWeight = FontWeight.Bold)
//                            }
                        }

                        InSeasonProducts(products = inSeasonProducts)
                    }
                }

                // Product Stock Levels
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
                                text = "Quotation Review",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Cocoa
                            )

                            TextButton(onClick = { showAllDialog = true }) {
                                Text(
                                    text = "See All",
                                    color = Cocoa,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        StockLevelBarGraph(items = itemData)
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

    if (showInSeasonDialog) {
        InSeasonProductListDialog(
            products = inSeasonProducts,
            onDismiss = { showInSeasonDialog = false }
        )
    }

    if (showAllDialog) {
        ProductListDialog(
            items = itemData,
            onDismiss = { showAllDialog = false }
        )
    }
}

@Composable
fun InSeasonProducts(products: List<ProductData>) {
    val currentMonth = LocalDate.now().month

    val statusCounts = mapOf(
        "In Progress" to 5,
        "Pending" to 3,
        "Accepted" to 7,
        "Rejected" to 2
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            statusCounts.forEach { (status, count) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = count.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = status,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = currentMonth.name,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Cocoa,
            modifier = Modifier.padding(top = 15.dp)
        )
    }
}

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

@Composable
fun StockLevelBarGraph(items: List<ProductData>) {
    val inNeedOfReviewCount = items.count { it.quantity < 500 }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(
                    color = Color(0xFFC0CFB2),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable {
                    // Action for button click
                }
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            Text(
                text = "$inNeedOfReviewCount items in need of review",
                fontSize = 14.sp,
                color = Color.DarkGray
            )
        }
    }
}