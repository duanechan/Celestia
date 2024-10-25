package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.viewmodel.model.ContactData
import com.coco.celestia.viewmodel.model.ItemData
import com.coco.celestia.viewmodel.model.ProductData
import com.google.firebase.auth.FirebaseAuth
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
                val query = database.child(uid).child("items").child(product.name.lowercase())
                query.addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val item = snapshot.getValue(ProductData::class.java)
                        if (item == null) {
                            query.setValue(product)
                        } else {
                            query.child("quantity").setValue(item.quantity + product.quantity)
                        }
                        _itemState.value = ItemState.SUCCESS
                    }

                    override fun onCancelled(error: DatabaseError) {
                        _itemState.value = ItemState.ERROR(error.message)
                    }
                })
            } catch (e: Exception) {
                _itemState.value = ItemState.ERROR(e.message.toString())
            }
        }
    }
    fun updateItemQuantity(itemName: String, quantity: Int) {
        viewModelScope.launch {
            try {
                _itemState.value = ItemState.LOADING
                val uid = FirebaseAuth.getInstance().uid.toString()
                val snapshot = database.child(uid).child("items").get().await()
                var itemFound = false
                for (item in snapshot.children) {
                    val name = item.child("name").getValue(String::class.java)

                    if (name.equals(itemName, ignoreCase = true)) {
                        val currentQuantity = item.child("quantity").getValue(Int::class.java) ?: 0
                        val newQuantity = currentQuantity + quantity

                        item.child("quantity").ref.setValue(newQuantity).await()

                        val updatedItem = item.getValue(ProductData::class.java)?.copy(quantity = newQuantity)
                        updatedItem?.let {
                            _itemData.value = _itemData.value?.map { existingItem ->
                                if (existingItem.name.equals(itemName, ignoreCase = true)) updatedItem else existingItem
                            }?.toList()
                        }
                        itemFound = true
                        break
                    }
                }
                if (!itemFound) {
                    _itemState.value = ItemState.ERROR("Item not found")
                } else {
                    getItems(uid)
                }
            } catch (e: Exception) {
                _itemState.value = ItemState.ERROR(e.message ?: "Error updating item quantity")
            }
        }
    }
    fun updateItemPrice(itemName: String, newPrice: Double) {
        viewModelScope.launch {
            try {
                _itemState.value = ItemState.LOADING
                val uid = FirebaseAuth.getInstance().uid.toString()
                val snapshot = database.child(uid).child("items").get().await()
                var itemFound = false
                for (item in snapshot.children) {
                    val name = item.child("name").getValue(String::class.java)

                    if (name.equals(itemName, ignoreCase = true)) {
                        item.child("priceKg").ref.setValue(newPrice).await()

                        val updatedItem = item.getValue(ProductData::class.java)?.copy(priceKg = newPrice)
                        updatedItem?.let {
                            _itemData.value = _itemData.value?.map { existingItem ->
                                if (existingItem.name.equals(itemName, ignoreCase = true)) updatedItem else existingItem
                            }?.toList()
                        }
                        itemFound = true
                        break
                    }
                }
                if (!itemFound) {
                    _itemState.value = ItemState.ERROR("Item not found")
                } else {
                    getItems(uid)
                }
            } catch (e: Exception) {
                _itemState.value = ItemState.ERROR(e.message ?: "Error updating item price")
            }
        }
    }
}