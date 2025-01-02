package com.coco.celestia.screens.coop.facility.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.Green1
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.TransactionData
import com.coco.celestia.viewmodel.model.WeightUnit
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

// TODO: Add checks for every field
// TODO: To add other fields later on

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductForm(
    userViewModel: UserViewModel,
    productViewModel: ProductViewModel,
    facilityViewModel: FacilityViewModel,
    quantity: Int,
    price: Double,
    isInStore: Boolean,
    weightUnit: WeightUnit,
    onProductNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onIsInStoreChange: (Boolean) -> Unit,
    onWeightUnitChange: (WeightUnit) -> Unit,
    onAddClick: () -> Unit,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val email = FirebaseAuth.getInstance().currentUser?.email.orEmpty()
    var weightUnitExpanded by remember { mutableStateOf(false) }
    val userData by userViewModel.userData.observeAsState()
    val productName by productViewModel.productName.observeAsState("")
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .semantics { testTag = "android:id/AddProductFormColumn" },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = productName,
            onValueChange = onProductNameChange,
            label = { Text("Product Name") },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "android:id/ProductNameField" }
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
                    .width(235.dp)
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
                    value = weightUnit.name.lowercase(),
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
                    WeightUnit.values().forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.name.lowercase()) },
                            onClick = {
                                onWeightUnitChange(unit)
                                weightUnitExpanded = false
                            },
                            modifier = Modifier.semantics { testTag = "android:id/WeightUnit_${unit.name}" }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = if (price == 0.0) "" else price.toString(),
            onValueChange = onPriceChange,
            label = { Text("Price per ${weightUnit.name.lowercase()}") },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "android:id/PriceField" },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
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

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                if (productName.isNotEmpty() && quantity > 0 && price > 0.0) {
                    val currentDateTime = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
                    val formattedDateTime = currentDateTime.format(formatter)

                    val product = ProductData(
                        name = productName,
                        quantity = quantity,
                        type = facilityName,
                        price = price,
                        weightUnit = weightUnit,
                        isInStore = isInStore,
                        dateAdded = formattedDateTime
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
                .padding(bottom = 16.dp),
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
    quantityAmount: Int,
    productType: String,
    price: Double,
    isInStore: Boolean,
    weightUnit: WeightUnit,
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
                quantity = quantityAmount,
                type = productType,
                price = price,
                weightUnit = weightUnit,
                isInStore = isInStore,
                dateAdded = formattedDateTime
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
                        description = "Added ${product.quantity}${weightUnit.name.lowercase()} of ${product.name}."
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
                        "${quantityAmount}${weightUnit.name.lowercase()} of $productName added to $productType inventory.",
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