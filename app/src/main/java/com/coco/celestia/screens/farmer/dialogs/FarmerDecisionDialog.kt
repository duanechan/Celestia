package com.coco.celestia.screens.farmer.dialogs

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.ui.theme.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.viewmodel.FarmerItemViewModel
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.model.FullFilledBy
import com.coco.celestia.viewmodel.model.OrderData
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FarmerDecisionDialog(
    decisionType: String,
    orderData: OrderData,
    orderViewModel: OrderViewModel,
    navController: NavController,
    onDismiss: () -> Unit,
    farmerItemViewModel: FarmerItemViewModel = viewModel()
) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    var farmerName by remember { mutableStateOf("") }
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var showReasonDropdown by remember { mutableStateOf(false) }
    var showFulfillmentDialog by remember { mutableStateOf(false) }
    var isPartialFulfillment by remember { mutableStateOf<Boolean?>(null) }
    var partialQuantity by remember { mutableStateOf("0") }
    val maxPartial = (orderData.orderData.quantity - orderData.partialQuantity) * 0.8f

    val rejectionReasons = listOf("Not in season", "Too Far", "Not Available")

    val title = when (decisionType) {
        "Accept" -> "Accept Order"
        "Reject" -> "Reject Order"
        else -> ""
    }

    val message = when (decisionType) {
        "Accept" -> "Are you sure you want to accept this order?"
        "Reject" -> "Are you sure you want to reject this order?"
        else -> ""
    }

    LaunchedEffect(Unit) {
        farmerName = farmerItemViewModel.fetchFarmerName(uid)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontSize = 20.sp,
                color = Cocoa,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column {
                Text(
                    text = message,
                    fontSize = 16.sp,
                    color = Cocoa
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    showFulfillmentDialog = true
                },
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = "Confirm",
                    fontWeight = FontWeight.Bold,
                    color = Cocoa,
                    fontSize = 16.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.Bold,
                    color = Cocoa,
                    fontSize = 16.sp
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Sand2
    )

    if (showFulfillmentDialog) {
        AlertDialog(
            onDismissRequest = { showFulfillmentDialog = false },
            text = {
                Column {
                    if (decisionType == "Reject") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Reason",
                            fontSize = 20.sp,
                            color = Cocoa,
                            fontWeight = FontWeight.Bold
                        )

                        Box {
                            OutlinedButton(
                                onClick = { showReasonDropdown = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Apricot,
                                    contentColor = Cocoa
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, color = Apricot)
                            ) {
                                Text(
                                    text = selectedReason ?: "Select reason",
                                    color = if (selectedReason == null) Cocoa.copy(alpha = 0.8f) else Cocoa
                                )
                            }
                            DropdownMenu(
                                expanded = showReasonDropdown,
                                onDismissRequest = { showReasonDropdown = false }
                            ) {
                                rejectionReasons.forEach { reason ->
                                    DropdownMenuItem(
                                        text = { Text(text = reason) },
                                        onClick = {
                                            selectedReason = reason
                                            showReasonDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        if (selectedReason == null) {
                            Spacer(modifier = Modifier.height(15.dp))
                            Text(
                                text = "You must select a reason before rejecting.",
                                color = Cinnabar,
                                fontSize = 12.sp
                            )
                        }
                    } else if (decisionType == "Accept") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Fulfillment Type:",
                            color = Cocoa,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isPartialFulfillment == false,
                                    onClick = { isPartialFulfillment = false }
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Full Fulfill",
                                    color = Cocoa,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isPartialFulfillment == true,
                                    onClick = { isPartialFulfillment = true }
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Partial Fulfill",
                                    color = Cocoa,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (isPartialFulfillment == true) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            val currentValue = partialQuantity.toIntOrNull() ?: 0
                                            if (currentValue > 0) {
                                                partialQuantity = (currentValue - 1).toString()
                                            }
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Text("-", fontSize = 20.sp, color = Cocoa)
                                    }
                                    TextField(
                                        value = partialQuantity,
                                        onValueChange = { newValue ->
                                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                                val newQuantity = newValue.toIntOrNull() ?: 0
                                                if (newQuantity <= maxPartial) {
                                                    partialQuantity = newValue
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(color = Apricot, shape = RoundedCornerShape(8.dp)),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Apricot,
                                            unfocusedContainerColor = Apricot,
                                            disabledContainerColor = Apricot,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        label = { Text("Quantity", color = Cocoa, textAlign = TextAlign.Center) },
                                        textStyle = TextStyle(color = Cocoa, textAlign = TextAlign.Center)
                                    )

                                    IconButton(
                                        onClick = {
                                            val currentValue = partialQuantity.toIntOrNull() ?: 0
                                            if (currentValue <= maxPartial) {
                                                partialQuantity = (currentValue + 1).toString()
                                            }
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Text("+", fontSize = 20.sp, color = Cocoa)
                                    }
                                }
                            }
                        }

                        if (isPartialFulfillment == null) {
                            Spacer(modifier = Modifier.height(15.dp))
                            Text(
                                text = "You must select a fulfillment type.",
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (decisionType == "Reject" && selectedReason != null) {
                            val updatedOrder = orderData.copy(
                                status = "REJECTED",
                                rejectionReason = selectedReason
                            )
                            orderViewModel.updateOrder(updatedOrder)
                            showFulfillmentDialog = false
                        } else if (decisionType == "Accept" && isPartialFulfillment != null) {
                            if (isPartialFulfillment == true) {
                                val partialToInt = partialQuantity.toIntOrNull() ?: 0
                                val fulfilled = orderData.fulfilled + partialToInt
                                val fulFiller = FullFilledBy (
                                    farmerName = farmerName,
                                    quantityFulfilled = partialToInt,
                                    status = "ACCEPTED"
                                )
                                val updatedOrder = orderData.copy(
                                    status = "PARTIALLY_FULFILLED",
                                    fulfilledBy = orderData.fulfilledBy.plus(fulFiller),
                                    partialQuantity = fulfilled
                                )
                                orderViewModel.updateOrder(updatedOrder)
                            } else {
                                if (orderData.status == "PARTIALLY_FULFILLED") {
                                    val fulFiller = FullFilledBy (
                                        farmerName = farmerName,
                                        quantityFulfilled = orderData.orderData.quantity - orderData.partialQuantity,
                                        status = "ACCEPTED"
                                    )
                                    val updatedOrder = orderData.copy(
                                        fulfilledBy = orderData.fulfilledBy.plus(fulFiller),
                                        partialQuantity = orderData.orderData.quantity
                                    )
                                    orderViewModel.updateOrder(updatedOrder)

                                } else {
                                    val fulFiller = FullFilledBy (
                                        farmerName = farmerName,
                                        quantityFulfilled = orderData.orderData.quantity - orderData.partialQuantity
                                    )
                                    val updatedOrder = orderData.copy(
                                        status = "ACCEPTED",
                                        fulfilledBy = orderData.fulfilledBy.plus(fulFiller),
                                        partialQuantity = orderData.orderData.quantity
                                    )
                                    orderViewModel.updateOrder(updatedOrder)
                                }
                            }
                            showFulfillmentDialog = false
                            navController.navigate(Screen.FarmerManageOrder.route)
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OliveGreen),
                    enabled = (decisionType == "Reject" && selectedReason != null) || (decisionType == "Accept" && isPartialFulfillment != null)
                ) {
                    Text(
                        text = "Confirm",
                        fontWeight = FontWeight.Bold,
                        color = Apricot,
                        fontSize = 16.sp
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFulfillmentDialog = false },
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.Bold,
                        color = Cocoa,
                        fontSize = 16.sp
                    )
                }
            }
        )
    }
}