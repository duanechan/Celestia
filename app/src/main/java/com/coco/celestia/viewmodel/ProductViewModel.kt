package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    /**
     * Fetches product data from the database based on the provided type.
     *
     * This function fetches product data from the database based on the provided type.
     *
     * @param type The type of product to fetch.
     */
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

    /**
     * Fetches all product data from the database.
     *
     * This function fetches all product data from the database.
     */
    fun fetchProducts(
        filter: String,
        role: String
    ) {
        viewModelScope.launch {
            _productState.value = ProductState.LOADING
            try {
                val snapshot = database.orderByChild("type").get().await()
                val filterKeywords = filter.split(",").map { it.trim() }
                if (snapshot.exists()) {
                    val products = snapshot.children.mapNotNull {
                        it.getValue(ProductData::class.java)
                    }.filter { product ->
                        val coffeeOrMeat = product.type.equals("Coffee", ignoreCase = true) ||
                                product.type.equals("Meat", ignoreCase = true)
                        val vegetable = product.type.equals("Vegetable", ignoreCase = true)
                        val filtered = filterKeywords.any { keyword ->
                            product.name.contains(keyword, ignoreCase = true)
                        }
                        when (role) {
                            "Coop" -> coffeeOrMeat && filtered
                            "Farmer" -> vegetable && filtered
                            "Admin" -> filtered
                            else -> filtered
                        }
                    }
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

    fun addProduct(product: ProductData) {
        viewModelScope.launch {
            _productState.value = ProductState.LOADING
            val query = database.child(product.name.lowercase())
            query.setValue(product)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _productState.value = ProductState.SUCCESS
                    } else {
                        _productState.value = ProductState.ERROR(task.exception?.message ?: "Unknown error")
                    }
                }
                .addOnFailureListener { exception ->
                    _productState.value = ProductState.ERROR(exception.message ?: "Unknown error")
                }
        }
    }
}
