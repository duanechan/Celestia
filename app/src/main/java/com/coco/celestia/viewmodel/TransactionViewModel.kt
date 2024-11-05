package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.viewmodel.model.TransactionData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.protobuf.Value
import kotlinx.coroutines.launch
import kotlin.reflect.full.memberProperties

sealed class TransactionState {
    data object LOADING : TransactionState()
    data object SUCCESS : TransactionState()
    data object EMPTY : TransactionState()
    data class ERROR(val message: String) : TransactionState()
}

class TransactionViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("transactions")
    private val _transactionData = MutableLiveData<HashMap<String, List<TransactionData>>>()
    private val _transactionState = MutableLiveData<TransactionState>()
    val transactionData: LiveData<HashMap<String, List<TransactionData>>> = _transactionData
    val transactionState: LiveData<TransactionState> = _transactionState

    fun recordTransaction(uid: String, transaction: TransactionData) {
        viewModelScope.launch {
            _transactionState.value = TransactionState.LOADING
            val query = database.child(uid).push()
            query.setValue(transaction)
                .addOnCompleteListener {
                    _transactionState.value = TransactionState.SUCCESS
                }
                .addOnFailureListener { exception ->
                    _transactionState.value = TransactionState.ERROR(exception.message ?: "Unknown error")
                }
        }
    }

    fun fetchAllTransactions(filter: String = "") {
        viewModelScope.launch {
            _transactionState.value = TransactionState.LOADING
            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val filterKeywords = filter.split(",").map { it.trim() }
                    val transactionMap = hashMapOf<String, List<TransactionData>>()
                    snapshot.children.forEach { userSnapshot ->
                        val userId = userSnapshot.key.toString()
                        val transactions = userSnapshot.children.mapNotNull { transactionSnapshot ->
                            transactionSnapshot.getValue(TransactionData::class.java)
                        }.filter { transaction ->
                            val matchesFilter = filterKeywords.any { keyword ->
                                transaction::class.memberProperties.any { property ->
                                    val value = property.getter.call(transaction)?.toString() ?: ""
                                    value.contains(keyword, ignoreCase = true)
                                }
                            }
                            matchesFilter
                        }
                        if (transactions.isNotEmpty()) {
                            transactionMap[userId] = transactions
                        }
                    }
                    _transactionData.value = transactionMap
                    _transactionState.value = if (transactionMap.isEmpty()) TransactionState.EMPTY  else TransactionState.SUCCESS
                }

                override fun onCancelled(error: DatabaseError) {
                    _transactionState.value = TransactionState.ERROR(error.message)
                }

            })
        }
    }

    fun fetchTransactions(
        uid: String,
        filter: String
    ) {
        viewModelScope.launch {
            _transactionState.value = TransactionState.LOADING
            database.child(uid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val filterKeywords = filter.split(",").map { it.trim() }
                    val transactions = snapshot.children
                        .mapNotNull { it.getValue(TransactionData::class.java) }
                        .filter { product ->
                            val matches = filterKeywords.any { keyword ->
                                TransactionData::class.memberProperties.any { prop ->
                                    val value = prop.get(product)
                                    value?.toString()?.contains(keyword, ignoreCase = true) == true
                                }
                            }
                            matches
                        }
                    val transactionMap = hashMapOf(uid to transactions)

                    _transactionData.value = transactionMap
                    _transactionState.value = if (transactionMap.isEmpty()) TransactionState.EMPTY else TransactionState.SUCCESS
                }

                override fun onCancelled(error: DatabaseError) {
                    _transactionState.value = TransactionState.ERROR(error.message)
                }
            })
        }
    }
}