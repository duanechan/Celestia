package com.coco.celestia.screens.farmer.dialogs

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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign

@Composable
fun FarmerDecisionDialog(
    decisionType: String,
    farmerName: String,
    availableQuantity: Int,
    orderedQuantity: Int,
    onConfirm: (String?, Boolean?, Int?) -> Unit,
    onDismiss: () -> Unit
) {
    println("Farmer Name: $farmerName")

    var selectedReason by remember { mutableStateOf<String?>(null) }
    var showReasonDropdown by remember { mutableStateOf(false) }
    var isPartialFulfillment by remember { mutableStateOf<Boolean?>(null) }
    var partialQuantity by remember { mutableStateOf("0") }
    var showMaxMessage by remember { mutableStateOf(false) }

    val rejectionReasons = listOf("Not in season", "Too Far", "Not Available")
    val maxPartialQuantity = orderedQuantity / 2

    val title = when (decisionType) {
        "ACCEPT" -> "Accept Order"
        "REJECT" -> "Reject Order"
        else -> ""
    }

    val message = when (decisionType) {
        "ACCEPT" -> "Are you sure you want to accept this order?"
        "REJECT" -> "Are you sure you want to reject this order?"
        else -> ""
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

                if (decisionType == "REJECT") {
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
                } else if (decisionType == "ACCEPT") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Fulfillment Type:",
                        color = Cocoa,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    Column {
                        if (availableQuantity >= orderedQuantity) {
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
                                                showMaxMessage = false
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

                                                if (newQuantity == maxPartialQuantity) {
                                                    showMaxMessage = true
                                                } else {
                                                    showMaxMessage = false
                                                }

                                                if (newQuantity <= maxPartialQuantity) {
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
                                            if (currentValue < maxPartialQuantity) {
                                                partialQuantity = (currentValue + 1).toString()
                                                showMaxMessage = false
                                            }
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Text("+", fontSize = 20.sp, color = Cocoa)
                                    }
                                }
                                if (showMaxMessage) {
                                    Text(
                                        text = "You have reached the max partial quantity to fulfill.",
                                        color = Color.Red,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            // Only show partial fulfill option if inventory is insufficient
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
                                                showMaxMessage = false
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

                                                if (newQuantity == availableQuantity) {
                                                    showMaxMessage = true
                                                } else {
                                                    showMaxMessage = false
                                                }

                                                if (newQuantity <= availableQuantity) {
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
                                            if (currentValue < availableQuantity) {
                                                partialQuantity = (currentValue + 1).toString()
                                                showMaxMessage = false
                                            }
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Text("+", fontSize = 20.sp, color = Cocoa)
                                    }
                                }
                                if (showMaxMessage) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "You have reached the maximum partial quantity to fulfill.",
                                        color = Cinnabar,
                                        fontSize = 12.sp
                                    )
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
                    if (decisionType == "REJECT" && selectedReason != null) {
                        onConfirm(selectedReason, null, null)
                    } else if (decisionType == "ACCEPT" && isPartialFulfillment != null) {
                        val quantity = if (isPartialFulfillment == true) {
                            partialQuantity.toIntOrNull() ?: 0
                        } else orderedQuantity
                        onConfirm(null, isPartialFulfillment, quantity)
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OliveGreen),
                enabled = (decisionType == "REJECT" && selectedReason != null) || (decisionType == "ACCEPT" && isPartialFulfillment != null)
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
}