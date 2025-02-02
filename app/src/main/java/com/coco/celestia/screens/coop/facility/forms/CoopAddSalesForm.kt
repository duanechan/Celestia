package com.coco.celestia.screens.coop.facility.forms

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.coco.celestia.screens.coop.facility.encodeEmail
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.SalesViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.SalesData
import com.coco.celestia.viewmodel.model.TransactionData
import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder

@Composable
fun SalesAddForm(
    viewModel: SalesViewModel,
    productViewModel: ProductViewModel,
    transactionViewModel: TransactionViewModel,
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
                salesNumber = salesNumber ?: "",
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                facility = facilityName
            )
        )
    }
    var selectedProduct by remember { mutableStateOf<ProductData?>(null) }
    var hasErrors by remember { mutableStateOf(false) }
    var showErrorMessages by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isEditMode by remember { mutableStateOf(salesNumber != null) }

    val products by productViewModel.productData.observeAsState(emptyList())
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)

    val total = remember(salesData.quantity, selectedProduct) {
        if (selectedProduct != null && salesData.quantity > 0) {
            salesData.quantity * selectedProduct!!.price
        } else 0.0
    }

    LaunchedEffect(Unit) {
        productViewModel.fetchProducts("", userRole)

        if (!isEditMode) {
            val count = viewModel.getSalesCount() + 1
            val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val newSalesNumber = "SO-$currentDate-${count.toString().padStart(3, '0')}"
            salesData = salesData.copy(salesNumber = newSalesNumber)
        }
    }

    LaunchedEffect(salesNumber) {
        if (salesNumber != null) {
            isLoading = true
            viewModel.fetchSaleById(salesNumber) { fetchedSale ->
                fetchedSale?.let {
                    salesData = it.copy(facility = facilityName)
                    selectedProduct = products.find { product -> product.name == it.productName }
                }
                isLoading = false
            }
        }
    }

    fun validateForm(): Boolean {
        return salesData.productName.isNotBlank() &&
                salesData.quantity > 0 &&
                selectedProduct != null &&
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

    SalesFormContent(
        salesData = salesData,
        onSalesDataChange = { salesData = it },
        selectedProduct = selectedProduct,
        onSelectedProductChange = { selectedProduct = it },
        products = products,
        productState = productState,
        total = total,
        isEditMode = isEditMode,
        showErrorMessages = showErrorMessages,
        isLoading = isLoading,
        onSubmit = {
            showErrorMessages = true
            if (!hasErrors && validateForm()) {
                isLoading = true
                if (isEditMode) {
                    viewModel.updateSale(
                        salesData,
                        onSuccess = {
                            recordSaleTransaction(
                                sale = salesData,
                                productId = selectedProduct?.productId ?: "",
                                facilityName = facilityName,
                                transactionViewModel = transactionViewModel,
                                productViewModel = productViewModel
                            )
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
                            recordSaleTransaction(
                                sale = salesData,
                                productId = selectedProduct?.productId ?: "",
                                facilityName = facilityName,
                                transactionViewModel = transactionViewModel,
                                productViewModel = productViewModel
                            )
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
        onCancel = onCancel,
        modifier = modifier
    )
}

fun recordSaleTransaction(
    sale: SalesData,
    productId: String,
    facilityName: String,
    transactionViewModel: TransactionViewModel,
    productViewModel: ProductViewModel
) {
    val formattedDate = try {
        val inputDate = LocalDate.parse(sale.date).atStartOfDay()
        val outputFormatter = DateTimeFormatterBuilder()
            .appendPattern("dd MMM yyyy")
            .toFormatter(Locale.ENGLISH)
        inputDate.format(outputFormatter)
    } catch (e: Exception) {
        sale.date
    }

    val transaction = TransactionData(
        transactionId = sale.salesNumber,
        type = "In-Store Sale",
        date = formattedDate,
        description = "Sale of ${sale.quantity} ${sale.weightUnit} of ${sale.productName}",
        status = "COMPLETED",
        productName = sale.productName,
        productId = productId,
        facilityName = facilityName
    )

    val encodedFacility = encodeEmail(sale.facility)
    transactionViewModel.recordTransaction(encodedFacility, transaction)

    productViewModel.updateProductTotalSold(sale.productName, sale.quantity)
    productViewModel.updateProductQuantity(sale.productName, -sale.quantity)
}

fun validateSalesForm(
    salesNumber: String,
    date: String,
    productName: String,
    quantity: Int,
    price: Double?
): Map<String, String> {
    val errors = mutableMapOf<String, String>()

    if (salesNumber.isBlank()) {
        errors["salesNumber"] = "Sales number is required"
    }

    if (!date.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
        errors["date"] = "Invalid date format"
    }

    if (productName.isBlank()) {
        errors["productName"] = "Product name is required"
    }

    if (quantity <= 0) {
        errors["quantity"] = "Quantity must be greater than 0"
    }

    if (price == null || price <= 0) {
        errors["price"] = "Invalid price"
    }

    return errors
}

@Composable
fun SalesFormContent(
    salesData: SalesData,
    onSalesDataChange: (SalesData) -> Unit,
    selectedProduct: ProductData?,
    onSelectedProductChange: (ProductData?) -> Unit,
    products: List<ProductData>,
    productState: ProductState,
    total: Double,
    isEditMode: Boolean,
    showErrorMessages: Boolean,
    isLoading: Boolean,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showProductDropdown by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var errors by remember { mutableStateOf(mapOf<String, String>()) }
    var shouldShowErrors by remember { mutableStateOf(false) }

    LaunchedEffect(isEditMode, products, salesData) {
        if (isEditMode && selectedProduct == null) {
            val existingProduct = products.find { it.name == salesData.productName }
            if (existingProduct != null) {
                onSelectedProductChange(existingProduct)
            }
        }
    }

    LaunchedEffect(salesData, selectedProduct) {
        if (shouldShowErrors) {
            errors = validateSalesForm(
                salesNumber = salesData.salesNumber,
                date = salesData.date,
                productName = salesData.productName,
                quantity = salesData.quantity,
                price = selectedProduct?.price
            )
        }
    }

    val filteredProducts = remember(searchQuery, products) {
        products.filter { product ->
            product.isInStore &&
                    product.isActive &&
                    (searchQuery.isEmpty() || product.name.contains(searchQuery, ignoreCase = true))
        }
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        cursorColor = Green1,
        focusedBorderColor = Green1,
        unfocusedBorderColor = Green1,
        focusedLabelColor = Green1,
        unfocusedLabelColor = Green1,
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error,
        errorCursorColor = MaterialTheme.colorScheme.error
    )

    val disabledTextFieldColors = OutlinedTextFieldDefaults.colors(
        cursorColor = Green1,
        focusedBorderColor = MaterialTheme.colorScheme.secondary,
        unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
        focusedLabelColor = MaterialTheme.colorScheme.secondary,
        unfocusedLabelColor = MaterialTheme.colorScheme.secondary,
        disabledBorderColor = MaterialTheme.colorScheme.secondary,
        disabledLabelColor = MaterialTheme.colorScheme.secondary,
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error,
        errorCursorColor = MaterialTheme.colorScheme.error
    )

    fun validateField(field: String) {
        if (shouldShowErrors && salesData.productName.isNotEmpty()) {
            errors = validateSalesForm(
                salesNumber = salesData.salesNumber,
                date = salesData.date,
                productName = salesData.productName,
                quantity = salesData.quantity,
                price = selectedProduct?.price
            )
        }
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

                val formattedSalesNumber = remember(salesData.salesNumber) {
                    try {
                        val parts = salesData.salesNumber.split("-")
                        if (parts.size == 3) {
                            val prefix = parts[0]
                            val date = parts[1]
                            val number = parts[2].toInt()
                            val formattedDate = "${date.substring(0, 4)}${date.substring(4, 6)}${date.substring(6, 8)}"
                            "$prefix-$formattedDate-${number.toString().padStart(3, '0')}"
                        } else {
                            salesData.salesNumber
                        }
                    } catch (e: Exception) {
                        salesData.salesNumber
                    }
                }

                OutlinedTextField(
                    value = formattedSalesNumber,
                    onValueChange = { },
                    label = { Text("Sales Number") },
                    readOnly = true,
                    isError = shouldShowErrors && errors["salesNumber"] != null,
                    supportingText = {
                        if (shouldShowErrors) {
                            errors["salesNumber"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                val context = LocalContext.current
                val calendar = Calendar.getInstance()

                OutlinedTextField(
                    value = salesData.date,
                    onValueChange = { },
                    label = { Text("Date of Sale") },
                    readOnly = true,
                    isError = shouldShowErrors && errors["date"] != null,
                    supportingText = {
                        if (shouldShowErrors) {
                            errors["date"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
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
                                        onSalesDataChange(salesData.copy(date = formattedDate.format(calendar.time)))
                                        validateField("date")
                                    },
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                        )
                    }
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = salesData.productName,
                        onValueChange = { newQuery ->
                            if (!isEditMode) {
                                searchQuery = newQuery
                                if (!showProductDropdown) {
                                    onSalesDataChange(salesData.copy(productName = newQuery))
                                    validateField("productName")
                                }
                                showProductDropdown = true
                            }
                        },
                        label = { Text("Product Name") },
                        readOnly = isEditMode,  // Make read-only in edit mode
                        enabled = !isEditMode,  // Disable in edit mode
                        isError = shouldShowErrors && errors["productName"] != null,
                        supportingText = {
                            if (shouldShowErrors) {
                                errors["productName"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = if (isEditMode) disabledTextFieldColors else textFieldColors,
                        trailingIcon = {
                            if (!isEditMode) {  // Only show dropdown icon if not in edit mode
                                IconButton(onClick = {
                                    showProductDropdown = !showProductDropdown
                                    if (showProductDropdown) {
                                        searchQuery = ""
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select product"
                                    )
                                }
                            }
                        }
                    )

                    // Only show dropdown if not in edit mode
                    if (!isEditMode) {
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
                                                onSelectedProductChange(product)
                                                onSalesDataChange(salesData.copy(
                                                    productName = product.name,
                                                    weightUnit = product.weightUnit,
                                                    price = product.price
                                                ))
                                                searchQuery = ""
                                                showProductDropdown = false
                                                validateField("productName")
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = if (salesData.quantity == 0) "" else salesData.quantity.toString(),
                        onValueChange = {
                            val quantity = it.toIntOrNull() ?: 0
                            onSalesDataChange(salesData.copy(quantity = quantity))
                            validateField("quantity")
                        },
                        label = { Text("Quantity") },
                        enabled = selectedProduct != null,
                        isError = shouldShowErrors && errors["quantity"] != null,
                        supportingText = {
                            if (shouldShowErrors) {
                                errors["quantity"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            } else if (selectedProduct != null && salesData.quantity == 0) {
                                Text(
                                    "Input a quantity",
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        },
                        placeholder = {
                            if (selectedProduct == null) {
                                Text("Select product first")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = if (selectedProduct == null) disabledTextFieldColors else textFieldColors
                    )

                    OutlinedTextField(
                        value = selectedProduct?.weightUnit ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Unit") },
                        enabled = selectedProduct != null,
                        modifier = Modifier.weight(1f),
                        colors = if (selectedProduct == null) disabledTextFieldColors else textFieldColors
                    )
                }

                OutlinedTextField(
                    value = selectedProduct?.price?.toString() ?: "",
                    onValueChange = { },
                    readOnly = true,
                    enabled = selectedProduct != null,
                    label = { Text("Price per Unit") },
                    isError = shouldShowErrors && errors["price"] != null,
                    supportingText = {
                        if (shouldShowErrors) {
                            errors["price"]?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        }
                    },
                    placeholder = {
                        if (selectedProduct == null) {
                            Text("Select product first")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (selectedProduct == null) disabledTextFieldColors else textFieldColors
                )

                Text(
                    text = "TOTAL",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${salesData.quantity} X ${selectedProduct?.price?.format(2) ?: "0.00"}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "PHP ${total.format(2)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

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
                    onValueChange = { onSalesDataChange(salesData.copy(notes = it)) },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = textFieldColors
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    shouldShowErrors = false  // Reset error state
                    onCancel()
                },
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
                    shouldShowErrors = true

                    if (salesData.productName.isEmpty()) {
                        errors = mapOf("productName" to "Please select a product first")
                    } else {
                        errors = validateSalesForm(
                            salesNumber = salesData.salesNumber,
                            date = salesData.date,
                            productName = salesData.productName,
                            quantity = salesData.quantity,
                            price = selectedProduct?.price
                        )
                    }
                    if (errors.isEmpty()) {
                        onSubmit()
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

fun Double.format(digits: Int) = "%.${digits}f".format(this)