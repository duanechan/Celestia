package com.coco.celestia.screens.farmer.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.ui.theme.*
import androidx.compose.ui.Alignment

@Composable
fun FarmerDecisionDialog(
    decisionType: String,
    onConfirm: (String?, Boolean?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedReason by remember { mutableStateOf<String?>(null) }
    var showReasonDropdown by remember { mutableStateOf(false) }
    var isPartialFulfillment by remember { mutableStateOf<Boolean?>(null) } // State to track full or partial

    val rejectionReasons = listOf("Out of stock", "Too Far", "Not Available")

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
            Text(text = title, fontSize = 20.sp, color = Color(0xFF6D4A26))
        },
        text = {
            Column {
                Text(text = message, fontSize = 16.sp)

                if (decisionType == "REJECT") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Reason:", fontSize = 20.sp)

                    Box {
                        OutlinedButton(
                            onClick = { showReasonDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = selectedReason ?: "Select reason",
                                color = if (selectedReason == null) Color.Gray else Color.Black
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
                            color = Color.Red,
                            fontSize = 12.sp
                        )
                    }
                } else if (decisionType == "ACCEPT") {
                    // Full or partial fulfillment options
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Fulfillment Type:", fontSize = 20.sp)

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isPartialFulfillment == false,
                                onClick = { isPartialFulfillment = false }
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(text = "Full Fulfill")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isPartialFulfillment == true,
                                onClick = { isPartialFulfillment = true }
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(text = "Partial Fulfill")
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
                        onConfirm(selectedReason, null)
                    } else if (decisionType == "ACCEPT" && isPartialFulfillment != null) {
                        onConfirm(null, isPartialFulfillment)
                    }
                },
                enabled = decisionType != "REJECT" || selectedReason != null
            ) {
                Text("Confirm", color = SageGreen, fontSize = 16.sp)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel", color = Copper, fontSize = 16.sp)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color(0xFFF2E3DB)
    )
}