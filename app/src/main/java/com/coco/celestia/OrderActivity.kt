package com.coco.celestia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.coco.celestia.ui.theme.CelestiaTheme
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OrderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CelestiaTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF2E3DB))
                ) {
                    OrderPanel()
                }
            }
        }
    }
}

@Composable
fun OrderPanel() {
    var orderList by remember { mutableStateOf<List<OrderData>>(emptyList()) }
    var isError by remember { mutableStateOf(false) }

    // Fetch orders when the composable is first composed
    LaunchedEffect(Unit) {
        fetchOrderList(
            onSuccess = { orders ->
                orderList = orders
            },
            onError = {
                isError = true
            }
        )
    }

    if (isError) {
        Text("Failed to load orders.")
    } else {
        LazyColumn {
            items(orderList) { order ->
                OrderItem(order)
            }
        }
    }
}

@Composable
fun OrderItem(order: OrderData) {
    // TODO: @Earl @Ron 🥹👉👈 design the order item?
    Text(text = "Product: ${order.product}")
    Text(text = "Quantity: ${order.quantity}")
    Text(text = "City: ${order.city}")
    Text(text = "Type: ${order.type}")
    Text(text = "Street: ${order.street}")
    Text(text = "Postal Code: ${order.postalCode}")
    Text(text = "Barangay: ${order.barangay}")
    Text(text = "Additional Info: ${order.additionalInfo}")
    Text(text = "Order Date: ${order.orderDate}")
    Spacer(modifier = Modifier.height(8.dp))
}

private fun fetchOrderList(
    onSuccess: (List<OrderData>) -> Unit,
    onError: () -> Unit
) {
    val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("orders")

    databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            try {
                val orderList = snapshot.children.mapNotNull { child ->
                    val orderData = child.getValue(OrderData::class.java)
                    orderData?.copy(orderDate = orderData.orderDate)
                }
                onSuccess(orderList)
            } catch (e: Exception) {
                e.printStackTrace()
                onError()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            error.toException().printStackTrace()
            onError()
        }
    })
}

