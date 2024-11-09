package com.coco.celestia.screens.farmer.dialogs

import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.coco.celestia.ui.theme.*

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FarmerAddProductDialog(
    farmerName: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, quantity: Int, seasonStart: String, seasonEnd: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
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
                    println("Farmer's Name: $farmerName")
                    onConfirm(name, quantityInt!!, seasonStart, seasonEnd)
                    onDismiss()
                }
            },
                modifier = Modifier
                    .semantics { testTagsAsResourceId = true }
                    .semantics { testTag = "android:id/confirmButton" }) {
                Text("Confirm", color = OliveGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss,
                modifier = Modifier
                    .semantics { testTagsAsResourceId = true }
                    .semantics { testTag = "android:id/dismissButton" }) {
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
                    .semantics { testTagsAsResourceId = true }
                    .semantics { testTag = "android:id/addProductLabel" }
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Sand2)
                    .padding(16.dp)
            ) {
                // Product Name
                Text(text = "Enter Product Name", color = Cocoa, fontWeight = FontWeight.Bold)
                TextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    placeholder = { Text("Enter vegetable name", color = Cocoa.copy(alpha = 0.7f)) },
                    isError = nameError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .semantics { testTagsAsResourceId = true }
                        .semantics { testTag = "android:id/enterVegetableName" },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Apricot,
                        focusedContainerColor = Apricot,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )
                if (nameError) {
                    Text(
                        text = "Please enter a valid name.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .semantics { testTagsAsResourceId = true }
                            .semantics { testTag = "vegNameError" }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity Input
                Text(text = "Enter Quantity", color = Cocoa, fontWeight = FontWeight.Bold)
                TextField(
                    value = quantity,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            quantity = newValue
                            quantityError = false
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    trailingIcon = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(end = 8.dp, top = 4.dp, bottom = 4.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    val currentValue = quantity.toIntOrNull() ?: 0
                                    quantity = (currentValue + 1).toString()
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            ) {
                                Text("▲", fontSize = 10.sp, color = Cocoa)
                            }

                            IconButton(
                                onClick = {
                                    val currentValue = quantity.toIntOrNull() ?: 0
                                    if (currentValue > 1) {
                                        quantity = (currentValue - 1).toString()
                                    }
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            ) {
                                Text("▼", fontSize = 10.sp, color = Cocoa)
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Apricot,
                        focusedContainerColor = Apricot,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Cocoa.copy(alpha = 0.7f))
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
                TextField(
                    value = seasonStart,
                    onValueChange = {},
                    enabled = false,
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { isSeasonStartDropdownExpanded = true }
                        .semantics { testTagsAsResourceId = true }
                        .semantics { testTag = "android:id/selectStartMonth" },
                    placeholder = { Text("Select start month", color = Cocoa.copy(alpha = 0.7f)) },
                    colors = TextFieldDefaults.colors(
                        disabledContainerColor = Apricot,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )
                DropdownMenu(
                    expanded = isSeasonStartDropdownExpanded,
                    onDismissRequest = { isSeasonStartDropdownExpanded = false },
                    modifier = Modifier
                        .semantics { testTagsAsResourceId = true }
                        .semantics { testTag = "android:id/seasonStartDropdown" }
                ) {
                    months.forEach { month ->
                        DropdownMenuItem(
                            text = { Text(month) },
                            onClick = {
                                seasonStart = month
                                seasonStartError = false
                                isSeasonStartDropdownExpanded = false
                            },
                            modifier = Modifier
                                .semantics { testTagsAsResourceId = true }
                                .semantics { testTag = "android:id/seasonStartOption_$month" }
                        )
                    }
                }
                if (seasonStartError) {
                    Text(
                        text = "Please select a start month.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .semantics { testTagsAsResourceId = true }
                            .semantics { testTag = "android:id/seasonStartError" }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Season End Dropdown
                Text(text = "Select Season End", color = Cocoa, fontWeight = FontWeight.Bold)
                TextField(
                    value = seasonEnd,
                    onValueChange = {},
                    enabled = false,
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { isSeasonEndDropdownExpanded = true }
                        .semantics { testTagsAsResourceId = true }
                        .semantics { testTag = "android:id/seasonEndInput" },
                    placeholder = { Text("Select end month", color = Cocoa.copy(alpha = 0.7f)) },
                    colors = TextFieldDefaults.colors(
                        disabledContainerColor = Apricot,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent
                    )
                )
                DropdownMenu(
                    expanded = isSeasonEndDropdownExpanded,
                    onDismissRequest = { isSeasonEndDropdownExpanded = false },
                    modifier = Modifier
                        .semantics { testTagsAsResourceId = true }
                        .semantics { testTag = "android:id/seasonEndDropdown" }
                ) {
                    months.forEach { month ->
                        DropdownMenuItem(
                            text = { Text(month) },
                            onClick = {
                                seasonEnd = month
                                seasonEndError = false
                                isSeasonEndDropdownExpanded = false
                            },
                            modifier = Modifier
                                .semantics { testTagsAsResourceId = true }
                                .semantics { testTag = "android:id/seasonEndOption_$month" }
                        )
                    }
                }
                if (seasonEndError) {
                    Text(
                        text = "Please select an end month.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .semantics { testTagsAsResourceId = true }
                            .semantics { testTag = "seasonEndError" }
                    )
                }
            }
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        containerColor = Sand2
    )
}