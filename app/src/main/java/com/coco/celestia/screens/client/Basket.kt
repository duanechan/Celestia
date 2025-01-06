package com.coco.celestia.screens.client

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.BgColor
import com.coco.celestia.ui.theme.Green1
import com.coco.celestia.ui.theme.Green4
import com.coco.celestia.ui.theme.White1
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.BasketItem
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth

@Composable
fun BasketScreen(userViewModel: UserViewModel) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val userData by userViewModel.userData.observeAsState(UserData())
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)

    LaunchedEffect(Unit) {
        userViewModel.fetchUser(uid)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .background(BgColor)
            .fillMaxSize()
            .padding(15.dp)
    ) {
        when (userState) {
            UserState.EMPTY -> BasketEmpty()
            UserState.EMAIL_SENT_SUCCESS,
            is UserState.LOGIN_SUCCESS,
            UserState.REGISTER_SUCCESS,
            is UserState.ERROR -> BasketError(message = (userState as UserState.ERROR).message ?: "Unknown error")
            UserState.LOADING -> BasketLoading()
            UserState.SUCCESS -> Basket(items = userData.basket)
        }
    }
}

@Composable
fun Basket(items: List<BasketItem>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        itemsIndexed(items) { _, item ->
            BasketItemCard(item)
        }
    }
}

@Composable
fun BasketItemCard(item: BasketItem) {
    var image by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        ImageService.fetchProductImage(productName = item.product) {
            image = it
        }
    }

    Card(
        elevation = CardDefaults.elevatedCardElevation(8.dp, 4.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Green4)
                .padding(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = rememberImagePainter(image),
                    contentDescription = item.product,
                    modifier = Modifier
                        .width(100.dp)
                        .height(125.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(White1)
                )
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, top = 4.dp)
                    ) {
                        Text(text = item.product, fontWeight = FontWeight.Bold, color = Green1)
                        Text(text = "Php ${item.price}", fontWeight = FontWeight.Bold, color = Green1)
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, top = 4.dp)
                    ) {
//                        Text(text = "Php ${item.price}", fontWeight = FontWeight.Bold, color = Green1)
                    }
                }
            }
        }
    }
}

@Composable
fun BasketLoading() {
    CircularProgressIndicator()
}

@Composable
fun BasketEmpty() {
    Text(text = "Basket is empty.")
}

@Composable
fun BasketError(message: String) {
    Text(text = "Error: $message")
}
