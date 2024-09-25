package com.coco.celestia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import androidx.compose.ui.Alignment

@Composable
fun FarmerInventoryDetail(navController: NavController, productName: String) {
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
    ) {
        when (farmerProductState) {
            ProductState.LOADING -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            ProductState.SUCCESS -> {
                // Find the product by name
                val product = farmerProductData.find { it.name == productName }
                if (product != null) {
                    // Display the inventory details for the found product
                    Column(
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Spacer(modifier = Modifier.height(70.dp))
                        Text(
                            text = "Product Name: ${product.name}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Quantity: ${product.quantity}",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Type: ${product.type}",
                            fontSize = 16.sp
                        )
                    }
                } else {
                    // Product is not found
                    Text(
                        text = "Product not found",
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            ProductState.EMPTY -> {
                // Handle empty state
                Text(
                    text = "No products available",
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is ProductState.ERROR -> {
                Text(
                    text = "Error loading products",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
