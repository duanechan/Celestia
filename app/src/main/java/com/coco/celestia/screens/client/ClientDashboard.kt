package com.coco.celestia.screens.client

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.CarouselItem
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
    var showDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        orderViewModel.fetchAllOrders("", "Client")
        delay(1000)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White1)
    ) {
        ProductCatalog(
            productViewModel = productViewModel,
            facilityViewModel = facilityViewModel,
            role = "Client",
            navController = navController,
            showSearch = true,
            showCarousel = true,
            onFilterClick = { showDialog = true }
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

@Composable
fun ProductCatalog(
    productViewModel: ProductViewModel = viewModel(),
    facilityViewModel: FacilityViewModel = viewModel(),
    role: String,
    navController: NavController,
    searchQuery: String = "",
    showSearch: Boolean = false,
    showCarousel: Boolean = false,
    onFilterClick: () -> Unit = {}
) {
    val productState by productViewModel.productState.observeAsState()
    val products by productViewModel.productData.observeAsState()
    val facilityState by facilityViewModel.facilityState.observeAsState()
    val facilities by facilityViewModel.facilitiesData.observeAsState(emptyList())
    var currentSearchQuery by remember { mutableStateOf(searchQuery) }
    val scrollState = rememberLazyListState()
    var isSearchActive by remember { mutableStateOf(showSearch) }

    val currentDestination = navController.currentBackStackEntry?.destination?.route?.substringBefore("?")
    val isProductCatalogScreen = currentDestination == Screen.ProductCatalog.route

    // Filter for online-only products
    val onlineProducts = products?.filter { !it.isInStore }

    LaunchedEffect(currentSearchQuery) {
        productViewModel.fetchProducts(currentSearchQuery, role)
        facilityViewModel.fetchFacilities()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White2)
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize()
        ) {
            // Search Bar
            if (showSearch) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isProductCatalogScreen) {
                            BasicTextField(
                                value = currentSearchQuery,
                                onValueChange = { newValue ->
                                    currentSearchQuery = newValue
                                },
                                decorationBox = { innerTextField ->
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .background(
                                                Color.White,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search",
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(16.dp)
                                        ) {
                                            if (currentSearchQuery.isEmpty()) {
                                                Text(
                                                    text = "Search",
                                                    color = Color.Gray
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                }
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White, shape = RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp)
                                    .clickable {
                                        navController.navigate(
                                            Screen.ProductCatalog.createRoute(
                                                searchQuery = "",
                                                role = role,
                                                showSearch = true
                                            )
                                        )
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                                Text(
                                    text = "Search",
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(16.dp),
                                    color = Color.Gray
                                )
                            }
                        }

                        if (onFilterClick != {}) {
                            Icon(
                                painter = painterResource(R.drawable.filter2),
                                contentDescription = "Filter",
                                modifier = Modifier
                                    .size(48.dp)
                                    .padding(start = 10.dp, end = 10.dp)
                                    .clickable(onClick = onFilterClick)
                            )
                        }
                    }
                }
            }

            // Carousel - Updated to use onlineProducts
            if (showCarousel && onlineProducts != null) {
                item {
                    val featuredProducts = onlineProducts.take(3).map { product ->
                        CarouselItem(
                            carouselId = product.productId,
                            imageRes = R.drawable.product_image,
                            title = product.name,
                            subtitle = product.description,
                            price = "Php ${product.price}/${product.weightUnit}"
                        )
                    }

                    if (featuredProducts.isNotEmpty()) {
                        SlideshowCarousel(
                            items = featuredProducts,
                            navController = navController
                        )
                    }
                }
            }

            // Product Catalog Content
            when {
                productState is ProductState.LOADING || facilityState is FacilityState.LOADING -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                productState is ProductState.ERROR -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (productState as ProductState.ERROR).message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                facilityState is FacilityState.ERROR -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (facilityState as FacilityState.ERROR).message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                productState is ProductState.SUCCESS -> {
                    if (currentSearchQuery.isNotEmpty()) {
                        val searchResults = onlineProducts?.filter { product ->
                            product.name.lowercase().contains(currentSearchQuery.lowercase()) ||
                                    product.type.lowercase().contains(currentSearchQuery.lowercase())
                        }

                        if (searchResults.isNullOrEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No online products found for '$currentSearchQuery'",
                                        modifier = Modifier.padding(16.dp),
                                        fontFamily = mintsansFontFamily
                                    )
                                }
                            }
                        } else {
                            item {
                                ProductGrid(
                                    title = "Search Results",
                                    products = searchResults,
                                    navController = navController
                                )
                            }
                        }
                    } else {
                        facilities.forEach { facility ->
                            val facilityProducts = onlineProducts?.filter { product ->
                                product.type.lowercase().contains(facility.name.lowercase())
                            }

                            if (!facilityProducts.isNullOrEmpty()) {
                                item {
                                    ProductGrid(
                                        title = facility.name,
                                        products = facilityProducts,
                                        navController = navController
                                    )
                                    Spacer(modifier = Modifier.height(5.dp))
                                }
                            }
                        }

                        if (facilities.isEmpty() || onlineProducts?.isEmpty() == true) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (facilities.isEmpty()) "No facilities available"
                                        else "No online products available",
                                        modifier = Modifier.padding(16.dp),
                                        fontFamily = mintsansFontFamily
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductGrid(title: String, products: List<ProductData>, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White2)

    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                fontFamily = mintsansFontFamily,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Products",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                fontFamily = mintsansFontFamily,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column {
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
                                    val encodedName = java.net.URLEncoder.encode(product.productId, "UTF-8")
                                    navController.navigate(Screen.ProductDetails.createRoute(encodedName))
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
}

@Composable
fun ProductCard(
    product: ProductData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    var productImage by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        ImageService.fetchProductImage(product.productId) {
            productImage = it
        }
    }

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
            painter = if (productImage != null) rememberImagePainter(productImage) else painterResource(R.drawable.product_image),
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
                navController.navigate(Screen.ProductDetails.createRoute(items[index].carouselId))
            }
        }
    }

    LaunchedEffect(currentIndex) {
        delay(3000)
        coroutineScope.launch {
            currentIndex = (currentIndex + 1) % items.size
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun CarouselCard(item: CarouselItem, onClick: () -> Unit) {
    var productImage by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(item.carouselId) {
        isLoading = true
        ImageService.fetchProductImage(item.carouselId) { uri ->
            productImage = uri
            isLoading = false
        }
        onDispose { }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        if (isLoading || productImage == null) {
            Image(
                painter = painterResource(R.drawable.product_image),
                contentDescription = "Loading",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Image(
                painter = rememberImagePainter(productImage),
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
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