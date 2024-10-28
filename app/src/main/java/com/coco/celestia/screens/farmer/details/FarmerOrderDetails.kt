package com.coco.celestia.screens.farmer.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.screens.farmer.dialogs.FarmerFulfillDialog
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FarmerItemViewModel
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.ItemData
import com.coco.celestia.viewmodel.model.UserData

@Composable
fun FarmerOrderDetails(
    navController: NavController,
    orderId: String
) {
    val orderViewModel: OrderViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()
    val farmerItemViewModel: FarmerItemViewModel = viewModel() // Add FarmerItemViewModel
    val allOrders by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    val usersData by userViewModel.usersData.observeAsState(emptyList())
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)
    var showFulfillDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (allOrders.isEmpty()) {
            orderViewModel.fetchAllOrders(
                filter = "",
                role = "Farmer"
            )
        }
        if (usersData.isEmpty()) {
            userViewModel.fetchUsers()
        }
    }

    val orderData: OrderData? = remember(orderId, allOrders) {
        allOrders.find { it.orderId == orderId }
    }

    when {
        orderState == OrderState.LOADING -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = BgColor)
                    .semantics { testTag = "android:id/loadingOrderDetails" },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Cocoa)
            }
        }

        orderData == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = BgColor)
                    .semantics { testTag = "android:id/orderNotFound" },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Order not found",
                    color = Copper,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        else -> {
            if (orderData.status == "INCOMPLETE") {
                showFulfillDialog = true
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = BgColor)
                    .padding(top = 80.dp)
                    .semantics { testTag = "android:id/orderDetailsScreen" }
            ) {
                OrderDetailsCard(orderData = orderData)
                Spacer(modifier = Modifier.height(20.dp))

                if (orderData.status != "REJECTED") {
                    val fulfilledByList = orderData.fulfilledBy ?: emptyList()
                    FulfilledBySection(fulfilledBy = fulfilledByList, allUsers = usersData)
                }
            }
        }
    }

    if (showFulfillDialog && orderData != null) {
        val totalFarmers = 2 // to change i think
        DisplayFarmerFulfillDialog(
            navController = navController,
            onDismiss = { showFulfillDialog = false },
            farmerItemViewModel = farmerItemViewModel,
            orderViewModel = orderViewModel,
            orderData = orderData,
            item = ItemData(name = orderData.orderData.name, items = mutableListOf(orderData.orderData)),
            totalFarmers = totalFarmers
        )
    }
}

@Composable
fun DisplayFarmerFulfillDialog(
    navController: NavController,
    onDismiss: () -> Unit,
    farmerItemViewModel: FarmerItemViewModel,
    orderViewModel: OrderViewModel,
    orderData: OrderData,
    item: ItemData,
    totalFarmers: Int
) {
    FarmerFulfillDialog(
        navController = navController,
        item = item,
        orderViewModel = orderViewModel,
        orderData = orderData,
        farmerItemViewModel = farmerItemViewModel,
        totalFarmers = totalFarmers,
        onAccept = {
            val updatedOrder = orderData.copy(status = "PREPARING")
            orderViewModel.updateOrder(updatedOrder)

            farmerItemViewModel.reduceItemQuantity(item, totalFarmers)
            onDismiss()
        },
        onReject = {
            onDismiss()
        },
        onDismiss = onDismiss
    )
}

@Composable
fun OrderDetailsCard(orderData: OrderData) {
    val product = orderData.orderData

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(385.dp)
            .semantics { testTag = "android:id/orderDetailsCard" },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
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
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(5.dp))

                // Order ID
                Text(
                    text = "Order ID",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Cocoa,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/orderIdLabel" }
                )
                Text(
                    text = orderData.orderId.substring(6, 38),
                    fontSize = 15.sp,
                    color = Cocoa,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/orderIdText" }
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Delivery Address
                Text(
                    text = "Delivery Address",
                    fontSize = 20.sp,
                    color = Cocoa,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/deliveryAddressLabel" }
                )
                Text(
                    text = "${orderData.street}, ${orderData.barangay}",
                    fontSize = 15.sp,
                    color = Cocoa,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/deliveryAddressText" }
                )

                // Target Date
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Target Date",
                    fontSize = 20.sp,
                    color = Cocoa,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/targetDateLabel" }
                )
                Text(
                    text = orderData.targetDate,
                    fontSize = 15.sp,
                    color = Cocoa,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/targetDateText" }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Product Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 90.dp)
                        .semantics { testTag = "android:id/orderedProductRow" }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Ordered Products Icon",
                        tint = Cocoa,
                        modifier = Modifier
                            .size(24.dp)
                            .semantics { testTag = "android:id/shoppingCartIcon" }
                    )

                    Spacer(modifier = Modifier.width(1.dp))

                    Text(
                        text = "Ordered Product",
                        color = Cocoa,
                        textAlign = TextAlign.Start,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .padding(start = 15.dp)
                            .semantics { testTag = "android:id/orderedProductLabel" }
                    )
                }

                Spacer(modifier = Modifier.height(15.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/productInfoRow" }
                ) {
                    Spacer(modifier = Modifier.width(50.dp))
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .weight(1f)
                            .semantics { testTag = "android:id/productInfoColumn" }
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 50.dp)
                                .semantics { testTag = "android:id/productCard" },
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.elevatedCardElevation(
                                defaultElevation = 4.dp
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Apricot2, Copper)
                                        )
                                    )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(15.dp)
                                        .semantics { testTag = "android:id/productColumn" },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = product.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 30.sp,
                                        color = Cocoa,
                                        modifier = Modifier.semantics { testTag = "android:id/productNameText" }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "${product.quantity} kg",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 40.sp,
                                        color = Cocoa,
                                        modifier = Modifier.semantics { testTag = "android:id/productQuantityText" }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FulfilledBySection(fulfilledBy: List<UserData>?, allUsers: List<UserData>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .semantics { testTag = "android:id/fulfilledByCard" },
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(
            containerColor = PaleGold
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = "Fulfilled By:",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Cocoa,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "android:id/fulfilledByLabel" }
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (fulfilledBy.isNullOrEmpty()) {
                Text(
                    text = "N/A",
                    fontSize = 16.sp,
                    color = Copper,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/noFarmersMessage" }
                )
            } else {
                fulfilledBy.forEach { farmer ->
                    Text(
                        text = "${farmer.firstname} ${farmer.lastname}",
                        fontSize = 18.sp,
                        color = Cocoa,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { testTag = "android:id/fulfilledByName" }
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                }
            }
        }
    }
}