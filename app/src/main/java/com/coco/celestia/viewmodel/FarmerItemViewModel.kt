package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.viewmodel.model.ContactData
import com.coco.celestia.viewmodel.model.ItemData
import com.coco.celestia.viewmodel.model.ProductData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class ItemState {
    data object LOADING : ItemState()
    data object SUCCESS : ItemState()
    data object EMPTY : ItemState()
    data class ERROR(val message: String) : ItemState()
}

class FarmerItemViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("farmer_items")
    private val _itemData = MutableLiveData<List<ProductData>>()
    private val _itemState = MutableLiveData<ItemState>()
    val itemData: LiveData<List<ProductData>> = _itemData
    val itemState: LiveData<ItemState> = _itemState

    fun getItems(uid: String) {
        viewModelScope.launch {
            try {
                _itemState.value = ItemState.LOADING
                 val snapshot = database.child(uid).child("items").get().await()
                if (snapshot.exists()) {
                    val list = mutableListOf<ProductData>()
                    for (item in snapshot.children) {
                        val itemData = item.getValue(ProductData::class.java)
                        list.add(itemData!!)
                    }
                    _itemData.value = list
                    _itemState.value = ItemState.SUCCESS
                } else {
                    _itemData.value = emptyList()
                    _itemState.value = ItemState.EMPTY
                }
            } catch (e: Exception) {
                _itemState.value = ItemState.ERROR(e.message.toString())
            }
        }
    }

    fun addItem(uid: String, product: ProductData) {
        viewModelScope.launch {
            try {
                _itemState.value = ItemState.LOADING
                val query = database.child(uid).child("items").push()
                query.setValue(product)
                    .addOnSuccessListener {
                        _itemState.value = ItemState.SUCCESS
                    }
                    .addOnFailureListener {
                        _itemState.value = ItemState.ERROR("Error: Failed to add product.")
                    }
            } catch (e: Exception) {
                _itemState.value = ItemState.ERROR(e.message.toString())
            }
        }
    }
}