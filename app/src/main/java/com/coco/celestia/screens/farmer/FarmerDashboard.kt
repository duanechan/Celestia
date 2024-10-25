package com.coco.celestia.screens.farmer

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.UserData
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FarmerItemViewModel
import com.coco.celestia.viewmodel.ItemState
import com.coco.celestia.viewmodel.model.ProductData
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FarmerDashboard(
    navController: NavController,
    userData: UserData?,
    orderData: List<OrderData>,
    orderState: OrderState,
    selectedCategory: String,
    searchQuery: String,
    itemViewModel: FarmerItemViewModel = viewModel()
) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    val itemData by itemViewModel.itemData.observeAsState(emptyList())
    val itemState by itemViewModel.itemState.observeAsState(ItemState.LOADING)
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d yyyy", Locale.getDefault()) }
    val today = dateFormat.format(Date())
    val scrollState = rememberScrollState()

    LaunchedEffect(uid) {
        itemViewModel.getItems(uid = uid)
    }

    when (itemState) {
        is ItemState.LOADING -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is ItemState.SUCCESS -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BgColor)
                    .verticalScroll(scrollState)
                    .padding(top = 120.dp, bottom = 150.dp)
            ) {
                Text(
                    text = today,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, bottom = 10.dp),
                    color = Cocoa
                )

                userData?.let { user ->
                    Text(
                        text = "Welcome, ${user.firstname} ${user.lastname}!",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, bottom = 16.dp),
                        color = Cocoa
                    )
                }

                // In Season Products section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                        .background(LightApricot, shape = RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "In Season Products",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Cocoa
                        )
                        InSeasonCropsPlaceholder()
                    }
                }

                // Product Stock Levels section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
                        .background(LightApricot, shape = RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Product Stock Levels",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Cocoa
                        )
                        StockLevelBarGraph(items = itemData)
                    }
                }

                // Order Listings section
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
                                text = "Order Listings",
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
                            userData = userData,
                            orderData = orderData,
                            orderState = orderState,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery
                        )
                    }
                }
            }
        }
        is ItemState.ERROR -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Error loading item data: ${(itemState as ItemState.ERROR).message}",
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        is ItemState.EMPTY -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No items available", modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun InSeasonCropsPlaceholder() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, top = 10.dp, end = 5.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        repeat(4) {
            Box(
                modifier = Modifier
                    .size(65.dp)
                    .background(PaleGold, shape = RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "In Season",
                    fontSize = 9.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun OrderStatusSection(
    navController: NavController,
    userData: UserData?,
    orderData: List<OrderData>,
    orderState: OrderState,
    selectedCategory: String,
    searchQuery: String
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
                order.status != "PENDING" && order.status != "REJECTED" &&
                        order.orderId.contains(searchQuery, ignoreCase = true)
            }.take(2)

            if (filteredOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No matching orders found.")
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
                                cardWidth = 220.dp,
                                cardHeight = 80.dp
                            )
                            OrderStatusCard(
                                orderStatus = order.status,
                                orderId = order.orderId,
                                cardWidth = 400.dp,
                                cardHeight = 80.dp,
                                showText = false
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
    val topItems = items.sortedByDescending { it.quantity }.take(3)
    val maxQuantity = 3000
    val itemsToDisplay = topItems + List(3 - topItems.size) { ProductData(name = "Placeholder", quantity = 0) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalAlignment = Alignment.Start
    ) {
        itemsToDisplay.forEach { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = if (item.name == "Placeholder") "No Product" else item.name.replaceFirstChar { it.uppercase() },
                    fontSize = 14.sp,
                    color = if (item.name == "Placeholder") Color.Gray else Cocoa,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    var animationPlayed by remember { mutableStateOf(false) }
                    val animatedWidth by animateDpAsState(
                        targetValue = if (animationPlayed) (item.quantity.toFloat() / maxQuantity.toFloat() * 200).dp else 0.dp,
                        animationSpec = tween(durationMillis = 1000)
                    )

                    LaunchedEffect(Unit) {
                        animationPlayed = true
                    }

                    Box(
                        modifier = Modifier
                            .width(if (item.name == "Placeholder") 0.dp else animatedWidth)
                            .height(40.dp)
                            .background(
                                if (item.name == "Placeholder") Color.LightGray else Copper3,
                                shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)
                            )
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .background(Color.Gray.copy(alpha = 0.2f), shape = RoundedCornerShape(topEnd = 10.dp, bottomEnd = 10.dp)),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "${item.quantity}",
                            fontSize = 14.sp,
                            color = if (item.name == "Placeholder") Color.Gray else Cocoa,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }
        }
    }
}