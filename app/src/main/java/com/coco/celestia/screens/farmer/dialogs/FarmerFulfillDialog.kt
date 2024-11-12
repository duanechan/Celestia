package com.coco.celestia.screens.farmer.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    println("Farmer Name: $farmerName")

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
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Fulfill Order Request",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Cocoa,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = Apricot, shape = RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Client", fontSize = 17.sp, color = Cocoa)
                            Text(text = orderData.client, fontSize = 17.sp, color = Cocoa)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Product", fontSize = 17.sp, color = Cocoa)
                            Text(text = orderData.orderData.name, fontSize = 17.sp, color = Cocoa)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Ordered", fontSize = 17.sp, color = Cocoa)
                            Text(text = "${orderData.orderData.quantity} kg", fontSize = 17.sp, color = Cocoa)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Your Current Stock", fontSize = 17.sp, color = Cocoa, fontWeight = FontWeight.Bold)
                            Text(text = "$farmerStock kg", fontSize = 17.sp, color = Cocoa, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Remaining to Fulfill", fontSize = 17.sp, color = Cocoa, fontWeight = FontWeight.Bold)
                            Text(text = "$remainingQuantity kg", fontSize = 17.sp, color = Cocoa, fontWeight = FontWeight.Bold)
                        }
                    }
                }

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
                    textAlign = TextAlign.Center
                )
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    if (canFulfill) {
                        onReject()
                    }
                    navController.popBackStack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Sand2),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = if (canFulfill) "Refuse" else "Ok",
                    color = Cocoa,
                    fontSize = 16.sp
                )
            }
        },
        confirmButton = {
            if (canFulfill) {
                Button(
                    onClick = {
                        onAccept()
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OliveGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = "Accept", color = Apricot, fontSize = 14.sp)
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = Sand2
    )
}