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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag

@Composable
fun EditQuantityDialog(
    productName: String,
    currentQuantity: Int,
    onDismiss: () -> Unit,
    onConfirm: (newQuantity: Int) -> Unit
) {
    var quantityToEdit by remember { mutableStateOf(currentQuantity.toString()) }
    var quantityError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val newQuantity = quantityToEdit.toIntOrNull()
                if (newQuantity != null && newQuantity >= 0) {
                    onConfirm(newQuantity)
                    onDismiss()
                } else {
                    quantityError = true
                }
            },
                modifier = Modifier.semantics { testTag = "android:id/editQuantityConfirmButton" }) {
                Text("Confirm",
                    modifier = Modifier.semantics { testTag = "android:id/editQuantityConfirmButtonText" })
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel",
                    modifier = Modifier.semantics { testTag = "android:id/editQuantityDismissButtonText" })
            }
        },
        title = { Text(text = "Edit Quantity for $productName",
            modifier = Modifier.semantics { testTag = "android:id/editQuantityTitle" }
        ) },
        text = {
            Column(modifier = Modifier.semantics { testTag = "android:id/editQuantityContent" }) {
                Text("Enter new quantity:",
                    modifier = Modifier.semantics { testTag = "android:id/editQuantityLabel" })
                Spacer(modifier = Modifier.height(10.dp))
                TextField(
                    value = quantityToEdit,
                    onValueChange = {
                        quantityToEdit = it
                        quantityError = false
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    placeholder = { Text(text = "Enter quantity",
                        modifier = Modifier.semantics { testTag = "android:id/editQuantityPlaceholder" }) },
                    isError = quantityError,
                    modifier = Modifier.semantics { testTag = "android:id/editQuantityTextField" }
                )
                if (quantityError) {
                    Text(
                        text = "Please enter a valid non-negative number.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.semantics { testTag = "android:id/editQuantityErrorText" }
                    )
                }
            }
        },
        properties = DialogProperties(dismissOnClickOutside = true),
        containerColor = Color(0xFFF2E3DB),
        modifier = Modifier.semantics { testTag = "android:id/editQuantityDialog" }
    )
}