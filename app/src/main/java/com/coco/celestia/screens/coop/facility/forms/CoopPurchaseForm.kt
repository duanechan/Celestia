package com.coco.celestia.screens.coop.facility.forms

import android.app.DatePickerDialog
import android.widget.DatePicker
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.model.Constants
import com.coco.celestia.viewmodel.model.TransactionData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@Composable
fun CoopPurchaseForm(
    purchaseOrderViewModel: PurchaseOrderViewModel,
    vendorViewModel: VendorViewModel,
    facilityViewModel: FacilityViewModel,
    productViewModel: ProductViewModel,
    transactionViewModel: TransactionViewModel,
    facilityName: String,
    currentEmail: String,
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
                purchaseNumber = purchaseNumber ?: "",
                dateAdded = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                dateOfPurchase = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                customerNotes = "",
                items = emptyList(),
                facility = facilityName,
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

    val facilities by facilityViewModel.facilitiesData.observeAsState(initial = emptyList())
    val userFacility = facilities.find { facility ->
        facility.emails.contains(currentEmail)
    }

    val products by productViewModel.productData.observeAsState(initial = emptyList())
    val currentStockMap = products.associate { it.name to it.quantity }

    LaunchedEffect(Unit) {
        facilityViewModel.fetchFacilities()
        vendorViewModel.fetchVendors(facilityName = facilityName)

        if (purchaseNumber == null && draftId == null) {
            val count = purchaseOrderViewModel.getPurchaseCount() + 1
            val currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            val newPurchaseNumber = "PO-$currentDate-${count.toString().padStart(3, '0')}"
            purchaseOrderData = purchaseOrderData.copy(purchaseNumber = newPurchaseNumber)
        }
    }

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

    val isEdit = !purchaseNumber.isNullOrEmpty()

    fun validateForm(): Boolean {
        return purchaseOrderData.purchaseNumber.isNotBlank() &&
                purchaseOrderData.dateOfPurchase.isNotBlank() &&
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
                if (userFacility != null) {
                    PurchaseFormHeader(
                        purchaseOrderData = purchaseOrderData,
                        onPurchaseOrderChange = { purchaseOrderData = it },
                        vendorViewModel = vendorViewModel,
                        showErrorMessages = showErrorMessages,
                        textFieldColors = textFieldColors,
                        isEditingDraft = draftId != null && !isEdit,
                        navController = navController,
                        facilityName = facilityName
                    )

                    // Items Section
                    PurchaseFormItemsSection(
                        items = items,
                        productViewModel = productViewModel,
                        onShowAddItem = { showAddItemDialog = true },
                        onRemoveItem = { index ->
                            items = items.filterIndexed { i, _ -> i != index }
                            hasErrors = !validateForm()
                        },
                        showErrorMessages = showErrorMessages
                    )

                    // Notes Section
                    OutlinedTextField(
                        value = purchaseOrderData.customerNotes,
                        onValueChange = { purchaseOrderData = purchaseOrderData.copy(customerNotes = it) },
                        label = { Text("Notes") },
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
                                    val finalPurchaseOrder = purchaseOrderData.copy(
                                        items = items,
                                        savedAsDraft = false,
                                        status = "completed"
                                    )

                                    purchaseOrderViewModel.addPurchaseOrder(
                                        purchaseOrder = finalPurchaseOrder,
                                        onSuccess = {
                                            recordPurchaseOrderTransactions(
                                                transactionViewModel = transactionViewModel,
                                                purchaseOrder = finalPurchaseOrder,
                                                facilityName = facilityName
                                            )

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
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No facility assigned to this user",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
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
            onDismiss = { showAddItemDialog = false },
            facilityViewModel = facilityViewModel,
            productViewModel = productViewModel,
            currentEmail = currentEmail,
            currentStockMap = currentStockMap
        )
    }
}

fun recordPurchaseOrderTransactions(
    transactionViewModel: TransactionViewModel,
    purchaseOrder: PurchaseOrder,
    facilityName: String
) {
    val vendorName = if (purchaseOrder.vendor.isBlank()) "Unknown Vendor" else purchaseOrder.vendor

    purchaseOrder.items.forEach { item ->
        val transaction = TransactionData(
            transactionId = purchaseOrder.purchaseNumber,
            type = "Purchased",
            date = purchaseOrder.dateOfPurchase,
            description = "Purchased ${item.quantity} units from $vendorName",
            status = "COMPLETED",
            productName = item.itemName,
            productId = item.productId,
            facilityName = facilityName,
            vendorName = vendorName
        )

        transactionViewModel.recordTransaction(
            uid = facilityName,
            transaction = transaction
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseFormHeader(
    purchaseOrderData: PurchaseOrder,
    onPurchaseOrderChange: (PurchaseOrder) -> Unit,
    vendorViewModel: VendorViewModel,
    showErrorMessages: Boolean,
    textFieldColors: TextFieldColors,
    isEditingDraft: Boolean = false,
    navController: NavController,
    facilityName: String
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    LaunchedEffect(navController) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow<String?>(Screen.VENDOR_RESULT_KEY, null)
            ?.collect { vendorName ->
                if (vendorName != null) {
                    onPurchaseOrderChange(purchaseOrderData.copy(vendor = vendorName))
                    vendorViewModel.fetchVendors()
                    navController.currentBackStackEntry?.savedStateHandle?.remove<String>(Screen.VENDOR_RESULT_KEY)
                }
            }
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

        val formattedPurchaseNumber = remember(purchaseOrderData.purchaseNumber) {
            try {
                val parts = purchaseOrderData.purchaseNumber.split("-")
                if (parts.size == 3) {
                    val prefix = parts[0]
                    val date = parts[1]
                    val number = parts[2].toInt()
                    val formattedDate = "${date.substring(0, 4)}${date.substring(4, 6)}${date.substring(6, 8)}"
                    "$prefix-$formattedDate-${number.toString().padStart(3, '0')}"
                } else {
                    purchaseOrderData.purchaseNumber
                }
            } catch (e: Exception) {
                purchaseOrderData.purchaseNumber
            }
        }

        OutlinedTextField(
            value = formattedPurchaseNumber,
            onValueChange = { },
            label = { Text("Purchase Number") },
            readOnly = true,
            isError = showErrorMessages && purchaseOrderData.purchaseNumber.isBlank(),
            supportingText = {
                if (showErrorMessages && purchaseOrderData.purchaseNumber.isBlank()) {
                    Text("Purchase number is required", color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors
        )

        OutlinedTextField(
            value = purchaseOrderData.dateOfPurchase,
            onValueChange = { },
            label = { Text("Date of Purchase") },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select date",
                    modifier = Modifier.clickable {
                        DatePickerDialog(
                            context,
                            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                                calendar.set(year, month, dayOfMonth)
                                val formattedDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                onPurchaseOrderChange(
                                    purchaseOrderData.copy(
                                        dateOfPurchase = formattedDate.format(calendar.time)
                                    )
                                )
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = textFieldColors
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = purchaseOrderData.vendor.ifBlank { "None" },
                onValueChange = { },
                readOnly = true,
                label = { Text("Select Vendor") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = textFieldColors,
                isError = showErrorMessages && purchaseOrderData.vendor.isBlank(),
                supportingText = if (showErrorMessages && purchaseOrderData.vendor.isBlank()) {
                    { Text("Vendor is required") }
                } else null
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.exposedDropdownSize()
            ) {
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add vendor"
                            )
                            Text("Add New Vendor")
                        }
                    },
                    onClick = {
                        expanded = false
                        navController.navigate(Screen.CoopAddVendor.route)
                    }
                )

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                when (val state = vendorViewModel.vendorState.value) {
                    is VendorState.SUCCESS -> {
                        val facilityVendors = vendorViewModel.vendorData.value
                            ?.filter { vendor -> vendor.facility == facilityName }
                            ?: emptyList()

                        if (facilityVendors.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No vendors available for this facility") },
                                onClick = { }
                            )
                        } else {
                            facilityVendors.forEach { vendor ->
                                val vendorName = "${vendor.firstName} ${vendor.lastName}".trim()
                                DropdownMenuItem(
                                    text = { Text(vendorName) },
                                    onClick = {
                                        onPurchaseOrderChange(purchaseOrderData.copy(vendor = vendorName))
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    is VendorState.LOADING -> {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Text("Loading vendors...")
                                }
                            },
                            onClick = { }
                        )
                    }
                    else -> {
                        DropdownMenuItem(
                            text = { Text("No vendors available") },
                            onClick = { }
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
    productViewModel: ProductViewModel,
    onShowAddItem: () -> Unit,
    onRemoveItem: (Int) -> Unit,
    showErrorMessages: Boolean = false
) {
    val products by productViewModel.productData.observeAsState(initial = emptyList())

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
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Add Item")
        }

        if (showErrorMessages && items.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        "You must add at least one item to the purchase order",
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 200.dp)
        ) {
            items(items.size) { index ->
                val item = items[index]
                val product = products.find { it.productId == item.productId }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Green4
                    )
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
                            Column(modifier = Modifier.weight(1f)) {
                                if (product != null) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (product.isInStore) Icons.Default.Home else Icons.Default.ShoppingCart,
                                            contentDescription = if (product.isInStore) "In Store" else "Online",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = if (product.isInStore) "In Store" else "Online",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                                Text(
                                    text = item.itemName,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
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
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
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
                    containerColor = Green4
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseOrderItemForm(
    onAddItem: (PurchaseOrderItem) -> Unit,
    onDismiss: () -> Unit,
    facilityViewModel: FacilityViewModel,
    productViewModel: ProductViewModel,
    currentEmail: String,
    currentStockMap: Map<String, Int>
) {
    var selectedProductId by remember { mutableStateOf("") }
    var itemName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var weightUnit by remember { mutableStateOf(Constants.WEIGHT_GRAMS) }
    var showErrorMessages by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var hasInitialFetch by remember { mutableStateOf(false) }
    var isInStore by remember { mutableStateOf<Boolean?>(null) }
    var productTypeExpanded by remember { mutableStateOf(false) }

    val products by productViewModel.productData.observeAsState(initial = emptyList())
    val productState by productViewModel.productState.observeAsState(initial = ProductState.LOADING)
    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(initial = emptyList())

    val userFacility = facilitiesData.find { facility ->
        facility.emails.contains(currentEmail)
    }

    val currentStock = currentStockMap[itemName] ?: 0

    val selectedProduct = products.find { it.productId == selectedProductId }

    val availableProducts = products.filter { product ->
        product.isActive &&
                (isInStore == null || product.isInStore == isInStore) &&
                product.type == userFacility?.name
    }.sortedBy { it.name }

    fun validateForm(): Boolean {
        return itemName.isNotBlank() &&
                quantity.isNotBlank() &&
                rate.isNotBlank() &&
                quantity.toIntOrNull() != null &&
                rate.toDoubleOrNull() != null &&
                isInStore != null
    }

    LaunchedEffect(Unit) {
        facilityViewModel.fetchFacilities()
    }

    LaunchedEffect(userFacility?.name, isInStore) {
        if (userFacility != null && (!hasInitialFetch || products.isEmpty())) {
            productViewModel.fetchProducts("", "Admin")
            hasInitialFetch = true
        }
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

                when {
                    userFacility == null -> {
                        Text(
                            text = "No facility assigned to this user",
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {
                        // Product Type Dropdown
                        ExposedDropdownMenuBox(
                            expanded = productTypeExpanded,
                            onExpandedChange = { productTypeExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = when (isInStore) {
                                    true -> "In Store"
                                    false -> "Online"
                                    null -> ""
                                },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Select Product Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productTypeExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = textFieldColors,
                                isError = showErrorMessages && isInStore == null,
                                supportingText = if (showErrorMessages && isInStore == null) {
                                    { Text("Please select product type") }
                                } else null
                            )

                            ExposedDropdownMenu(
                                expanded = productTypeExpanded,
                                onDismissRequest = { productTypeExpanded = false }
                            ) {
                                listOf(
                                    "In Store" to true,
                                    "Online" to false
                                ).forEach { (label, value) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            isInStore = value
                                            itemName = "" // Reset product selection
                                            selectedProductId = "" // Reset selected product ID
                                            productTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        when (productState) {
                            ProductState.LOADING -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    color = Green1
                                )
                            }
                            ProductState.EMPTY -> {
                                Text(
                                    text = "No products available",
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            ProductState.SUCCESS -> {
                                if (availableProducts.isEmpty()) {
                                    Text(
                                        text = "No products available for selected product type",
                                        modifier = Modifier.align(Alignment.CenterHorizontally),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                } else {
                                    // Product Dropdown
                                    ExposedDropdownMenuBox(
                                        expanded = expanded,
                                        onExpandedChange = { expanded = it },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = itemName,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Select Product") },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor(),
                                            colors = textFieldColors,
                                            isError = showErrorMessages && itemName.isBlank(),
                                            supportingText = if (showErrorMessages && itemName.isBlank()) {
                                                { Text("Please select a product") }
                                            } else null
                                        )

                                        ExposedDropdownMenu(
                                            expanded = expanded,
                                            onDismissRequest = { expanded = false }
                                        ) {
                                            availableProducts.forEach { product ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text("${product.name} (${if (product.isInStore) "In Store" else "Online"})")
                                                            Row(
                                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                            ) {
                                                                Text(
                                                                    "Price: ₱${product.price}",
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                                Text(
                                                                    "Unit: ${product.weightUnit}",
                                                                    style = MaterialTheme.typography.bodySmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }
                                                        }
                                                    },
                                                    onClick = {
                                                        itemName = product.name
                                                        selectedProductId = product.productId
                                                        rate = product.price.toString()
                                                        weightUnit = product.weightUnit
                                                        expanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    if (itemName.isNotBlank()) {
                                        // Display current stock and weight unit
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Current Stock: $currentStock",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "Unit: $weightUnit",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        OutlinedTextField(
                                            value = description,
                                            onValueChange = { description = it },
                                            label = { Text("Description (Optional)") },
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
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text("Price per Unit") },
                                            isError = showErrorMessages && (rate.isBlank() || rate.toDoubleOrNull() == null),
                                            supportingText = {
                                                if (showErrorMessages && rate.isBlank()) {
                                                    Text("Rate is required")
                                                } else if (showErrorMessages && rate.toDoubleOrNull() == null) {
                                                    Text("Please enter a valid number")
                                                }
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = textFieldColors
                                        )
                                    }
                                }
                            }
                            is ProductState.ERROR -> {
                                Text(
                                    text = (productState as ProductState.ERROR).message,
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Cancel",
                        color = Green1
                    )
                }

                Button(
                    onClick = {
                        showErrorMessages = true
                        if (validateForm()) {
                            onAddItem(
                                PurchaseOrderItem(
                                    itemName = itemName,
                                    productId = selectedProductId,
                                    description = description,
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
                    ),
                    modifier = Modifier.weight(1f),
                    enabled = productState == ProductState.SUCCESS && userFacility != null
                ) {
                    Text("Add")
                }
            }
        }
    )
}

private fun formatCurrency(amount: Double): String {
    return "₱%.2f".format(amount)
}