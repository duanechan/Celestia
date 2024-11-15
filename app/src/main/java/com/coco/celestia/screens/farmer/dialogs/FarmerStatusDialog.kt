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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.R
import com.coco.celestia.components.dialogs.PendingOrderDialog
import com.coco.celestia.components.dialogs.UpdateOrderStatusDialog
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.ui.theme.Brown1
import com.coco.celestia.ui.theme.Cinnabar
import com.coco.celestia.ui.theme.DeliveringStatus
import com.coco.celestia.ui.theme.Green
import com.coco.celestia.ui.theme.JadeGreen
import com.coco.celestia.ui.theme.SageGreen
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.model.FullFilledBy
import com.coco.celestia.viewmodel.model.OrderData

@Composable
fun PendingStatusDialog (
    orderData: OrderData,
    orderViewModel: OrderViewModel
) {
    var onUpdateOrder by remember { mutableStateOf(Triple(ToastStatus.INFO, "", 0L)) }
    var showDialog by remember { mutableStateOf(false) }
    var showFulfillmentDialog by remember { mutableStateOf(false) }
    var action by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray)
            .padding(15.dp)
            .semantics { testTag = "android:id/PendingOrderActions" },
        horizontalArrangement = Arrangement.SpaceAround,
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
                    .background(Cinnabar)
                    .semantics { testTag = "android:id/RejectButton" }
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Reject",
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(text = "Reject", modifier = Modifier.padding(top = 16.dp), color = Color.White)
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
                    .background(JadeGreen)
                    .semantics { testTag = "android:id/AcceptButton" }
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Check",
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()

                )
            }
            Text(text = "Accept", modifier = Modifier.padding(top = 16.dp), color = Color.White)
        }
    }

    if (showDialog) {
        PendingOrderDialog(
            order = orderData,
            action = action,
            onDismiss = { showDialog = false },
            onAccept = {
                showFulfillmentDialog = true
                onUpdateOrder = (Triple(ToastStatus.SUCCESSFUL, "Order updated successfully!", System.currentTimeMillis()))
                showDialog = false
            }
        )
    }

    if (showFulfillmentDialog) {
        FarmerDecisionDialog(
            decisionType = action,
            orderData = orderData,
            orderViewModel = orderViewModel,
            onDismiss = { showFulfillmentDialog = false }
        )
    }
}

@Composable
fun PreparingStatusDialog(
    orderData: OrderData,
    orderViewModel: OrderViewModel
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
            text = "Ship this order?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .padding(8.dp, 0.dp)
                .semantics { testTag = "android:id/ShipOrderText" }
        )
        IconButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(DeliveringStatus)
                .semantics { testTag = "android:id/ShipOrderButton" }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.deliveryicon),
                contentDescription = "Deliver",
                tint = Color.White,
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
                orderViewModel.updateOrder(orderData.copy(status = "DELIVERING"))
                onUpdateOrder = (Triple(ToastStatus.SUCCESSFUL, "Order updated successfully!", System.currentTimeMillis()))
                showDialog = false
            }
        )
    }
}

@Composable
fun AcceptedStatusDialog(
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
            text = "Plant Order Request?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .padding(8.dp, 0.dp)
                .semantics { testTag = "android:id/ShipOrderText" }
        )
        IconButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(DeliveringStatus)
                .semantics { testTag = "android:id/ShipOrderButton" }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.planting),
                contentDescription = "Plant",
                tint = Color.White,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }
    }

    if (showDialog) {
        UpdateOrderStatusDialog(
            status = "PLANTING",
            onDismiss = { showDialog = false },
            onAccept = {
                if (status == "partial") {
                    val updatedFulfilledBy = orderData.fulfilledBy.map { fulFiller ->
                        if (fulFiller.farmerName == (fulfilledByFarmer?.farmerName ?: "")) {
                            fulFiller.copy(status = "PLANTING")
                        } else {
                            fulFiller
                        }
                    }
                    orderViewModel.updateOrder(orderData.copy(fulfilledBy = updatedFulfilledBy))
                    onUpdateOrder = (Triple(ToastStatus.SUCCESSFUL, "Order updated successfully!", System.currentTimeMillis()))
                    showDialog = false
                } else {
                    orderViewModel.updateOrder(orderData.copy(status = "PLANTING"))
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
            color = Color.White,
            modifier = Modifier
                .padding(8.dp, 0.dp)
                .semantics { testTag = "android:id/ShipOrderText" }
        )
        IconButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(DeliveringStatus)
                .semantics { testTag = "android:id/ShipOrderButton" }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.harvest),
                contentDescription = "Harvest",
                tint = Color.White,
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
            color = Color.White,
            modifier = Modifier
                .padding(8.dp, 0.dp)
                .semantics { testTag = "android:id/ShipOrderText" }
        )
        IconButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(DeliveringStatus)
                .semantics { testTag = "android:id/ShipOrderButton" }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.deliveryicon),
                contentDescription = "Delivery",
                tint = Color.White,
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
                color = Color.White,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .semantics { testTag = "android:id/OrderDeliveryText" }
            )
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Deliver",
                tint = Color.White,
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
                tint = Color.White,
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
            color = Color.White,
            modifier = Modifier.padding(8.dp, 0.dp)
        )
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Deliver",
            tint = Color.White,
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