@file:OptIn(ExperimentalCoilApi::class)

package com.coco.celestia.screens.admin

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.model.ProductData
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.service.ImageService
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.model.TransactionData
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun AdminAddProduct(
    productPrice: String,
    productName: String,
    onUpdatedProductImage: (Uri?) -> Unit,
    onProductNameChanged: (String) -> Unit,
    onTypeSelected: (String) -> Unit,
    onPriceChanged: (String) -> Unit,
    onToastEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val radioOptions = listOf("Coffee", "Meat")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
    val context = LocalContext.current
    var productImage by remember { mutableStateOf<Uri?>(null) }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        onUpdatedProductImage(it)
        productImage = it
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                galleryLauncher.launch("image/*")
            } else {
                onToastEvent(
                    Triple(
                        ToastStatus.WARNING,
                        "Grant app access to add product image.",
                        System.currentTimeMillis()
                    )
                )
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

    LaunchedEffect(productImage) {
        onTypeSelected(selectedOption)
        if (productName.isNotEmpty()) {
            ImageService.fetchProfilePicture(productName) {
                productImage = it
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 10.dp, vertical = 16.dp)
            .semantics { testTag = "AdminAddProductColumn" }
    ) {
        Row(
            modifier = Modifier
                .padding(bottom = 10.dp)
                .semantics { testTag = "ProductImageRow" }
        ) {
            Column {
                Image(
                    painter = rememberImagePainter(data = productImage ?: R.drawable.product_image),
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .size(150.dp)
                        .semantics { testTag = "ProductImage" },
                    contentScale = ContentScale.Crop
                )

                Button(
                    onClick = {
                        openGallery()
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                        .semantics { testTag = "AddImageButton" }
                ) {
                    Text("Add Image")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Product Name",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp)
                        .semantics { testTag = "ProductNameLabel" }
                )

                OutlinedTextField(
                    value = productName,
                    onValueChange = { onProductNameChanged(it) },
                    singleLine = true,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                        .semantics { testTag = "ProductNameField" }
                )

                Text(
                    text = "Product Price",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp)
                        .semantics { testTag = "ProductPriceLabel" }
                )

                OutlinedTextField(
                    value = productPrice,
                    onValueChange = { onPriceChanged(it) },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                        .semantics { testTag = "ProductPriceField" }
                )
            }
        }

        Divider(
            modifier = Modifier
                .padding(vertical = 5.dp)
                .semantics { testTag = "Divider1" },
            thickness = 2.dp
        )

        Text(
            text = "Type of Product",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
                .semantics { testTag = "ProductTypeLabel" }
        )

        radioOptions.forEach { option ->
            Row(
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
                    .semantics { testTag = "RadioOptionRow_$option" }
            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    onClick = {
                        onOptionSelected(option)
                        onTypeSelected(option)
                    },
                    modifier = Modifier.semantics { testTag = "RadioButton_$option" }
                )

                Text(
                    text = option,
                    modifier = Modifier.semantics { testTag = "RadioText_$option" }
                )
            }
        }

        Divider(
            modifier = Modifier
                .padding(top = 5.dp)
                .semantics { testTag = "Divider2" },
            thickness = 2.dp
        )
    }
}

@Composable
fun ConfirmAddProduct(
    navController: NavController,
    productViewModel: ProductViewModel,
    transactionViewModel: TransactionViewModel,
    productName: String,
    productType: String,
    productPrice: String,
    updatedProductImage: Uri?,
    onToastEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    val formattedDateTime = currentDateTime.format(formatter).toString()
    val transactionRecorded = remember { mutableStateOf(false) }
    val productState by productViewModel.productState.observeAsState()
    val product = ProductData(
        name = productName,
        quantity = 0,
        type = productType,
        priceKg = productPrice.toDouble()
    )

    LaunchedEffect(productState) {
        when (productState) {
            is ProductState.ERROR -> {
                onToastEvent(
                    Triple(
                        ToastStatus.FAILED,
                        (productState as ProductState.ERROR).message,
                        System.currentTimeMillis()
                    )
                )
            }
            is ProductState.SUCCESS -> {
                if (!transactionRecorded.value) {
                    transactionViewModel.recordTransaction(
                        uid = uid,
                        transaction = TransactionData(
                            transactionId = "Transaction-${UUID.randomUUID()}",
                            type = "Product_Added",
                            date = formattedDateTime,
                            description = "${product.name} product added to the inventory."
                        )
                    )
                    transactionRecorded.value = true
                }
                onToastEvent(
                    Triple(
                        ToastStatus.SUCCESSFUL,
                        "Product Added Successfully",
                        System.currentTimeMillis()
                    )
                )
                navController.navigate(Screen.AdminInventory.route)
            }
            else -> {}
        }
    }

    if (productName.isNotEmpty() && productType.isNotEmpty() && productPrice.isNotEmpty()) {
        productViewModel.addProduct(product)
        updatedProductImage?.let {
            ImageService.uploadProductPicture(productName, it) { status ->
                if (status) {
                    Log.d("ProfileScreen", "Product image uploaded successfully!")
                } else {
                    Log.d("ProfileScreen", "Product image upload failed!")
                }
            }
        }
    } else {
        onToastEvent(
            Triple(
                ToastStatus.WARNING,
                "All Fields must be filled",
                System.currentTimeMillis()
            )
        )
    }

    // Adding semantic test tags for testing purposes
    Column(modifier = Modifier.semantics { testTag = "ConfirmAddProductColumn" }) {
        Text(
            text = "Confirm Product Details",
            modifier = Modifier.semantics { testTag = "ConfirmProductDetailsText" }
        )

        Text(
            text = "Product Name: $productName",
            modifier = Modifier.semantics { testTag = "ProductNameText" }
        )

        Text(
            text = "Product Type: $productType",
            modifier = Modifier.semantics { testTag = "ProductTypeText" }
        )

        Text(
            text = "Product Price: $productPrice",
            modifier = Modifier.semantics { testTag = "ProductPriceText" }
        )

        Button(
            onClick = {
                if (productName.isNotEmpty() && productType.isNotEmpty() && productPrice.isNotEmpty()) {
                    productViewModel.addProduct(product)
                }
            },
            modifier = Modifier.semantics { testTag = "AddProductButton" }
        ) {
            Text("Add Product")
        }
    }
}