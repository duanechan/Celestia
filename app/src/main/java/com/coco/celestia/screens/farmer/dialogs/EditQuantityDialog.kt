package com.coco.celestia.screens.farmer.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.Color

@Composable
fun EditQuantityDialog(
    productName: String,
    currentQuantity: Int,
    onDismiss: () -> Unit,
    onConfirm: (newQuantity: Int) -> Unit
) {
    var quantityToEdit by remember { mutableStateOf(currentQuantity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val newQuantity = quantityToEdit.toIntOrNull() ?: currentQuantity
                onConfirm(newQuantity)
                onDismiss()
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text(text = "Edit Quantity for $productName") },
        text = {
            Column {
                Text("Enter new quantity:")
                Spacer(modifier = Modifier.height(10.dp))
                TextField(
                    value = quantityToEdit,
                    onValueChange = { quantityToEdit = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    placeholder = { Text(text = "Enter quantity") }
                )
            }
        },
        properties = DialogProperties(dismissOnClickOutside = true),
        containerColor = Color(0xFFF2E3DB)
    )
}