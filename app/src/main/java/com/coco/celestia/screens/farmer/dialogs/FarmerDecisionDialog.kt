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
    onConfirm: (String?, Boolean?, Int?) -> Unit,
    onDismiss: () -> Unit
) {
    println("Farmer Name: $farmerName")

    var selectedReason by remember { mutableStateOf<String?>(null) }
    var showReasonDropdown by remember { mutableStateOf(false) }
    var isPartialFulfillment by remember { mutableStateOf<Boolean?>(null) }
    var partialQuantity by remember { mutableStateOf("0") }

    val rejectionReasons = listOf("Not in season", "Too Far", "Not Available")

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
                modifier = Modifier.fillMaxWidth().semantics { testTag = "android:id/dialogTitle" }
            )
        },
        text = {
            Column(modifier = Modifier.semantics { testTag = "android:id/dialogContent" }) {
                Text(
                    text = message,
                    fontSize = 16.sp,
                    color = Cocoa,
                    modifier = Modifier.semantics { testTag = "android:id/dialogMessage" }
                )

                if (decisionType == "REJECT") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Reason",
                        fontSize = 20.sp,
                        color = Cocoa,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { testTag = "android:id/dialogReasonLabel" }
                    )

                    Box(modifier = Modifier.semantics { testTag = "android:id/dialogReasonBox" }) {
                        OutlinedButton(
                            onClick = { showReasonDropdown = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { testTag = "android:id/dialogReasonButton" },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Apricot,
                                contentColor = Cocoa
                            ),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, color = Apricot)
                        ) {
                            Text(
                                text = selectedReason ?: "Select reason",
                                color = if (selectedReason == null) Cocoa.copy(alpha = 0.8f) else Cocoa,
                                modifier = Modifier.semantics { testTag = "android:id/SelectedReasonText" }
                            )
                        }
                        DropdownMenu(
                            expanded = showReasonDropdown,
                            onDismissRequest = { showReasonDropdown = false },
                            modifier = Modifier.semantics { testTag = "android:id/dialogReasonDropdown" }
                        ) {
                            rejectionReasons.forEach { reason ->
                                DropdownMenuItem(
                                    text = { Text(text = reason) },
                                    onClick = {
                                        selectedReason = reason
                                        showReasonDropdown = false
                                    },
                                    modifier = Modifier.semantics { testTag = "android:id/dialogReasonItem_$reason" }
                                )
                            }
                        }
                    }

                    if (selectedReason == null) {
                        Spacer(modifier = Modifier.height(15.dp))
                        Text(
                            text = "You must select a reason before rejecting.",
                            color = Cinnabar,
                            fontSize = 12.sp,
                            modifier = Modifier.semantics { testTag = "android:id/dialogReasonWarning" }
                        )
                    }
                } else if (decisionType == "ACCEPT") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Fulfillment Type:",
                        color = Cocoa,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.semantics { testTag = "android:id/dialogFulfillmentLabel" }
                    )

                    Column(modifier = Modifier.semantics { testTag = "android:id/dialogFulfillmentOptions" }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.semantics { testTag = "android:id/dialogFulfillmentOption" }
                        ) {
                            RadioButton(
                                selected = isPartialFulfillment == false,
                                onClick = { isPartialFulfillment = false },
                                modifier = Modifier.semantics { testTag = "android:id/dialogFullFulfillRadioButton" }
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Full Fulfill",
                                color = Cocoa,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.semantics { testTag = "android:id/dialogFullFulfillText" }
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.semantics { testTag = "android:id/dialogPartialFulfillOption" }
                        ) {
                            RadioButton(
                                selected = isPartialFulfillment == true,
                                onClick = { isPartialFulfillment = true },
                                modifier = Modifier.semantics { testTag = "android:id/dialogPartialFulfillRadioButton" }
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Partial Fulfill",
                                color = Cocoa,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.semantics { testTag = "android:id/dialogPartialFulfillText" }
                            )
                        }

                        if (isPartialFulfillment == true) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics { testTag = "android:id/partialQuantityInput" },
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
                                    modifier = Modifier
                                        .size(40.dp)
                                        .semantics { testTag = "android:id/decrementButton" }
                                ) {
                                    Text("-", fontSize = 20.sp, color = Cocoa)
                                }

                                TextField(
                                    value = partialQuantity,
                                    onValueChange = { newValue ->
                                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                            partialQuantity = newValue
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .semantics { testTag = "android:id/quantityTextField" }
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
                                    label = { Text("Quantity", color = Cocoa, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                                    textStyle = TextStyle(color = Cocoa, textAlign = TextAlign.Center)
                                )

                                IconButton(
                                    onClick = {
                                        val currentValue = partialQuantity.toIntOrNull() ?: 0
                                        partialQuantity = (currentValue + 1).toString()
                                    },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .semantics { testTag = "android:id/incrementButton" }
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
                            fontSize = 12.sp,
                            modifier = Modifier.semantics { testTag = "android:id/dialogFulfillmentWarning" }
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
                        } else null
                        onConfirm(null, isPartialFulfillment, quantity)
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OliveGreen),
                enabled = when {
                    decisionType == "REJECT" -> selectedReason != null
                    decisionType == "ACCEPT" && isPartialFulfillment == true -> partialQuantity.toIntOrNull()?.let { it > 0 } ?: false
                    decisionType == "ACCEPT" -> isPartialFulfillment == false
                    else -> false
                },
                modifier = Modifier.semantics { testTag = "android:id/dialogConfirmButton" }
            ) {
                Text(
                    "Confirm",
                    color = Apricot,
                    fontSize = 16.sp,
                    modifier = Modifier.semantics { testTag = "android:id/dialogConfirmButtonText" }
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.semantics { testTag = "android:id/dialogDismissButton" }
            ) {
                Text(
                    "Cancel",
                    color = Cocoa,
                    fontSize = 16.sp,
                    modifier = Modifier.semantics { testTag = "android:id/dialogDismissButtonText" }
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Sand2,
        modifier = Modifier.semantics { testTag = "android:id/farmerDecisionDialog" }
    )
}