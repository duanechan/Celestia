package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.PurchaseOrder
import com.coco.celestia.viewmodel.model.PurchaseOrderItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

sealed class PurchaseOrderState {
    data object LOADING : PurchaseOrderState()
    data object SUCCESS : PurchaseOrderState()
    data object EMPTY : PurchaseOrderState()
    data class ERROR(val message: String) : PurchaseOrderState()
}

class PurchaseOrderViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("purchase_orders")
    private val productsRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("products")
    private val _purchaseOrderData = MutableLiveData<List<PurchaseOrder>>()
    private val _purchaseOrderState = MutableLiveData<PurchaseOrderState>()
    val InputDate: LiveData<List<PurchaseOrder>> = _purchaseOrderData
    val purchaseOrderState: LiveData<PurchaseOrderState> = _purchaseOrderState

    private var currentListener: ValueEventListener? = null
    private var purchaseCount = 0

    init {
        initializePurchaseCount()
    }

    private fun initializePurchaseCount() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val snapshot = database.get().await()
                if (snapshot.exists()) {
                    purchaseCount = snapshot.children.maxOfOrNull { child ->
                        child.key?.split("-")?.lastOrNull()?.toIntOrNull() ?: 0
                    } ?: 0
                }
            } catch (e: Exception) {
                purchaseCount = 0
            }
        }
    }

    private fun generatePurchaseNumber(): String {
        purchaseCount++
        val currentDate = java.time.LocalDate.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"))
        return "PO-$currentDate-$purchaseCount"
    }

    fun getPurchaseCount(): Int = purchaseCount

    fun fetchPurchaseOrders(filter: String = "all", searchQuery: String = "", facilityName: String? = null) {
        viewModelScope.launch {
            _purchaseOrderState.value = PurchaseOrderState.LOADING

            currentListener?.let {
                database.removeEventListener(it)
            }

            currentListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val searchKeywords = searchQuery.split(" ").map { it.trim() }

                    val purchaseOrders = snapshot.children
                        .mapNotNull {
                            val order = it.getValue(PurchaseOrder::class.java)
                            order?.copy(id = it.key ?: "")
                        }
                        .filter { purchaseOrder ->
                            if (facilityName != null) {
                                purchaseOrder.facility == facilityName
                            } else true
                        }
                        .filter { purchaseOrder ->
                            if (searchQuery.isNotEmpty()) {
                                searchKeywords.any { keyword ->
                                    purchaseOrder.vendor.contains(keyword, ignoreCase = true) ||
                                            purchaseOrder.purchaseNumber.contains(keyword, ignoreCase = true) ||
                                            purchaseOrder.referenceNumber.contains(keyword, ignoreCase = true)
                                }
                            } else true
                        }
                        .filter { purchaseOrder ->
                            when (filter.lowercase()) {
                                "draft" -> purchaseOrder.savedAsDraft
                                "all" -> true
                                else -> !purchaseOrder.savedAsDraft
                            }
                        }

                    _purchaseOrderData.value = purchaseOrders
                    _purchaseOrderState.value = if (purchaseOrders.isEmpty())
                        PurchaseOrderState.EMPTY else PurchaseOrderState.SUCCESS
                }

                override fun onCancelled(error: DatabaseError) {
                    _purchaseOrderState.value = PurchaseOrderState.ERROR(error.message)
                }
            }

            database.addValueEventListener(currentListener!!)
        }
    }

    suspend fun getPurchaseOrder(purchaseNumber: String): PurchaseOrder? {
        return suspendCoroutine { continuation ->
            database.child(purchaseNumber)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val purchaseOrder = snapshot.getValue(PurchaseOrder::class.java)
                        continuation.resume(purchaseOrder?.copy(id = snapshot.key ?: ""))
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(Exception(error.message))
                    }
                })
        }
    }

    fun addPurchaseOrder(
        purchaseOrder: PurchaseOrder,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val finalPurchaseOrder = if (purchaseOrder.id.isEmpty()) {
                    val newPurchaseNumber = generatePurchaseNumber()
                    purchaseOrder.copy(
                        id = newPurchaseNumber,
                        purchaseNumber = newPurchaseNumber
                    )
                } else {
                    purchaseOrder
                }

                if (!finalPurchaseOrder.savedAsDraft) {
                    updateProductQuantities(
                        finalPurchaseOrder.items,
                        onSuccess = {
                            database.child(finalPurchaseOrder.purchaseNumber)
                                .setValue(finalPurchaseOrder)
                                .addOnSuccessListener { onSuccess() }
                                .addOnFailureListener { error ->
                                    onError(error.message ?: "Error saving purchase order")
                                }
                        },
                        onError = { error -> onError(error) }
                    )
                } else {
                    database.child(finalPurchaseOrder.purchaseNumber)
                        .setValue(finalPurchaseOrder)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { error ->
                            onError(error.message ?: "Error saving purchase order")
                        }
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error processing purchase order")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentListener?.let {
            database.removeEventListener(it)
        }
    }

    private fun updateProductQuantities(
        items: List<PurchaseOrderItem>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (items.isEmpty()) {
            onSuccess()
            return
        }

        var completedUpdates = 0
        var hasError = false

        items.forEach { item ->
            val productRef = productsRef.child(item.productId)

            productRef.get().addOnSuccessListener { snapshot ->
                if (hasError) return@addOnSuccessListener

                if (!snapshot.exists()) {
                    hasError = true
                    onError("Product with ID ${item.productId} not found")
                    return@addOnSuccessListener
                }

                val product = snapshot.getValue(ProductData::class.java)
                if (product == null) {
                    hasError = true
                    onError("Invalid data for product ${item.productId}")
                    return@addOnSuccessListener
                }

                val newQuantity = product.quantity + item.quantity
                val newTotalPurchases = product.totalPurchases + item.quantity
                val updates = hashMapOf<String, Any>(
                    "quantity" to newQuantity,
                    "totalPurchases" to newTotalPurchases
                )

                productRef.updateChildren(updates)
                    .addOnSuccessListener {
                        completedUpdates++
                        if (completedUpdates == items.size && !hasError) {
                            onSuccess()
                        }
                    }
                    .addOnFailureListener { error ->
                        if (!hasError) {
                            hasError = true
                            onError("Failed to update product ${item.itemName}: ${error.message}")
                        }
                    }
            }.addOnFailureListener { error ->
                if (!hasError) {
                    hasError = true
                    onError("Failed to fetch product ${item.itemName}: ${error.message}")
                }
            }
        }
    }

    fun deletePurchaseOrder(
        purchaseOrderNumber: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                database.orderByChild("purchaseNumber")
                    .equalTo(purchaseOrderNumber)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val purchaseOrderSnapshot = snapshot.children.firstOrNull()
                            if (purchaseOrderSnapshot != null) {
                                database.child(purchaseOrderSnapshot.key!!)
                                    .removeValue()
                                    .addOnSuccessListener { onSuccess() }
                                    .addOnFailureListener { error ->
                                        onError(error.message ?: "Error deleting purchase order")
                                    }
                            } else {
                                onError("Purchase order not found")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            onError(error.message)
                        }
                    })
            } catch (e: Exception) {
                onError(e.message ?: "Error deleting purchase order")
            }
        }
    }
}