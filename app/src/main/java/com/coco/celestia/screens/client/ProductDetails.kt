package com.coco.celestia.screens.client

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.Green1
import com.coco.celestia.ui.theme.Green4
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
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { ProductDetails_Header(name = product.name, productImage) }
        item { ProductDetails_StockAndQuantity(name = product.name, price = product.price) }
        item { Divider(thickness = 1.dp, modifier = Modifier.shadow(elevation  = 4.dp)) }
        item { ProductDetails_Breakdown() }
        item { ProductDetails_Description() }
        item { ProductDetails_ShelfLife() }
        item { ProductDetails_Action() }
    }
}

@Composable
fun ProductDetails_Action() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 25.dp, vertical = 15.dp)
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Green4, contentColor = Green1),
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Add to Basket", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Green1)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Green4, contentColor = Green1),
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Checkout", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Green1)
        }
    }
}

@Composable
fun ProductDetails_ShelfLife() {
    Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 25.dp)) {
        Text(
            text = "Shelf Life:",
            fontSize = 13.sp,
            textAlign = TextAlign.Justify,
            color = Green1
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Origin:",
            fontSize = 13.sp,
            textAlign = TextAlign.Justify,
            color = Green1
        )
    }
}

@Composable
fun ProductDetails_Description() {
    Column(modifier = Modifier.padding(15.dp)) {
        Text(text = "Description", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Green1)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp)
        ) {
            Text(
                text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do" +
                        " eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim" +
                        " ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut" +
                        " aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit" +
                        " in voluptate velit esse cillum dolore eu fugiat nulla pariatur. " +
                        "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia" +
                        " deserunt mollit anim id est laborum.",
                fontSize = 13.sp,
                textAlign = TextAlign.Justify,
                color = Green1
            )
        }
    }
}

@Composable
fun ProductDetails_Breakdown() {
    Column(modifier = Modifier.padding(horizontal = 25.dp, vertical = 15.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(text = "Price per 100 grams", fontSize = 16.sp, color = Green1)
            Text(text = "Php 0", fontSize = 16.sp, color = Green1)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(text = "Total Quantity", fontSize = 16.sp, color = Green1)
            Text(text = "x 0", fontSize = 16.sp, color = Green1)
        }
        Divider(thickness = 1.dp, modifier = Modifier.shadow(elevation  = 4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(text = "Total Cost", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Green1)
            Text(text = "Php 0", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Green1)
        }
    }
}

@Composable
fun ProductDetails_Header(name: String, productImage: Uri?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Image(
            painter = rememberImagePainter(productImage),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ProductDetails_StockAndQuantity(name: String, price: Double) {
    Column (modifier = Modifier.padding(15.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp)
        ) {
            Text(text = name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Green1)
            Text(text = "Php ${price}", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Green1)
        }
        // Quantity selector
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Quantity", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Green1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = {}) {
                    Text(text = "-", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Green1)
                }
                Text(text = "0", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Green1)
                TextButton(onClick = {}) {
                    Text(text = "+", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Green1)
                }
            }
        }
        // Stock level & minimum order
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "Available Stocks", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Green1)
                Text(text = "0 Kg", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Green1)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(text = "Minimum order allowed", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Green1)
                Text(text = "0 Kg", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Green1)
            }
        }
    }
}
