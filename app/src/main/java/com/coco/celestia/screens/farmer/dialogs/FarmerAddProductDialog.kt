package com.coco.celestia.screens.farmer.dialogs

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TextField
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import com.coco.celestia.ui.theme.*

@Composable
fun FarmerAddProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, quantity: Int, seasonStart: String, seasonEnd: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var seasonStart by remember { mutableStateOf("") }
    var seasonEnd by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var quantityError by remember { mutableStateOf(false) }
    var seasonStartError by remember { mutableStateOf(false) }
    var seasonEndError by remember { mutableStateOf(false) }
    var isSeasonStartDropdownExpanded by remember { mutableStateOf(false) }
    var isSeasonEndDropdownExpanded by remember { mutableStateOf(false) }

    // List of months
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val quantityInt = quantity.toIntOrNull()

                // Validate inputs
                nameError = name.isBlank()
                quantityError = quantityInt == null || quantityInt <= 0
                seasonStartError = seasonStart.isBlank()
                seasonEndError = seasonEnd.isBlank()

                if (!nameError && !quantityError && !seasonStartError && !seasonEndError) {
                    onConfirm(name, quantityInt!!, seasonStart, seasonEnd)
                    onDismiss()
                }
            }) {
                Text("Confirm", color = SageGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Copper)
            }
        },
        title = {
            Text(
                text = "Add Product",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Cocoa,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Product Name Input
                Text(text = "Enter Product Name", color = Cocoa, fontWeight = FontWeight.Bold)
                TextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    placeholder = { Text("Enter vegetable name", color = Color.Gray) },
                    isError = nameError,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Apricot,
                        focusedContainerColor = Apricot
                    )
                )
                if (nameError) {
                    Text(
                        text = "Please enter a valid name.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity Input
                Text(text = "Enter Quantity", color = Cocoa, fontWeight = FontWeight.Bold)
                TextField(
                    value = quantity,
                    onValueChange = {
                        quantity = it
                        quantityError = false
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    placeholder = { Text("Enter quantity", color = Color.Gray) },
                    isError = quantityError,
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Apricot,
                        focusedContainerColor = Apricot
                    )
                )
                if (quantityError) {
                    Text(
                        text = "Please enter a valid positive number.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Season Start Dropdown
                Text(text = "Select Season Start", color = Cocoa, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = seasonStart,
                    onValueChange = {},
                    enabled = false,
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isSeasonStartDropdownExpanded = true },
                    placeholder = { Text("Select start month") },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledContainerColor = Apricot,
                    )
                )
                DropdownMenu(
                    expanded = isSeasonStartDropdownExpanded,
                    onDismissRequest = { isSeasonStartDropdownExpanded = false }
                ) {
                    months.forEach { month ->
                        DropdownMenuItem(
                            text = { Text(month) },
                            onClick = {
                                seasonStart = month
                                seasonStartError = false
                                isSeasonStartDropdownExpanded = false
                            }
                        )
                    }
                }
                if (seasonStartError) {
                    Text(
                        text = "Please select a start month.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Season End Dropdown
                Text(text = "Select Season End", color = Cocoa, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = seasonEnd,
                    onValueChange = {}, // No input changes directly
                    enabled = false,
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isSeasonEndDropdownExpanded = true },
                    placeholder = { Text("Select end month") },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledContainerColor = Apricot,
                    )
                )
                DropdownMenu(
                    expanded = isSeasonEndDropdownExpanded,
                    onDismissRequest = { isSeasonEndDropdownExpanded = false }
                ) {
                    months.forEach { month ->
                        DropdownMenuItem(
                            text = { Text(month) },
                            onClick = {
                                seasonEnd = month
                                seasonEndError = false
                                isSeasonEndDropdownExpanded = false
                            }
                        )
                    }
                }
                if (seasonEndError) {
                    Text(
                        text = "Please select an end month.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        properties = DialogProperties(dismissOnClickOutside = true),
        containerColor = BgColor
    )
}