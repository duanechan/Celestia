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
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FarmerItemViewModel
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.model.ItemData
import com.coco.celestia.viewmodel.model.OrderData

@Composable
fun FarmerFulfillDialog(
    navController: NavController,
    item: ItemData,
    orderViewModel: OrderViewModel,
    orderData: OrderData,
    farmerItemViewModel: FarmerItemViewModel,
    totalFarmers: Int,
    farmerName: String,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onDismiss: () -> Unit
) {
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
                    text = "Incomplete Fulfillment",
                    fontSize = 20.sp,
                    color = Cocoa,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { testTag = "android:id/dialogTitleText" }
                )
            }
        },
        text = {
            Text(
                text = "Would you like to fulfill the remaining quantity of this order?",
                fontSize = 13.sp,
                color = Cocoa,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "android:id/dialogText" }
            )
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onReject()
                    navController.popBackStack()
                },
                modifier = Modifier.semantics { testTag = "android:id/dialogRejectButton" }
            ) {
                Text(
                    text = "Refuse",
                    color = Copper,
                    fontSize = 16.sp,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedOrder = orderData.copy(status = "PREPARING", fulfilledBy = orderData.fulfilledBy + farmerName)
                    orderViewModel.updateOrder(updatedOrder)
                    farmerItemViewModel.reduceItemQuantity(item, totalFarmers)

                    onAccept()
                    navController.popBackStack()
                },
                modifier = Modifier.semantics { testTag = "android:id/dialogAcceptButton" }
            ) {
                Text(
                    text = "Accept",
                    color = SageGreen,
                    fontSize = 16.sp,
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Color(0xFFF2E3DB)
    )
}