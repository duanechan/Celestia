package com.coco.celestia.screens.coop.facility.forms

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.SalesViewModel
import com.coco.celestia.viewmodel.model.Constants
import com.coco.celestia.viewmodel.model.SalesData

//TODO: Only display the products that are in-store

@Composable
fun SalesAddForm(
    viewModel: SalesViewModel,
    productViewModel: ProductViewModel,
    facilityName: String,
    userRole: String,
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    salesNumber: String? = null
) {
    var salesData by remember {
        mutableStateOf(
            SalesData(
                salesNumber = generateSalesNumber(),
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                facility = facilityName
            )
        )
    }
    var hasErrors by remember { mutableStateOf(false) }
    var showErrorMessages by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isEditMode by remember { mutableStateOf(salesNumber != null) }
    var showWeightUnitDropdown by remember { mutableStateOf(false) }
    var showProductDropdown by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val products by productViewModel.productData.observeAsState(emptyList())
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)
    val filteredProducts = remember(searchQuery, products) {
        products.filter { product ->
            product.isInStore &&
                    (searchQuery.isEmpty() || product.name.contains(searchQuery, ignoreCase = true))
        }
    }

    LaunchedEffect(Unit) {
        productViewModel.fetchProducts(searchQuery, userRole)
    }

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            productViewModel.fetchProducts(searchQuery, userRole)
        }
    }

    val weightUnits = listOf(
        Constants.WEIGHT_GRAMS,
        Constants.WEIGHT_KILOGRAMS,
        Constants.WEIGHT_POUNDS
    )

    LaunchedEffect(salesNumber) {
        if (salesNumber != null) {
            isLoading = true
            viewModel.fetchSaleById(salesNumber) { fetchedSale ->
                fetchedSale?.let {
                    salesData = it.copy(facility = facilityName)
                }
                isLoading = false
            }
        }
    }

    fun validateForm(): Boolean {
        return salesData.productName.isNotBlank() &&
                salesData.quantity > 0 &&
                salesData.price > 0.0 &&
                salesData.date.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))
    }

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
            title = { Text(if (isEditMode) "Loading Sale" else "Adding Sale") },
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

    // Define common text field colors
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        cursorColor = Green1,
        focusedBorderColor = Green1,
        unfocusedBorderColor = Green1,
        focusedLabelColor = Green1,
        unfocusedLabelColor = Green1,
    )

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
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Sale Information",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )

                OutlinedTextField(
                    value = salesData.salesNumber,
                    onValueChange = { },
                    label = { Text("Sales Number") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = if (showProductDropdown) searchQuery else salesData.productName,
                        onValueChange = { newQuery ->
                            searchQuery = newQuery
                            if (!showProductDropdown) {
                                salesData = salesData.copy(productName = newQuery)
                            }
                            showProductDropdown = true
                            hasErrors = !validateForm()
                        },
                        label = { Text("Product Name") },
                        isError = showErrorMessages && salesData.productName.isBlank(),
                        supportingText = {
                            if (showErrorMessages && salesData.productName.isBlank()) {
                                Text("Product name is required")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors,
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    showProductDropdown = !showProductDropdown
                                    if (showProductDropdown) {
                                        searchQuery = ""
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select product"
                                )
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = showProductDropdown && filteredProducts.isNotEmpty(),
                        onDismissRequest = {
                            showProductDropdown = false
                            searchQuery = ""
                        },
                        modifier = Modifier
                            .heightIn(max = 300.dp)
                            .width(with(LocalDensity.current) {
                                LocalView.current.width.toDp()
                            })
                    ) {
                        when (productState) {
                            ProductState.LOADING -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = Green1
                                    )
                                }
                            }
                            is ProductState.ERROR -> {
                                DropdownMenuItem(
                                    text = { Text("Error loading products") },
                                    onClick = { }
                                )
                            }
                            ProductState.EMPTY -> {
                                if (searchQuery.isNotEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("No matching products") },
                                        onClick = { }
                                    )
                                }
                            }
                            ProductState.SUCCESS -> {
                                filteredProducts.forEach { product ->
                                    DropdownMenuItem(
                                        text = { Text(product.name) },
                                        onClick = {
                                            salesData = salesData.copy(
                                                productName = product.name,
                                                weightUnit = product.weightUnit
                                            )
                                            searchQuery = ""
                                            showProductDropdown = false
                                            hasErrors = !validateForm()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Quantity and Weight Unit Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = if (salesData.quantity == 0) "" else salesData.quantity.toString(),
                        onValueChange = {
                            val quantity = it.toIntOrNull() ?: 0
                            salesData = salesData.copy(quantity = quantity)
                            hasErrors = !validateForm()
                        },
                        label = { Text("Quantity") },
                        isError = showErrorMessages && salesData.quantity <= 0,
                        supportingText = {
                            if (showErrorMessages && salesData.quantity <= 0) {
                                Text("Quantity must be greater than 0")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = textFieldColors
                    )

                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = salesData.weightUnit,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Unit") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select unit",
                                    modifier = Modifier.clickable {
                                        showWeightUnitDropdown = true
                                    }
                                )
                            }
                        )

                        DropdownMenu(
                            expanded = showWeightUnitDropdown,
                            onDismissRequest = { showWeightUnitDropdown = false },
                            modifier = Modifier.width(IntrinsicSize.Min)
                        ) {
                            weightUnits.forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        salesData = salesData.copy(weightUnit = unit)
                                        showWeightUnitDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = if (salesData.price == 0.0) "" else salesData.price.toString(),
                    onValueChange = {
                        val price = it.toDoubleOrNull() ?: 0.0
                        salesData = salesData.copy(price = price)
                        hasErrors = !validateForm()
                    },
                    label = { Text("Price") },
                    isError = showErrorMessages && salesData.price <= 0.0,
                    supportingText = {
                        if (showErrorMessages && salesData.price <= 0.0) {
                            Text("Price must be greater than 0")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                val context = LocalContext.current
                val calendar = Calendar.getInstance()

                OutlinedTextField(
                    value = salesData.date,
                    onValueChange = { },
                    label = { Text("Date") },
                    readOnly = true,
                    isError = showErrorMessages && salesData.date.isEmpty(),
                    supportingText = {
                        if (showErrorMessages && salesData.date.isEmpty()) {
                            Text("Date is required")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select date",
                            modifier = Modifier.clickable {
                                DatePickerDialog(
                                    context,
                                    { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                                        calendar.set(year, month, dayOfMonth)
                                        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                        salesData = salesData.copy(date = formattedDate.format(calendar.time))
                                        hasErrors = !validateForm()
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                        )
                    }
                )
            }
        }

        // Additional Information Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Additional Information",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )

                OutlinedTextField(
                    value = salesData.notes,
                    onValueChange = { salesData = salesData.copy(notes = it) },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = textFieldColors
                )
            }
        }

        // Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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

            Button(
                onClick = {
                    showErrorMessages = true
                    if (!hasErrors && validateForm()) {
                        isLoading = true
                        if (isEditMode) {
                            viewModel.updateSale(
                                salesData,
                                onSuccess = {
                                    isLoading = false
                                    onSuccess()
                                },
                                onError = { error ->
                                    isLoading = false
                                    errorMessage = error
                                }
                            )
                        } else {
                            viewModel.addSale(
                                salesData,
                                onSuccess = {
                                    isLoading = false
                                    onSuccess()
                                },
                                onError = { error ->
                                    isLoading = false
                                    errorMessage = error
                                }
                            )
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green1,
                    contentColor = Color.White
                )
            ) {
                Text(if (isEditMode) "Update" else "Submit")
            }
        }
    }
}

private var salesCount = 0

private fun generateSalesNumber(): String {
    salesCount++

    val currentDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))

    return "SO-$currentDate-$salesCount"
}