package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.viewmodel.model.PurchaseOrder
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
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
    private val _purchaseOrderData = MutableLiveData<List<PurchaseOrder>>()
    private val _purchaseOrderState = MutableLiveData<PurchaseOrderState>()
    val purchaseOrderData: LiveData<List<PurchaseOrder>> = _purchaseOrderData
    val purchaseOrderState: LiveData<PurchaseOrderState> = _purchaseOrderState

    fun fetchPurchaseOrders(filter: String = "all", searchQuery: String = "", facilityName: String? = null) {
        viewModelScope.launch {
            _purchaseOrderState.value = PurchaseOrderState.LOADING

            database.addValueEventListener(object : ValueEventListener {
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
            })
        }
    }

    suspend fun getPurchaseOrder(purchaseNumber: String): PurchaseOrder? {
        return suspendCoroutine { continuation ->
            database.orderByChild("purchaseNumber")
                .equalTo(purchaseNumber)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val order = snapshot.children.firstOrNull()?.let {
                            val purchaseOrder = it.getValue(PurchaseOrder::class.java)
                            purchaseOrder?.copy(id = it.key ?: "")
                        }
                        continuation.resume(order)
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
                if (purchaseOrder.id.isNotEmpty()) {
                    database.child(purchaseOrder.id).setValue(purchaseOrder)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onError(it.message ?: "Error updating purchase order") }
                } else {
                    val newPurchaseOrderRef = database.push()
                    newPurchaseOrderRef.setValue(purchaseOrder)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onError(it.message ?: "Error adding purchase order") }
                }
            } catch (e: Exception) {
                onError(e.message ?: "Error processing purchase order")
            }
        }
    }

    fun updatePurchaseOrderStatus(
        purchaseOrderNumber: String,
        newStatus: String,
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
                                // Get the purchase order and update its status
                                val purchaseOrder = purchaseOrderSnapshot.getValue(PurchaseOrder::class.java)
                                purchaseOrder?.let {
                                    val updatedOrder = it.copy(status = newStatus)
                                    database.child(purchaseOrderSnapshot.key!!)
                                        .setValue(updatedOrder)
                                        .addOnSuccessListener { onSuccess() }
                                        .addOnFailureListener { error ->
                                            onError(error.message ?: "Error updating purchase order status")
                                        }
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
                onError(e.message ?: "Error updating purchase order status")
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
                                // Delete the found purchase order
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