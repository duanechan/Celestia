package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.viewmodel.model.SalesData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

sealed class SalesState {
    data object LOADING : SalesState()
    data object SUCCESS : SalesState()
    data object EMPTY : SalesState()
    data class ERROR(val message: String) : SalesState()
}

class SalesViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("sales")
    private val _salesData = MutableLiveData<List<SalesData>>()
    private val _salesState = MutableLiveData<SalesState>()
    val salesData: LiveData<List<SalesData>> = _salesData
    val salesState: LiveData<SalesState> = _salesState

    private var currentListener: ValueEventListener? = null

    fun fetchSales(
        facility: String,
        dateRange: Pair<String, String>? = null,
        searchQuery: String = "",
        minPrice: Double? = null,
        maxPrice: Double? = null
    ) {
        viewModelScope.launch {
            _salesState.value = SalesState.LOADING

            currentListener?.let {
                database.removeEventListener(it)
            }

            val query = database.orderByChild("facility").equalTo(facility)

            currentListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val searchKeywords = searchQuery.split(" ").map { it.trim() }
                    val sales = snapshot.children
                        .mapNotNull { childSnapshot ->
                            try {
                                val data = childSnapshot.value as? Map<*, *>
                                if (data != null) {
                                    SalesData(
                                        salesNumber = childSnapshot.key ?: "",
                                        productName = data["productName"] as? String ?: "",
                                        quantity = (data["quantity"] as? Long)?.toInt() ?: 0,
                                        price = (data["price"] as? Number)?.toDouble() ?: 0.0,
                                        date = data["date"] as? String ?: "",
                                        notes = data["notes"] as? String ?: "",
                                        facility = data["facility"] as? String ?: ""
                                    )
                                } else {
                                    childSnapshot.getValue(SalesData::class.java)
                                }
                            } catch (e: Exception) {
                                null
                            }
                        }
                        .filter { sale ->
                            if (dateRange != null) {
                                val (startDate, endDate) = dateRange
                                sale.date in startDate..endDate
                            } else true
                        }
                        .filter { sale ->
                            if (searchQuery.isNotEmpty()) {
                                searchKeywords.any { keyword ->
                                    sale.productName.contains(keyword, ignoreCase = true) ||
                                            sale.notes.contains(keyword, ignoreCase = true)
                                }
                            } else true
                        }
                        .filter { sale ->
                            if (minPrice != null) {
                                sale.price >= minPrice
                            } else true
                        }
                        .filter { sale ->
                            if (maxPrice != null) {
                                sale.price <= maxPrice
                            } else true
                        }

                    _salesData.value = sales
                    _salesState.value = if (sales.isEmpty()) SalesState.EMPTY else SalesState.SUCCESS
                }

                override fun onCancelled(error: DatabaseError) {
                    _salesState.value = SalesState.ERROR(error.message)
                }
            }

            query.addValueEventListener(currentListener!!)
        }
    }

    fun fetchSaleById(salesId: String, onComplete: (SalesData?) -> Unit) {
        database.child(salesId).get()
            .addOnSuccessListener { snapshot ->
                try {
                    val data = snapshot.value as? Map<*, *>
                    if (data != null) {
                        val sale = SalesData(
                            salesNumber = snapshot.key ?: "",
                            productName = data["productName"] as? String ?: "",
                            quantity = (data["quantity"] as? Long)?.toInt() ?: 0,
                            price = (data["price"] as? Number)?.toDouble() ?: 0.0,
                            date = data["date"] as? String ?: "",
                            notes = data["notes"] as? String ?: "",
                            facility = data["facility"] as? String ?: ""
                        )
                        onComplete(sale)
                    } else {
                        val sale = snapshot.getValue(SalesData::class.java)
                        onComplete(sale)
                    }
                } catch (e: Exception) {
                    onComplete(null)
                }
            }
            .addOnFailureListener {
                onComplete(null)
            }
    }

    fun addSale(sale: SalesData, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val newSaleRef = database.push()
        val saleWithId = sale.copy(salesNumber = newSaleRef.key ?: "")

        newSaleRef.setValue(saleWithId)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to add sale") }
    }

    fun updateSale(sale: SalesData, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (sale.salesNumber.isEmpty()) {
            onError("Invalid sale ID")
            return
        }

        database.child(sale.salesNumber).setValue(sale)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to update sale") }
    }

    fun deleteSale(salesId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        database.child(salesId).removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Failed to delete sale") }
    }

    override fun onCleared() {
        super.onCleared()
        currentListener?.let {
            database.removeEventListener(it)
        }
    }
}