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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
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
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.coco.celestia.service.ImageService
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.model.TransactionData
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun AdminAddProduct(
    navController: NavController,
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

    LaunchedEffect(Unit) {
        onTypeSelected(selectedOption)
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    .semantics { testTag = "android:id/BackButton" }
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
                    .semantics { testTag = "android:id/AddProductTitle" }
            )
        }

        OutlinedTextField(
            value = productName,
            onValueChange = { onProductNameChanged(it) },
            label = { Text(text = "Product Name") },
            singleLine = true,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
                .semantics { testTag = "android:id/ProductNameField" }
        )

        Divider(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(vertical = 5.dp),
            thickness = 2.dp
        )

        Text(
            text = "Type of Product",
            fontSize = 18.sp,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
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
            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    onClick = {
                        onOptionSelected(option)
                        onTypeSelected(option)
                    },
                    modifier = Modifier.semantics { testTag = "android:id/RadioButton_$option" }
                )

                Text(
                    text = option,
                    modifier = Modifier.semantics { testTag = "android:id/RadioText_$option" }
                )
            }
        }

        Divider(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 5.dp),
            thickness = 2.dp
        )

        OutlinedTextField(
            value = productPrice,
            onValueChange = { onPriceChanged(it) },
            label = { Text(text = "Price/Kg") },
            singleLine = true,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
                .semantics { testTag = "android:id/ProductPriceField" }
        )

        Button(
            onClick = {
                openGallery()
            },
            modifier = Modifier
                .padding(horizontal = 16.dp)
        ) {
            Text("Add Product Image")
        }

        productImage?.let {
            Image(
                painter = rememberImagePainter(
                    ImageRequest.Builder(context)
                        .data(it)
                        .build()
                ),
                contentDescription = "Product Image",
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Fit
            )
        }
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
                onToastEvent(Triple(ToastStatus.FAILED, (productState as ProductState.ERROR).message, System.currentTimeMillis()))
            }
            is ProductState.SUCCESS -> {
                if (!transactionRecorded.value) {
                    transactionViewModel.recordTransaction(
                        uid = uid,
                        transaction = TransactionData(
                            transactionId = "Transaction-${UUID.randomUUID()}",
                            type = "ProductAdded",
                            date = formattedDateTime,
                            description = "${product.name} product added to the inventory."
                        )
                    )
                    transactionRecorded.value = true
                }
                onToastEvent(Triple(ToastStatus.SUCCESSFUL, "Product Added Successfully", System.currentTimeMillis()))
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
        onToastEvent(Triple(ToastStatus.WARNING, "All Fields must be filled", System.currentTimeMillis()))
    }
}
