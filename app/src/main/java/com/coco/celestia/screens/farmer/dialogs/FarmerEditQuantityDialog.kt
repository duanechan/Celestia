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
    currentPrice: Double,
    onDismiss: () -> Unit,
    onConfirm: (newQuantity: Int, newPrice: Double) -> Unit
) {
    var quantityToEdit by remember { mutableStateOf(currentQuantity.toString()) }
    var priceToEdit by remember { mutableStateOf(currentPrice.toString()) }
    var quantityError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val newQuantity = quantityToEdit.toIntOrNull()
                val newPrice = priceToEdit.toDoubleOrNull()

                if (newQuantity != null && newQuantity >= 0 && newPrice != null && newPrice >= 0) {
                    onConfirm(newQuantity, newPrice)
                    onDismiss()
                } else {
                    quantityError = newQuantity == null || newQuantity < 0
                    priceError = newPrice == null || newPrice < 0
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
        title = { Text(text = "Edit Details for $productName",
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

                Spacer(modifier = Modifier.height(20.dp))

                Text("Enter new price per kg:",
                    modifier = Modifier.semantics { testTag = "android:id/editPriceLabel" })
                Spacer(modifier = Modifier.height(10.dp))
                TextField(
                    value = priceToEdit,
                    onValueChange = {
                        priceToEdit = it
                        priceError = false
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text(text = "Enter price per kg",
                        modifier = Modifier.semantics { testTag = "android:id/editPricePlaceholder" }) },
                    isError = priceError,
                    modifier = Modifier.semantics { testTag = "android:id/editPriceTextField" }
                )
                if (priceError) {
                    Text(
                        text = "Please enter a valid non-negative number.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.semantics { testTag = "android:id/editPriceErrorText" }
                    )
                }
            }
        },
        properties = DialogProperties(dismissOnClickOutside = true),
        containerColor = Color(0xFFF2E3DB),
        modifier = Modifier.semantics { testTag = "android:id/editQuantityDialog" }
    )
}