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
import androidx.compose.runtime.mutableIntStateOf
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
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.Green1
import com.coco.celestia.ui.theme.Green4
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.BasketItem
import com.coco.celestia.viewmodel.model.ProductData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.selects.select

@Composable
fun ProductDetailScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    productViewModel: ProductViewModel,
    productName: String,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit
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
                ProductDetails(
                    navController = navController,
                    userViewModel = userViewModel,
                    product = productData[0],
                    productImage = productImage,
                    onAddItem = { onEvent(it) }
                )
            }
        }
        else -> Text(text = productName)
    }
}

@Composable
fun ProductDetails(
    navController: NavController,
    userViewModel: UserViewModel,
    product: ProductData,
    productImage: Uri?,
    onAddItem: (Triple<ToastStatus, String, Long>) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    var selectedQuantity by remember { mutableIntStateOf(0) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item { ProductDetails_Header(name = product.name, productImage) }
        item {
            ProductDetails_StockAndQuantity(
                name = product.name,
                price = product.price,
                selectedQuantity = selectedQuantity,
                maxQuantity = product.quantity,
                onUpdate = { selectedQuantity = it }
            )
        }
        item { Divider(thickness = 1.dp, modifier = Modifier.shadow(elevation  = 4.dp)) }
        item { ProductDetails_Breakdown(selectedQuantity = selectedQuantity, price = product.price) }
        item { ProductDetails_Description(description = product.description) }
        item { ProductDetails_ShelfLife() }
        item {
            ProductDetails_Action(
                onCheckout = {},
                onAddItem = {
                    if (selectedQuantity > 0) {
                        userViewModel.addItem(
                            uid = uid,
                            item = BasketItem(
                                product = product.name,
                                price = product.price * selectedQuantity,
                                quantity = selectedQuantity,
                                isRetail = true
                            )
                        )
                        onAddItem(
                            Triple(
                                ToastStatus.SUCCESSFUL,
                                "$selectedQuantity kg of ${product.name} added to the basket!",
                                System.currentTimeMillis()
                            )
                        )
                        navController.navigate(Screen.Basket.route)
                    } else {
                        onAddItem(
                            Triple(
                                ToastStatus.WARNING,
                                "Insufficient quantity.",
                                System.currentTimeMillis()
                            )
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun ProductDetails_Action(
    onCheckout: () -> Unit,
    onAddItem: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 25.dp, vertical = 15.dp)
    ) {
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Green4, contentColor = Green1),
            onClick = { onAddItem() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Add to Basket", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Green1)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Green4, contentColor = Green1),
            onClick = { onCheckout() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Checkout", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Green1)
        }
    }
}

@Composable
fun ProductDetails_ShelfLife() {
    // TODO: Get shelf life and origin (how?)
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
fun ProductDetails_Description(description: String) {
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
                text = if (description == "") "No description." else description,
                fontSize = 13.sp,
                textAlign = TextAlign.Justify,
                color = Green1
            )
        }
    }
}

@Composable
fun ProductDetails_Breakdown(selectedQuantity: Int, price: Double) {
    Column(modifier = Modifier.padding(horizontal = 25.dp, vertical = 15.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp)
        ) {
            Text(text = "Price per kilogram", fontSize = 16.sp, color = Green1)
            Text(text = "Php $price", fontSize = 16.sp, color = Green1)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp)
        ) {
            Text(text = "Total Quantity", fontSize = 16.sp, color = Green1)
            Text(text = "x $selectedQuantity", fontSize = 16.sp, color = Green1)
        }
        Divider(thickness = 1.dp, modifier = Modifier
            .padding(vertical = 15.dp)
            .shadow(elevation = 4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp)
        ) {
            Text(text = "Total Cost", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Green1)
            Text(text = "Php ${price * selectedQuantity}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Green1)
        }
    }
}

@Composable
fun ProductDetails_Header(name: String, image: Uri?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Image(
            painter = rememberImagePainter(image),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun ProductDetails_StockAndQuantity(
    name: String,
    price: Double,
    selectedQuantity: Int,
    maxQuantity: Int,
    onUpdate: (Int) -> Unit
) {
    var quantity by remember { mutableIntStateOf(0) }

    Column (modifier = Modifier.padding(15.dp)) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp)
        ) {
            Text(text = name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Green1)
            Text(text = "Php $price", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Green1)
        }
        // Quantity selector
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Quantity", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Green1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = {
                        if (quantity > 0) {
                            onUpdate(--quantity)
                        }
                    },
                    enabled = quantity > 0
                ) {
                    Text(text = "-", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Green1)
                }
                Text(text = selectedQuantity.toString(), fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Green1)
                TextButton(
                    onClick = {
                        if (quantity < maxQuantity) {
                            onUpdate(++quantity)
                        }
                    },
                    enabled = quantity < maxQuantity
                ) {
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
                Text(text = "$maxQuantity Kg", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Green1)
            }
            // TODO: Get minimum order (how?)
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
