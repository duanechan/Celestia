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
import kotlin.reflect.full.memberProperties

sealed class TransactionState {
    object LOADING : TransactionState()
    object SUCCESS : TransactionState()
    object EMPTY : TransactionState()
    data class ERROR(val message: String) : TransactionState()
}

class TransactionViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("transactions")
    private val _transactionData = MutableLiveData<List<TransactionData>>()
    private val _transactionState = MutableLiveData<TransactionState>()
    val transactionData: LiveData<List<TransactionData>> = _transactionData
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

                    _transactionData.value = transactions
                    _transactionState.value = if (transactions.isEmpty()) TransactionState.EMPTY else TransactionState.SUCCESS
                }

                override fun onCancelled(error: DatabaseError) {
                    _transactionState.value = TransactionState.ERROR(error.message)
                }
            })
        }
    }
}