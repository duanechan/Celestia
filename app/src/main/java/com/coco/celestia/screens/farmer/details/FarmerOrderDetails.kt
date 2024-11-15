package com.coco.celestia.screens.farmer.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.coco.celestia.screens.farmer.dialogs.FarmerFulfillDialog
import com.coco.celestia.screens.farmer.dialogs.HarvestingStatusDialog
import com.coco.celestia.screens.farmer.dialogs.PendingStatusDialog
import com.coco.celestia.screens.farmer.dialogs.PlantingStatusDialog
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FarmerItemViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.FullFilledBy
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
    val fulfilledByFarmer = orderData?.fulfilledBy?.find { it.farmerName == farmerName }

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
    if (orderData.fulfilledBy.any { it.farmerName == farmerName }) {
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
            val fulFiller = FullFilledBy(
                farmerName = farmerName,
                quantityFulfilled = validRemainingQuantity
            )
            val updatedOrder = orderData.copy(
                status = "PREPARING",
                fulfilledBy = orderData.fulfilledBy.plus(fulFiller),
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
    val uid = FirebaseAuth.getInstance().uid.toString()
    val product = orderData.orderData
    var farmerName by remember { mutableStateOf("") }
    val farmerItemViewModel: FarmerItemViewModel = viewModel()

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

                if (orderData.status == "PENDING") {
                    PendingStatusDialog(
                        orderData,
                        orderViewModel
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
    var farmerName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    val farmerItemViewModel: FarmerItemViewModel = viewModel()
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()

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
                val fulfilledByFarmer = orderData.fulfilledBy.find { it.farmerName == farmerName }
                if (fulfilledByFarmer != null) {
                    quantity = fulfilledByFarmer.quantityFulfilled.toString()
                }
                Text(
                    text = "Accepted order of ${quantity}kg of ${orderData.orderData.name}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

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