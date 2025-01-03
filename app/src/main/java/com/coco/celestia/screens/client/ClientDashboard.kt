package com.coco.celestia.screens.client

import android.util.Log
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
import com.coco.celestia.viewmodel.model.Notification
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White1)
    ) {
        Column {
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
                        .padding(horizontal = 8.dp), // Inner padding for the search bar
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

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp)
            ) {
                val sampleItems = listOf(
                    CarouselItem(R.drawable.greenbeansimg, "Green Beans", "In Season", "Php 40/Kg"),
                    CarouselItem(R.drawable.arabicaimg, "Tinapong: Arabica", "Freshly Harvested", "Php 120/Kg"),
                    CarouselItem(R.drawable.sortedimg, "Coffee Beans", "Organic", "Php 200/Kg")
                )
                SlideshowCarousel(items = sampleItems, navController = navController)
            }
        }
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
                // Navigate to ProductDetailScreen without passing any data
                navController.navigate(Screen.ProductDetails.route)
            }
        }
    }

    // Navigation Indicators
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        items.forEachIndexed { index, _ ->
            Box(
                modifier = Modifier
                    .width(35.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (index == currentIndex) Green1 else Color.Gray)
            )

            if (index < items.size - 1) {
                Spacer(modifier = Modifier.width(5.dp))
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

        Text(
            text = "Featured Products",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = item.subtitle,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = Color.White,
            modifier = Modifier.padding(top = 32.dp, start = 16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = item.title,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.price,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}

// Data class for Carousel item
data class CarouselItem(
    val imageRes: Int,
    val title: String,
    val subtitle: String,
    val price: String
)