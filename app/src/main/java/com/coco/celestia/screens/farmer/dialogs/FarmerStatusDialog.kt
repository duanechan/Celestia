package com.coco.celestia.screens.farmer.dialogs

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.components.dialogs.PendingOrderDialog
import com.coco.celestia.components.dialogs.UpdateOrderStatusDialog
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.ui.theme.Brown1
import com.coco.celestia.ui.theme.Green
import com.coco.celestia.ui.theme.SageGreen
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.model.FullFilledBy
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.ui.theme.*

@Composable
fun PendingStatusDialog(
    orderData: OrderData,
    orderViewModel: OrderViewModel,
    navController: NavController
) {
    var onUpdateOrder by remember { mutableStateOf(Triple(ToastStatus.INFO, "", 0L)) }
    var showDialog by remember { mutableStateOf(false) }
    var showFulfillmentDialog by remember { mutableStateOf(false) }
    var action by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = Sand,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                )
                .padding(vertical = 60.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Would you like to fulfill this order?",
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleMedium,
                    color = Cocoa,
                    modifier = Modifier.padding(bottom = 50.dp),
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp)
                        .semantics { testTag = "android:id/PendingOrderActions" },
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = {
                                action = "Reject"
                                showFulfillmentDialog = true
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(Copper)
                                .semantics { testTag = "android:id/RejectButton" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Reject",
                                tint = Cocoa,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(text = "Reject", modifier = Modifier.padding(top = 8.dp), color = Cocoa)
                    }
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = {
                                action = "Accept"
                                showFulfillmentDialog = true
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(SageGreen)
                                .semantics { testTag = "android:id/AcceptButton" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Check",
                                tint = Cocoa,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(text = "Accept", modifier = Modifier.padding(top = 8.dp), color = Cocoa)
                    }
                }
                Spacer(modifier = Modifier.height(200.dp))
            }
        }
    }

    if (showDialog) {
        PendingOrderDialog(
            order = orderData,
            action = action,
            onDismiss = { showDialog = false },
            onAccept = {
                showFulfillmentDialog = true
                onUpdateOrder = Triple(ToastStatus.SUCCESSFUL, "Order updated successfully!", System.currentTimeMillis())
                showDialog = false
            }
        )
    }

    if (showFulfillmentDialog) {
        FarmerDecisionDialog(
            decisionType = action,
            orderData = orderData,
            orderViewModel = orderViewModel,
            navController = navController,
            onDismiss = { showFulfillmentDialog = false }
        )
    }
}

@Composable
fun AcceptedStatusDialog(
    orderData: OrderData,
    orderViewModel: OrderViewModel,
    status: String,
    type: String,
    fulfilledByFarmer: FullFilledBy?
) {
    var onUpdateOrder by remember { mutableStateOf(Triple(ToastStatus.INFO, "", 0L)) }
    var showDialog by remember { mutableStateOf(false) }

    val text = when (type) {
        "Coffee" -> "Plant Order Request?"
        "Vegetable" -> "Plant Order Request?"
        "Meat" -> "Give the animal a peaceful end"
        else -> ""
    }

    val setStatus = when (type) {
        "Coffee" -> "PLANTING"
        "Vegetable" -> "PLANTING"
        "Meat" -> "HARVESTING_MEAT"
        else -> ""
    }

    val iconPainter: Painter? = when (type) {
        "Coffee" -> painterResource(id = R.drawable.coffeeicon)
        "Vegetable" -> painterResource(id = R.drawable.planting)
        "Meat" -> painterResource(id = R.drawable.meaticon)
        else -> null
    }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brown1)
            .padding(8.dp)
            .semantics { testTag = "android:id/PreparingOrderActions" },
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Cocoa,
            modifier = Modifier
                .padding(8.dp, 0.dp)
                .semantics { testTag = "android:id/ShipOrderText" }
        )
        IconButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Blue)
                .semantics { testTag = "android:id/ShipOrderButton" }
        ) {
            Icon(
                painter = iconPainter!!,
                contentDescription = "Plant",
                tint = Cocoa,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
    }

    if (showDialog) {
        UpdateOrderStatusDialog(
            status = setStatus,
            onDismiss = { showDialog = false },
            onAccept = {
                if (status == "partial") {
                    val updatedFulfilledBy = orderData.fulfilledBy.map { fulFiller ->
                        if (fulFiller.farmerName == (fulfilledByFarmer?.farmerName ?: "")) {
                            fulFiller.copy(status = setStatus)
                        } else {
                            fulFiller
                        }
                    }
                    orderViewModel.updateOrder(orderData.copy(fulfilledBy = updatedFulfilledBy))
                    onUpdateOrder = (Triple(ToastStatus.SUCCESSFUL, "Order updated successfully!", System.currentTimeMillis()))
                    showDialog = false
                } else {
                    orderViewModel.updateOrder(orderData.copy(status = setStatus))
                    onUpdateOrder = (Triple(ToastStatus.SUCCESSFUL, "Order updated successfully!", System.currentTimeMillis()))
                    showDialog = false
                }
            }
        )
    }
}

@Composable
fun PlantingStatusDialog(
    orderData: OrderData,
    orderViewModel: OrderViewModel,
    status: String,
    fulfilledByFarmer: FullFilledBy?
) {
    var onUpdateOrder by remember { mutableStateOf(Triple(ToastStatus.INFO, "", 0L)) }
    var showDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brown1)
            .padding(8.dp)
            .semantics { testTag = "android:id/PreparingOrderActions" },
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Harvest Grown Crops?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Cocoa,
            modifier = Modifier
                .padding(8.dp, 0.dp)
                .semantics { testTag = "android:id/ShipOrderText" }
        )
        IconButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Blue)
                .semantics { testTag = "android:id/ShipOrderButton" }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.harvest),
                contentDescription = "Harvest",
                tint = Cocoa,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
    }

    if (showDialog) {
        UpdateOrderStatusDialog(
            status = "HARVESTING",
            onDismiss = { showDialog = false },
            onAccept = {
                if (status == "partial") {
                    val updatedFulfilledBy = orderData.fulfilledBy.map { fulFiller ->
                        if (fulFiller.farmerName == (fulfilledByFarmer?.farmerName ?: "")) {
                            fulFiller.copy(status = "HARVESTING")
                        } else {
                            fulFiller
                        }
                    }
                    orderViewModel.updateOrder(orderData.copy(fulfilledBy = updatedFulfilledBy))
                    onUpdateOrder = (Triple(ToastStatus.SUCCESSFUL, "Order updated successfully!", System.currentTimeMillis()))
                    showDialog = false
                } else {
                    orderViewModel.updateOrder(orderData.copy(status = "HARVESTING"))
                    onUpdateOrder = (Triple(ToastStatus.SUCCESSFUL, "Order updated successfully!", System.currentTimeMillis()))
                    showDialog = false
                }

            }
        )
    }
}

@Composable
fun HarvestingStatusDialog(
    orderData: OrderData,
    orderViewModel: OrderViewModel,
    status: String,
    fulfilledByFarmer: FullFilledBy?
) {
    var onUpdateOrder by remember { mutableStateOf(Triple(ToastStatus.INFO, "", 0L)) }
    var showDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brown1)
            .padding(8.dp)
            .semantics { testTag = "android:id/PreparingOrderActions" },
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Ship harvested order?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Cocoa,
            modifier = Modifier
                .padding(8.dp, 0.dp)
                .semantics { testTag = "android:id/ShipOrderText" }
        )
        IconButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Blue)
                .semantics { testTag = "android:id/ShipOrderButton" }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.deliveryicon),
                contentDescription = "Delivery",
                tint = Cocoa,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
    }

    if (showDialog) {
        UpdateOrderStatusDialog(
            status = "DELIVERING",
            onDismiss = { showDialog = false },
            onAccept = {
                if (status == "partial") {
                    val updatedFulfilledBy = orderData.fulfilledBy.map { fulFiller ->
                        if (fulFiller.farmerName == (fulfilledByFarmer?.farmerName ?: "")) {
                            fulFiller.copy(status = "DELIVERING")
                        } else {
                            fulFiller
                        }
                    }
                    orderViewModel.updateOrder(orderData.copy(fulfilledBy = updatedFulfilledBy))
                    onUpdateOrder = (Triple(ToastStatus.SUCCESSFUL, "Order updated successfully!", System.currentTimeMillis()))
                    showDialog = false
                } else {
                    orderViewModel.updateOrder(orderData.copy(status = "DELIVERING"))
                    onUpdateOrder = (Triple(ToastStatus.SUCCESSFUL, "Order updated successfully!", System.currentTimeMillis()))
                    showDialog = false
                }
            }
        )
    }
}

@Composable
fun HarvestingMeatStatusDialog(
    orderData: OrderData,
    orderViewModel: OrderViewModel,
    status: String,
    fulfilledByFarmer: FullFilledBy?
) {
    var onUpdateOrder by remember { mutableStateOf(Triple(ToastStatus.INFO, "", 0L)) }
    var showDialog by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brown1)
            .padding(8.dp)
            .semantics { testTag = "android:id/PreparingOrderActions" },
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Ship Harvested Order?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Cocoa,
            modifier = Modifier
                .padding(8.dp, 0.dp)
                .semantics { testTag = "android:id/ShipOrderText" }
        )
        IconButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Blue)
                .semantics { testTag = "android:id/ShipOrderButton" }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.deliveryicon),
                contentDescription = "Deliver",
                tint = Cocoa,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
    }

    if (showDialog) {
        UpdateOrderStatusDialog(
            status = "DELIVERING",
            onDismiss = { showDialog = false },
            onAccept = {
                if (status == "partial") {
                    val updatedFulfilledBy = orderData.fulfilledBy.map { fulFiller ->
                        if (fulFiller.farmerName == (fulfilledByFarmer?.farmerName ?: "")) {
                            fulFiller.copy(status = "DELIVERING")
                        } else {
                            fulFiller
                        }
                    }
                    orderViewModel.updateOrder(orderData.copy(fulfilledBy = updatedFulfilledBy))
                    onUpdateOrder = (Triple(ToastStatus.SUCCESSFUL, "Order updated successfully!", System.currentTimeMillis()))
                    showDialog = false
                } else {
                    orderViewModel.updateOrder(orderData.copy(status = "DELIVERING"))
                    onUpdateOrder = (Triple(ToastStatus.SUCCESSFUL, "Order updated successfully!", System.currentTimeMillis()))
                    showDialog = false
                }
            }
        )
    }
}

@Composable
fun DeliveringStatusDialog (
    orderData: OrderData,
    orderViewModel: OrderViewModel,
    status: String,
    fulfilledByFarmer: FullFilledBy?
) {
    var onUpdateOrder by remember { mutableStateOf(Triple(ToastStatus.INFO, "", 0L)) }
    var showDialog by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "Bouncing animation")
    val bouncingAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -2f,
        animationSpec = infiniteRepeatable(
            animation = tween(200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bouncing animation"
    )
    val fadeEffect by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                0.0f at 0 with LinearEasing
                1.0f at 1000 with LinearEasing
                1.0f at 2000 with LinearEasing
                0.0f at 3000 with LinearEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "Fade effect"
    )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Green)
                .padding(8.dp)
                .semantics { testTag = "android:id/DeliveringOrderActions" },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Order is being delivered.",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Cocoa,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .semantics { testTag = "android:id/OrderDeliveryText" }
            )
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Deliver",
                tint = Cocoa,
                modifier = Modifier
                    .size(50.dp)
                    .rotate(90f)
                    .padding(8.dp)
                    .offset(y = (-15).dp)
                    .alpha(fadeEffect)
            )
            Icon(
                painter = painterResource(id = R.drawable.deliveryicon),
                contentDescription = "Deliver",
                tint = Cocoa,
                modifier = Modifier
                    .size(50.dp)
                    .padding(8.dp)
                    .offset(y = bouncingAnimation.dp)
                    .clickable { showDialog = true }
            )
        }
    }

    if (showDialog) {
        UpdateOrderStatusDialog(
            status = "COMPLETED",
            onDismiss = { showDialog = false },
            onAccept = {
                if (status == "partial") {
                    val updatedFulfilledBy = orderData.fulfilledBy.map { fulFiller ->
                        if (fulFiller.farmerName == (fulfilledByFarmer?.farmerName ?: "")) {
                            fulFiller.copy(status = "COMPLETED")
                        } else {
                            fulFiller
                        }
                    }
                    orderViewModel.updateOrder(orderData.copy(fulfilledBy = updatedFulfilledBy))
                    onUpdateOrder = (Triple(ToastStatus.SUCCESSFUL, "Order updated successfully!", System.currentTimeMillis()))
                    showDialog = false
                } else {
                    orderViewModel.updateOrder(orderData.copy(status = "COMPLETED"))
                    onUpdateOrder = (Triple(ToastStatus.SUCCESSFUL, "Order updated successfully!", System.currentTimeMillis()))
                    showDialog = false
                }
            }
        )
    }
}

@Composable
fun CompletedStatusDialog (
    orderData: OrderData,
    orderViewModel: OrderViewModel,
    status: String,
    fulfilledByFarmer: FullFilledBy?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SageGreen)
            .padding(8.dp)
            .semantics { testTag = "android:id/CompletedOrderActions" },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Order successfully delivered.",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Cocoa,
            modifier = Modifier.padding(8.dp, 0.dp)
        )
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Deliver",
            tint = Cocoa,
            modifier = Modifier
                .size(50.dp)
                .padding(8.dp)
        )
    }
    if (status == "partial") {
        val updatedFulfilledBy = orderData.fulfilledBy.map { fulFiller ->
            if (fulFiller.farmerName == (fulfilledByFarmer?.farmerName ?: "")) {
                fulFiller.copy(status = "COMPLETED")
            } else {
                fulFiller
            }
        }
        val allFulFilled = updatedFulfilledBy.all { it.status == "COMPLETED" }
        if (allFulFilled) {
            orderViewModel.updateOrder(orderData.copy(status = "COMPLETED"))
        }
    }
}