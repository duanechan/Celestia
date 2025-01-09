package com.coco.celestia.screens.client

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ClientDashboard(
    navController: NavController,
    userViewModel: UserViewModel,
    productViewModel: ProductViewModel,
    orderViewModel: OrderViewModel,
    facilityViewModel: FacilityViewModel = viewModel()
) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    val userData by userViewModel.userData.observeAsState(UserData())
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        orderViewModel.fetchAllOrders("", "Client")
        facilityViewModel.fetchFacilities()
        productViewModel.fetchProducts(searchQuery, "Client")
        delay(1000)
    }

    LaunchedEffect(searchQuery) {
        productViewModel.fetchProducts(searchQuery, "Client")
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
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
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

        // Product Catalog for Clients
        ProductCatalog(
            productViewModel = productViewModel,
            facilityViewModel = facilityViewModel,
            role = "Client",
            navController = navController
        )
    }

    // Filter Dialog
    if (showDialog) {
        FilterDialog(
            onDismiss = { showDialog = false },
            onApplyFilter = { filter ->
                productViewModel.fetchProducts(filter, "Client")
                showDialog = false
            }
        )
    }
}

@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onApplyFilter: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Filter Products",
                style = MaterialTheme.typography.titleMedium,
                fontFamily = mintsansFontFamily
            )
        },
        text = {
            Column {
                listOf("Price: Low to High", "Price: High to Low", "Newest First").forEach { filter ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedFilter = filter }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter }
                        )
                        Text(
                            text = filter,
                            modifier = Modifier.padding(start = 8.dp),
                            fontFamily = mintsansFontFamily
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onApplyFilter(selectedFilter) }) {
                Text("Apply", fontFamily = mintsansFontFamily)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontFamily = mintsansFontFamily)
            }
        }
    )
}

// TODO: Images for each product need to add

@Composable
fun ProductCatalog(
    productViewModel: ProductViewModel = viewModel(),
    facilityViewModel: FacilityViewModel = viewModel(),
    role: String,
    navController: NavController
) {
    val productState by productViewModel.productState.observeAsState()
    val products by productViewModel.productData.observeAsState()
    val facilityState by facilityViewModel.facilityState.observeAsState()
    val facilities by facilityViewModel.facilitiesData.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        productViewModel.fetchProducts("", role)
        facilityViewModel.fetchFacilities()
    }

    when {
        productState is ProductState.LOADING || facilityState is FacilityState.LOADING -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        productState is ProductState.ERROR -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (productState as ProductState.ERROR).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        facilityState is FacilityState.ERROR -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (facilityState as FacilityState.ERROR).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        productState is ProductState.SUCCESS -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                facilities.forEach { facility ->
                    val facilityProducts = products?.filter { product ->
                        product.type.lowercase().contains(facility.name.lowercase())
                    }

                    if (facilityProducts != null && facilityProducts.isNotEmpty()) {
                        ProductGrid(
                            title = facility.name,
                            products = facilityProducts,
                            navController = navController
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                if (facilities.isEmpty() || products?.isEmpty() == true) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (facilities.isEmpty()) "No facilities available"
                            else "No products available",
                            modifier = Modifier.padding(16.dp),
                            fontFamily = mintsansFontFamily
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductGrid(title: String, products: List<ProductData>, navController: NavController) {
    Column {
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
                                .padding(horizontal = 4.dp),
                            onClick = {
                                navController.navigate(Screen.ProductDetails.createRoute(product.name))
                            }
                        )
                    }

                    repeat(3 - rowProducts.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ProductCard(
    product: ProductData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(White1)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.product_image),
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
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "₱${product.price}/${product.weightUnit}",
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
