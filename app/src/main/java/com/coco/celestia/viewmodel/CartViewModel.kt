package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.viewmodel.model.CartData
import com.coco.celestia.viewmodel.model.ProductData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

sealed class CartState {
    object LOADING : CartState()
    object SUCCESS : CartState()
    object EMPTY : CartState()
    data class ERROR(val message: String) : CartState()
}

class CartViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("carts")
    private val _cartData = MutableLiveData<CartData>()
    private val _cartState = MutableLiveData<CartState>()
    val cartData: LiveData<CartData> = _cartData
    val cartState: LiveData<CartState> = _cartState

    fun getCart(uid: String) {
        viewModelScope.launch {
            _cartState.value = CartState.LOADING
            val query = database.child(uid)
            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val products = snapshot.children.mapNotNull {
                        it.getValue(ProductData::class.java)
                    }.toMutableList()
                    val cart = CartData(products)
                    _cartData.value = cart
                    _cartState.value = if (products.isEmpty()) CartState.EMPTY else CartState.SUCCESS
                }

                override fun onCancelled(error: DatabaseError) {
                    _cartState.value = CartState.ERROR("Error: ${error.message}")
                }
            })
        }
    }

    fun addToCart(uid: String, product: ProductData) {
        viewModelScope.launch {
            _cartState.value = CartState.LOADING
            val currentCart = _cartData.value ?: CartData()
            currentCart.items.add(product)
            _cartData.value = currentCart
            database.child(uid).setValue(currentCart.items)
                .addOnSuccessListener {
                    _cartState.value = CartState.SUCCESS
                }
                .addOnFailureListener { error ->
                    _cartState.value = CartState.ERROR("Error: ${error.message}")
                }
        }
    }
}