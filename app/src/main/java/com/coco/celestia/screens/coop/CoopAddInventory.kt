package com.coco.celestia.screens.coop

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.ProductData
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductForm(
    userViewModel: UserViewModel,
    productViewModel: ProductViewModel,
    quantity: Int,
    defectBeans: Int,
    onProductNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onDefectBeansChange: (String) -> Unit,
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    var expanded by remember { mutableStateOf(false) }
    val userData by userViewModel.userData.observeAsState()
    val productData by productViewModel.productData.observeAsState()
    val productName by productViewModel.productName.observeAsState("")
    val from by productViewModel.from.observeAsState("")
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Add Product",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        // From
        OutlinedTextField(
            value = from,
            onValueChange = {},
            label = { Text("From") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        // Product Name
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.semantics { testTag = "android:id/ProductDropdown" }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = productName,
                onValueChange = {},
                placeholder = { Text("Select Product") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                productData?.forEach { productItem ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(productItem.name) },
                        onClick = {
                            onProductNameChange(productItem.name)
                            expanded = false
                        }
                    )
                }
            }
        }

        // Quantity
        OutlinedTextField(
            value = if (quantity == 0) "" else quantity.toString(),
            onValueChange = onQuantityChange,
            label = { Text("Quantity (kg)") },
            modifier = Modifier
                .fillMaxWidth()
                .semantics { testTag = "android:id/QuantityField" },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        // Defect Beans
        if (productName == "Sorted Beans") {
            OutlinedTextField(
                value = if (defectBeans == 0) "" else defectBeans.toString(),
                onValueChange = onDefectBeansChange,
                label = { Text("Defect Beans (kg)") },
                modifier = Modifier
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
    }
}

@Composable
fun CoopAddInventory(
    navController: NavController,
    productViewModel: ProductViewModel,
    productName: String,
    quantityAmount: Int,
    productType: String,
    defectBeans: Int,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val productData by productViewModel.productData.observeAsState()
    val from by productViewModel.from.observeAsState("")

    LaunchedEffect(Unit) {
        if (productName.isNotEmpty() &&
            quantityAmount > 0
        ) {
            productViewModel.fetchProduct(from)
            val product = ProductData(
                name = productName,
                quantity = quantityAmount,
                type = productType
            )

            if (((productData?.get(0)?.quantity ?: 0) - (product.quantity + defectBeans)) >= 0 ||
                productName == "Green Beans") {
                productViewModel.updateProductQuantity(product.name, product.quantity)
                productViewModel.updateFromProduct(product.name, product.quantity, defectBeans)
                navController.navigate(Screen.CoopInventory.route)
                onEvent(
                    Triple(
                        ToastStatus.SUCCESSFUL,
                        "${quantityAmount}kg of $productName added to $productType inventory.",
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