package com.coco.celestia.screens.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
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

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Change $productName Price/Kg") },
        text = {
            Column {
                OutlinedTextField(
                    value = updatedPrice,
                    onValueChange = { updatedPrice = it },
                    label = { Text("Price/Kg") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/priceInputField" }
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                    onDismiss()
                }) {
                Text("Save", modifier = Modifier.semantics { testTag = "android:id/saveButton" })
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel", modifier = Modifier.semantics { testTag = "android:id/cancelButton" })
            }
        }
    )
}
