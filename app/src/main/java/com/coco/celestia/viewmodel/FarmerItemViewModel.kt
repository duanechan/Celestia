package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.viewmodel.model.ItemData
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.UserData
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
    private val usersDatabase: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
    private val _itemData = MutableLiveData<List<ProductData>>()
    private val _itemState = MutableLiveData<ItemState>()
    val itemData: LiveData<List<ProductData>> = _itemData
    val itemState: LiveData<ItemState> = _itemState

    suspend fun fetchFarmerName(uid: String): String {
        val userSnapshot = usersDatabase.child(uid).get().await()
        return if (userSnapshot.exists()) {
            val userData = userSnapshot.getValue(UserData::class.java)
            userData?.let { "${it.firstname} ${it.lastname}" } ?: ""
        } else {
            ""
        }
    }

    fun getItems(uid: String) {
        viewModelScope.launch {
            try {
                _itemState.value = ItemState.LOADING

                val farmerName = fetchFarmerName(uid)
                println("Fetching items for farmer: $farmerName")

                val itemsSnapshot = database.child(uid).child("items").get().await()
                if (itemsSnapshot.exists()) {
                    val list = mutableListOf<ProductData>()
                    for (item in itemsSnapshot.children) {
                        val itemData = item.getValue(ProductData::class.java)
                        if (itemData != null) {
                            list.add(itemData)
                        }
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

                val farmerName = fetchFarmerName(uid)

                val productRef = database.child(uid).child("items").child(product.name.lowercase())

                productRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val existingItem = snapshot.getValue(ProductData::class.java)

                        if (existingItem == null) {
                            val itemData = hashMapOf(
                                "farmerName" to farmerName,
                                "endSeason" to product.endSeason,
                                "name" to product.name,
                                "priceKg" to product.priceKg,
                                "quantity" to product.quantity,
                                "startSeason" to product.startSeason,
                                "type" to product.type
                            )
                            productRef.setValue(itemData)
                        } else {
                            val newQuantity = existingItem.quantity + product.quantity
                            productRef.child("quantity").setValue(newQuantity)
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
    fun reduceItemQuantity(item: ItemData, totalFarmers: Int) {
        viewModelScope.launch {
            try {
                _itemState.value = ItemState.LOADING

                var itemFound = false
                val uid = FirebaseAuth.getInstance().uid.toString()
                val snapshot = database.child(uid).child("items").get().await()
                val orderedQuantity = item.items.firstOrNull()?.quantity ?: 0
                val baseReductionAmount = orderedQuantity / totalFarmers
                val remainder = orderedQuantity % totalFarmers

                for ((index, itemNode) in snapshot.children.withIndex()) {
                    val name = itemNode.child("name").getValue(String::class.java)

                    if (name?.equals(item.name, ignoreCase = true) == true) {
                        val currentQuantity = itemNode.child("quantity").getValue(Int::class.java) ?: 0
                        val reductionAmount = if (index == totalFarmers - 1) {
                            baseReductionAmount + remainder
                        } else {
                            baseReductionAmount
                        }

                        val newQuantity = (currentQuantity - reductionAmount).coerceAtLeast(0)
                        itemNode.child("quantity").ref.setValue(newQuantity).await()

                        val updatedItem = itemNode.getValue(ProductData::class.java)?.copy(quantity = newQuantity)
                        updatedItem?.let {
                            _itemData.value = _itemData.value?.map { existingItem ->
                                if (existingItem.name.equals(item.name, ignoreCase = true)) updatedItem else existingItem
                            }?.toList()
                        }

                        itemFound = true
                    }
                }

                if (!itemFound) {
                    val availableItems = snapshot.children.mapNotNull {
                        it.child("name").getValue(String::class.java)
                    }.joinToString(", ")

                    _itemState.value = ItemState.ERROR("Product ${item.name} not found in inventory")
                } else {
                    getItems(uid)
                }

            } catch (e: Exception) {
                _itemState.value = ItemState.ERROR(e.message ?: "Failed to update inventory")
            }
        }
    }
}