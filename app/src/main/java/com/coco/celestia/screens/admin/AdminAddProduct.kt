package com.coco.celestia.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.util.sendEmail
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.model.ProductData

@Composable
fun AdminAddProduct(
    navController: NavController,
    productName: String,
    onProductNameChanged: (String) -> Unit,
    onTypeSelected: (String) -> Unit
) {
    val radioOptions = listOf("Coffee", "Meat")
    val ( selectedOption, onOptionSelected) = remember{ mutableStateOf(radioOptions[0])}

    Column (
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.navigate(Screen.AdminInventory.route) },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Text(
                text = "Add Product",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }

        OutlinedTextField (
            value = productName,
            onValueChange = { onProductNameChanged(it) },
            label = { Text(text = "Product Name") },
            singleLine = true,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
        )

        radioOptions.forEach { option ->
            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (option == selectedOption),
                        onClick = {
                            onOptionSelected(option)
                            onTypeSelected(option)
                        }
                    )
                    .padding(start = 16.dp)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    onClick = null
                )

                Text(
                    text = option
                )
            }
        }
    }
}

@Composable
fun ConfirmAddProduct(
    navController: NavController,
    productViewModel: ProductViewModel,
    productName: String,
    productType: String,
    onToastEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val productState by productViewModel.productState.observeAsState()
    val product = ProductData (
        name = productName,
        quantity = 0,
        type = productType
    )

    LaunchedEffect(productState) {
        when (productState) {
            is ProductState.ERROR -> {
                onToastEvent(Triple(ToastStatus.FAILED, (productState as ProductState.ERROR).message, System.currentTimeMillis()))
            }
            is ProductState.SUCCESS -> {
                onToastEvent(Triple(ToastStatus.SUCCESSFUL, "Product Added Successfully", System.currentTimeMillis()))
                navController.navigate(Screen.AdminInventory.route)
            }
            else -> {}
        }
    }

    if (productName.isNotEmpty() && productType.isNotEmpty()) {
        productViewModel.addProduct(product)
    } else {
        onToastEvent(Triple(ToastStatus.WARNING, "All Fields must be filled", System.currentTimeMillis()))
    }
}
