package com.coco.celestia.screens.coop.facility

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.model.ProductData

@Composable
fun CoopInventory(navController: NavController, role: String, userEmail: String) {
    val facilityViewModel: FacilityViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)
    val productData by productViewModel.productData.observeAsState(emptyList())
    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())
    val facilityState by facilityViewModel.facilityState.observeAsState(FacilityState.LOADING)

    LaunchedEffect(Unit) {
        facilityViewModel.fetchFacilities()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (facilityState) {
            is FacilityState.LOADING -> LoadingScreen("Loading facilities...")
            is FacilityState.ERROR -> ErrorScreen((facilityState as FacilityState.ERROR).message)
            else -> {
                val userFacility = facilitiesData.find { it.emails.contains(userEmail) }

                if (userFacility != null) {
                    val facilityName = userFacility.name
                    LaunchedEffect(facilityName) {
                        productViewModel.fetchProductByType(facilityName)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .height(860.dp)
                            .background(CoopBackground)
                            .verticalScroll(rememberScrollState())
                            .semantics { testTag = "android:id/CoopInventoryColumn" }
                    ) {
                        when (productState) {
                            is ProductState.LOADING -> LoadingScreen("Loading products...")
                            is ProductState.ERROR -> ErrorScreen((productState as ProductState.ERROR).message)
                            is ProductState.SUCCESS -> {
                                if (productData.isNotEmpty() && role.contains("Coop", ignoreCase = true)) {
                                    LaunchedEffect(Unit) {
                                        Log.d("CoopInventory", "Facility name being passed: $facilityName")
                                        navController.navigate(Screen.CoopInStoreProducts.createRoute(facilityName))
                                    }
                                }
                            }
                            is ProductState.EMPTY -> {
                                LaunchedEffect(Unit) {
                                    navController.navigate(Screen.AddProductInventory.route)
                                }
                            }
                        }
                    }

                    FloatingActionButton(
                        onClick = {
                            navController.navigate(Screen.AddProductInventory.route) {
                                popUpTo(Screen.AddProductInventory.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .semantics { testTag = "android:id/AddProductFAB" }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Product"
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
fun CoopProductInventory(
    navController: NavController,
    facilityName: String,
    currentEmail: String,
    isInStore: Boolean,
    productViewModel: ProductViewModel = viewModel(),
    facilityViewModel: FacilityViewModel = viewModel()
) {
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)
    val productData by productViewModel.productData.observeAsState(emptyList())
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
                    LaunchedEffect(userFacility.name) {
                        productViewModel.fetchProductByType(userFacility.name)
                    }

                    when (productState) {
                        is ProductState.LOADING -> {
                            LoadingScreen("Loading products...")
                        }
                        is ProductState.ERROR -> {
                            ErrorScreen((productState as ProductState.ERROR).message)
                        }
                        is ProductState.SUCCESS -> {
                            val filteredProducts = productData.filter { product ->
                                product.isInStore == isInStore &&
                                        product.type == userFacility.name
                            }

                            if (filteredProducts.isEmpty()) {
                                EmptyProductsScreen(isInStore, userFacility.name)
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    itemsIndexed(filteredProducts) { index, product ->
                                        ProductCard(
                                            product = product,
                                            onClick = {}
                                        )
                                        if (index < filteredProducts.lastIndex) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        }
                        is ProductState.EMPTY -> {
                            EmptyProductsScreen(isInStore, userFacility.name)
                        }
                    }

                    FloatingActionButton(
                        onClick = {
                            navController.navigate(Screen.AddProductInventory.route)
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .semantics { testTag = "android:id/AddProductFAB" },
                        containerColor = White1
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Product"
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
fun LoadingScreen(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun ErrorScreen(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Error: $message",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyProductsScreen(isInStore: Boolean, facilityName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "No Products",
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No ${if (isInStore) "in-store" else "online"} products available at $facilityName",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun NoFacilityScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CoopBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "You are not assigned to any facility.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun ProductCard(
    product: ProductData,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics { testTag = "android:id/ProductCard" },
        colors = CardDefaults.cardColors(
            containerColor = White1
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.name,
                    color = Green1,
                    modifier = Modifier.semantics { testTag = "android:id/ProductName" }
                )
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = White1
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = Green2
                    ),
                    modifier = Modifier.semantics { testTag = "android:id/ProductLocation" }
                ) {
                    Text(
                        text = if (product.isInStore) "In-Store" else "Online",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Green2
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Quantity: ${product.quantity} ${product.weightUnit.lowercase()}",
                color = Green1,
                modifier = Modifier.semantics { testTag = "android:id/ProductQuantity" }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Price: ₱${product.price}",
                color = Green1,
                modifier = Modifier.semantics { testTag = "android:id/ProductPrice" }
            )
        }
    }
}