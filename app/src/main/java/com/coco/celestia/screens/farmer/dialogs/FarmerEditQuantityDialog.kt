package com.coco.celestia.screens.farmer.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.coco.celestia.ui.theme.*

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
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OliveGreen),
                modifier = Modifier.semantics { testTag = "android:id/editQuantityConfirmButton" }) {
                Text("Confirm", color = Apricot,
                    modifier = Modifier.semantics { testTag = "android:id/editQuantityConfirmButtonText" })
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Cocoa,
                    modifier = Modifier.semantics { testTag = "android:id/editQuantityDismissButtonText" })
            }
        },
        title = { Text(text = "Edit Details for $productName", color = Cocoa, fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { testTag = "android:id/editQuantityTitle" }
        ) },
        text = {
            Column(modifier = Modifier.semantics { testTag = "android:id/editQuantityContent" }) {
                Text("Enter new quantity:", color = Cocoa, fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { testTag = "android:id/editQuantityLabel" })
                Spacer(modifier = Modifier.height(10.dp))
                TextField(
                    value = quantityToEdit,
                    onValueChange = {
                        quantityToEdit = it
                        quantityError = false
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    placeholder = {
                        Text(
                            text = "Enter quantity",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    isError = quantityError,
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Apricot,
                        unfocusedContainerColor = Apricot,
                        disabledContainerColor = Apricot,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/editQuantityTextField" },
                    textStyle = LocalTextStyle.current.copy(color = Cocoa, textAlign = TextAlign.Center),
                    leadingIcon = {
                        IconButton(onClick = {
                            quantityToEdit = (quantityToEdit.toIntOrNull()?.minus(1)?.takeIf { it >= 0 } ?: 0).toString()
                            quantityError = false
                        }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease Quantity", tint = Cocoa)
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            quantityToEdit = (quantityToEdit.toIntOrNull()?.plus(1) ?: 1).toString()
                            quantityError = false
                        }) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase Quantity", tint = Cocoa)
                        }
                    }
                )
                if (quantityError) {
                    Text(
                        text = "Please enter a valid non-negative number.",
                        color = Cinnabar,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.semantics { testTag = "android:id/editQuantityErrorText" }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text("Enter price per kg", color = Cocoa, fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { testTag = "android:id/editPriceLabel" })
                Spacer(modifier = Modifier.height(10.dp))
                TextField(
                    value = priceToEdit,
                    onValueChange = {
                        priceToEdit = it
                        priceError = false
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                    placeholder = {
                        Text(
                            text = "Enter price per kg",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    isError = priceError,
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Apricot,
                        unfocusedContainerColor = Apricot,
                        disabledContainerColor = Apricot,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/editPriceTextField" },
                    textStyle = LocalTextStyle.current.copy(color = Cocoa, textAlign = TextAlign.Center),
                    leadingIcon = {
                        IconButton(onClick = {
                            priceToEdit = (priceToEdit.toDoubleOrNull()?.minus(1)?.takeIf { it >= 0 } ?: 0.0).toString()
                            priceError = false
                        }) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease Price", tint = Cocoa)
                        }
                    },
                    trailingIcon = {
                        IconButton(onClick = {
                            priceToEdit = (priceToEdit.toDoubleOrNull()?.plus(1) ?: 1.0).toString()
                            priceError = false
                        }) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase Price", tint = Cocoa)
                        }
                    }
                )
                if (priceError) {
                    Text(
                        text = "Please enter a valid non-negative number.",
                        color = Cinnabar,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.semantics { testTag = "android:id/editPriceErrorText" }
                    )
                }
            }
        },
        properties = DialogProperties(dismissOnClickOutside = true),
        containerColor = Sand2,
        modifier = Modifier.semantics { testTag = "android:id/editQuantityDialog" }
    )
}