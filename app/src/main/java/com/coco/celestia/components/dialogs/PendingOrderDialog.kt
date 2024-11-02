package com.coco.celestia.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import com.coco.celestia.ui.theme.mintsansFontFamily
import com.coco.celestia.viewmodel.model.OrderData

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PendingOrderDialog(order: OrderData, action: String, onDismiss: () -> Unit, onAccept: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Order ${order.orderId.substring(6,10).uppercase()}", fontWeight = FontWeight.Bold, fontFamily = mintsansFontFamily) },
        text = { Text(text = "Are you sure you want to ${action.lowercase()} this order?", fontFamily = mintsansFontFamily) },
        confirmButton = {
            Button(onClick = { onAccept() },
                modifier = Modifier
                    .semantics { testTagsAsResourceId = true }
                    .semantics { testTag = "android:id/PendingOrderConfirmButton" }) {
                Text(action)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { onDismiss() },
                modifier = Modifier
                    .semantics { testTagsAsResourceId = true }
                    .semantics { testTag = "android:id/PendingOrderDismissButton" }) {
                Text("Cancel")
            }
        },
        modifier = Modifier.semantics { testTag = "android:id/PendingOrderDialog" }
    )
}