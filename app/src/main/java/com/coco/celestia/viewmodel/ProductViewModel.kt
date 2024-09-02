package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.ProductData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class ProductState {
    object LOADING : ProductState()
    object SUCCESS : ProductState()
    object EMPTY : ProductState()
    data class ERROR(val message: String) : ProductState()
}

class ProductViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("products")
    private val _productData = MutableLiveData<List<ProductData>>()
    private val _productState = MutableLiveData<ProductState>()
    val productData: LiveData<List<ProductData>> = _productData
    val productState: LiveData<ProductState> = _productState

    fun fetchProduct(type: String) {
        viewModelScope.launch {
            _productState.value = ProductState.LOADING
            try {
                val snapshot = database.orderByChild("type").equalTo(type).get().await()
                if (snapshot.exists()) {
                    val products = snapshot.children.mapNotNull { it.getValue(ProductData::class.java) }
                    _productData.value = products
                    _productState.value = if (products.isEmpty()) ProductState.EMPTY else ProductState.SUCCESS
                } else {
                    _productData.value = emptyList()
                    _productState.value = ProductState.EMPTY
                }
            } catch(e: Exception) {
                _productState.value = ProductState.ERROR(e.message ?: "Unknown error")
            }
        }
    }

    fun fetchProducts() {
        viewModelScope.launch {
            _productState.value = ProductState.LOADING
            try {
                val snapshot = database.orderByChild("type").get().await()
                if (snapshot.exists()) {
                    val products = snapshot.children.mapNotNull { it.getValue(ProductData::class.java) }
                    _productData.value = products
                    _productState.value = if (products.isEmpty()) ProductState.EMPTY else ProductState.SUCCESS
                } else {
                    _productData.value = emptyList()
                    _productState.value = ProductState.EMPTY
                }
            } catch(e: Exception) {
                _productState.value = ProductState.ERROR(e.message ?: "Unknown error")
            }
        }
    }
}
