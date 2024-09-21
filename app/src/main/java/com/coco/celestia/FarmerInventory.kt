package com.coco.celestia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Alignment


@Composable
fun FarmerInventory(navController: NavController) {
    val farmerProductViewModel: ProductViewModel = viewModel()
    val farmerProductData by farmerProductViewModel.productData.observeAsState(emptyList())
    val farmerProductState by farmerProductViewModel.productState.observeAsState(ProductState.LOADING)

    LaunchedEffect(Unit) {
        if (farmerProductData.isEmpty()) {
            farmerProductViewModel.fetchProducts(
                filter = "",
                role = "Farmer"
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2E3DB))
            .padding(top = 40.dp)
    ) {
        when (farmerProductState) {
            is ProductState.LOADING -> {
                Text("Loading products...", modifier = Modifier.padding(16.dp))
            }
            is ProductState.ERROR -> {
                Text(
                    "Failed to load products: ${(farmerProductState as ProductState.ERROR).message}",
                    modifier = Modifier.padding(16.dp)
                )
            }
            is ProductState.EMPTY -> {
                Text("No products available.", modifier = Modifier.padding(16.dp))
            }
            is ProductState.SUCCESS -> {
                LazyColumn(
                    contentPadding = PaddingValues(top = 35.dp, bottom = 100.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = "Vegetables",
                                fontSize = 25.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    items(farmerProductData) { product ->
                        FarmerProductTypeInventory(product = product)
                    }
                }
            }
        }
    }
}

@Composable
fun FarmerProductTypeInventory(product: ProductData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF41644A), // Start of gradient
                            Color(0xFF83CA95)  // End of gradient
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = product.name, fontSize = 18.sp, color = Color.White)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "${product.quantity}kg", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}
