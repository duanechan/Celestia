package com.coco.celestia.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.ui.theme.DarkBlue
import com.coco.celestia.ui.theme.Gray
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.ProductViewModel

@Composable
fun AdminInventory(productViewModel: ProductViewModel, navController: NavController) {
    val productData by productViewModel.productData.observeAsState(emptyList())
    val productState by productViewModel.productState.observeAsState(ProductState.LOADING)
    var query by remember { mutableStateOf("Coffee") }
    var selectedButton by remember { mutableStateOf<String?>("Coffee") } // Row for aligned buttons

    LaunchedEffect(query) {
        productViewModel.fetchProducts(
            filter = query,
            role = "Admin"
        )
    }
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBlue)
                .padding(top = 7.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TopBarInventory("Inventory", navController)
            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .offset(y = ((-50).dp)),
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
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (productState) {
                is ProductState.LOADING -> {
                    Text("Loading products...", color = Color.White)
                }

                is ProductState.ERROR -> {
                    Text(
                        "Failed to load products: ${(productState as ProductState.ERROR).message}",
                        color = Color.Red
                    )
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
}
@Composable
fun AdminItemList(itemList: List<ProductData>) {
    if (itemList.isNotEmpty()) {
        itemList.forEach { (name, quantity) ->
            AdminItemCard(name, quantity)
            Spacer(modifier = Modifier.height(10.dp))
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
fun AdminItemCard(productName: String, quantity: Int) {
    Card(modifier = Modifier
        .width(500.dp)
        .height(200.dp)
        .offset(x = (-16).dp, y = (-50).dp)
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
                text = productName,
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold,
                color = Gray,
                modifier = Modifier.padding(top = 15.dp, start = 10.dp)
            )
            Text(
                text = "${quantity}kg",
                fontSize = 25.sp,
                fontWeight = FontWeight.Medium,
                color = Gray,
                modifier = Modifier.padding(top = 15.dp, start = 10.dp)
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarInventory(title: String, navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBlue)
    ) {
        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Calendar Icon",
                        modifier = Modifier
                            .padding(10.dp)
                            .offset(x = (-10).dp)
                            .clickable {
                            navController.navigate(Screen.Calendar.route)
                        }
                    )

                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White
            ),
            modifier = Modifier
                .background(Color.Transparent)
        )
    }
}