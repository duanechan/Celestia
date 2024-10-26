package com.coco.celestia.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import com.coco.celestia.ui.theme.mintsansFontFamily

@Composable
fun UpdateOrderStatusDialog(status: String,onDismiss: () -> Unit, onAccept: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Update Order Status", fontWeight = FontWeight.Bold, fontFamily = mintsansFontFamily) },
        text = { Text(text = "Mark this status as ${status.lowercase()}?", fontFamily = mintsansFontFamily) },
        confirmButton = {
            Button(onClick = { onAccept() }) {
                Text(text = "Save", fontFamily = mintsansFontFamily)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = { onDismiss() }) {
                Text(text = "Cancel", fontFamily = mintsansFontFamily)
            }
        },
        modifier = Modifier.semantics { testTag = "android:id/UpdateOrderStatusDialog" }
    )
}