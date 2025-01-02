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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
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
    orderViewModel: OrderViewModel,
    transactionViewModel: TransactionViewModel
) {
    val uid = FirebaseAuth.getInstance().uid.toString()
    val userData by userViewModel.userData.observeAsState(UserData())
    val context = LocalContext.current
    var notifications = remember { mutableListOf<Notification>() }
    var showDialog by remember { mutableStateOf(false) }
    var showRequestPopup by remember { mutableStateOf(false) }

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
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(top = 0.dp)
        ) {
            val sampleItems = listOf(
                CarouselItem(R.drawable.greenbeansimg, "Green Beans", "In Season", "Php 40/Kg"),
                CarouselItem(R.drawable.arabicaimg, "Tinapong: Arabica", "Freshly Harvested", "Php 120/Kg"),
                CarouselItem(R.drawable.sortedimg, "Coffee Beans", "Organic", "Php 200/Kg")
            )
            SlideshowCarousel(items = sampleItems)
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
@Composable
fun SlideshowCarousel(items: List<CarouselItem>) {
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
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing) // Customize duration and easing
        ) { index ->
            CarouselCard(item = items[index])
        }
    }

    // Navigation Indicators
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        items.forEachIndexed { index, _ ->
            Box(
                modifier = Modifier
                    .width(35.dp) // Set a fixed width for the indicators
                    .height(5.dp) // Set a fixed height for the indicators
                    .clip(RoundedCornerShape(4.dp)) // Rounded corners
                    .background(if (index == currentIndex) Green1 else Color.Gray) // Change to Color.Green1 if defined
            )

            // Add space between indicators
            if (index < items.size - 1) {
                Spacer(modifier = Modifier.width(5.dp)) // Space between indicators
            }
        }
    }

    // Auto-scroll functionality
    LaunchedEffect(currentIndex) {
        delay(3000) // Change slide every 3 seconds
        coroutineScope.launch {
            currentIndex = (currentIndex + 1) % items.size
        }
    }
}

@Composable
fun CarouselCard(item: CarouselItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(12.dp)) // Clip the image to the card shape
    ) {
        // Background Image
        Image(
            painter = painterResource(item.imageRes),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Text(
            text = "Featured Products",
            fontFamily = mintsansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )

        Text(
            text = "In Season",
            fontFamily = mintsansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = Color.White,
            modifier = Modifier.padding(top = 32.dp, start = 16.dp)
        )

        // Overlay Text
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
                fontFamily = mintsansFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.price,
                fontFamily = mintsansFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}