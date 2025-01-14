package com.coco.celestia.screens.farmer.details

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.coco.celestia.screens.farmer.dialogs.AcceptedStatusDialog
import com.coco.celestia.screens.farmer.dialogs.CompletedStatusDialog
import com.coco.celestia.screens.farmer.dialogs.DeliveringStatusDialog
import com.coco.celestia.screens.farmer.dialogs.HarvestingMeatStatusDialog
import com.coco.celestia.screens.farmer.dialogs.HarvestingStatusDialog
import com.coco.celestia.screens.farmer.dialogs.PendingStatusDialog
import com.coco.celestia.screens.farmer.dialogs.PlantingStatusDialog
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FarmerItemViewModel
import com.coco.celestia.viewmodel.UserViewModel
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
        Log.d("FarmerOrderDetails", "Fetched all orders: $allOrders")
    }

    val orderData: OrderData? = remember(orderId, allOrders) {
        allOrders.find { it.orderId == orderId }
    }

    Log.d("FarmerOrderDetails", "orderId: $orderId, orderData: $orderData")

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
                OrderDetailsCard(orderData = orderData, orderViewModel = orderViewModel, navController = navController)
                Spacer(modifier = Modifier.height(20.dp))

                when (orderData.status) {
                    "REJECTED" -> {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            text = "Rejection Reason\n ${orderData.rejectionReason}",
                            color = NylonRed,
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
                            color = NylonRed,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {
//                        OrderStatusUpdates(orderData = orderData)
                    }
                }
            }
        }
    }
}

@Composable
fun FarmerOrderMilestones(
    navController: NavController,
    orderId: String
) {
    val orderViewModel: OrderViewModel = viewModel()
    val allOrders by orderViewModel.orderData.observeAsState(emptyList())
    val orderData: OrderData? = remember(orderId, allOrders) {
        allOrders.find { it.orderId == orderId }
    }

    Log.d("FarmerOrderMilestones", "orderId: $orderId, orderData: $orderData")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Milestones for Order $orderId",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        orderData?.let {
            OrderStatusUpdates(orderData = it)
        } ?: run {
            Text(text = "Order data not found")
        }
    }
}

@Composable
fun OrderDetailsCard(
    orderData: OrderData,
    orderViewModel: OrderViewModel,
    navController: NavController
) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    val product = orderData.orderData
    var farmerName by remember { mutableStateOf("") }
    val farmerItemViewModel: FarmerItemViewModel = viewModel()
    var showCalamityDialog by remember { mutableStateOf(false) }

    // Allowed statuses for displaying the calamity button
    val allowedStatuses = listOf(
        "ACCEPTED",
        "PLANTING",
        "PLANTED",
        "GROWING",
        "READY_FOR_HARVEST",
        "HARVESTING",
        "HARVESTED"
    )

    LaunchedEffect(Unit) {
        farmerName = farmerItemViewModel.fetchFarmerName(uid)
    }

    val fulfilledByFarmer = orderData.fulfilledBy.find { it.farmerName == farmerName }

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
                        colors = listOf(Green4, White1)
                    )
                )
        ) {
            Column {
                DisplayOrderDetail("Order ID", orderData.orderId.substring(6, 10).uppercase())
                DisplayOrderDetail("Item", product[0].name)
                DisplayOrderDetail("Target Date", orderData.targetDate)
                DisplayOrderDetail("Client Name", orderData.client)
                DisplayOrderDetail("Address", "${orderData.street}, ${orderData.barangay}")

                // Track Progress button
                Button(
                    onClick = {
                        navController.navigate(Screen.FarmerOrderMilestones.createRoute(orderData.orderId))
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Track Progress")
                }

                // Display calamity-related UI if status matches allowed statuses
                if (orderData.status in allowedStatuses) {
                    CalamityAffectedStatus(
                        orderStatus = orderData.status,        // Pass the current order status
                        allowedStatuses = allowedStatuses,     // Pass the allowed statuses list
                        onNotifyClick = { showCalamityDialog = true }  // Lambda for the notify click action
                    )
                }

                if (showCalamityDialog) {
                    AlertDialog(
                        onDismissRequest = { showCalamityDialog = false },
                        confirmButton = {
                            TextButton(onClick = {
                                orderViewModel.tagOrderAsCalamityAffected(orderData)
                                showCalamityDialog = false
                            }) {
                                Text("Confirm")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showCalamityDialog = false }) {
                                Text("Cancel")
                            }
                        },
                        title = { Text("Confirm Action") },
                        text = { Text("Are you sure you want to tag this order as affected by calamity?") }
                    )
                }

                if (orderData.status == "PENDING") {
                    PendingStatusDialog(
                        orderData,
                        orderViewModel,
                        navController
                    )
                }

                if (orderData.status == "PARTIALLY_FULFILLED") {
                    if (fulfilledByFarmer != null) {
                        when (fulfilledByFarmer.status) {
                            "ACCEPTED" -> {
                                AcceptedStatusDialog(
                                    orderData,
                                    orderViewModel,
                                    "partial",
                                    orderData.orderData[0].type,
                                    fulfilledByFarmer
                                )
                            }

                            "PLANTING" -> {
                                PlantingStatusDialog(
                                    orderData,
                                    orderViewModel,
                                    "partial",
                                    fulfilledByFarmer
                                )
                            }

                            "HARVESTING" -> {
                                HarvestingStatusDialog(
                                    orderData,
                                    orderViewModel,
                                    "partial",
                                    fulfilledByFarmer
                                )
                            }

                            "HARVESTING_MEAT" -> {
                                HarvestingMeatStatusDialog(
                                    orderData,
                                    orderViewModel,
                                    "partial",
                                    fulfilledByFarmer
                                )
                            }

                            "DELIVERING" -> DeliveringStatusDialog(
                                orderData,
                                orderViewModel,
                                "partial",
                                fulfilledByFarmer
                            )

                            "COMPLETED" -> CompletedStatusDialog(
                                orderData,
                                orderViewModel,
                                "partial",
                                fulfilledByFarmer
                            )
                        }
                    }
                } else {
                    when (orderData.status) {
                        "ACCEPTED" -> {
                            AcceptedStatusDialog(
                                orderData,
                                orderViewModel,
                                "full",
                                orderData.orderData[0].type,
                                fulfilledByFarmer
                            )
                        }

                        "PLANTING" -> {
                            PlantingStatusDialog(
                                orderData,
                                orderViewModel,
                                "full",
                                fulfilledByFarmer
                            )
                        }

                        "HARVESTING" -> {
                            HarvestingStatusDialog(
                                orderData,
                                orderViewModel,
                                "full",
                                fulfilledByFarmer
                            )
                        }

                        "HARVESTING_MEAT" -> {
                            HarvestingMeatStatusDialog(
                                orderData,
                                orderViewModel,
                                "full",
                                fulfilledByFarmer
                            )
                        }

                        "DELIVERING" -> DeliveringStatusDialog(
                            orderData,
                            orderViewModel,
                            "full",
                            fulfilledByFarmer
                        )
                        "COMPLETED" -> CompletedStatusDialog(
                            orderData,
                            orderViewModel,
                            "full",
                            fulfilledByFarmer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DisplayOrderDetail(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
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
            fontSize = 15.sp,
            color = Cocoa,
            modifier = Modifier
                .weight(1f)
                .semantics { testTag = "android:id/deliveryAddressText" },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CalamityAffectedStatus(
    orderStatus: String,
    allowedStatuses: List<String>,
    onNotifyClick: () -> Unit
) {
    if (orderStatus in allowedStatuses) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = onNotifyClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF28C8C),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .wrapContentWidth()  // Adjust width to content size
                    .height(50.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.calamity),
                    contentDescription = "Notify Coop",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Notify Coop of Unforeseen Events",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

//            Text(
//                text = "Current Order Status: $orderStatus",
//                color = Color.Gray
//            )
        }
    }
}

@Composable
fun OrderStatusUpdates(orderData: OrderData) {
    val client = orderData.client
    var farmerName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    val farmerItemViewModel: FarmerItemViewModel = viewModel()
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val allowedStatuses = listOf(
        "ACCEPTED",
        "PLANTING",
        "PLANTED",
        "GROWING",
        "READY_FOR_HARVEST",
        "HARVESTING",
        "HARVESTED"
    )

    LaunchedEffect(Unit) {
        farmerName = farmerItemViewModel.fetchFarmerName(uid)
    }

    var displayStatus by remember { mutableStateOf("") }
    val fulfilledByFarmer = orderData.fulfilledBy.find { it.farmerName == farmerName }

    if (orderData.status == "PARTIALLY_FULFILLED") {
        if (fulfilledByFarmer != null) {
            displayStatus = fulfilledByFarmer.status
        }
    } else {
        displayStatus = if (orderData.status == "RECEIVED") "COMPLETED" else orderData.status
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
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
                val fulfilledByFarmer = orderData.fulfilledBy.find { it.farmerName == farmerName }
                if (fulfilledByFarmer != null) {
                    quantity = fulfilledByFarmer.quantityFulfilled.toString()
                }
                Text(
                    text = "Accepted order of ${quantity}kg of ${orderData.orderData[0].name}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            if (orderData.orderData[0].type == "Meat") {
                if (displayStatus in listOf("HARVESTING_MEAT", "DELIVERING", "COMPLETED")) {
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
                            painter = painterResource(id = R.drawable.cow_animal),
                            contentDescription = "Harvesting Meat",
                            modifier = Modifier.size(40.dp),
                            colorFilter = ColorFilter.tint(Cocoa)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Harvesting the animal.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                if (displayStatus in listOf("PLANTING", "HARVESTING", "DELIVERING", "COMPLETED")) {
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
                            painter = painterResource(id = R.drawable.planting),
                            contentDescription = "Planting",
                            modifier = Modifier.size(40.dp),
                            colorFilter = ColorFilter.tint(Cocoa)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Crop is currently being grown.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                if (displayStatus in listOf("HARVESTING", "DELIVERING", "COMPLETED")) {
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
                            painter = painterResource(id = R.drawable.harvest),
                            contentDescription = "Harvesting",
                            modifier = Modifier.size(40.dp),
                            colorFilter = ColorFilter.tint(Cocoa)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Harvesting of the Crop is underway.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
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
                        modifier = Modifier
                            .size(30.dp)
                            .padding(start = 3.dp),
                        colorFilter = ColorFilter.tint(OliveGreen)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Text(
                        text = "$client's order has been fulfilled",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    if (displayStatus in allowedStatuses) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Update Cooperative with Progress",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        OutlinedTextField(
                            value = "",
                            onValueChange = { /* Handle text change */ },
                            label = { Text("Description") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                        Button(
                            onClick = { /* Handle submit action */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(text = "Submit Progress Update")
                        }
                    }
                }
            }
        }
    }
}