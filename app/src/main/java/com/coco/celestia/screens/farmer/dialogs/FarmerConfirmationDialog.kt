package com.coco.celestia.screens.farmer.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun FarmerConfirmationDialog(
    navController: NavController,
    isAccepted: Boolean,
    rejectionReason: String?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "android:id/dialogTitleColumn" }
            ) {
                Icon(
                    imageVector = if (isAccepted) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (isAccepted) "Checkmark Icon" else "Close Icon",
                    tint = if (isAccepted) Color.Green else Color.Red,
                    modifier = Modifier
                        .size(50.dp)
                        .semantics { testTag = "android:id/dialogIcon" }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isAccepted) "Order Accepted" else "Order Rejected",
                    fontSize = 20.sp,
                    color = Color(0xFF6D4A26),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { testTag = "android:id/dialogTitleText" }
                )
            }
        },
        text = {
            if (!isAccepted && rejectionReason != null) {
                Text(
                    text = "The order has been rejected.\n\nReason: \n$rejectionReason",
                    fontSize = 16.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/dialogRejectReasonText" }
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "android:id/dialogConfirmButtonBox" },
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier.semantics { testTag = "android:id/dialogConfirmButton" }
                ) {
                    Text(
                        text = "OK",
                        color = Color(0xFF4CAF50),
                        fontSize = 16.sp,
                        modifier = Modifier.semantics { testTag = "android:id/dialogConfirmButtonText" }
                    )
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color(0xFFF2E3DB)
    )
}