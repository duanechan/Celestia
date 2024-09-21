package com.coco.celestia

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.ui.theme.DarkBlue
import com.coco.celestia.ui.theme.Gray
import com.coco.celestia.ui.theme.LightGreen
import com.coco.celestia.ui.theme.PurpleGrey40
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInventory(productViewModel: ProductViewModel) {
    val productData by productViewModel.productData.observeAsState(emptyList())
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)

    LaunchedEffect(Unit) {
        productViewModel.fetchProducts(
            filter = "",
            role = "Admin"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBlue)
            .padding(top = 75.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(top = 27.dp, bottom = 8.dp, start = 25.dp, end = 16.dp)
        ) {
            Text(text = "Inventory", fontSize = 31.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { /* TODO: Handle notification click */ }) {
                Image(
                    painter = painterResource(id = R.drawable.notification_icon),
                    contentDescription = "Notification Icon",
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        var text by remember { mutableStateOf("") }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(PurpleGrey40)
                .padding(top = 10.dp, bottom = 15.dp, start = 25.dp, end = 16.dp)
        ) {
            SearchBar(
                query = text,
                onQueryChange = {},
                onSearch = {},
                active = false,
                onActiveChange = {},
                placeholder = { Text(text = "Search...", color = DarkBlue, fontSize = 15.sp)},
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")},
                modifier = Modifier
                    .width(225.dp)
                    .height(35.dp)){
                //TO DO
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Row for aligned buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { /* TODO: Handle coffee button click */ }) {
                Text("Coffee")
            }
            Button(onClick = { /* TODO: Handle meat button click */ }) {
                Text("Meat")
            }
            Button(onClick = { /* TODO: Handle vegetable button click */ }) {
                Text("Vegetable")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (productState) {
            is ProductState.LOADING -> {
                Text("Loading products...", color = Color.White)
            }
            is ProductState.ERROR -> {
                Text("Failed to load products: ${(productState as ProductState.ERROR).message}", color = Color.Red)
            }
            is ProductState.EMPTY -> {
                Text("No products available.", color = Color.White)
            }
            is ProductState.SUCCESS -> {
                AdminItemList(productData)
            }
        }
    }
}
@Composable
fun AdminItemList(itemList: List<ProductData>) {
    if (itemList.isNotEmpty()) {
        itemList.forEach { (type, quantity) ->
            val productType = type.replace("_", " ")
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            AdminItemCard(productType, quantity)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun AdminItemCard(productType: String, quantity: Int) {
    Card(modifier = Modifier
        .width(500.dp)
        .height(200.dp)
        .offset(x = (-16).dp, y = 0.dp)
        .padding(top = 0.dp, bottom = 5.dp, start = 30.dp, end = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )) {
        var expanded by remember { mutableStateOf(false) }
        Column(
            Modifier
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Text(
                text = productType,
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold,
                color = Gray,
                modifier = Modifier.padding(top = 15.dp, start = 10.dp)
            )
            Text(
                text = "${quantity}kg",
                fontSize = 25.sp,
                fontWeight = FontWeight.Light,
                color = Gray,
                modifier = Modifier.padding(top = 15.dp, start = 10.dp)
            )
        }
    }
}