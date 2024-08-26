package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.OrderData
import com.coco.celestia.UserData
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.reflect.full.memberProperties

sealed class OrderState {
    object LOADING : OrderState()
    object SUCCESS : OrderState()
    object EMPTY : OrderState()
    data class ERROR(val message: String) : OrderState()
}

class OrderViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("orders")
    private val _orderData = MutableLiveData<List<OrderData>>()
    private val _orderState = MutableLiveData<OrderState>()
    val orderData: LiveData<List<OrderData>> = _orderData
    val orderState: LiveData<OrderState> = _orderState

    /**
     * Fetches order data from the database based on the provided UID and order ID.
     */
    fun fetchOrder(
        uid: String,
        orderId: String
    ) {
        viewModelScope.launch {
            _orderState.value = OrderState.LOADING
            try {
                val snapshot = database.child(uid).get().await()
                if (snapshot.exists()) {
                    for (orderSnapshot in snapshot.children) {
                        val orderInfo = orderSnapshot.getValue(OrderData::class.java)
                        if (orderInfo?.orderId == orderId) {
                            val orderData = mutableListOf<OrderData>()
                            orderData.add(orderInfo)
                            _orderData.value = orderData
                            _orderState.value = OrderState.SUCCESS
                            break
                        }
                    }
                } else {
                    _orderData.value = emptyList()
                    _orderState.value = OrderState.EMPTY
                }
            } catch(e: Exception) {
                _orderState.value = OrderState.ERROR(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Fetches orders from the database based on the provided UID.
     */
    fun fetchOrders(
        uid: String,
        filter: String,
    ) {
        viewModelScope.launch {
            _orderState.value = OrderState.LOADING
            database.child(uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val filterKeywords = filter.split(",").map { it.trim() }

                    val orders = snapshot.children
                        .mapNotNull { it.getValue(OrderData::class.java) }
                        .filter { product ->
                            val matches = filterKeywords.any { keyword ->
                                OrderData::class.memberProperties.any { prop ->
                                    val value = prop.get(product)
                                    value?.toString()?.contains(keyword, ignoreCase = true) == true
                                }
                            }
                            matches && product.status == "PENDING"
                        }

                    _orderData.value = orders
                    _orderState.value = if (orders.isEmpty()) OrderState.EMPTY else OrderState.SUCCESS
                }

                override fun onCancelled(error: DatabaseError) {
                    _orderState.value = OrderState.ERROR(error.message)
                }
            })
        }
    }

    /**
     * Places an order in the database based on the provided UID.
     */
    fun placeOrder(
        uid: String,
        order: OrderData
    ) {
        viewModelScope.launch {
            _orderState.value = OrderState.LOADING
            val query = database.child(uid).push()
            query.setValue(order)
                .addOnCompleteListener {
                    _orderState.value = OrderState.SUCCESS
                }
                .addOnFailureListener { exception ->
                    _orderState.value = OrderState.ERROR(exception.message ?: "Unknown error")
                }
        }
    }

    /**
     * Updates an order in the database based on the provided UID.
     */
    fun updateOrder(
        uid: String,
        updatedOrderData: OrderData
    ) {
        val query = database.child(uid).orderByChild("orderId").equalTo(updatedOrderData.orderId)

        viewModelScope.launch {
            _orderState.value = OrderState.LOADING
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val orderSnapshot = snapshot.children.first()
                        orderSnapshot.ref.setValue(updatedOrderData)
                            .addOnSuccessListener {
                                _orderState.value = OrderState.SUCCESS
                            }
                            .addOnFailureListener { exception ->
                                _orderState.value = OrderState.ERROR(exception.message ?: "Unknown error")
                            }
                    } else {
                        _orderState.value = OrderState.EMPTY
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _orderState.value = OrderState.ERROR(error.message)
                }
            })
        }
    }
}