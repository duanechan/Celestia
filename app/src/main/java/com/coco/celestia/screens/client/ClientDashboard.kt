package com.coco.celestia.screens.client

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ClientDashboard(
    navController: NavController,
    userViewModel: UserViewModel,
    productViewModel: ProductViewModel,
    orderViewModel: OrderViewModel
) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    val userData by userViewModel.userData.observeAsState(UserData())
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        orderViewModel.fetchAllOrders("", "Client")
        productViewModel.fetchFeaturedProducts()
        delay(1000)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White1)
            .verticalScroll(rememberScrollState())
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                    TextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Search") },
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }

                Icon(
                    painter = painterResource(R.drawable.filter2),
                    contentDescription = "Filter",
                    modifier = Modifier
                        .size(48.dp)
                        .padding(start = 10.dp, end = 10.dp)
                        .clickable {
                            showDialog = true
                        }
                )
            }

            // Carousel
            val sampleItems = listOf(
                CarouselItem(R.drawable.greenbeansimg, "Green Beans", "In Season", "Php 40/Kg"),
                CarouselItem(R.drawable.arabicaimg, "Tinapong: Arabica", "Freshly Harvested", "Php 120/Kg"),
                CarouselItem(R.drawable.sortedimg, "Coffee Beans", "Organic", "Php 200/Kg")
            )
            SlideshowCarousel(items = sampleItems, navController = navController)

            ProductCatalog()
    }
}

@Composable
fun ProductCatalog() {
    val vegetableProducts = listOf(
        ProductItem(R.drawable.product_image, "Potato", "Php 50/Kg"),
        ProductItem(R.drawable.product_image, "Carrot", "Php 60/Kg"),
        ProductItem(R.drawable.product_image, "Cucumber", "Php 40/Kg"),
        ProductItem(R.drawable.product_image, "Red Onion", "Php 90/Kg"),
        ProductItem(R.drawable.product_image, "Bell Pepper", "Php 80/Kg"),
        ProductItem(R.drawable.product_image, "Eggplant", "Php 55/Kg")
    )

    val coffeeProducts = listOf(
        ProductItem(R.drawable.product_image, "Arabica Beans", "Php 300/Kg"),
        ProductItem(R.drawable.product_image, "Roasted Beans", "Php 250/Kg"),
        ProductItem(R.drawable.product_image, "Green Beans", "Php 400/Kg")
    )

    val meatProducts = listOf(
        ProductItem(R.drawable.product_image, "Kiniing", "Php 200/Kg"),
        ProductItem(R.drawable.product_image, "Pork Belly", "Php 350/Kg"),
        ProductItem(R.drawable.product_image, "Raw Meat", "Php 100/Kg")
    )

    Column(modifier = Modifier.padding(8.dp)) {
        ProductGrid("Vegetables", vegetableProducts)
        ProductGrid("Coffee", coffeeProducts)
        ProductGrid("Meat", meatProducts)
    }
}

@Composable
fun ProductGrid(title: String, products: List<ProductItem>) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        fontFamily = mintsansFontFamily,
        color = Color.Black,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp)
    )
    Text(
        text = "Products",
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        fontFamily = mintsansFontFamily,
        color = Color.Black,
        modifier = Modifier.padding(start = 20.dp, top = 8.dp)
    )

    Column(modifier = Modifier.padding(8.dp)) {
        products.chunked(3).forEach { rowProducts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowProducts.forEach { product ->
                    ProductCard(
                        product = product,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                    )
                }

                repeat(3 - rowProducts.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: ProductItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(White1)
            .fillMaxWidth()
            .clickable { /* should navigate to product details */ },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(product.imageRes),
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = product.name,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.Black,
            fontFamily = mintsansFontFamily,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = product.price,
            fontSize = 12.sp,
            fontFamily = mintsansFontFamily,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SlideshowCarousel(items: List<CarouselItem>, navController: NavController) {
    var currentIndex by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Crossfade(
            targetState = currentIndex,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        ) { index ->
            CarouselCard(item = items[index]) {
                navController.navigate(Screen.ProductDetails.createRoute(items[index].title))
            }
        }
    }

    // Auto-scroll functionality
    LaunchedEffect(currentIndex) {
        delay(3000)
        coroutineScope.launch {
            currentIndex = (currentIndex + 1) % items.size
        }
    }
}

@Composable
fun CarouselCard(item: CarouselItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Image(
            painter = painterResource(item.imageRes),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        ) {
            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
            Text(
                text = item.price,
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}

// Data classes for the carousel and products
data class CarouselItem(
    val imageRes: Int,
    val title: String,
    val subtitle: String,
    val price: String
)
data class ProductItem(
    val imageRes: Int,
    val name: String,
    val price: String
)
