package com.coco.celestia.screens.farmer.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.screens.farmer.dialogs.FarmerFulfillDialog
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FarmerItemViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.ItemData
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FarmerOrderDetails(
    navController: NavController,
    orderId: String
) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    val orderViewModel: OrderViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()
    val farmerItemViewModel: FarmerItemViewModel = viewModel()
    val allOrders by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    val usersData by userViewModel.usersData.observeAsState(emptyList())
    var showFulfillDialog by remember { mutableStateOf(false) }

    var farmerName by remember { mutableStateOf("") }

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
        if (uid.isNotEmpty()) {
            farmerName = farmerItemViewModel.fetchFarmerName(uid)
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

                if (orderData.status == "REJECTED") {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        text = "Rejection Reason\n ${orderData.rejectionReason}",
                        color = Copper,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                } else if (orderData.status == "CANCELLED") {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        text = "${orderData.client} cancelled the order",
                        color = Copper,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                } else {
                    OrderStatusDropdown(orderData = orderData, orderViewModel = orderViewModel)
                    OrderStatusUpdates(orderData = orderData)
                }
            }
        }
    }

    if (showFulfillDialog && orderData != null) {
        val totalFarmers = 2 // to change
        DisplayFarmerFulfillDialog(
            navController = navController,
            onDismiss = { showFulfillDialog = false },
            farmerItemViewModel = farmerItemViewModel,
            orderViewModel = orderViewModel,
            orderData = orderData,
            item = ItemData(name = orderData.orderData.name, items = mutableListOf(orderData.orderData)),
            totalFarmers = totalFarmers,
            farmerName = farmerName
        )
    }
}

@Composable
fun OrderStatusDropdown(orderData: OrderData, orderViewModel: OrderViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf(orderData.status) }

    val statusOptions = when (orderData.status) {
        "PREPARING" -> listOf("DELIVERING")
        "DELIVERING" -> listOf("COMPLETED")
        "COMPLETED" -> emptyList()
        else -> emptyList()
    }

    val statusColor = when (selectedStatus) {
        "PREPARING" -> Brown1
        "DELIVERING" -> Blue
        "COMPLETED" -> SageGreen
        else -> Color.Gray.copy(alpha = 0.3f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Order Tracking",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Cocoa
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "Current Status: ",
                fontSize = 18.sp,
                color = Cocoa
            )

            Spacer(modifier = Modifier.width(5.dp))

            Box {
                Box(
                    modifier = Modifier
                        .background(
                            color = if (statusOptions.isEmpty()) OliveGreen.copy(alpha = 0.3f) else statusColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable(enabled = statusOptions.isNotEmpty()) { expanded = true }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.width(IntrinsicSize.Max)
                    ) {
                        Text(
                            text = selectedStatus,
                            fontWeight = FontWeight.Bold,
                            color = Cocoa,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        if (statusOptions.isNotEmpty()) {
                            Icon(
                                imageVector = if (expanded)
                                    Icons.Default.KeyboardArrowUp
                                else
                                    Icons.Default.KeyboardArrowDown,
                                contentDescription = "Dropdown Arrow",
                                tint = Cocoa
                            )
                        }
                    }
                }

                if (statusOptions.isNotEmpty()) {
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .width(150.dp)
                            .background(Color.White)
                            .align(Alignment.BottomStart)
                    ) {
                        statusOptions.forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = status,
                                        color = Cocoa,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    selectedStatus = status
                                    expanded = false
                                    orderViewModel.updateOrder(orderData.copy(status = status))
                                }
                            )
                        }
                    }
                }
            }
        }
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
    totalFarmers: Int,
    farmerName: String
) {
    FarmerFulfillDialog(
        navController = navController,
        farmerName = farmerName,
        item = item,
        orderViewModel = orderViewModel,
        orderData = orderData,
        farmerItemViewModel = farmerItemViewModel,
        totalFarmers = totalFarmers,
        onAccept = {
            val updatedOrder = orderData.copy(status = "PREPARING", fulfilledBy = orderData.fulfilledBy + farmerName)
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
            .height(400.dp)
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
                modifier = Modifier.padding(30.dp)
            ) {
                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .weight(1f) // Adjusted weight
                            .border(1.dp, Cocoa, RoundedCornerShape(5.dp))
                            .background(Sand2, shape = RoundedCornerShape(5.dp))
                            .padding(top = 8.dp, start = 8.dp, bottom = 30.dp)
                    ) {
                        Text(
                            text = "Order ID",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Cocoa,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().semantics { testTag = "android:id/orderIdLabel" }
                        )
                        Spacer(modifier = Modifier.height(15.dp))
                        Text(
                            text = orderData.orderId.substring(6, 10).uppercase(),
                            fontSize = 30.sp,
                            color = Cocoa,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().semantics { testTag = "android:id/orderIdText" }
                        )
                    }

                    Spacer(modifier = Modifier.width(15.dp))

                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Cocoa, RoundedCornerShape(5.dp))
                            .background(Sand2, shape = RoundedCornerShape(5.dp))
                            .padding(top = 8.dp, start = 8.dp, bottom = 30.dp)
                    ) {
                        Text(
                            text = "Address",
                            fontSize = 20.sp,
                            color = Cocoa,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth().semantics { testTag = "android:id/deliveryAddressLabel" }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${orderData.street}, ${orderData.barangay}",
                            fontSize = 15.sp,
                            color = Cocoa,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().semantics { testTag = "android:id/deliveryAddressText" }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Cocoa, RoundedCornerShape(5.dp))
                            .background(Sand2, shape = RoundedCornerShape(5.dp))
                            .padding(top = 8.dp, start = 8.dp, bottom = 25.dp)
                    ) {
                        Text(
                            text = "Target Date",
                            fontSize = 20.sp,
                            color = Cocoa,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().semantics { testTag = "android:id/targetDateLabel" }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = orderData.targetDate,
                            fontSize = 15.sp,
                            color = Cocoa,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().semantics { testTag = "android:id/targetDateText" }
                        )
                    }

                    Spacer(modifier = Modifier.width(15.dp))

                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, Cocoa, RoundedCornerShape(5.dp))
                            .background(Sand2, shape = RoundedCornerShape(5.dp))
                            .padding(top = 8.dp, start = 8.dp, bottom = 25.dp)
                    ) {
                        Text(
                            text = "Client Name",
                            fontSize = 20.sp,
                            color = Cocoa,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().semantics { testTag = "android:id/clientNameLabel" }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = orderData.client,
                            fontSize = 15.sp,
                            color = Cocoa,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().semantics { testTag = "android:id/clientNameText" }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(15.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .border(1.dp, Cocoa, RoundedCornerShape(5.dp))
                            .background(Sand2, shape = RoundedCornerShape(5.dp))
                            .padding(8.dp)
                            .semantics { testTag = "android:id/orderedProductSection" }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                        ) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Item",
                                color = Cocoa,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics { testTag = "android:id/orderedProductLabel" }
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = product.name,
                            fontSize = 18.sp,
                            color = Cocoa,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().semantics { testTag = "android:id/productNameText" }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${product.quantity} kg",
                            fontSize = 18.sp,
                            color = Cocoa,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().semantics { testTag = "android:id/productQuantityText" }
                        )
                    }

                    Spacer(modifier = Modifier.width(15.dp))

                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .border(1.dp, Cocoa, RoundedCornerShape(5.dp))
                            .background(Sand2, shape = RoundedCornerShape(5.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "Fulfilled By",
                            fontSize = 20.sp,
                            color = Cocoa,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().semantics { testTag = "android:id/fulfilledByLabel" }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        if (orderData.fulfilledBy.isNotEmpty()) {
                            orderData.fulfilledBy.forEach { name ->
                                Text(
                                    text = name,
                                    fontSize = 15.sp,
                                    color = Cocoa,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .semantics { testTag = "android:id/fulfilledByText" }
                                        .padding(vertical = 2.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "None",
                                fontSize = 15.sp,
                                color = Cocoa,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().semantics { testTag = "android:id/fulfilledByText" }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderStatusUpdates(orderData: OrderData) {
    val client = orderData.client
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.preparing),
                    contentDescription = "Preparing",
                    modifier = Modifier.size(40.dp),
                    colorFilter = ColorFilter.tint(Cocoa)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Spacer(modifier = Modifier.height(50.dp))

                Text(
                    text = when {
                        orderData.fulfilledBy.isEmpty() -> "No farmers are preparing the order"
                        orderData.fulfilledBy.size == 1 -> "${orderData.fulfilledBy.first()} is preparing the order"
                        orderData.fulfilledBy.size == 2 -> "${orderData.fulfilledBy.joinToString(" and ")} are preparing the order"
                        else -> {
                            val lastFarmer = orderData.fulfilledBy.last()
                            val otherFarmers = orderData.fulfilledBy.dropLast(1).joinToString(", ")
                            "$otherFarmers and $lastFarmer are preparing the order"
                        }
                    },
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            if (orderData.status in listOf("DELIVERING", "COMPLETED")) {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Spacer(modifier = Modifier.width(18.dp))
                    Divider(
                        color = GoldenYellow,
                        modifier = Modifier
                            .width(2.dp)
                            .height(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.deliveryicon),
                        contentDescription = "Delivery",
                        modifier = Modifier.size(40.dp),
                        colorFilter = ColorFilter.tint(Cocoa)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "$client's order is out for delivery",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            if (orderData.status == "COMPLETED") {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Spacer(modifier = Modifier.width(18.dp))
                    Divider(
                        color = GoldenYellow,
                        modifier = Modifier
                            .width(2.dp)
                            .height(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.completed),
                        contentDescription = "Completed",
                        modifier = Modifier.size(30.dp).padding(start = 3.dp),
                        colorFilter = ColorFilter.tint(OliveGreen)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = "$client's order has been fulfilled",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}