package com.coco.celestia.screens.client

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.screens.coop.admin.EmptyOrders
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.Green1
import com.coco.celestia.ui.theme.Green4
import com.coco.celestia.ui.theme.White1
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.BasketItem

@Composable
fun OrderSummary(
    navController: NavController,
    userViewModel: UserViewModel,
    productViewModel: ProductViewModel,
    items: List<BasketItem>
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        if (items.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item { UserDetailsHeader() }
                items(items) { ItemSummaryCard(it) }
            }
        } else {
            EmptyOrders()
        }
    }
}

@Composable
fun UserDetailsHeader() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Green4),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Image(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(50.dp),
                colorFilter = ColorFilter.tint(Green1)
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(text = "Diwata Pares", fontWeight = FontWeight.Bold, color = Green1)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "#12 Paliparan 3, Cavite",fontSize = 13.sp, color = Green1)
            }
        }
    }
}

@Composable
fun ItemSummaryCard(item: BasketItem) {
    var image by remember { mutableStateOf<Uri?>(null) }

//    LaunchedEffect(Unit) {
//        ImageService.fetchProductImage(productName = item.product) {
//            image = it
//        }
//    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Green4),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        modifier = Modifier
            .height(300.dp)
            .padding(vertical = 3.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
            ) {
                Box(modifier = Modifier.padding(12.dp)) {
                    Image(
                        painter = rememberImagePainter(image),
                        contentDescription = item.product,
                        modifier = Modifier
                            .width(100.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(White1)
                    )
                }
                Column(verticalArrangement = Arrangement.SpaceBetween) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, end = 12.dp)
                    ) {
                        Text(text = item.product, fontWeight = FontWeight.Bold, color = Green1)
                        Text(text = "Php ${item.price}", fontWeight = FontWeight.Bold, color = Green1)
                    }
                }
            }
            Row(modifier = Modifier.padding()) {
                Text(text = item.product, fontWeight = FontWeight.Bold, color = Green1)
            }
        }
    }
}