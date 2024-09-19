package com.coco.celestia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.ui.theme.CelestiaTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


// Initial Code
data class Product(
    val id: Int,
    val name: String,
    val quantity: Int
)


class FarmerInventoryViewModel : ViewModel() {
    // Sample inventory data
    val inventoryList = listOf(
        Product(id = 1, name = "Tomatoes", quantity = 50),
        Product(id = 2, name = "Carrots", quantity = 100),
        Product(id = 3, name = "Potatoes", quantity = 75)
    )
}

// activity that shows the inventory list
class FarmerInventoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CelestiaTheme {
                FarmerInventoryScreen()
            }
        }
    }
}

// Composable function to display the inventory screen
@Composable
fun FarmerInventoryScreen(viewModel: FarmerInventoryViewModel = viewModel()) {
    val inventory = viewModel.inventoryList

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = "Farmer Inventory", modifier = Modifier.padding(16.dp))

        // Inventory list displayed using LazyColumn
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(inventory) { product ->
                ProductCard(product)
            }
        }
    }
}

// Composable function to display individual product details in a card
@Composable
fun ProductCard(product: Product) {
    Card(
        modifier = Modifier
            .padding(vertical = 20.dp)
            .fillMaxSize(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = Color.Transparent // Transparent to allow the gradient
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize() // Ensure the Box fills the entire card
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF41644A), // Hex for #41644A
                            Color(0xFF83CA95)  // Hex for #83CA95
                        )
                    )
                )
                .padding(70.dp)
        ) {
            Column {
                Text(text = "Product: ${product.name}")
                Text(text = "Quantity: ${product.quantity}")
            }
        }
    }
}



// Preview for FarmerInventoryScreen
@Preview
@Composable
fun FarmerInventoryPreview() {
    CelestiaTheme {
        FarmerInventoryScreen()
    }
}
