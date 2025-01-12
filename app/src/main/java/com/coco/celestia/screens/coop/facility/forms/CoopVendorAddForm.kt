package com.coco.celestia.screens.coop.facility.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.LocationState
import com.coco.celestia.viewmodel.LocationViewModel
import com.coco.celestia.viewmodel.VendorViewModel
import com.coco.celestia.viewmodel.model.VendorData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoopVendorAddForm(
    viewModel: VendorViewModel,
    locationViewModel: LocationViewModel,
    facilityName: String,
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    email: String? = null
) {
    var vendorData by remember {
        mutableStateOf(
            VendorData(
                email = email ?: "",
                facility = facilityName,
                isActive = true
            )
        )
    }

    var street by remember { mutableStateOf("") }
    var barangay by remember { mutableStateOf("") }
    var formErrors by remember { mutableStateOf(mapOf<String, String>()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isEditMode by remember { mutableStateOf(email != null) }
    var isBarangayDropdownExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val locationState by locationViewModel.locationState.observeAsState(LocationState.EMPTY)
    val locations by locationViewModel.locationData.observeAsState(emptyList())

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        cursorColor = Green1,
        focusedBorderColor = Green1,
        unfocusedBorderColor = Green1,
        focusedLabelColor = Green1,
        unfocusedLabelColor = Green1,
    )

    fun updateAddress() {
        val combinedAddress = if (street.isNotBlank() && barangay.isNotBlank()) {
            "$street, $barangay"
        } else {
            street + barangay
        }
        vendorData = vendorData.copy(address = combinedAddress)
    }

    LaunchedEffect(vendorData.address) {
        if (vendorData.address.isNotBlank()) {
            val parts = vendorData.address.split(",").map { it.trim() }
            street = parts.getOrNull(0) ?: ""
            barangay = parts.getOrNull(1) ?: ""
        }
    }

    LaunchedEffect(searchQuery) {
        locationViewModel.fetchLocations(searchQuery)
    }

    LaunchedEffect(email) {
        if (email != null) {
            isLoading = true
            viewModel.fetchVendorByEmail(email) { fetchedVendor ->
                fetchedVendor?.let {
                    vendorData = it.copy(facility = facilityName)
                }
                isLoading = false
            }
        }
    }

    fun validateForm(): Boolean {
        val errors = validateVendorForm(
            firstName = vendorData.firstName,
            lastName = vendorData.lastName,
            email = vendorData.email,
            phoneNumber = vendorData.phoneNumber,
            companyName = vendorData.companyName,
            address = vendorData.address,
            remarks = vendorData.remarks
        )
        formErrors = errors
        return errors.isEmpty()
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
            title = { Text(if (isEditMode) "Loading Vendor" else "Adding Vendor") },
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Personal Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Personal Information",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )

                OutlinedTextField(
                    value = vendorData.firstName,
                    onValueChange = {
                        vendorData = vendorData.copy(firstName = it)
                        if (formErrors.isNotEmpty()) validateForm()
                    },
                    label = { Text("First Name") },
                    isError = formErrors.containsKey("firstName"),
                    supportingText = {
                        formErrors["firstName"]?.let { error ->
                            Text(error, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = vendorData.lastName,
                    onValueChange = {
                        vendorData = vendorData.copy(lastName = it)
                        if (formErrors.isNotEmpty()) validateForm()
                    },
                    label = { Text("Last Name") },
                    isError = formErrors.containsKey("lastName"),
                    supportingText = {
                        formErrors["lastName"]?.let { error ->
                            Text(error, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
            }
        }

        // Company Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Company Information",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )

                OutlinedTextField(
                    value = vendorData.companyName,
                    onValueChange = {
                        vendorData = vendorData.copy(companyName = it)
                        if (formErrors.isNotEmpty()) validateForm()
                    },
                    label = { Text("Company Name") },
                    isError = formErrors.containsKey("companyName"),
                    supportingText = {
                        formErrors["companyName"]?.let { error ->
                            Text(error, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = vendorData.email,
                    onValueChange = {
                        vendorData = vendorData.copy(email = it)
                        if (formErrors.isNotEmpty()) validateForm()
                    },
                    label = { Text("Email Address") },
                    isError = formErrors.containsKey("email"),
                    supportingText = {
                        formErrors["email"]?.let { error ->
                            Text(error, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    enabled = !isEditMode,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = vendorData.phoneNumber,
                    onValueChange = {
                        if (it.length <= 11 && it.all { char -> char.isDigit() }) {
                            vendorData = vendorData.copy(phoneNumber = it)
                            if (formErrors.isNotEmpty()) validateForm()
                        }
                    },
                    label = { Text("Phone Number") },
                    isError = formErrors.containsKey("phoneNumber"),
                    supportingText = {
                        formErrors["phoneNumber"]?.let { error ->
                            Text(error, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )
            }
        }

        // Address Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Address Information",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )

                OutlinedTextField(
                    value = street,
                    onValueChange = {
                        street = it
                        updateAddress()
                        if (formErrors.isNotEmpty()) validateForm()
                    },
                    label = { Text("Street Address") },
                    isError = formErrors.containsKey("street"),
                    supportingText = {
                        formErrors["street"]?.let { error ->
                            Text(error, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                ExposedDropdownMenuBox(
                    expanded = isBarangayDropdownExpanded,
                    onExpandedChange = { isBarangayDropdownExpanded = it },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = barangay,
                        onValueChange = {
                            barangay = it
                            searchQuery = it
                            updateAddress()
                            if (formErrors.isNotEmpty()) validateForm()
                        },
                        label = { Text("Barangay") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBarangayDropdownExpanded)
                        },
                        isError = formErrors.containsKey("barangay"),
                        supportingText = {
                            formErrors["barangay"]?.let { error ->
                                Text(error, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        colors = textFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = isBarangayDropdownExpanded,
                        onDismissRequest = { isBarangayDropdownExpanded = false }
                    ) {
                        when (locationState) {
                            LocationState.LOADING -> {
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

                            LocationState.EMPTY -> {
                                DropdownMenuItem(
                                    text = { Text("No barangays found") },
                                    onClick = { }
                                )
                            }

                            LocationState.SUCCESS -> {
                                locations.map { it.barangay }
                                    .distinct()
                                    .forEach { barangayName ->
                                        DropdownMenuItem(
                                            text = { Text(barangayName) },
                                            onClick = {
                                                barangay = barangayName
                                                updateAddress()
                                                isBarangayDropdownExpanded = false
                                                if (formErrors.isNotEmpty()) validateForm()
                                            }
                                        )
                                    }
                            }

                            is LocationState.ERROR -> {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Error loading barangays",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = { }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Other Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Other Information",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )

                OutlinedTextField(
                    value = vendorData.remarks,
                    onValueChange = {
                        vendorData = vendorData.copy(remarks = it)
                        if (formErrors.isNotEmpty()) validateForm()
                    },
                    label = { Text("Remarks") },
                    isError = formErrors.containsKey("remarks"),
                    supportingText = {
                        formErrors["remarks"]?.let { error ->
                            Text(error, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = textFieldColors
                )
            }
        }

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    if (validateForm()) {
                        isLoading = true
                        if (isEditMode) {
                            viewModel.updateVendor(
                                email = email!!,
                                vendor = vendorData,
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
                            viewModel.addVendor(
                                vendor = vendorData,
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

fun validateVendorForm(
    firstName: String,
    lastName: String,
    email: String,
    phoneNumber: String,
    companyName: String,
    address: String,
    remarks: String
): Map<String, String> {
    val errors = mutableMapOf<String, String>()

    // First Name Validation
    if (firstName.isBlank()) {
        errors["firstName"] = "First name is required"
    } else if (!firstName.matches(Regex("^[a-zA-Z\\s-']+$"))) {
        errors["firstName"] = "First name can only contain letters, spaces, hyphens, and apostrophes"
    }

    // Last Name Validation
    if (lastName.isBlank()) {
        errors["lastName"] = "Last name is required"
    } else if (!lastName.matches(Regex("^[a-zA-Z\\s-']+$"))) {
        errors["lastName"] = "Last name can only contain letters, spaces, hyphens, and apostrophes"
    }

    // Email Validation
    if (email.isBlank()) {
        errors["email"] = "Email address is required"
    } else if (!email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
        errors["email"] = "Please enter a valid email address"
    }

    // Phone Number Validation
    if (phoneNumber.isBlank()) {
        errors["phoneNumber"] = "Phone number is required"
    } else if (!phoneNumber.matches(Regex("^[0-9]{11}$"))) {
        errors["phoneNumber"] = "Please enter a valid 11-digit phone number"
    }

    // Company Name Validation (Optional field)
    if (companyName.isNotBlank() && companyName.length < 2) {
        errors["companyName"] = "Company name must be at least 2 characters long"
    }

    // Address Validation
    val addressParts = address.split(",").map { it.trim() }
    val street = addressParts.getOrNull(0) ?: ""
    val barangay = addressParts.getOrNull(1) ?: ""

    if (street.isBlank()) {
        errors["street"] = "Street address is required"
    }

    if (barangay.isBlank()) {
        errors["barangay"] = "Barangay is required"
    }

    // Remarks Validation (Optional field)
    if (remarks.length > 500) {
        errors["remarks"] = "Remarks cannot exceed 500 characters"
    }

    return errors
}