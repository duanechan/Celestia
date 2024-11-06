package com.coco.celestia.screens.farmer.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData

@Composable
fun FarmerFulfillDialog(
    navController: NavController,
    itemData: List<ProductData>,
    orderData: OrderData,
    farmerName: String,
    remainingQuantity: Int,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onDismiss: () -> Unit
) {
    val productInInventory = itemData.find {
        it.name.equals(orderData.orderData.name, ignoreCase = true)
    }

    val farmerStock = productInInventory?.quantity ?: 0
    val canFulfill = remainingQuantity <= farmerStock

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "android:id/dialogTitleRow" }
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning Icon",
                    tint = Cocoa,
                    modifier = Modifier
                        .size(30.dp)
                        .semantics { testTag = "android:id/dialogIcon" }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Fulfill Order Request",
                    fontSize = 20.sp,
                    color = Cocoa,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { testTag = "android:id/dialogTitleText" }
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = farmerName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cocoa,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { testTag = "android:id/dialogFarmerName" }
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Product: ${orderData.orderData.name}",
                    fontSize = 14.sp,
                    color = Cocoa,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { testTag = "android:id/dialogProductName" }
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ordered: ${orderData.orderData.quantity} kg",
                    fontSize = 13.sp,
                    color = Cocoa,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { testTag = "android:id/dialogOrderQuantity" }
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your Current Stock: $farmerStock kg",
                    fontSize = 13.sp,
                    color = Cocoa,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { testTag = "android:id/dialogFarmerStock" }
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Remaining to Fulfill: $remainingQuantity kg",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Cocoa,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { testTag = "android:id/dialogRemainingQuantity" }
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (canFulfill) {
                        "You have enough stock to fulfill this order."
                    } else {
                        "You do not have enough stock to fulfill this order."
                    },
                    color = if (canFulfill) OliveGreen else Copper,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { testTag = "android:id/dialogStatusMessage" }
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (canFulfill) {
                        onReject()
                    }
                    navController.popBackStack()
                },
                modifier = Modifier.semantics {
                    testTag = if (canFulfill) "android:id/dialogRejectButton" else "android:id/dialogOkButton"
                }
            ) {
                Text(
                    text = if (canFulfill) "Refuse" else "Ok",
                    color = Copper,
                    fontSize = 16.sp,
                )
            }
        },
        confirmButton = {
            if (canFulfill) {
                TextButton(
                    onClick = {
                        onAccept()
                        navController.popBackStack()
                    },
                    modifier = Modifier.semantics { testTag = "android:id/dialogAcceptButton" }
                ) {
                    Text(
                        text = "Accept",
                        color = OliveGreen,
                        fontSize = 16.sp,
                    )
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color(0xFFF2E3DB)
    )
}