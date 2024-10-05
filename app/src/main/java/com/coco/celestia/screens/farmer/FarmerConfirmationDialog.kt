package com.coco.celestia.screens.farmer

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FarmerConfirmationDialog(
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isAccepted) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = if (isAccepted) "Checkmark Icon" else "Close Icon",
                    tint = if (isAccepted) Color.Green else Color.Red,
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isAccepted) "Order Accepted" else "Order Rejected",
                    fontSize = 20.sp,
                    color = Color(0xFF6D4A26),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        },
        text = {
            if (!isAccepted && rejectionReason != null) {
                Text(
                    text = "The order has been rejected.\n\nReason: \n$rejectionReason",
                    fontSize = 16.sp,
                    color = Color.Black,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(
                        text = "OK",
                        color = Color(0xFF4CAF50),
                        fontSize = 16.sp
                    )
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color(0xFFF2E3DB)
    )
}