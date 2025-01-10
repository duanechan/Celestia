package com.coco.celestia.screens.coop.facility

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.R
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
                            .background(White2)
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

    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDropdown by remember { mutableStateOf(false) }
    var filterByActive by remember { mutableStateOf<Boolean?>(null) }
    var selectedWeightUnit by remember { mutableStateOf<String?>(null) }
    var sortDirection by remember { mutableStateOf("ascending") }

    LaunchedEffect(Unit) {
        facilityViewModel.fetchFacilities()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White2)
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
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        modifier = Modifier.weight(1f),
                                        placeholder = { Text("Search products...") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Search"
                                            )
                                        },
                                        colors = TextFieldDefaults.colors(
                                            unfocusedContainerColor = White1,
                                            focusedContainerColor = White1
                                        ),
                                        singleLine = true
                                    )

                                    IconButton(
                                        onClick = { showFilterDialog = true },
                                        modifier = Modifier
                                            .background(White1, CircleShape)
                                            .size(48.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.filter2),
                                            contentDescription = "Filter",
                                            modifier = Modifier
                                                .size(24.dp)
                                                .padding(4.dp)
                                        )
                                    }

                                    Box {
                                        IconButton(
                                            onClick = { showSortDropdown = !showSortDropdown },
                                            modifier = Modifier
                                                .background(White1, CircleShape)
                                                .size(48.dp)
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.sort),
                                                contentDescription = "Sort",
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .padding(4.dp)
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = showSortDropdown,
                                            onDismissRequest = { showSortDropdown = false },
                                            modifier = Modifier.background(White1)
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("A to Z") },
                                                onClick = {
                                                    sortDirection = "ascending"
                                                    showSortDropdown = false
                                                },
                                                leadingIcon = {
                                                    if (sortDirection == "ascending") {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Selected"
                                                        )
                                                    }
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Z to A") },
                                                onClick = {
                                                    sortDirection = "descending"
                                                    showSortDropdown = false
                                                },
                                                leadingIcon = {
                                                    if (sortDirection == "descending") {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Selected"
                                                        )
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }

                                val filteredProducts = productData.filter { product ->
                                    product.isInStore == isInStore &&
                                            product.type == userFacility.name &&
                                            product.name.contains(searchQuery, ignoreCase = true) &&
                                            (filterByActive == null || product.isActive == filterByActive) &&
                                            (selectedWeightUnit == null || product.weightUnit.equals(selectedWeightUnit, ignoreCase = true))
                                }.let { filtered ->
                                    when (sortDirection) {
                                        "ascending" -> filtered.sortedBy { it.name }
                                        "descending" -> filtered.sortedByDescending { it.name }
                                        else -> filtered
                                    }
                                }

                                if (filteredProducts.isEmpty()) {
                                    EmptyProductsScreen(isInStore, userFacility.name)
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        itemsIndexed(filteredProducts) { index, product ->
                                            ProductCard(
                                                product = product,
                                                onClick = {
                                                    navController.navigate(
                                                        Screen.CoopInventoryDetails.createRoute(product.productId)
                                                    )
                                                }
                                            )
                                            if (index < filteredProducts.lastIndex) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            FilterDialog(
                                showDialog = showFilterDialog,
                                filterByActive = filterByActive,
                                selectedWeightUnit = selectedWeightUnit,
                                onDismiss = { showFilterDialog = false },
                                onFilterChange = { newActiveFilter, newWeightUnit ->
                                    filterByActive = newActiveFilter
                                    selectedWeightUnit = newWeightUnit
                                }
                            )
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
fun FilterDialog(
    showDialog: Boolean,
    filterByActive: Boolean?,
    selectedWeightUnit: String?,
    onDismiss: () -> Unit,
    onFilterChange: (Boolean?, String?) -> Unit
) {
    var tempActiveFilter by remember(showDialog) { mutableStateOf(filterByActive) }
    var tempWeightUnit by remember(showDialog) { mutableStateOf(selectedWeightUnit) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Filter Products") },
            text = {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Status filter
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectableGroup(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf(
                                Triple(null, "All", null),
                                Triple(true, "Active", true),
                                Triple(false, "Inactive", false)
                            ).forEach { (value, text, active) ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    RadioButton(
                                        selected = tempActiveFilter == value,
                                        onClick = { tempActiveFilter = active }
                                    )
                                    Text(text)
                                }
                            }
                        }
                    }

                    // Weight unit filter
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Weight Unit",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Column(
                            modifier = Modifier.selectableGroup(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                null to "All",
                                "kilograms" to "KILOGRAMS",
                                "grams" to "GRAMS",
                                "pounds" to "POUNDS"
                            ).forEach { (value, display) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    RadioButton(
                                        selected = tempWeightUnit == value,
                                        onClick = { tempWeightUnit = value }
                                    )
                                    Text(display)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onFilterChange(tempActiveFilter, tempWeightUnit)
                        onDismiss()
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Cancel")
                }
            }
        )
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
                    text = product.name.split(" ").joinToString(" ") {
                        it.lowercase().replaceFirstChar { char -> char.uppercase() }
                    },
                    color = Green1,
                    modifier = Modifier.semantics { testTag = "android:id/ProductName" }
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (product.isActive) Green4 else Color.Red.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.semantics { testTag = "android:id/ProductStatus" }
                    ) {
                        Text(
                            text = if (product.isActive) "Active" else "Inactive",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = if (product.isActive) Green1 else Color.Red
                        )
                    }

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