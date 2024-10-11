package com.coco.celestia.screens.client

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.ui.theme.Orange
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.model.OrderData
import com.google.firebase.auth.FirebaseAuth

@Composable
fun OrderPanel() {
    val orderViewModel: OrderViewModel = viewModel()
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid.toString()


    LaunchedEffect(Unit) {
        orderViewModel.fetchOrders(
            uid = uid,
            filter = "Coffee, Meat, Vegetable"
        )
    }

    when (orderState) {
        is OrderState.LOADING -> {
            Text("Loading orders...")
        }
        is OrderState.ERROR -> {
            Text("Failed to load orders: ${(orderState as OrderState.ERROR).message}")
        }
        is OrderState.EMPTY -> {
            Text("Mag-order ka na, man.")
        }
        is OrderState.SUCCESS -> {
            LazyColumn {
                items(orderData) { order ->
                    OrderItem(order)
                }
            }
        }
        else -> {}
    }

}

@Composable
fun OrderItem(order: OrderData) {
    var showDialog by remember { mutableStateOf(false) }
    var action by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Card {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .animateContentSize()
            ) {
//                Text(text = if (order.orderData.type != "Vegetable") "${order.orderData.name}, ${order.orderData.quantity}kg" else order.orderData.name,
//                    fontSize = 30.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                Text(text = "${order.status} ●", fontSize = 20.sp, fontWeight = FontWeight.Light, color = Orange)
                Text(text = "${order.street}, ${order.barangay}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = order.orderDate)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.Absolute.Right,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            action = "Edit"
                            showDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(contentColor = Color.Gray, containerColor = Color.Transparent)
                    ) {
                        Text("Edit")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (showDialog) {
        EditOrderDialog(order = order, onDismiss = { showDialog = false })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOrderDialog(order: OrderData, onDismiss: () -> Unit) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid.toString()
    val productViewModel: ProductViewModel = viewModel()
    val productData by productViewModel.productData.observeAsState(emptyList())
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)
//    var selectedType by remember { mutableStateOf(order.orderData.type) }
//    var editedQuantity by remember { mutableStateOf(order.orderData.quantity.toString()) }
    val orderViewModel: OrderViewModel = viewModel()
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
//        productViewModel.fetchProductByType(order.orderData.name)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Order") },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
//                    TextField(
//                        value = selectedType,
//                        onValueChange = { },
//                        label = { Text("Type") },
//                        readOnly = true,
//                        modifier = Modifier
//                            .menuAnchor()
//                            .fillMaxWidth()
//                    )
//                    ExposedDropdownMenu(
//                        expanded = expanded,
//                        onDismissRequest = { expanded = false }
//                    ) {
//                        when (productState) {
//                            is ProductState.LOADING -> {
//                                DropdownMenuItem(text = { Text("Loading products...") }, onClick = {})
//                            }
//                            is ProductState.ERROR -> {
//                                DropdownMenuItem(text = { Text("Failed to load products: ${(productState as ProductState.ERROR).message}") }, onClick = {})
//                            }
//                            is ProductState.EMPTY -> {
//                                DropdownMenuItem(text = { Text("No products available.") }, onClick = {})
//                            }
//                            is ProductState.SUCCESS -> {
//                                if (productData.isNotEmpty()) {
//                                    productData.forEach { product ->
//                                        DropdownMenuItem(
//                                            text = { Text(product.name) },
//                                            onClick = {
//                                                expanded = false
//                                                selectedType = product.name
//                                            }
//                                        )
//                                    }
//                                } else {
//                                    DropdownMenuItem(text = { Text("No products available.") }, onClick = {})
//                                }
//                            }
//
//                            else -> {}
//                        }
//                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
//                TextField(
//                    value = editedQuantity,
//                    onValueChange = { editedQuantity = it },
//                    label = { Text("Quantity (kg)") },
//                    modifier = Modifier.fillMaxWidth()
//                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
//                    val quantity = editedQuantity.toIntOrNull() ?: 0
//                    val updatedProductData = order.orderData.copy(type = selectedType, quantity = quantity)
//                    val updatedOrder = order.copy(orderData = updatedProductData)
//                    orderViewModel.updateOrder(updatedOrder)
//                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}