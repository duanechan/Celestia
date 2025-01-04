package com.coco.celestia.screens.coop.facility.forms

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.VendorViewModel
import com.coco.celestia.viewmodel.model.VendorData

// TODO: Add checks for every field
// TODO: To add other fields later on (if there will still be other fields)

@Composable
fun CoopVendorAddForm(
    viewModel: VendorViewModel,
    facilityName: String,
    onSuccess: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    email: String? = null
) {
    var vendorData by remember {
        mutableStateOf(VendorData(email = email ?: "", facility = facilityName))
    }
    var hasErrors by remember { mutableStateOf(false) }
    var showErrorMessages by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(facilityName) {
        Log.d("CoopVendorAddForm", "Current Facility: $facilityName")
    }

    fun validateForm(): Boolean {
        return vendorData.firstName.isNotBlank() &&
                vendorData.lastName.isNotBlank() &&
                vendorData.email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)\$")) &&
                vendorData.phoneNumber.matches(Regex("^[0-9]{11}$"))
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
            title = { Text("Adding Vendor") },
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

                val textFieldColors = OutlinedTextFieldDefaults.colors(
                    cursorColor = Green1,
                    focusedBorderColor = Green1,
                    unfocusedBorderColor = Green1,
                    focusedLabelColor = Green1,
                    unfocusedLabelColor = Green1,
                )

                OutlinedTextField(
                    value = vendorData.firstName,
                    onValueChange = {
                        vendorData = vendorData.copy(firstName = it)
                        hasErrors = !validateForm()
                    },
                    label = { Text("First Name") },
                    isError = showErrorMessages && vendorData.firstName.isBlank(),
                    supportingText = {
                        if (showErrorMessages && vendorData.firstName.isBlank()) {
                            Text("First name is required")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = vendorData.lastName,
                    onValueChange = {
                        vendorData = vendorData.copy(lastName = it)
                        hasErrors = !validateForm()
                    },
                    label = { Text("Last Name") },
                    isError = showErrorMessages && vendorData.lastName.isBlank(),
                    supportingText = {
                        if (showErrorMessages && vendorData.lastName.isBlank()) {
                            Text("Last name is required")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                Text(
                    text = "Company Information",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green1
                )

                OutlinedTextField(
                    value = vendorData.companyName,
                    onValueChange = { vendorData = vendorData.copy(companyName = it) },
                    label = { Text("Company Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = vendorData.email,
                    onValueChange = {
                        vendorData = vendorData.copy(email = it)
                        hasErrors = !validateForm()
                    },
                    label = { Text("Email Address") },
                    isError = showErrorMessages && !vendorData.email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)\$")),
                    supportingText = {
                        if (showErrorMessages && !vendorData.email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)\$"))) {
                            Text("Please enter a valid email address")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = vendorData.phoneNumber,
                    onValueChange = {
                        if (it.length <= 11 && it.all { char -> char.isDigit() }) {
                            vendorData = vendorData.copy(phoneNumber = it)
                            hasErrors = !validateForm()
                        }
                    },
                    label = { Text("Phone Number") },
                    isError = showErrorMessages && !vendorData.phoneNumber.matches(Regex("^[0-9]{11}$")),
                    supportingText = {
                        if (showErrorMessages && !vendorData.phoneNumber.matches(Regex("^[0-9]{11}$"))) {
                            Text("Please enter a valid 11-digit phone number")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = vendorData.address,
                    onValueChange = { vendorData = vendorData.copy(address = it) },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    colors = textFieldColors
                )

                OutlinedTextField(
                    value = vendorData.remarks,
                    onValueChange = { vendorData = vendorData.copy(remarks = it) },
                    label = { Text("Remarks") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = textFieldColors
                )

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

                    Button(
                        onClick = {
                            showErrorMessages = true
                            if (!hasErrors && validateForm()) {
                                isLoading = true
                                Log.d("CoopVendorAddForm", "Submitting vendor with email: ${vendorData.email}, facility: ${vendorData.facility}")
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
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green1,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}