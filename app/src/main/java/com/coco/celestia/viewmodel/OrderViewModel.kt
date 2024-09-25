package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
     *
     * This function fetches order data from the database based on the provided UID and order ID.
     *
     * @param uid The UID of the user whose order is to be fetched.
     * @param orderId The ID of the order to be fetched.
     * @throws DatabaseError If there is an error fetching the order.
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
                        .filter { product ->
                            val matches = filterKeywords.any { keyword ->
                                OrderData::class.memberProperties.any { prop ->
                                    val value = prop.get(product)
                                    value?.toString()?.contains(keyword, ignoreCase = true) == true
                                }
                            }
                            matches
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
     * @param isPending If true, only pending orders will be fetched. If false, all orders will be fetched.
     * @param role The role of the user (e.g., "Coop", "Farmer", "Admin").
     * @throws DatabaseError If there is an error fetching the orders
     */
    fun fetchAllOrders(
        filter: String,
        isPending: Boolean = false,
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
                        }.filter { orderData ->
                            val pending = orderData.status.equals("PENDING", ignoreCase = true)
                            val coffeeOrMeat = orderData.orderData.type.equals("Coffee", ignoreCase = true) ||
                                    orderData.orderData.type.equals("Meat", ignoreCase = true)
                            val vegetable = orderData.orderData.type.equals("Vegetable", ignoreCase = true)
                            val filtered = filterKeywords.any { keyword ->
                                OrderData::class.memberProperties.any { prop ->
                                    prop.get(orderData)?.toString()?.contains(keyword, ignoreCase = true) == true
                                }
                            }
                            when (role) {
                                "Coop" -> if (isPending) pending && coffeeOrMeat && filtered else coffeeOrMeat && filtered
                                "Farmer" -> if (isPending) pending && vegetable && filtered else vegetable && filtered
                                "Admin" -> filtered
                                else -> false
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
     *
     * This function updates an order in the database based on the provided UID and updated order data.
     *
     * @param uid The UID of the user placing the order.
     * @param updatedOrderData The updated order data to be placed.
     * @throws DatabaseError If there is an error updating the order.
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