package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.viewmodel.model.OrderData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
     * Fetches order data from the database based on the provided order ID.
     *
     * This function fetches order data from the database based on the provided order ID.
     *
     * @param targetId The ID of the order to be fetched.
     * @throws DatabaseError If there is an error fetching the order.
     */
    fun fetchOrder(targetId: String) {
        viewModelScope.launch {
            _orderState.value = OrderState.LOADING
            try {
                val snapshot = database.get().await()
                if (snapshot.exists()) {
                    for(user in snapshot.children) {
                        var found = false
                        val orders = user.children
                        for(order in orders) {
                            val orderInfo = order.getValue(OrderData::class.java)
                            val orderId = order.child("orderId").getValue(String::class.java)
                            if(orderId == targetId) {
                                val orderData = mutableListOf<OrderData>()
                                orderData.add(orderInfo!!)
                                _orderData.value = orderData
                                _orderState.value = OrderState.SUCCESS
                                found = true
                                break
                            }
                        }
                        if(found) {
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
     *
     * This function fetches orders from the database based on the provided UID and applies a filter to the orders.
     *
     * @param uid The UID of the user whose orders are to be fetched.
     * @param filter The filter criteria to apply to the orders (e.g., "Packed coffee, Green beans").
     * @throws DatabaseError If there is an error fetching the orders.
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
                        .filter { order ->
                            order.orderData.any { product ->
                                filterKeywords.any { keyword ->
                                    product.name.contains(keyword, ignoreCase = true) ||
                                    product.type.contains(keyword, ignoreCase = true)
                                }
                            }
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
     * Fetches all orders from the database based on the provided filter criteria.
     *
     * This function fetches all orders from the database based on the provided filter criteria and role.
     *
     * @param filter The filter criteria to apply to the orders (e.g., "Onion, Lettuce, Carrots").
     * @param role The role of the user (e.g., "Coop", "Farmer", "Admin").
     * @throws DatabaseError If there is an error fetching the orders
     */
    fun fetchAllOrders(
        filter: String,
        role: String
    ) {
        viewModelScope.launch {
            _orderState.value = OrderState.LOADING
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val filterKeywords = filter.split(",").map { it.trim() }

                    val orders = snapshot.children.flatMap { userSnapshot ->
                        userSnapshot.children.mapNotNull { orderSnapshot ->
                            orderSnapshot.getValue(OrderData::class.java)
                        }.filter { order ->
                            order.orderData.any { product ->
                                val isCoffeeOrMeat = product.type.equals("Coffee", ignoreCase = true) ||
                                        product.type.equals("Meat", ignoreCase = true)
                                val isVegetable = product.type.equals("Vegetable", ignoreCase = true)
                                val matchesFilter = filterKeywords.any { keyword ->
                                    product.name.contains(keyword, ignoreCase = true) ||
                                            product.type.contains(keyword, ignoreCase = true)
                                }
                                when (role) {
                                    "Coop", "Admin" -> isCoffeeOrMeat && matchesFilter
                                    "Farmer" -> isVegetable && matchesFilter
                                    "Client" -> matchesFilter
                                    else -> false
                                }
                            }
                        }
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
     *
     * This function places an order in the database based on the provided UID and order data.
     *
     * @param uid The UID of the user placing the order.
     * @param order The order data to be placed.
     * @throws Exception If there is an error placing the order.
     */
    fun placeOrder(
        uid: String,
        order: OrderData,
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
     * Updates an order in the database based on the order ID.
     *
     * This function updates an order in the database by finding iterating through all users and orders.
     *
     * @param updatedOrderData The updated order data to be placed.
     * @throws DatabaseError If there is an error updating the order.
     */
    fun updateOrder(updatedOrderData: OrderData) {
        viewModelScope.launch {
            _orderState.value = OrderState.LOADING
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for(user in snapshot.children) {
                            var found = false
                            val orders = user.children
                            for(order in orders) {
                                val orderId = order.child("orderId").getValue(String::class.java)
                                if(orderId == updatedOrderData.orderId) {
                                    order.ref.setValue(updatedOrderData)
                                        .addOnSuccessListener {
                                            _orderState.value = OrderState.SUCCESS
                                        }
                                        .addOnFailureListener { exception ->
                                            _orderState.value = OrderState.ERROR(exception.message ?: "Unknown error")
                                        }
                                    found = true
                                    break
                                }
                            }
                            if(found) {
                                break
                            }
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