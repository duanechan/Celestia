package com.coco.celestia.screens.client

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.viewmodel.FarmerItemViewModel
import com.coco.celestia.viewmodel.ItemState
import com.coco.celestia.viewmodel.ProductViewModel
import android.util.Log

@Composable
fun ShowAvailableFarmers(
    productName: String,
    onDismissRequest: () -> Unit,
    itemViewModel: FarmerItemViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel(),
) {
    val productData by productViewModel.productData.observeAsState(emptyList())
    val itemData by itemViewModel.itemData.observeAsState(emptyList())
    val itemState by itemViewModel.itemState.observeAsState(ItemState.LOADING)

    LaunchedEffect(productName) {
        itemViewModel.getFarmersWithProduct(productName)
    }

    AlertDialog(
        onDismissRequest = {
            onDismissRequest()
        },
        title = { Text("Available Farmers for $productName") },
        text = {
            when (itemState) {
                ItemState.LOADING -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                ItemState.SUCCESS -> {
                    val availableFarmers = itemData.filter { it.name.equals(productName, ignoreCase = true) }

                    if (availableFarmers.isEmpty()) {
                        Text("No farmers currently have this product available.")
                    } else {
                        LazyColumn {
                            items(availableFarmers.filter { it.quantity > 0 }) { product ->
//                                val farmerNames = product.farmerNames

                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
//                                            farmerNames.forEach { farmerName ->
//                                                Text(
//                                                    text = farmerName,
//                                                    style = MaterialTheme.typography.titleMedium,
//                                                    color = MaterialTheme.colorScheme.secondary
//                                                )
//                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
//                                            Text(
//                                                text = "₱${product.priceKg}/kg",
//                                                style = MaterialTheme.typography.bodyMedium,
//                                                color = MaterialTheme.colorScheme.secondary
//                                            )
                                        }
                                        Text(
                                            text = "${product.quantity} kg",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                ItemState.EMPTY -> {
                    Text("No farmers currently have this product available.")
                }
                is ItemState.ERROR -> {
                    Text("Error: ${(itemState as ItemState.ERROR).message}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onDismissRequest()
            }) {
                Text("Close")
            }
        }
    )
}