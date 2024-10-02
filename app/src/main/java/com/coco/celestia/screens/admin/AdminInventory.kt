package com.coco.celestia.screens.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.R
import com.coco.celestia.ui.theme.DarkBlue
import com.coco.celestia.ui.theme.Gray
import com.coco.celestia.ui.theme.PurpleGrey40
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInventory(productViewModel: ProductViewModel) {
    val productData by productViewModel.productData.observeAsState(emptyList())
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)
    var query by remember { mutableStateOf("") }
    var selectedButton by remember { mutableStateOf<String?>(null) } // Row for aligned buttons

    LaunchedEffect(query) {
        productViewModel.fetchProducts(
            filter = query,
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PurpleGrey40)
                .padding(5.dp, 0.dp, 5.dp, 10.dp)
         ) {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                onSearch = { query = it },
                active = false,
                onActiveChange = {},
                placeholder = { Text(text = "Search", color = DarkBlue)},
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")},
                modifier = Modifier
            ){
                //TO DO
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            // Coffee Button
            Button(
                onClick = {
                    query = "Coffee"
                    selectedButton = if (selectedButton == "Coffee") null else "Coffee"
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedButton == "Coffee") Color(0xFF795548) else Color.White
                ),
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                Text(
                    text = "Coffee",
                    fontSize = 20.sp,
                    color = if (selectedButton == "Coffee") Color.White else Gray
                )
            }

            // Meat Button
            Button(
                onClick = {
                    query = "Meat"
                    selectedButton = if (selectedButton == "Meat") null else "Meat"
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedButton == "Meat") Color.Red else Color.White
                ),
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                Text(
                    text = "Meat",
                    fontSize = 20.sp,
                    color = if (selectedButton == "Meat") Color.White else Gray
                )
            }

            // Vegetable Button
//            Button(
//                onClick = {
//                    query = "Vegetable"
//                    selectedButton = if (selectedButton == "Vegetable") null else "Vegetable"
//                },
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = if (selectedButton == "Vegetable") Color(0xFF4CAF50) else Color.White
//                ),
//                modifier = Modifier.padding(horizontal = 10.dp)
//            ) {
//                Text(
//                    text = "Vegetable",
//                    fontSize = 20.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = if (selectedButton == "Vegetable") Color.White else Gray
//                )
//            }
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