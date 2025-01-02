package com.coco.celestia.screens.coop.facility

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.viewmodel.PurchaseOrderState
import com.coco.celestia.viewmodel.PurchaseOrderViewModel
import com.coco.celestia.viewmodel.model.PurchaseOrder
import com.coco.celestia.viewmodel.model.PurchaseOrderItem
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel

// TODO: fix bug on switching between all and draft tabs
// TODO: filtering options

@Composable
fun CoopPurchases(
    navController: NavController,
    currentEmail: String,
    purchaseOrderViewModel: PurchaseOrderViewModel,
    facilityViewModel: FacilityViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())
    val facilityState by facilityViewModel.facilityState.observeAsState(FacilityState.LOADING)

    LaunchedEffect(Unit) {
        facilityViewModel.fetchFacilities()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CoopBackground)
    ) {
        when (facilityState) {
            is FacilityState.LOADING -> {
                LoadingScreen("Loading facilities...")
            }
            is FacilityState.ERROR -> {
                ErrorScreen((facilityState as FacilityState.ERROR).message)
            }
            else -> {
                val userFacility = facilitiesData.find { facility ->
                    facility.emails.contains(currentEmail)
                }

                if (userFacility != null) {
                    LaunchedEffect(Unit) {
                        purchaseOrderViewModel.fetchPurchaseOrders(facilityName = userFacility.name)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                TextButton(
                                    onClick = {
                                        selectedTab = "All"
                                        purchaseOrderViewModel.fetchPurchaseOrders(
                                            filter = "all",
                                            searchQuery = searchQuery,
                                            facilityName = userFacility.name
                                        )
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = if (selectedTab == "All")
                                            Green1
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text(
                                        text = "All",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        selectedTab = "Draft"
                                        purchaseOrderViewModel.fetchPurchaseOrders(
                                            filter = "draft",
                                            searchQuery = searchQuery,
                                            facilityName = userFacility.name
                                        )
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = if (selectedTab == "Draft")
                                            Green1
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text(
                                        text = "Draft",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }

                            IconButton(onClick = { /* Handle filter click */ }) {
                                Icon(
                                    imageVector = Icons.Default.List,
                                    contentDescription = "Filter"
                                )
                            }
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { newQuery ->
                                searchQuery = newQuery
                                purchaseOrderViewModel.fetchPurchaseOrders(
                                    filter = selectedTab.lowercase(),
                                    searchQuery = newQuery,
                                    facilityName = userFacility.name
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            placeholder = { Text("Search purchase orders...") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = if (searchQuery.isNotEmpty()) {
                                {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        purchaseOrderViewModel.fetchPurchaseOrders(
                                            filter = selectedTab.lowercase(),
                                            facilityName = userFacility.name
                                        )
                                    }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear search",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                cursorColor = Green1,
                                focusedBorderColor = Green1,
                                unfocusedBorderColor = Green1,
                                focusedLabelColor = Green1,
                                unfocusedLabelColor = Green1,
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        when (val state = purchaseOrderViewModel.purchaseOrderState.value) {
                            is PurchaseOrderState.LOADING -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Green1)
                                }
                            }
                            is PurchaseOrderState.SUCCESS -> {
                                LazyColumn {
                                    items(purchaseOrderViewModel.purchaseOrderData.value ?: emptyList()) { purchaseOrder ->
                                        PurchaseOrderCard(
                                            purchaseOrder = purchaseOrder,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp)
                                        )
                                    }
                                }
                            }
                            is PurchaseOrderState.EMPTY -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No purchase orders found for ${userFacility.name}")
                                }
                            }
                            is PurchaseOrderState.ERROR -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(state.message)
                                }
                            }
                            else -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Loading purchase orders...")
                                }
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = { navController.navigate(Screen.CoopPurchaseForm.route) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        containerColor = White1,
                        contentColor = Green1
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Purchase Order"
                        )
                    }
                } else {
                    NoFacilityScreen()
                }
            }
        }
    }
}

@Composable
fun PurchaseOrderCard(
    purchaseOrder: PurchaseOrder,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = White1
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = purchaseOrder.vendor,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "PHP${calculateTotalAmount(purchaseOrder.items)}",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${purchaseOrder.date} • ${purchaseOrder.purchaseNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@SuppressLint("DefaultLocale")
private fun calculateTotalAmount(items: List<PurchaseOrderItem>): String {
    val total = items.sumOf { it.quantity * it.rate }
    return String.format("%,.2f", total)
}