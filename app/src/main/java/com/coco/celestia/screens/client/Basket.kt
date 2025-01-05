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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.coco.celestia.R
import com.coco.celestia.service.ImageService
import com.coco.celestia.ui.theme.BgColor
import com.coco.celestia.ui.theme.CelestiaTheme
import com.coco.celestia.ui.theme.Gray
import com.coco.celestia.ui.theme.Green1
import com.coco.celestia.ui.theme.Green4
import com.coco.celestia.ui.theme.White1
import com.coco.celestia.ui.theme.White2
import com.coco.celestia.util.UserIdentifier
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.BasketItem
import com.coco.celestia.viewmodel.model.UserData
import com.google.firebase.auth.FirebaseAuth

@Preview
@Composable
private fun Basket_Prev() {
    CelestiaTheme {
        Surface {
            BasketScreen(userViewModel = UserViewModel())
        }
    }
}

@Composable
fun BasketScreen(userViewModel: UserViewModel) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val userData by userViewModel.userData.observeAsState(UserData())
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)

    LaunchedEffect(Unit) {
        userViewModel.fetchUser(uid)
    }

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

@Composable
fun Basket(items: List<BasketItem>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .background(BgColor)
            .fillMaxSize()
            .padding(15.dp)
    ) {
        Text(text = "Basket", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Green1)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            itemsIndexed(items) { _, item ->
                BasketItemCard(item)
            }
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
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(Green4)
                .fillMaxSize()
                .padding(vertical = 12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = false, onCheckedChange = {})
                Image(
                    painter = rememberImagePainter(image) ?: painterResource(R.drawable.product_image),
                    contentDescription = item.product,
                    modifier = Modifier
                        .size(75.dp)
                        .clip(RoundedCornerShape(25.dp))
                        .background(White1)
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(text = item.product, fontWeight = FontWeight.Bold, color = Green1)
//                    Text(text = item.quantity.toString(), fontWeight = FontWeight.Bold, color = Green1)
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
