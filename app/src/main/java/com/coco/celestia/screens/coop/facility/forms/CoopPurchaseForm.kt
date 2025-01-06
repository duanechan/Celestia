package com.coco.celestia.screens.coop.facility.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.coco.celestia.viewmodel.PurchaseOrderViewModel
import com.coco.celestia.viewmodel.VendorViewModel
import com.coco.celestia.viewmodel.model.PurchaseOrder
import com.coco.celestia.viewmodel.model.PurchaseOrderItem
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.VendorState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Surface
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.coco.celestia.screens.`object`.Screen
import java.time.DayOfWeek
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

// TODO: Add checks for every field
// TODO: Dropdown for accounts (idk what categories to put there)

@Composable
fun CoopPurchaseForm(
    purchaseOrderViewModel: PurchaseOrderViewModel,
    vendorViewModel: VendorViewModel,
    facilityName: String,
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    navController: NavController,
    draftId: String? = null,
    purchaseNumber: String? = null,
    modifier: Modifier = Modifier
) {
    var purchaseOrderData by remember {
        mutableStateOf(
            PurchaseOrder(
                vendor = "",
                purchaseNumber = generatePurchaseNumber(),
                referenceNumber = "",
                dateAdded = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                expectedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                shipmentPreference = "",
                customerNotes = "",
                termsAndConditions = "",
                items = emptyList(),
                facility = facilityName,
                status = "processing",
                savedAsDraft = false
            )
        )
    }

    var hasErrors by remember { mutableStateOf(false) }
    var showErrorMessages by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var items by remember { mutableStateOf<List<PurchaseOrderItem>>(emptyList()) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showShipmentDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(draftId, purchaseNumber) {
        try {
            when {
                purchaseNumber != null -> {
                    val existingOrder = purchaseOrderViewModel.getPurchaseOrder(purchaseNumber)
                    existingOrder?.let { order ->
                        purchaseOrderData = order
                        items = order.items
                    }
                }
                draftId != null -> {
                    val draft = purchaseOrderViewModel.getPurchaseOrder(draftId)
                    draft?.let { draftOrder ->
                        purchaseOrderData = draftOrder
                        items = draftOrder.items
                    }
                }
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load data: ${e.message}"
        }
    }

    val shipmentOptions = listOf("Taxi", "Public Utility Jeepney", "Private Vehicle")
    val isEdit = !purchaseNumber.isNullOrEmpty()

    LaunchedEffect(Unit) {
        vendorViewModel.fetchVendors(facilityName = facilityName)
    }

    fun validateForm(): Boolean {
        return purchaseOrderData.purchaseNumber.isNotBlank() &&
                purchaseOrderData.referenceNumber.isNotBlank() &&
                purchaseOrderData.expectedDate.isNotBlank() &&
                purchaseOrderData.shipmentPreference.isNotBlank() &&
                items.isNotEmpty()
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        cursorColor = Green1,
        focusedBorderColor = Green1,
        unfocusedBorderColor = Green1,
        focusedLabelColor = Green1,
        unfocusedLabelColor = Green1,
    )

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Error") },
            text = { Text(errorMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    if (isLoading) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Processing Purchase Order") },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text("Please wait...")
                }
            },
            confirmButton = { }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(White2)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = White1
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PurchaseFormHeader(
                    purchaseOrderData = purchaseOrderData,
                    onPurchaseOrderChange = { purchaseOrderData = it },
                    vendorViewModel = vendorViewModel,
                    hasErrors = hasErrors,
                    showErrorMessages = showErrorMessages,
                    textFieldColors = textFieldColors,
                    isEditingDraft = draftId != null && !isEdit
                )

                // Shipment Preference Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = purchaseOrderData.shipmentPreference,
                        onValueChange = { },
                        label = { Text("Shipment Preference") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showShipmentDropdown = !showShipmentDropdown }) {
                                Icon(
                                    if (showShipmentDropdown) Icons.Default.KeyboardArrowUp
                                    else Icons.Default.KeyboardArrowDown,
                                    "Toggle shipment dropdown"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showShipmentDropdown = !showShipmentDropdown },
                        colors = textFieldColors,
                        isError = showErrorMessages && purchaseOrderData.shipmentPreference.isBlank(),
                        supportingText = if (showErrorMessages && purchaseOrderData.shipmentPreference.isBlank()) {
                            { Text("Shipment preference is required") }
                        } else null
                    )

                    DropdownMenu(
                        expanded = showShipmentDropdown,
                        onDismissRequest = { showShipmentDropdown = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        shipmentOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                onClick = {
                                    purchaseOrderData = purchaseOrderData.copy(shipmentPreference = option)
                                    showShipmentDropdown = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Items Section
                PurchaseFormItemsSection(
                    items = items,
                    onShowAddItem = { showAddItemDialog = true },
                    onRemoveItem = { index ->
                        items = items.filterIndexed { i, _ -> i != index }
                        hasErrors = !validateForm()
                    }
                )

                // Notes Section
                OutlinedTextField(
                    value = purchaseOrderData.customerNotes,
                    onValueChange = { purchaseOrderData = purchaseOrderData.copy(customerNotes = it) },
                    label = { Text("Customer Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = purchaseOrderData.termsAndConditions,
                    onValueChange = { purchaseOrderData = purchaseOrderData.copy(termsAndConditions = it) },
                    label = { Text("Terms and Conditions") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = textFieldColors
                )

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text(
                            text = "Cancel",
                            color = Green1
                        )
                    }

                    // Show "Save as Draft" button when not in edit mode (new purchase or draft)
                    if (!isEdit) {
                        OutlinedButton(
                            onClick = {
                                isLoading = true
                                purchaseOrderViewModel.addPurchaseOrder(
                                    purchaseOrder = purchaseOrderData.copy(
                                        items = items,
                                        savedAsDraft = true,
                                        status = "draft"
                                    ),
                                    onSuccess = {
                                        isLoading = false
                                        onSuccess()
                                        navController.navigate(Screen.CoopPurchases.route) {
                                            popUpTo(Screen.CoopPurchases.route) { inclusive = true }
                                        }
                                    },
                                    onError = { error ->
                                        isLoading = false
                                        errorMessage = error
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isLoading,
                            border = BorderStroke(1.dp, Green1)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Save as Draft",
                                    color = Green1,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            showErrorMessages = true
                            if (!hasErrors && validateForm()) {
                                isLoading = true
                                purchaseOrderViewModel.addPurchaseOrder(
                                    purchaseOrder = purchaseOrderData.copy(
                                        items = items,
                                        savedAsDraft = false,
                                        status = if (isEdit) purchaseOrderData.status else "processing"
                                    ),
                                    onSuccess = {
                                        isLoading = false
                                        onSuccess()
                                        navController.navigate(Screen.CoopPurchases.route) {
                                            popUpTo(Screen.CoopPurchases.route) { inclusive = true }
                                        }
                                    },
                                    onError = { error ->
                                        isLoading = false
                                        errorMessage = error
                                    }
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green1,
                            contentColor = Color.White
                        )
                    ) {
                        Text(if (isEdit) "Update" else "Submit")
                    }
                }
            }
        }
    }

    if (showAddItemDialog) {
        PurchaseOrderItemForm(
            onAddItem = { newItem ->
                items = items + newItem
                hasErrors = !validateForm()
            },
            onDismiss = { showAddItemDialog = false }
        )
    }
}

@Composable
fun PurchaseFormHeader(
    purchaseOrderData: PurchaseOrder,
    onPurchaseOrderChange: (PurchaseOrder) -> Unit,
    vendorViewModel: VendorViewModel,
    hasErrors: Boolean,
    showErrorMessages: Boolean,
    textFieldColors: TextFieldColors,
    isEditingDraft: Boolean = false
) {
    var showVendorDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Select Expected Date") },
            text = {
                CalendarPicker(
                    selectedDate = purchaseOrderData.expectedDate,
                    onDateSelected = { newDate ->
                        onPurchaseOrderChange(purchaseOrderData.copy(expectedDate = newDate))
                    },
                    onDismiss = { showDatePicker = false }
                )
            },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Done")
                }
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isEditingDraft) "Edit Purchase Order" else "Purchase Order Information",
            style = MaterialTheme.typography.titleMedium,
            color = Green1
        )

        OutlinedTextField(
            value = purchaseOrderData.purchaseNumber,
            onValueChange = { },
            label = { Text("Purchase Number") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = purchaseOrderData.vendor,
                onValueChange = { },
                label = { Text("Select Vendor") },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { showVendorDropdown = !showVendorDropdown }) {
                        Icon(
                            if (showVendorDropdown) Icons.Default.KeyboardArrowUp
                            else Icons.Default.KeyboardArrowDown,
                            "Toggle vendor dropdown"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showVendorDropdown = !showVendorDropdown },
                colors = textFieldColors,
                isError = showErrorMessages && purchaseOrderData.vendor.isBlank(),
                supportingText = if (showErrorMessages && purchaseOrderData.vendor.isBlank()) {
                    { Text("Vendor is required") }
                } else null
            )

            DropdownMenu(
                expanded = showVendorDropdown,
                onDismissRequest = { showVendorDropdown = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                when (val state = vendorViewModel.vendorState.value) {
                    is VendorState.SUCCESS -> {
                        vendorViewModel.vendorData.value?.forEach { vendor ->
                            val vendorName = "${vendor.firstName} ${vendor.lastName}".trim()
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = vendorName,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                onClick = {
                                    onPurchaseOrderChange(purchaseOrderData.copy(vendor = vendorName))
                                    showVendorDropdown = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    is VendorState.LOADING -> {
                        DropdownMenuItem(
                            text = { Text("Loading vendors...") },
                            onClick = { },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> {
                        DropdownMenuItem(
                            text = { Text("No vendors available") },
                            onClick = { },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = purchaseOrderData.referenceNumber,
            onValueChange = { onPurchaseOrderChange(purchaseOrderData.copy(referenceNumber = it)) },
            label = { Text("Reference Number") },
            isError = showErrorMessages && purchaseOrderData.referenceNumber.isBlank(),
            supportingText = {
                if (showErrorMessages && purchaseOrderData.referenceNumber.isBlank()) {
                    Text("Reference number is required")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors
        )

        OutlinedTextField(
            value = purchaseOrderData.expectedDate,
            onValueChange = { },
            label = { Text("Expected Date") },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.DateRange, "Select expected date")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            colors = textFieldColors
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarPicker(
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedYear by remember { mutableStateOf(LocalDate.now().year) }
    var selectedMonth by remember { mutableStateOf(LocalDate.now().monthValue - 1) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Year selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { selectedYear-- }) {
                Icon(Icons.Default.KeyboardArrowLeft, "Previous year")
            }
            Text(selectedYear.toString(), style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { selectedYear++ }) {
                Icon(Icons.Default.KeyboardArrowRight, "Next year")
            }
        }

        // Month selection
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(12) { month ->
                val monthName = Month.of(month + 1).getDisplayName(TextStyle.SHORT, Locale.getDefault())
                FilterChip(
                    selected = selectedMonth == month,
                    onClick = { selectedMonth = month },
                    label = { Text(monthName) }
                )
            }
        }

        // Days grid
        val daysInMonth = YearMonth.of(selectedYear, selectedMonth + 1).lengthOfMonth()
        val firstDayOfMonth = LocalDate.of(selectedYear, selectedMonth + 1, 1).dayOfWeek.value % 7

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Day names
            items(7) { dayIndex ->
                val dayName = DayOfWeek.of(((dayIndex + 1) % 7) + 1)
                    .getDisplayName(TextStyle.SHORT, Locale.getDefault())
                Text(
                    text = dayName,
                    modifier = Modifier.padding(vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            // Empty spaces before first day
            items(firstDayOfMonth) {
                Box {}
            }

            // Days of the month
            items(daysInMonth) { day ->
                val date = LocalDate.of(selectedYear, selectedMonth + 1, day + 1)
                val formattedDate = date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                val isSelected = formattedDate == selectedDate

                Surface(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clickable {
                            onDateSelected(formattedDate)
                        },
                    shape = CircleShape,
                    color = if (isSelected) Green1 else Color.Transparent,
                    border = if (!isSelected) BorderStroke(1.dp, Green1) else null
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = (day + 1).toString(),
                            color = if (isSelected) Color.White else Color.Unspecified,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseFormItemsSection(
    items: List<PurchaseOrderItem>,
    onShowAddItem: () -> Unit,
    onRemoveItem: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Items",
            style = MaterialTheme.typography.titleMedium,
            color = Green1
        )

        Button(
            onClick = onShowAddItem,
            colors = ButtonDefaults.buttonColors(
                containerColor = Green1,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Item")
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
        ) {
            items(items.size) { index ->
                val item = items[index]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.itemName,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onRemoveItem(index) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Remove item",
                                    tint = Color.Red
                                )
                            }
                        }

                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Qty: ${item.quantity}")
                            Text("Rate: ${formatCurrency(item.rate)}")
                            Text("Total: ${formatCurrency(item.quantity * item.rate)}")
                        }
                    }
                }
            }
        }

        if (items.isNotEmpty()) {
            val totalAmount = items.sumOf { it.quantity * it.rate }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Total Amount",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        formatCurrency(totalAmount),
                        style = MaterialTheme.typography.titleMedium,
                        color = Green1
                    )
                }
            }
        }
    }
}

@Composable
fun PurchaseOrderItemForm(
    onAddItem: (PurchaseOrderItem) -> Unit,
    onDismiss: () -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var account by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var showErrorMessages by remember { mutableStateOf(false) }

    fun validateForm(): Boolean {
        return itemName.isNotBlank() &&
                description.isNotBlank() &&
                account.isNotBlank() &&
                quantity.isNotBlank() &&
                rate.isNotBlank() &&
                quantity.toIntOrNull() != null &&
                rate.toDoubleOrNull() != null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    cursorColor = Green1,
                    focusedBorderColor = Green1,
                    unfocusedBorderColor = Green1,
                    focusedLabelColor = Green1,
                    unfocusedLabelColor = Green1,
                )

                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name") },
                    isError = showErrorMessages && itemName.isBlank(),
                    supportingText = {
                        if (showErrorMessages && itemName.isBlank()) {
                            Text("Item name is required")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    isError = showErrorMessages && description.isBlank(),
                    supportingText = {
                        if (showErrorMessages && description.isBlank()) {
                            Text("Description is required")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = account,
                    onValueChange = { account = it },
                    label = { Text("Account") },
                    isError = showErrorMessages && account.isBlank(),
                    supportingText = {
                        if (showErrorMessages && account.isBlank()) {
                            Text("Account is required")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { if (it.all { char -> char.isDigit() }) quantity = it },
                    label = { Text("Quantity") },
                    isError = showErrorMessages && (quantity.isBlank() || quantity.toIntOrNull() == null),
                    supportingText = {
                        if (showErrorMessages && quantity.isBlank()) {
                            Text("Quantity is required")
                        } else if (showErrorMessages && quantity.toIntOrNull() == null) {
                            Text("Please enter a valid number")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = rate,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            rate = it
                        }
                    },
                    label = { Text("Rate") },
                    isError = showErrorMessages && (rate.isBlank() || rate.toDoubleOrNull() == null),
                    supportingText = {
                        if (showErrorMessages && rate.isBlank()) {
                            Text("Rate is required")
                        } else if (showErrorMessages && rate.toDoubleOrNull() == null) {
                            Text("Please enter a valid number")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    showErrorMessages = true
                    if (validateForm()) {
                        onAddItem(
                            PurchaseOrderItem(
                                itemName = itemName,
                                description = description,
                                account = account,
                                quantity = quantity.toInt(),
                                rate = rate.toDouble()
                            )
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green1,
                    contentColor = Color.White
                )
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancel",
                    color = Green1
                )
            }
        }
    )
}

private fun formatCurrency(amount: Double): String {
    return "₱%.2f".format(amount)
}

private fun generatePurchaseNumber(): String {
    val timestamp = System.currentTimeMillis()
    val random = Random.nextInt(1000, 9999)
    return "PO-${timestamp.toString().takeLast(6)}-$random"
}