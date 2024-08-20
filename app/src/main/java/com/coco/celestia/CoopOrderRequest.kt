package com.coco.celestia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.ui.theme.CelestiaTheme
import com.coco.celestia.ui.theme.Orange
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CoopOrderRequest : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CelestiaTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF2E3DB))
                ) {
                    OrderRequestPanel()
                }
            }
        }
    }
}

@Composable
fun OrderRequestPanel() {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var orderList by remember { mutableStateOf<List<OrderData>>(emptyList()) }
        var isError by remember { mutableStateOf(false) }

        Text(text = "Order Request", fontSize = 25.sp)
        Spacer(modifier = Modifier.height(150.dp))
        LaunchedEffect(Unit) {
            listenForOrderUpdates(
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
            if (orderList.isEmpty()) Text(text = "Awit man! No pending orders.")
            LazyColumn {
                items(orderList) { order ->
                    OrderItemDecision(
                        order,
                        onAccept = {
                            updateOrderStatus(order, true) {}
                        },
                        onReject = {
                            updateOrderStatus(order, false) {}
                        }
                    )
                }
            }
        }
    }
}

fun listenForOrderUpdates(
    onSuccess: (List<OrderData>) -> Unit,
    onError: (Exception) -> Unit
) {
    val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("orders")

    databaseReference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val orders = snapshot.children
                .mapNotNull { it.getValue(OrderData::class.java) }
                .filter { it.status == "PENDING" }
            onSuccess(orders)
        }

        override fun onCancelled(error: DatabaseError) {
            onError(error.toException())
        }
    })
}

@Composable
fun OrderItemDecision(
    order: OrderData,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var action by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Card {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .animateContentSize()
            ) {
                Text(text = if (order.product != "Vegetable") "${order.type}, ${order.quantity}kg" else order.type,
                    fontSize = 30.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif)
                Text(text = "${order.status} ●", fontSize = 20.sp, fontWeight = FontWeight.Light, color = Orange)
                Text(text = "${order.street}, ${order.barangay}, ${order.city} ${order.postalCode}")
                Text(text = order.additionalInfo, fontStyle = FontStyle.Italic)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = order.orderDate)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            action = "Reject"
                            showDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(contentColor = Color.Red, containerColor = Color.White)
                    ) {
                        Text("Reject")
                    }

                    Button(
                        onClick = {
                            action = "Accept"
                            showDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    ) {
                        Text("Accept")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
            },
            title = {
                Text(text = "$action Order")
            },
            text = {
                Text(text = "Are you sure you want to $action this order?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        if (action == "ACCEPTED") onAccept() else onReject()
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text("No")
                }
            }
        )
    }
}

fun updateOrderStatus(
    order: OrderData,
    accepted: Boolean,
    onComplete: () -> Unit
) {
    val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("orders")
    val query = databaseReference.orderByChild("orderId").equalTo(order.orderId)

    query.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                val orderSnapshot = snapshot.children.first()
                orderSnapshot.ref.child("status").setValue(if (accepted) "ACCEPTED" else "REJECTED")
                    .addOnSuccessListener {
                        onComplete()
                    }
                    .addOnFailureListener { exception ->
                        println("Error updating order status: ${exception.message}")
                    }
            } else {
                println("No order found with ID: ${order.orderId}")
            }
        }

        override fun onCancelled(error: DatabaseError) {
            println("Error querying orders: ${error.message}")
        }
    })
}
