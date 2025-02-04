package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.service.NotificationService
import com.coco.celestia.util.DataParser
import com.coco.celestia.viewmodel.model.Constants
import com.coco.celestia.viewmodel.model.MostOrdered
import com.coco.celestia.viewmodel.model.Notification
import com.coco.celestia.viewmodel.model.NotificationType
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.StatusUpdate
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
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
    private val productsRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("products")
    private val _orderData = MutableLiveData<List<OrderData>>()
    private val _mostOrderedData = MutableLiveData<List<MostOrdered>>()
    private val _orderState = MutableLiveData<OrderState>()
    private val _mostOrderedState = MutableLiveData<MostOrderedState>()
    val orderData: LiveData<List<OrderData>> = _orderData
    val mostOrderedData: LiveData<List<MostOrdered>> = _mostOrderedData
    val orderState: LiveData<OrderState> = _orderState

    private suspend fun notify(type: NotificationType, order: OrderData) {
        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mma")
        val formattedDateTime = LocalDateTime.now().format(formatter)

        val notification = Notification(
            timestamp = formattedDateTime
                .replace("am", "AM", ignoreCase = true)
                .replace("pm", "PM", ignoreCase = true),
            sender = order.client,
            detailsId = order.orderId,
            subject = when (type) {
                NotificationType.ClientSpecialRequest,
                NotificationType.CoopSpecialRequestUpdated,
                NotificationType.FarmerCalamityAffected,
                NotificationType.Notice -> "Coco"
                NotificationType.ClientOrderPlaced -> "New order request from ${order.client}!"
                NotificationType.OrderUpdated -> "Your order has been updated to: ${order.status}!"
            },
            message = NotificationService.parseDetailsMessage(type, order.orderId),
            type = type
        )

        NotificationService.pushNotifications(
            notification = notification,
            onComplete = { },
            onError = { }
        )
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
    fun fetchOrders(uid: String, filter: String) {
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
                        .sortedByDescending { it.timestamp }

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
    fun fetchAllOrders(filter: String, role: String) {
        viewModelScope.launch {
            _orderState.value = OrderState.LOADING
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val filterKeywords = filter.split(",").map { it.trim() }
                    val facilityName = role.replace("Coop", "")
                    val ordersMap = mutableMapOf<String, OrderData>()

                    snapshot.children.forEach { userSnapshot ->
                        userSnapshot.children.forEach { orderSnapshot ->
                            val order = DataParser.parseOrderData(orderSnapshot)
                            if (order != null) {
                                val orderIdParts = order.orderId.split("-")
                                val orderFacilityType = if (orderIdParts.size >= 3) orderIdParts[1] else ""

                                val matchesFilter = filterKeywords.isEmpty() || filterKeywords.any { keyword ->
                                    order::class.memberProperties.any { property ->
                                        val value = property.getter.call(order)?.toString() ?: ""
                                        value.contains(keyword, ignoreCase = true)
                                    }
                                }

                                val removeCancelReject = (order.status != "CANCELLED" && order.status != "REJECTED")

                                val shouldInclude = when {
                                    role.startsWith("Coop") ->
                                        orderFacilityType == facilityName && matchesFilter && removeCancelReject
                                    role == "Admin" ->
                                        matchesFilter
                                    role == "Farmer" || role == "Client" ->
                                        matchesFilter
                                    else ->
                                        matchesFilter
                                }

                                if (shouldInclude) {
                                    ordersMap[order.orderId] = order
                                }
                            }
                        }
                    }

                    val orders = ordersMap.values.toList()
                        .sortedByDescending { it.timestamp }

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
    fun placeOrder(uid: String, order: OrderData) {
        viewModelScope.launch {
            _orderState.value = OrderState.LOADING

            val displayFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mma")
            val timestampFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            val currentDateTime = LocalDateTime.now()

            val formattedDisplayDate = currentDateTime.format(displayFormatter)
            val timestamp = currentDateTime.format(timestampFormatter)

            val statusDescription = when(order.status) {
                "Pending" -> "Your order is being reviewed"
                "Confirmed" -> "Your order has been confirmed"
                "To Deliver" -> "Your order is ready for delivery"
                "To Receive" -> "Your order is out for delivery"
                "Completed" -> "Your order has been completed"
                "Cancelled" -> "Your order has been cancelled"
                "Return/Refund" -> "Your order is being processed for return/refund"
                else -> ""
            }

            val initialStatus = StatusUpdate(
                status = order.status,
                statusDescription = statusDescription,
                dateTime = formattedDisplayDate,
                updatedBy = ""
            )

            try {
                val updatedOrderItems = order.orderData.map { orderItem ->
                    try {
                        val productSnapshot = productsRef.child(orderItem.productId).get().await()
                        val weightUnit = productSnapshot.child("weightUnit").getValue(String::class.java)
                        orderItem.copy(
                            weightUnit = weightUnit ?: Constants.WEIGHT_GRAMS
                        )
                    } catch (e: Exception) {
                        orderItem
                    }
                }

                val orderWithTimestamp = order.copy(
                    orderDate = formattedDisplayDate,
                    timestamp = timestamp,
                    statusDescription = statusDescription,
                    statusHistory = listOf(initialStatus),
                    gcashPaymentId = order.gcashPaymentId.ifEmpty { "" },
                    orderData = updatedOrderItems
                )

                try {
                    updateProductStock(orderWithTimestamp.orderData, "Pending")

                    val query = database.child(uid).push()
                    query.setValue(orderWithTimestamp)
                        .addOnSuccessListener {
                            viewModelScope.launch {
                                notify(NotificationType.ClientOrderPlaced, orderWithTimestamp)
                                _orderState.value = OrderState.SUCCESS
                            }
                        }
                        .addOnFailureListener { exception ->
                            viewModelScope.launch {
                                _orderState.value = OrderState.ERROR(exception.message ?: "Unknown error")
                                try {
                                    updateProductStock(orderWithTimestamp.orderData, "Cancelled")
                                } catch (e: Exception) {
                                    _orderState.value = OrderState.ERROR("Failed to rollback stock changes")
                                }
                            }
                        }
                } catch (e: Exception) {
                    _orderState.value = OrderState.ERROR(e.message ?: "Failed to update product stock")
                }
            } catch (e: Exception) {
                _orderState.value = OrderState.ERROR("Failed to fetch product details: ${e.message}")
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

            try {
                database.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        var orderFound: MutableData? = null
                        var existingOrder: OrderData? = null

                        for (userSnapshot in currentData.children) {
                            for (orderSnapshot in userSnapshot.children) {
                                val order = orderSnapshot.getValue(OrderData::class.java)

                                if (order?.orderId == updatedOrderData.orderId) {
                                    orderFound = orderSnapshot
                                    existingOrder = order
                                    break
                                }
                            }
                            if (orderFound != null) break
                        }

                        if (orderFound == null || existingOrder == null) {
                            return Transaction.success(currentData)
                        }

                        if (existingOrder.status == "Cancelled" || existingOrder.status == "Completed") {
                            return Transaction.success(currentData)
                        }

                        val previousStatus = existingOrder.status
                        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mma")
                        val currentDateTime = LocalDateTime.now().format(formatter)

                        val newStatusUpdate = StatusUpdate(
                            status = updatedOrderData.status,
                            statusDescription = updatedOrderData.statusDescription.ifEmpty { getDefaultStatusDescription(updatedOrderData.status) },
                            dateTime = currentDateTime
                        )

                        val updatedHistory = existingOrder.statusHistory + newStatusUpdate

                        val updatedOrder = existingOrder.copy(
                            status = updatedOrderData.status,
                            statusHistory = updatedHistory
                        )

                        if (previousStatus != updatedOrder.status) {
                            viewModelScope.launch {
                                when (updatedOrder.status) {
                                    "Pending" -> updateProductStock(updatedOrder.orderData, "Pending")
                                    "Completed" -> updateProductStock(updatedOrder.orderData, "Completed")
                                    "Cancelled" -> updateProductStock(updatedOrder.orderData, "Cancelled")
                                }
                            }
                        }

                        orderFound.setValue(updatedOrder)
                        viewModelScope.launch { notify(NotificationType.OrderUpdated, updatedOrder) }
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        viewModelScope.launch {
                            if (error != null) {
                                _orderState.value = OrderState.ERROR(error.message)
                            } else if (!committed) {
                                _orderState.value = OrderState.ERROR("Order update rejected due to conflicts")
                            } else {
                                _orderState.value = OrderState.SUCCESS
                            }
                        }
                    }


                })
            } catch (e: Exception) {
                _orderState.value = OrderState.ERROR(e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun updateProductStock(orderData: List<ProductData>, status: String) {
        try {
            orderData.forEach { product ->
                withContext(Dispatchers.IO) {
                    suspendCancellableCoroutine { continuation ->
                        val stockRef = productsRef.child(product.productId)

                        stockRef.get()
                            .addOnSuccessListener { snapshot ->
                                if (snapshot.exists()) {
                                    val currentProduct = snapshot.getValue(ProductData::class.java)
                                    currentProduct?.let { current ->
                                        val updates = when (status) {
                                            "Pending" -> {
                                                mapOf(
                                                    "quantity" to (current.quantity - product.quantity),
                                                    "committedStock" to (current.committedStock + product.quantity)
                                                )
                                            }
                                            "Completed" -> {
                                                mapOf(
                                                    "committedStock" to (current.committedStock - product.quantity),
                                                    "totalQuantitySold" to (current.totalQuantitySold + product.quantity)
                                                )
                                            }
                                            "Cancelled" -> {
                                                mapOf(
                                                    "quantity" to (current.quantity + product.quantity),
                                                    "committedStock" to (current.committedStock - product.quantity)
                                                )
                                            }
                                            else -> null
                                        }

                                        updates?.let { stockUpdates ->
                                            stockRef.updateChildren(stockUpdates)
                                                .addOnSuccessListener {
                                                    if (continuation.isActive) {
                                                        continuation.resume(Unit)
                                                    }
                                                }
                                                .addOnFailureListener { error ->
                                                    if (continuation.isActive) {
                                                        continuation.resumeWithException(
                                                            Exception("Failed to update product ${product.name}: ${error.message}")
                                                        )
                                                    }
                                                }
                                        } ?: continuation.resume(Unit)
                                    } ?: run {
                                        if (continuation.isActive) {
                                            continuation.resumeWithException(
                                                Exception("Product data is null for ${product.name}")
                                            )
                                        }
                                    }
                                } else {
                                    if (continuation.isActive) {
                                        continuation.resumeWithException(
                                            Exception("Product not found: ${product.name}")
                                        )
                                    }
                                }
                            }
                            .addOnFailureListener { error ->
                                if (continuation.isActive) {
                                    continuation.resumeWithException(
                                        Exception("Failed to fetch product ${product.name}: ${error.message}")
                                    )
                                }
                            }
                    }
                }
            }
        } catch (e: Exception) {
            _orderState.value = OrderState.ERROR(e.message ?: "Failed to update product quantities")
        }
    }

    private fun getDefaultStatusDescription(status: String): String {
        return when(status) {
            "Pending" -> "Your order is being reviewed."
            "Confirmed" -> "Your order has been confirmed."
            "To Deliver" -> "Your order is to be handed to courier."
            "To Receive" -> "Your order is ready to be picked up/ has been shipped by courier."
            "Completed" -> "Your order has been completed."
            "Cancelled" -> "Your order has been cancelled."
            "Return/Refund" -> "Your order is being processed for return/refund."
            else -> "Status updated to $status"
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