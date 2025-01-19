package com.coco.celestia.screens.coop.facility.forms

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.Green1
import com.coco.celestia.ui.theme.White1
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.VendorViewModel
import com.coco.celestia.viewmodel.model.Constants
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.TransactionData
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)
@Composable
fun AddProductForm(
    navController: NavController,
    userViewModel: UserViewModel,
    productViewModel: ProductViewModel,
    facilityViewModel: FacilityViewModel,
    vendorViewModel: VendorViewModel,
    productId: String,
    quantity: Int,
    price: Double,
    totalPurchases: Double,
    reorderPoint: Double,
    isInStore: Boolean,
    weightUnit: String,
    onProductNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onReorderPointChange: (String) -> Unit,
    onIsInStoreChange: (Boolean) -> Unit,
    onWeightUnitChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit,
    isEditMode: Boolean = false,
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val email = FirebaseAuth.getInstance().currentUser?.email.orEmpty()
    var weightUnitExpanded by remember { mutableStateOf(false) }
    var vendorExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var productImage by remember { mutableStateOf<Uri?>(null) }
    var updatedProductImage by remember { mutableStateOf<Uri?>(null) }
    var isImageRequired by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        updatedProductImage = it
        isImageRequired = false
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                galleryLauncher.launch("image/*")
            } else {
                onEvent(Triple(
                    ToastStatus.WARNING,
                    "Grant app access to add product image.",
                    System.currentTimeMillis()
                ))
            }
        }
    )

    fun openGallery() {
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

    val userData by userViewModel.userData.observeAsState()
    val productName by productViewModel.productName.observeAsState("")
    val description by productViewModel.description.observeAsState("")
    val vendor by productViewModel.vendor.observeAsState("")
    val vendors by vendorViewModel.vendorData.observeAsState(emptyList())
    val facilityData by facilityViewModel.facilitiesData.observeAsState(emptyList())
    var facilityName by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }

    LaunchedEffect(uid) {
        userViewModel.fetchUser(uid)
    }

    LaunchedEffect(userData) {
        userData?.let {
            role = it.role
            productViewModel.fetchProducts("", role)
        }
    }

    LaunchedEffect(email, facilityData) {
        if (email.isNotEmpty() && facilityData.isNotEmpty()) {
            facilityName = facilityData.find { it.emails.contains(email) }?.name.orEmpty()
        }
    }

    LaunchedEffect(facilityViewModel.facilitiesData) {
        facilityViewModel.fetchFacilities()
    }

    LaunchedEffect(Unit) {
        vendorViewModel.fetchVendors()
    }

    LaunchedEffect(productId) {
        if (isEditMode && productId.isNotBlank()) {
            ImageService.fetchProductImage(productId) { uri ->
                productImage = uri
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(10.dp)
            .semantics { testTag = "android:id/AddProductFormColumn" },
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Product ID and Image Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                OutlinedTextField(
                    value = if (productId.isBlank()) {
                        val currentDateTime = LocalDateTime.now()
                        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                        val formattedDate = currentDateTime.format(formatter)
                        val nextCount = (productViewModel.getProductCount() + 1).toString().padStart(3, '0')
                        "PID-$formattedDate-$nextCount"
                    } else productId,
                    onValueChange = { /* Read only */ },
                    label = { Text("Product ID") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                )

                Box(
                    modifier = Modifier.size(150.dp)
                ) {
                    Image(
                        painter = rememberImagePainter(
                            data = updatedProductImage ?: productImage ?: R.drawable.product_icon,
                        ),
                        contentDescription = "Product Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                            .border(
                                width = if (isImageRequired) 2.dp else 0.dp,
                                color = if (isImageRequired) Color.Red else Color.Transparent,
                                shape = RectangleShape
                            ),
                        colorFilter = if (updatedProductImage == null && productImage == null)
                            ColorFilter.tint(Green1) else null
                    )

                    FloatingActionButton(
                        onClick = { openGallery() },
                        shape = CircleShape,
                        modifier = Modifier
                            .size(35.dp)
                            .align(Alignment.BottomEnd),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change Image",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Product Information Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Product Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = productName,
                    onValueChange = onProductNameChange,
                    label = { Text("Product Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/ProductNameField" }
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/DescriptionField" }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = if (quantity == 0) "" else quantity.toString(),
                        onValueChange = onQuantityChange,
                        label = { Text("Quantity") },
                        modifier = Modifier
                            .weight(1f)
                            .semantics { testTag = "android:id/QuantityField" },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    ExposedDropdownMenuBox(
                        expanded = weightUnitExpanded,
                        onExpandedChange = { weightUnitExpanded = !weightUnitExpanded },
                        modifier = Modifier
                            .width(150.dp)
                            .semantics { testTag = "android:id/WeightUnitDropdown" }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = weightUnit.lowercase(),
                            onValueChange = {},
                            label = { Text("Unit") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(weightUnitExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = weightUnitExpanded,
                            onDismissRequest = { weightUnitExpanded = false }
                        ) {
                            listOf(Constants.WEIGHT_GRAMS, Constants.WEIGHT_KILOGRAMS, Constants.WEIGHT_POUNDS).forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit.lowercase()) },
                                    onClick = {
                                        onWeightUnitChange(unit)
                                        weightUnitExpanded = false
                                    },
                                    modifier = Modifier.semantics { testTag = "android:id/WeightUnit_${unit}" }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sales Information Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Sales Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = if (price == 0.0) "" else price.toString(),
                    onValueChange = onPriceChange,
                    label = { Text("Price per ${weightUnit.lowercase()}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/PriceField" },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
                )
            }
        }

        // Inventory Tracking Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Inventory Tracking",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = if (reorderPoint == 0.0) "" else reorderPoint.toString(),
                    onValueChange = onReorderPointChange,
                    label = { Text("Reorder Point") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/ReorderPointField" },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
                )
            }
        }

        // Product Notes Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text("Product Notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/ProductNotes" },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                    maxLines = 4
                )
            }
        }

        // Availability Settings Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = White1)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "In-Store or Online Product",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Available In Store")
                    Switch(
                        checked = isInStore,
                        onCheckedChange = onIsInStoreChange,
                        modifier = Modifier.semantics { testTag = "android:id/StoreLocationSwitch" }
                    )
                }
            }
        }

        Button(
            onClick = {
                if (updatedProductImage == null && productImage == null) {
                    isImageRequired = true
                    onEvent(Triple(ToastStatus.WARNING, "Product image is required", System.currentTimeMillis()))
                    return@Button
                }

                if (productName.isNotEmpty() && quantity > 0 && price > 0.0) {
                    val currentDateTime = LocalDateTime.now()
                    val dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
                    val pidFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                    val formattedDateTime = currentDateTime.format(dateFormatter)
                    val formattedPIDDate = currentDateTime.format(pidFormatter)
                    val formattedTime = currentDateTime.format(timeFormatter)

                    val finalProductId = if (isEditMode) {
                        productId
                    } else {
                        val currentCount = productViewModel.getProductCount() + 1
                        "PID-$formattedPIDDate-$currentCount"
                    }

                    val product = ProductData(
                        productId = finalProductId,
                        timestamp = formattedTime,
                        name = productName,
                        description = description,
                        notes = notes,
                        quantity = quantity,
                        type = facilityName,
                        price = price,
                        vendor = vendor,
                        totalPurchases = totalPurchases,
                        totalQuantitySold = 0.0,
                        committedStock = 0.0,
                        reorderPoint = reorderPoint,
                        weightUnit = weightUnit,
                        isInStore = isInStore,
                        isActive = true,
                        dateAdded = formattedDateTime
                    )

                    updatedProductImage?.let { uri ->
                        ImageService.uploadProductPicture(finalProductId, uri) { success ->
                            if (success) {
                                if (isEditMode) {
                                    productViewModel.updateProduct(product)
                                    onEvent(Triple(ToastStatus.SUCCESSFUL, "Product updated successfully", System.currentTimeMillis()))
                                } else {
                                    productViewModel.addProduct(product)
                                    onEvent(Triple(ToastStatus.SUCCESSFUL, "Product added successfully", System.currentTimeMillis()))
                                }
                                onAddClick()

                                if (isInStore) {
                                    navController.navigate(Screen.CoopInStoreProducts.route) {
                                        popUpTo(Screen.AddProductInventory.route) { inclusive = true }
                                    }
                                } else {
                                    navController.navigate(Screen.CoopOnlineProducts.route) {
                                        popUpTo(Screen.AddProductInventory.route) { inclusive = true }
                                    }
                                }
                            } else {
                                onEvent(Triple(ToastStatus.FAILED, "Failed to upload image", System.currentTimeMillis()))
                            }
                        }
                    } ?: run {
                        if (isEditMode && productImage != null) {
                            productViewModel.updateProduct(product)
                            onEvent(Triple(ToastStatus.SUCCESSFUL, "Product updated successfully", System.currentTimeMillis()))
                            onAddClick()

                            if (isInStore) {
                                navController.navigate(Screen.CoopInStoreProducts.route) {
                                    popUpTo(Screen.AddProductInventory.route) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Screen.CoopOnlineProducts.route) {
                                    popUpTo(Screen.AddProductInventory.route) { inclusive = true }
                                }
                            }
                        } else {
                            onEvent(Triple(ToastStatus.WARNING, "Product image is required", System.currentTimeMillis()))
                            isImageRequired = true
                        }
                    }
                } else {
                    onEvent(Triple(ToastStatus.WARNING, "Please fill in all required fields", System.currentTimeMillis()))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green1)
        ) {
            val buttonText = if (isEditMode) "Update Product" else "Add Product"
            Text(
                text = buttonText,
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color.White
            )
        }
    }
}

@Composable
fun CoopAddInventory(
    navController: NavController,
    productViewModel: ProductViewModel,
    transactionViewModel: TransactionViewModel,
    productName: String,
    description: String,
    quantityAmount: Int,
    productType: String,
    price: Double,
    vendor: String,
    totalPurchases: Double,
    committedStock: Double,
    reorderPoint: Double,
    isInStore: Boolean,
    weightUnit: String,
//    isDelivery: Boolean,
//    isGcash: Boolean,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    val formattedDateTime = currentDateTime.format(formatter).toString()
    val productData by productViewModel.productData.observeAsState()
    val from by productViewModel.from.observeAsState("")

    LaunchedEffect(Unit) {
        if (productName.isNotEmpty() && quantityAmount > 0) {
            productViewModel.fetchProduct(from)
            val product = ProductData(
                name = productName,
                description = description,
                quantity = quantityAmount,
                type = productType,
                price = price,
                vendor = vendor,
                totalPurchases = totalPurchases,
                committedStock = committedStock,
                reorderPoint = reorderPoint,
                weightUnit = weightUnit,
                isInStore = isInStore,
                dateAdded = formattedDateTime,
//                collectionMethod = if (isDelivery) Constants.COLLECTION_DELIVERY else Constants.COLLECTION_PICKUP,
//                paymentMethod = if (isGcash) Constants.PAYMENT_GCASH else Constants.PAYMENT_CASH
            )

            if ((productData?.get(0)?.quantity ?: 0) - product.quantity >= 0 ||
                productName == "Green Beans"
            ) {
                transactionViewModel.recordTransaction(
                    uid = uid,
                    transaction = TransactionData(
                        transactionId = "Transaction-${UUID.randomUUID()}",
                        type = "Product_Added",
                        date = formattedDateTime,
                        description = "Added ${product.quantity}${weightUnit.lowercase()} of ${product.name}."
                    )
                )
                productViewModel.updateProductQuantity(product.name, product.quantity)
                productViewModel.updateFromProduct(product.name, product.quantity)
                if (isInStore) {
                    navController.navigate(Screen.CoopInStoreProducts.route)
                } else {
                    navController.navigate(Screen.CoopOnlineProducts.route)
                }
                onEvent(
                    Triple(
                        ToastStatus.SUCCESSFUL,
                        "${quantityAmount}${weightUnit.lowercase()} of $productName added to $productType inventory.",
                        System.currentTimeMillis()
                    )
                )
            } else {
                onEvent(
                    Triple(
                        ToastStatus.WARNING,
                        "Entered Quantity exceeds the limit of ${productData?.get(0)?.name}",
                        System.currentTimeMillis()
                    )
                )
                navController.navigate(Screen.AddProductInventory.route)
            }
        } else {
            onEvent(
                Triple(
                    ToastStatus.WARNING,
                    "Please fill in all fields",
                    System.currentTimeMillis()
                )
            )
            navController.navigate(Screen.AddProductInventory.route)
        }
    }
}