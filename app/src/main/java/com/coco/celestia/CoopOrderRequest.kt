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
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.ui.theme.CelestiaTheme
import com.coco.celestia.ui.theme.Orange
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.google.firebase.auth.FirebaseAuth

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
                    OrderRequestPanel("Coffee, Meat")
                }
            }
        }
    }
}

@Composable
fun OrderRequestPanel(keywords: String) {
    val orderViewModel: OrderViewModel = viewModel()
    val transactionViewModel: TransactionViewModel = viewModel()
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val orderState by orderViewModel.orderState.observeAsState(OrderState.LOADING)
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var isError by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LaunchedEffect(Unit) {
            orderViewModel.fetchOrders(
                uid = auth.currentUser?.uid.toString(),
                filter = keywords
            )
        }
        when (orderState) {
            is OrderState.LOADING -> {
                Text("Loading orders...")
            }
            is OrderState.ERROR -> {
                Text("Failed to load orders: ${(orderState as OrderState.ERROR).message}")
            }
            is OrderState.EMPTY -> {
                Text("Awit man! No pending orders.")
            }
            is OrderState.SUCCESS -> {
                LazyColumn {
                    items(orderData) { order ->
                        OrderItemDecision(
                            order,
                            onAccept = {
                                orderViewModel.updateOrder(
                                    auth.currentUser?.uid.toString(),
                                    order.copy(status = "ACCEPTED"),
                                )
                                transactionViewModel.recordTransaction(
                                    auth.currentUser?.uid.toString(),
                                    TransactionData(
                                        "TRNSCTN{${order.orderId}}",
                                        order.copy(status = "ACCEPTED")
                                    )
                                )
                            },
                            onReject = {
                                orderViewModel.updateOrder(
                                    auth.currentUser?.uid.toString(),
                                    order.copy(status = "REJECTED"),
                                )
                                transactionViewModel.recordTransaction(
                                    auth.currentUser?.uid.toString(),
                                    TransactionData(
                                        "TRNSCTN{${order.orderId}}",
                                        order.copy(status = "ACCEPTED")
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

//fun listenForOrderUpdates(
//    filter: String,
//    onSuccess: (List<OrderData>) -> Unit,
//    onError: (Exception) -> Unit
//) {
//    val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("orders")
//
//    databaseReference.addValueEventListener(object : ValueEventListener {
//        override fun onDataChange(snapshot: DataSnapshot) {
//            val filterKeywords = filter.split(",").map { it.trim() }
//
//            val orders = snapshot.children
//                .mapNotNull { it.getValue(OrderData::class.java) }
//                .filter { product ->
//                    val matches = filterKeywords.any { keyword ->
//                        OrderData::class.memberProperties.any { prop ->
//                            val value = prop.get(product)
//                            value?.toString()?.contains(keyword, ignoreCase = true) == true
//                        }
//                    }
//                    matches && product.status == "PENDING"
//                }
//            onSuccess(orders)
//        }
//
//        override fun onCancelled(error: DatabaseError) {
//            onError(error.toException())
//        }
//    })
//}

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
                        if (action == "Accept") onAccept() else onReject()
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

//fun updateOrderStatus(
//    order: OrderData,
//    accepted: Boolean,
//    onComplete: () -> Unit
//) {
//    val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("orders")
//    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
//    val query = databaseReference.child(uid).orderByChild("orderId").equalTo(order.orderId)
//
//    query.addListenerForSingleValueEvent(object : ValueEventListener {
//        override fun onDataChange(snapshot: DataSnapshot) {
//            if (snapshot.exists()) {
//                val orderSnapshot = snapshot.children.first()
//                orderSnapshot.ref.child("status").setValue(if (accepted) "ACCEPTED" else "REJECTED")
//                    .addOnSuccessListener {
//                        onComplete()
//                    }
//                    .addOnFailureListener { exception ->
//                        println("Error updating order status: ${exception.message}")
//                    }
//            } else {
//                println("No order found with ID: ${order.orderId}")
//            }
//        }
//
//        override fun onCancelled(error: DatabaseError) {
//            println("Error querying orders: ${error.message}")
//        }
//    })
//}
