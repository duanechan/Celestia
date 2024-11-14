package com.coco.celestia.screens.farmer.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.screens.farmer.dialogs.CompletedStatusDialog
import com.coco.celestia.screens.farmer.dialogs.DeliveringStatusDialog
import com.coco.celestia.screens.farmer.dialogs.FarmerFulfillDialog
import com.coco.celestia.screens.farmer.dialogs.PendingStatusDialog
import com.coco.celestia.screens.farmer.dialogs.PreparingStatusDialog
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FarmerItemViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.ItemData
import com.coco.celestia.viewmodel.model.ProductData
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
            farmerItemViewModel.getItems(uid)
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
                    .padding(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        orderData == null -> {
            Text(
                text = "Order not found",
                color = Color.Red,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxSize().padding(16.dp)
            )
        }

        else -> {
            if (orderData.status == "INCOMPLETE") {
                showFulfillDialog = true
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = BgColor)
                    .semantics { testTag = "android:id/orderDetailsScreen" }
            ) {
                OrderDetailsCard(orderData = orderData, orderViewModel = orderViewModel)
                Spacer(modifier = Modifier.height(20.dp))

                when (orderData.status) {
                    "REJECTED" -> {
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
                    }
                    "CANCELLED" -> {
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
                    }
                    else -> {
                        if (orderData.status == "PREPARING") {
                            OrderStatusDropdown(orderData = orderData, orderViewModel = orderViewModel)
                        }
                        OrderStatusUpdates(orderData = orderData)
                    }
                }
            }
        }
    }

    if (showFulfillDialog && orderData != null) {
        val farmerItems = farmerItemViewModel.itemData.observeAsState().value ?: emptyList()
        val farmerInventory = ItemData(
            name = orderData.orderData.name,
            farmerName = farmerName,
            items = farmerItems.toMutableList()
        )

        DisplayFarmerFulfillDialog(
            navController = navController,
            onDismiss = { showFulfillDialog = false },
            farmerItemViewModel = farmerItemViewModel,
            orderViewModel = orderViewModel,
            orderData = orderData,
            itemData = farmerInventory,
            items = farmerItems,
            farmerName = farmerName
        )
    }
}

@Composable
fun OrderStatusDropdown(orderData: OrderData, orderViewModel: OrderViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf(orderData.statusDetail) }

    val statusOptions = when (orderData.statusDetail) {
        "PREPARING" -> listOf("PLANTING")
        "PLANTING" -> listOf("HARVESTING")
        "HARVESTING" -> listOf("READY_TO_DELIVER")
        "READY_TO_DELIVER" -> emptyList()
        else -> emptyList()
    }

    val statusColor = when (selectedStatus) {
        "PREPARING" -> Brown1
        "PLANTING" -> OliveGreen
        "HARVESTING" -> Green
        "READY_TO_DELIVER" -> SageGreen
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
                text = "Status Detail: ",
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
                                    //orderViewModel.updateOrder(orderData.copy(status = status))
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
    itemData: ItemData,
    items: List<ProductData>,
    farmerName: String
) {
    if (orderData.fulfilledBy.contains(farmerName)) {
        return
    }

    val remainingQuantity = orderData.partialQuantity?.let {
        orderData.orderData.quantity - it
    } ?: orderData.orderData.quantity

    val validRemainingQuantity = maxOf(remainingQuantity, 0)

    FarmerFulfillDialog(
        navController = navController,
        farmerName = farmerName,
        itemData = items,
        orderData = orderData,
        remainingQuantity = validRemainingQuantity,
        onAccept = {
            val updatedOrder = orderData.copy(
                status = "PREPARING",
                fulfilledBy = orderData.fulfilledBy + farmerName,
                partialQuantity = validRemainingQuantity
            )
            orderViewModel.updateOrder(updatedOrder)
            farmerItemViewModel.reduceItemQuantity(itemData, validRemainingQuantity)

            onDismiss()
        },
        onReject = {
            onDismiss()
        },
        onDismiss = onDismiss
    )
}

@Composable
fun OrderDetailsCard(
    orderData: OrderData,
    orderViewModel: OrderViewModel
) {
    val product = orderData.orderData

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { testTag = "android:id/orderDetailsCard" },
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Yellow4, Sand)
                    )
                )
        ) {
            Column {
                DisplayOrderDetail("Order ID", orderData.orderId.substring(6, 10).uppercase())
                DisplayOrderDetail("Item", product.name)
                DisplayOrderDetail("Target Date", orderData.targetDate)
                DisplayOrderDetail("Client Name", orderData.client)
                DisplayOrderDetail("Address", "${orderData.street}, ${orderData.barangay}")

                when (orderData.status) {
                    "PENDING" -> PendingStatusDialog(
                        orderData,
                        orderViewModel
                    )
                    "PREPARING" -> {
                        if (orderData.statusDetail == "READY_TO_DELIVER") {
                            PreparingStatusDialog(
                                orderData,
                                orderViewModel
                            )
                        }
                    }
                    "DELIVERING" -> DeliveringStatusDialog(
                        orderData,
                        orderViewModel
                    )
                    "COMPLETED" -> CompletedStatusDialog()
                }
            }
        }
    }
}

@Composable
fun DisplayOrderDetail (
    label: String,
    value: String
) {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            color = Cocoa,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
                .semantics { testTag = "android:id/deliveryAddressLabel" },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            fontSize = 20.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .semantics { testTag = "android:id/deliveryAddressText" },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun OrderStatusUpdates(orderData: OrderData) {
    val client = orderData.client
    val displayStatus = if (orderData.status == "RECEIVED") "COMPLETED" else orderData.status

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

            if (displayStatus in listOf("DELIVERING", "COMPLETED")) {
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

            if (displayStatus == "COMPLETED") {
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