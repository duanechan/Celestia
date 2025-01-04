package com.coco.celestia.screens.coop.facility.forms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
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

// TODO: Add checks for every field

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductForm(
    userViewModel: UserViewModel,
    productViewModel: ProductViewModel,
    facilityViewModel: FacilityViewModel,
    vendorViewModel: VendorViewModel,
    quantity: Int,
    price: Double,
    purchasingCost: Double,
    openingStock: Double,
    reorderPoint: Double,
    isInStore: Boolean,
    weightUnit: String,
    isDelivery: Boolean,
    isGcash: Boolean,
    onProductNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onVendorChange: (String) -> Unit,
    onPurchasingCostChange: (String) -> Unit,
    onOpeningStockChange: (String) -> Unit,
    onReorderPointChange: (String) -> Unit,
    onIsInStoreChange: (Boolean) -> Unit,
    onWeightUnitChange: (String) -> Unit,
    onCollectionMethodChange: (Boolean) -> Unit,
    onPaymentMethodChange: (Boolean) -> Unit,
    onAddClick: () -> Unit,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val email = FirebaseAuth.getInstance().currentUser?.email.orEmpty()
    var weightUnitExpanded by remember { mutableStateOf(false) }
    var vendorExpanded by remember { mutableStateOf(false) }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(10.dp)
            .semantics { testTag = "android:id/AddProductFormColumn" },
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                            singleLine = true,
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

        // Purchasing Information Section
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
                    text = "Purchasing Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                ExposedDropdownMenuBox(
                    expanded = vendorExpanded,
                    onExpandedChange = { vendorExpanded = !vendorExpanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/VendorDropdown" }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = vendor,
                        onValueChange = {},
                        label = { Text("Vendor") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(vendorExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = vendorExpanded,
                        onDismissRequest = { vendorExpanded = false }
                    ) {
                        vendors.forEach { vendorItem ->
                            val fullName = "${vendorItem.firstName} ${vendorItem.lastName}".trim()
                            DropdownMenuItem(
                                text = { Text(fullName) },
                                onClick = {
                                    onVendorChange(fullName)
                                    vendorExpanded = false
                                },
                                modifier = Modifier.semantics {
                                    testTag = "android:id/Vendor_${fullName}"
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = if (purchasingCost == 0.0) "" else purchasingCost.toString(),
                    onValueChange = onPurchasingCostChange,
                    label = { Text("Purchasing Cost") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/PurchasingCostField" },
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
                    value = if (openingStock == 0.0) "" else openingStock.toString(),
                    onValueChange = onOpeningStockChange,
                    label = { Text("Opening Stock") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/OpeningStockField" },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
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

                AnimatedVisibility(
                    visible = !isInStore,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isDelivery) "Delivery" else "Pickup")
                            Switch(
                                checked = isDelivery,
                                onCheckedChange = onCollectionMethodChange,
                                modifier = Modifier.semantics { testTag = "android:id/CollectionMethodSwitch" }
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isGcash) "GCash" else "Cash")
                            Switch(
                                checked = isGcash,
                                onCheckedChange = onPaymentMethodChange,
                                modifier = Modifier.semantics { testTag = "android:id/PaymentMethodSwitch" }
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                if (productName.isNotEmpty() && quantity > 0 && price > 0.0) {
                    val currentDateTime = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
                    val formattedDateTime = currentDateTime.format(formatter)

                    val product = ProductData(
                        name = productName,
                        description = description,
                        quantity = quantity,
                        type = facilityName,
                        price = price,
                        vendor = vendor,
                        purchasingCost = purchasingCost,
                        openingStock = openingStock,
                        reorderPoint = reorderPoint,
                        weightUnit = weightUnit,
                        isInStore = isInStore,
                        dateAdded = formattedDateTime,
                        collectionMethod = if (isDelivery) Constants.COLLECTION_DELIVERY else Constants.COLLECTION_PICKUP,
                        paymentMethod = if (isGcash) Constants.PAYMENT_GCASH else Constants.PAYMENT_CASH
                    )
                    productViewModel.addProduct(product)

                    onEvent(
                        Triple(
                            ToastStatus.SUCCESSFUL,
                            "Product added successfully",
                            System.currentTimeMillis()
                        )
                    )
                    onAddClick()
                } else {
                    onEvent(
                        Triple(
                            ToastStatus.WARNING,
                            "Please fill in all required fields",
                            System.currentTimeMillis()
                        )
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green1)
        ) {
            Text(
                text = "Add Product",
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
    purchasingCost: Double,
    openingStock: Double,
    reorderPoint: Double,
    isInStore: Boolean,
    weightUnit: String,
    isDelivery: Boolean,
    isGcash: Boolean,
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
                purchasingCost = purchasingCost,
                openingStock = openingStock,
                reorderPoint = reorderPoint,
                weightUnit = weightUnit,
                isInStore = isInStore,
                dateAdded = formattedDateTime,
                collectionMethod = if (isDelivery) Constants.COLLECTION_DELIVERY else Constants.COLLECTION_PICKUP,
                paymentMethod = if (isGcash) Constants.PAYMENT_GCASH else Constants.PAYMENT_CASH
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