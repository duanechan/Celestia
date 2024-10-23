package com.coco.celestia.screens.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FarmerItemViewModel
import com.coco.celestia.viewmodel.ItemState
import com.google.firebase.auth.FirebaseAuth

@Composable
fun FarmerItems(navController: NavController) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    val itemViewModel: FarmerItemViewModel = viewModel()
    val itemData by itemViewModel.itemData.observeAsState(emptyList())
    val itemState by itemViewModel.itemState.observeAsState(ItemState.LOADING)

    LaunchedEffect(navController.currentBackStackEntry) {
        itemViewModel.getItems(uid = uid)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BgColor)
            .padding(top = 100.dp, bottom = 30.dp)
    ) {
        when (itemState) {
            is ItemState.LOADING -> LoadingFarmerProducts()
            is ItemState.ERROR -> ErrorFarmerProducts(message = (itemState as ItemState.ERROR).message)
            is ItemState.EMPTY -> EmptyFarmerProducts()
            is ItemState.SUCCESS -> FarmerItems(items = itemData, navController = navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerItems(items: List<ProductData>, navController: NavController) {
    var query by remember { mutableStateOf("") }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 100.dp),
            modifier = Modifier
                .fillMaxSize()
                .semantics { testTag = "android:id/farmerProductsList" }
        ) {
            item {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                }
            }
            // Use itemsIndexed to get the index and the product data
            itemsIndexed(items.sortedByDescending { it.quantity }) { index, product ->
                FarmerProductTypeInventory(
                    product = product,
                    navController = navController,
                    isFirst = (index == 0)
                )
            }
        }
    }
}

@Composable
fun ErrorFarmerProducts(message: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Failed to load products: $message}",
                modifier = Modifier
                    .padding(16.dp)
                    .semantics { testTag = "android:id/errorProductsText" }
            )
        }
    }
}

@Composable
fun EmptyFarmerProducts() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Outlined.Home, tint = SageGreen, contentDescription = "Add product", modifier = Modifier.size(100.dp))
            Text("No products available.",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(16.dp)
                    .semantics { testTag = "android:id/emptyProductsText" })
        }
    }
}

@Composable
fun LoadingFarmerProducts() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Loading products...",
                modifier = Modifier
                    .padding(16.dp)
                    .semantics { testTag = "android:id/loadingProductsText" })
        }
    }
}

@Composable
fun FarmerProductTypeInventory(
    product: ProductData,
    navController: NavController,
    isFirst: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .padding(
                horizontal = 20.dp,
                vertical = 10.dp
            )
            .then(if (isFirst) Modifier.padding(top = 16.dp) else Modifier)
            .clickable {
                navController.navigate(Screen.FarmerItemDetails.createRoute(product.name))
            }
            .semantics { testTag = "android:id/productCard_${product.name}" },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(PaleGold, GoldenYellow)
                    )
                )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = product.name.replaceFirstChar { it.uppercase() },
                        fontSize = 25.sp,
                        color = Cocoa,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { testTag = "android:id/productName_${product.name}" }
                    )
                    Text(
                        text = "${product.quantity}kg",
                        fontSize = 25.sp,
                        color = Cocoa,
                        modifier = Modifier.semantics { testTag = "android:id/productQuantity_${product.name}" }
                    )
                }

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowRight,
                    contentDescription = "Navigate",
                    tint = Cocoa,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Bottom)
                        .semantics { testTag = "android:id/navigateIcon_${product.name}" }
                )
            }
        }
    }
}