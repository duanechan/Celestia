package com.coco.celestia.screens.client

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.coco.celestia.service.ImageService
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.model.ProductData

@Composable
fun ProductDetailScreen(
    navController: NavController,
    productViewModel: ProductViewModel,
    productName: String
) {
    val productData by productViewModel.productData.observeAsState(emptyList())
    val productState by productViewModel.productState.observeAsState()
    var productImage by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        productViewModel.fetchProduct(productName)
        println("$productName $productState")
    }

    LaunchedEffect(productData) {
        if (productData.isNotEmpty()) {
            ImageService.fetchProductImage(productData[0].name) {
                productImage = it
            }
        }
    }

    when (productState) {
        ProductState.EMPTY -> Text(text = productName)
        is ProductState.ERROR -> Text(text = "Error fetching product: ${(productState as ProductState.ERROR).message}")
        ProductState.LOADING -> CircularProgressIndicator()
        ProductState.SUCCESS -> {
            if (productData.isNotEmpty()) {
                ProductDetails(productData[0], productImage)
            }
        }
        else -> Text(text = productName)
    }
}

@Composable
fun ProductDetails(product: ProductData, productImage: Uri?) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Image(
                painter = rememberImagePainter(productImage),
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column (modifier = Modifier.padding(15.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = product.name, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(text = "Php ${product.price}", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(top = 25.dp)
            ) {
                Text(text = "Quantity", fontSize = 25.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = {}) {
                        Text(text = "-", fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(text = "0.0", fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = {}) {
                        Text(text = "+", fontSize = 25.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column {
                Text(text = "Available Stocks", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(text = "Minimum order allowed", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        Divider(thickness = 1.dp)
    }
}
