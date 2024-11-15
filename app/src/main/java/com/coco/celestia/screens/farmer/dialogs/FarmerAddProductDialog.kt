@file:OptIn(ExperimentalCoilApi::class)

package com.coco.celestia.screens.farmer.dialogs

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
    val context = LocalContext.current
    val productImage by remember { mutableStateOf<Uri?>(null) }
    var updatedProductImage by remember { mutableStateOf<Uri?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        updatedProductImage = it
    }
    var toastEvent by remember { mutableStateOf(Triple(ToastStatus.INFO, "", 0L)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                galleryLauncher.launch("image/*")
            } else {
                toastEvent = Triple(
                    ToastStatus.WARNING,
                    "Grant app access to add product image.",
                    System.currentTimeMillis()
                )
            }
        }
    )

    fun openGallery () {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when (ContextCompat.checkSelfPermission(context, permission)) {
            PackageManager.PERMISSION_GRANTED -> {
                galleryLauncher.launch("image/*")
            }

            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }

    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val quantityInt = quantity.toIntOrNull()
                    nameError = name.isBlank()
                    quantityError = quantityInt == null || quantityInt <= 0
                    seasonStartError = seasonStart.isBlank()
                    seasonEndError = seasonEnd.isBlank()

                    if (!nameError && !quantityError && !seasonStartError && !seasonEndError) {
                        updatedProductImage?.let {
                            ImageService.uploadProductPicture(name, it) { status ->
                                if (status) {
                                    Log.d("ProfileScreen", "Product image uploaded successfully!")
                                } else {
                                    Log.d("ProfileScreen", "Product image upload failed!")
                                }
                            }
                        }
                        onConfirm(name, quantityInt!!, seasonStart, seasonEnd)
                        onDismiss()
                    }
                },
                enabled = name.isNotEmpty() && quantity.isNotEmpty() && seasonStart.isNotEmpty() && seasonEnd.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = OliveGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(8.dp)
                    .widthIn(min = 100.dp)
                    .height(36.dp)
                    .semantics { testTagsAsResourceId = true }
                    .semantics { testTag = "android:id/confirmButton" }
            ) {
                Text("Confirm", color = Apricot, fontSize = 14.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss,
                modifier = Modifier.semantics { testTagsAsResourceId = true }
                    .semantics { testTag = "android:id/dismissButton" }) {
                Text("Cancel", color = Cocoa)
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
                Box (
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 12.dp)
                ) {
                    Image(
                        painter = rememberImagePainter(
                            data = updatedProductImage ?: productImage ?: R.drawable.product_icon,
                        ),
                        contentDescription = "Product Image",
                        modifier = Modifier
                            .size(150.dp)
                            .align(Alignment.Center),
                        colorFilter = ColorFilter.tint(Cocoa)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    FloatingActionButton(
                        onClick = { openGallery() },
                        shape = CircleShape,
                        modifier = Modifier
                            .size(35.dp)
                            .align(Alignment.BottomEnd),
                        contentColor = Color.White.copy(0.5f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change Image",
                            tint = DarkBlue
                        )
                    }
                }

                // Product Name
                Text(text = "Enter Product Name", color = Cocoa, fontWeight = FontWeight.Bold)
                TextField(
                    value = name,
                    onValueChange = {
                        if (it.length <= 200) {  // Limit to 200 characters
                            name = it
                            nameError = false
                        }
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
                        modifier = Modifier.semantics { testTagsAsResourceId = true }
                            .semantics { testTag = "vegNameError" }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quantity Input
                Text(text = "Enter Quantity (Kg)", color = Cocoa, fontWeight = FontWeight.Bold)
                TextField(
                    value = quantity,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || (newValue.all { it.isDigit() } && (newValue.toIntOrNull()
                                ?: 0) <= 5000)) {
                            quantity = newValue
                            quantityError = false
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
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
                                    if (currentValue < 5000) {
                                        quantity = (currentValue + 1).toString()
                                    }
                                },
                                modifier = Modifier.size(24.dp)
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
                                modifier = Modifier.size(24.dp)
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