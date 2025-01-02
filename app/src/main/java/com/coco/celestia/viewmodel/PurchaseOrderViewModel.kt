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
                        .mapNotNull { it.getValue(PurchaseOrder::class.java) }
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
                                "draft" -> true
                                "approved" -> true
                                else -> true
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

    fun addPurchaseOrder(
        purchaseOrder: PurchaseOrder,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val newPurchaseOrderRef = database.push()
                newPurchaseOrderRef.setValue(purchaseOrder)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Error adding purchase order") }
            } catch (e: Exception) {
                onError(e.message ?: "Error adding purchase order")
            }
        }
    }

    // TODO: Implement this when there is already the purchase order details screen
    fun updatePurchaseOrder(
        purchaseOrderId: String,
        purchaseOrder: PurchaseOrder,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                database.child(purchaseOrderId).setValue(purchaseOrder)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Error updating purchase order") }
            } catch (e: Exception) {
                onError(e.message ?: "Error updating purchase order")
            }
        }
    }

    // TODO: Implement this when there is already the purchase order details screen
    fun deletePurchaseOrder(
        purchaseOrderId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                database.child(purchaseOrderId).removeValue()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Error deleting purchase order") }
            } catch (e: Exception) {
                onError(e.message ?: "Error deleting purchase order")
            }
        }
    }
}