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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.core.content.ContextCompat
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.service.ImageService
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.TransactionData
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun EditProduct(
    productViewModel: ProductViewModel,
    transactionViewModel: TransactionViewModel,
    productData: ProductData,
    onDismiss: () -> Unit
) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
    val formattedDateTime = currentDateTime.format(formatter).toString()
    val productName by remember { mutableStateOf(productData.name) }
    var updatedPrice by remember { mutableStateOf(productData.priceKg.toString()) }
    val context = LocalContext.current
    var productImage by remember { mutableStateOf<Uri?>(null) }
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

    LaunchedEffect(Unit) {
        ImageService.fetchProfilePicture(productName) {
            productImage = it
        }
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Edit $productName",
                modifier = Modifier.semantics { testTag = "android:id/dialogTitle" }
            )
        },
        text = {
            Column(modifier = Modifier.semantics { testTag = "android:id/dialogContent" }) {
                OutlinedTextField(
                    value = updatedPrice,
                    onValueChange = { updatedPrice = it },
                    label = { Text("Price/Kg") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/priceInputField" }
                )
                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        openGallery()
                    }
                ) {
                    Text("Change Product Image")
                }

                Image(
                    painter = rememberImagePainter(
                        data = updatedProductImage ?: productImage ?: R.drawable.profile_icon
                    ),
                    contentDescription = "Product Image"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    transactionViewModel.recordTransaction(
                        uid = uid,
                        transaction = TransactionData(
                            transactionId = "Transaction-${UUID.randomUUID()}",
                            type = "ProductUpdated",
                            date = formattedDateTime,
                            description = "$productName price updated to ₱${updatedPrice.toDouble()}."
                        )
                    )
                    productViewModel.updateProductPrice(productName, updatedPrice.toDouble())
                    updatedProductImage?.let {
                        ImageService.uploadProductPicture(productName, it) { status ->
                            if (status) {
                                Log.d("ProfileScreen", "Product image uploaded successfully!")
                            } else {
                                Log.d("ProfileScreen", "Product image upload failed!")
                            }
                        }
                    }
                    onDismiss()
                },
                modifier = Modifier.semantics { testTag = "android:id/saveButton" }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                modifier = Modifier.semantics { testTag = "android:id/cancelButton" }
            ) {
                Text("Cancel")
            }
        }
    )
}
