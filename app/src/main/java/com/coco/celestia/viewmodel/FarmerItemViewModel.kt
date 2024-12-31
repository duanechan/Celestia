package com.coco.celestia.viewmodel

import android.util.Log
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
                database.child(uid).child("items").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val items = snapshot.children.mapNotNull { itemSnapshot ->
                            itemSnapshot.getValue(ProductData::class.java)
                        }
                        Log.d("Items", items.toString())
                        _itemData.value = items
                        _itemState.value = if (items.isEmpty()) ItemState.EMPTY else ItemState.SUCCESS
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

//    fun addItem(uid: String, product: ProductData) {
//        viewModelScope.launch {
//            try {
//                _itemState.value = ItemState.LOADING
//
//                val farmerName = fetchFarmerName(uid)
//                val currentDateAdded = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
//                val updatedProduct = product.copy(dateAdded = currentDateAdded)
//
//                val productRef = database.child(uid).child("items").child(updatedProduct.name.lowercase())
//
//                productRef.addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        val existingItem = snapshot.getValue(ProductData::class.java)
//
//                        if (existingItem == null) {
//                            val itemData = hashMapOf(
//                                "farmerNames" to listOf(farmerName),
//                                "endSeason" to updatedProduct.endSeason,
//                                "dateAdded" to updatedProduct.dateAdded,
//                                "name" to updatedProduct.name,
//                                "priceKg" to updatedProduct.priceKg,
//                                "quantity" to updatedProduct.quantity,
//                                "startSeason" to updatedProduct.startSeason,
//                                "type" to updatedProduct.type
//                            )
//                            productRef.setValue(itemData)
//                        } else {
//                            val updatedFarmerNames = existingItem.farmerNames.toMutableList().apply {
//                                if (!contains(farmerName)) {
//                                    add(farmerName)
//                                }
//                            }
//
//                            val newQuantity = existingItem.quantity + updatedProduct.quantity
//                            productRef.child("quantity").setValue(newQuantity)
//                            productRef.child("farmerNames").setValue(updatedFarmerNames)
//                        }
//                        _itemState.value = ItemState.SUCCESS
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        _itemState.value = ItemState.ERROR(error.message)
//                    }
//                })
//            } catch (e: Exception) {
//                _itemState.value = ItemState.ERROR(e.message.toString())
//            }
//        }
//    }

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

                        val updatedItem = item.getValue(ProductData::class.java)?.copy(price = newPrice)
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

    fun reduceItemQuantity(item: String, quantityDeducted: Int) {
        viewModelScope.launch {
            try {
                _itemState.value = ItemState.LOADING
                val uid = FirebaseAuth.getInstance().uid.toString()
                val query = database.child(uid).child("items").child(item.lowercase()).child("quantity")

                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val oldQuantity = snapshot.getValue(Int::class.java) ?: 0
                        snapshot.ref.setValue(oldQuantity - quantityDeducted)
                            .addOnSuccessListener { _itemState.value = ItemState.SUCCESS }
                            .addOnFailureListener { _itemState.value = ItemState.ERROR("Failed to update inventory") }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        _itemState.value = ItemState.ERROR(error.message ?: "Failed to update inventory")
                    }

                })


            } catch (e: Exception) {
                _itemState.value = ItemState.ERROR(e.message ?: "Failed to update inventory")
            }
        }
    }

    fun deleteItem(productName: String) {
        viewModelScope.launch {
            try {
                _itemState.value = ItemState.LOADING
                val uid = FirebaseAuth.getInstance().uid.toString()
                database.child(uid).child("items").child(productName.lowercase()).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            snapshot.ref.removeValue()
                                .addOnSuccessListener { _itemState.value = ItemState.SUCCESS }
                                .addOnFailureListener { _itemState.value = ItemState.ERROR("Failed to delete item") }
                        } else {
                            _itemState.value = ItemState.ERROR("Item doesn't exist")
                        }
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

    fun getFarmersWithProduct(productName: String) {
        viewModelScope.launch {
            try {
                _itemState.value = ItemState.LOADING

                val farmersWithProduct = mutableListOf<Pair<String, ProductData>>()

                val snapshot = database.get().await()

                if (snapshot.exists()) {
                    for (farmerSnapshot in snapshot.children) {
                        val farmerUid = farmerSnapshot.key
                        val itemsSnapshot = farmerSnapshot.child("items")

                        if (itemsSnapshot.exists()) {
                            for (itemSnapshot in itemsSnapshot.children) {
                                val item = itemSnapshot.getValue(ProductData::class.java)
                                if (item != null && item.name.equals(productName, ignoreCase = true)) {
                                    val farmerName = fetchFarmerName(farmerUid.toString())
                                    farmersWithProduct.add(farmerName to item)
                                }
                            }
                        }
                    }

                    if (farmersWithProduct.isNotEmpty()) {
                        _itemData.value = farmersWithProduct.map { it.second }
                        _itemState.value = ItemState.SUCCESS
                    } else {
                        _itemData.value = emptyList()
                        _itemState.value = ItemState.EMPTY
                    }
                } else {
                    _itemData.value = emptyList()
                    _itemState.value = ItemState.EMPTY
                }
            } catch (e: Exception) {
                _itemState.value = ItemState.ERROR(e.message.toString())
            }
        }
    }
}