package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.service.NotificationService
import com.coco.celestia.viewmodel.model.Constants
import com.coco.celestia.viewmodel.model.FullFilledBy
import com.coco.celestia.viewmodel.model.MostOrdered
import com.coco.celestia.viewmodel.model.Notification
import com.coco.celestia.viewmodel.model.NotificationType
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.reflect.full.memberProperties

sealed class OrderState {
    data object LOADING : OrderState()
    data object SUCCESS : OrderState()
    data object EMPTY : OrderState()
    data class ERROR(val message: String) : OrderState()
}

sealed class MostOrderedState {
    data object LOADING : MostOrderedState()
    data object SUCCESS : MostOrderedState()
    data object EMPTY : MostOrderedState()
    data class ERROR(val message: String) : MostOrderedState()
}

class OrderViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("orders")
    private val _orderData = MutableLiveData<List<OrderData>>()
    private val _mostOrderedData = MutableLiveData<List<MostOrdered>>()
    private val _orderState = MutableLiveData<OrderState>()
    private val _mostOrderedState = MutableLiveData<MostOrderedState>()
    val orderData: LiveData<List<OrderData>> = _orderData
    val mostOrderedData: LiveData<List<MostOrdered>> = _mostOrderedData
    val orderState: LiveData<OrderState> = _orderState

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
                            filterKeywords.any { keyword ->
                                order::class.memberProperties.any { property ->
                                    val value = property.getter.call(order)?.toString() ?: ""
                                    value.contains(keyword, ignoreCase = true)
                                }
                            }
                        }
                    _orderData.value = orders
                    _orderState.value =
                        if (orders.isEmpty()) OrderState.EMPTY else OrderState.SUCCESS
                }

                override fun onCancelled(error: DatabaseError) {
                    _orderState.value = OrderState.ERROR(error.message)
                }
            })
        }
    }

    fun fetchMostOrderedItems() {
        viewModelScope.launch {
            val productCount = mutableMapOf<String, Triple<Int, String, Double>>()
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (orderSnapshot in snapshot.children) {

                        for (productSnapshot in orderSnapshot.children) {
                            val orderList = productSnapshot.child("orderData")
                            val productName = orderList.child("name").getValue(String::class.java)
                            val quantity =
                                orderList.child("quantity").getValue(Int::class.java) ?: 0
                            val type = orderList.child("type").getValue(String::class.java)
                            val price =
                                orderList.child("priceKg").getValue(Double::class.java) ?: 0.0

                            productName?.let {
                                val currentData =
                                    productCount[it] ?: Triple(0, type ?: "Unknown Type", 0.0)
                                productCount[it] =
                                    Triple(currentData.first + quantity, currentData.second, price)
                            }
                        }
                    }
                    val topProducts = productCount.entries
                        .sortedByDescending { it.value.first }
                        .take(6)
                        .map {
                            MostOrdered(
                                name = it.key,
                                quantity = it.value.first,
                                type = it.value.second,
                                priceKg = it.value.third
                            )
                        }

                    _mostOrderedData.value = topProducts
                    _mostOrderedState.value =
                        if (topProducts.isEmpty()) MostOrderedState.EMPTY else MostOrderedState.SUCCESS
                }

                override fun onCancelled(error: DatabaseError) {
                    _mostOrderedState.value = MostOrderedState.ERROR(error.message)
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

                    val orders = snapshot.children
                        .mapNotNull { snapshot -> parseOrderData(snapshot) }
                        .filter { order ->
                            // This should filter orders based on the facility? ☠️
                            val coopOrder = order.orderData.any { it.type == role.replace("Coop", "") }
                            val matchesFilter = filterKeywords.any { keyword ->
                                order::class.memberProperties.any { property ->
                                    val value = property.getter.call(order)?.toString() ?: ""
                                    value.contains(keyword, ignoreCase = true)
                                }
                            }
                            val removeCancelReject =
                                (order.status != "CANCELLED" && order.status != "REJECTED")
                            when {
                                role.contains("Coop") -> coopOrder && matchesFilter && removeCancelReject
                                role == "Admin" -> coopOrder && matchesFilter
                                role == "Farmer" -> matchesFilter
                                role == "Client" -> matchesFilter
                                else -> matchesFilter
                            }

                        }

                    _orderData.value = orders
                    _orderState.value =
                        if (orders.isEmpty()) OrderState.EMPTY else OrderState.SUCCESS
                }

                override fun onCancelled(error: DatabaseError) {
                    _orderState.value = OrderState.ERROR(error.message)
                }
            })
        }
    }

    private fun parseOrderData(snapshot: DataSnapshot): OrderData {
        return OrderData(
            orderId = snapshot.child("orderId").getValue(String::class.java) ?: "",
            orderDate = snapshot.child("orderDate").getValue(String::class.java) ?: "",
            targetDate = snapshot.child("targetDate").getValue(String::class.java) ?: "",
            status = snapshot.child("status").getValue(String::class.java) ?: "",
            orderData = snapshot.child("orderData").children
                .mapNotNull { it.getValue(ProductData::class.java) },
            client = snapshot.child("client").getValue(String::class.java) ?: "",
            barangay = snapshot.child("barangay").getValue(String::class.java) ?: "",
            street = snapshot.child("street").getValue(String::class.java) ?: "",
            rejectionReason = snapshot.child("rejectionReason").getValue(String::class.java) ?: "",
            fulfilledBy = snapshot.child("fulfilledBy").children
                .mapNotNull { it.getValue(FullFilledBy::class.java) },
            partialQuantity = snapshot.child("partialQuantity").getValue(Int::class.java) ?: 0,
            fulfilled = snapshot.child("fulfilled").getValue(Int::class.java) ?: 0,
            collectionMethod = snapshot.child("collectionMethod").getValue(String::class.java) ?: Constants.COLLECTION_PICKUP,
            paymentMethod = snapshot.child("paymentMethod").getValue(String::class.java) ?: Constants.PAYMENT_CASH
        )
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
                    viewModelScope.launch {
                        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mma")
                        val formattedDateTime = LocalDateTime.now().format(formatter)

                        val notification = Notification(
                            timestamp = formattedDateTime,
                            sender = order.client,
                            details = order,
                            message = "New order request from ${order.client}!",
                            type = NotificationType.OrderPlaced
                        )

                        NotificationService.pushNotifications(
                            notification = notification,
                            onComplete = { println("Success!") },
                            onError = { println("Error.") }
                        )
                    }

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
                        for (user in snapshot.children) {
                            var found = false
                            val orders = user.children
                            for (order in orders) {
                                val orderId = order.child("orderId").getValue(String::class.java)
                                if (orderId == updatedOrderData.orderId) {
                                    order.ref.setValue(updatedOrderData)
                                        .addOnSuccessListener {
                                            _orderState.value = OrderState.SUCCESS
                                        }
                                        .addOnFailureListener { exception ->
                                            _orderState.value = OrderState.ERROR(
                                                exception.message ?: "Unknown error"
                                            )
                                        }
                                    found = true
                                    break
                                }
                            }
                            if (found) {
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

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            _orderState.value = OrderState.LOADING
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (user in snapshot.children) {
                            var found = false
                            val orders = user.children
                            for (order in orders) {
                                val currentOrderId =
                                    order.child("orderId").getValue(String::class.java)
                                if (currentOrderId == orderId) {
                                    order.ref.child("status").setValue("CANCELLED")
                                        .addOnSuccessListener {
                                            _orderState.value = OrderState.SUCCESS
                                        }
                                        .addOnFailureListener { exception ->
                                            _orderState.value = OrderState.ERROR(
                                                exception.message ?: "Unknown error"
                                            )
                                        }
                                    found = true
                                    break
                                }
                            }
                            if (found) {
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

    fun markOrderReceived(orderId: String) {
        viewModelScope.launch {
            _orderState.value = OrderState.LOADING
            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (user in snapshot.children) {
                            var found = false
                            val orders = user.children
                            for (order in orders) {
                                val currentOrderId =
                                    order.child("orderId").getValue(String::class.java)
                                if (currentOrderId == orderId) {
                                    order.ref.child("status").setValue("RECEIVED")
                                        .addOnSuccessListener {
                                            _orderState.value = OrderState.SUCCESS
                                        }
                                        .addOnFailureListener { exception ->
                                            _orderState.value = OrderState.ERROR(
                                                exception.message ?: "Unknown error"
                                            )
                                        }
                                    found = true
                                    break
                                }
                            }
                            if (found) {
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

    fun tagOrderAsCalamityAffected(orderData: OrderData) {
        viewModelScope.launch {
            _orderState.value = OrderState.LOADING

            database.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (user in snapshot.children) {
                            var found = false
                            val orders = user.children
                            for (order in orders) {
                                val currentOrderId =
                                    order.child("orderId").getValue(String::class.java)
                                if (currentOrderId == orderData.orderId) {
                                    // Update the status to "CALAMITY_AFFECTED"
                                    order.ref.child("status").setValue("CALAMITY_AFFECTED")
                                        .addOnSuccessListener {
                                            _orderState.value = OrderState.SUCCESS
                                        }
                                        .addOnFailureListener { exception ->
                                            _orderState.value = OrderState.ERROR(
                                                exception.message ?: "Unknown error"
                                            )
                                        }
                                    found = true
                                    break
                                }
                            }
                            if (found) {
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