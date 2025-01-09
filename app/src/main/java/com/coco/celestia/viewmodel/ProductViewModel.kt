package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.viewmodel.model.Constants
import com.coco.celestia.viewmodel.model.ProductData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.reflect.full.memberProperties

sealed class ProductState {
    data object LOADING : ProductState()
    data object SUCCESS : ProductState()
    data object EMPTY : ProductState()
    data class ERROR(val message: String) : ProductState()
}

class ProductViewModel : ViewModel() {
    private val database: DatabaseReference =
        FirebaseDatabase.getInstance().getReference("products")
    private val _productData = MutableLiveData<List<ProductData>>()
    private val _productState = MutableLiveData<ProductState>()
    private val _productName = MutableLiveData<String>()
    private val _description = MutableLiveData<String>()
    private val _vendor = MutableLiveData<String>()
    private val _from = MutableLiveData<String>()

    val productData: LiveData<List<ProductData>> = _productData
    val productState: LiveData<ProductState> = _productState
    val productName: LiveData<String> = _productName
    val description: LiveData<String> = _description
    val vendor: LiveData<String> = _vendor
    val from: LiveData<String> = _from

    private val _featuredProducts = MutableLiveData<List<ProductData>>()
    val featuredProducts: LiveData<List<ProductData>> = _featuredProducts

    fun onProductNameChange(newProductName: String) {
        _productName.value = newProductName
        _from.value = when (newProductName) {
            "Green Beans" -> "--"
            "Sorted Beans" -> "Green Beans"
            "Roasted Beans" -> "Sorted Beans"
            "Packaged Beans" -> "Roasted Beans"
            else -> ""
        }
    }

    fun updateProductName(newName: String) {
        _productName.value = newName
    }

    fun updateDescription(newDescription: String) {
        _description.value = newDescription
    }

    fun updateVendor(newVendor: String) {
        _vendor.value = newVendor
    }

    fun fetchProduct(productName: String) {
        viewModelScope.launch {
            _productState.value = ProductState.LOADING
            try {
                if (productName.isBlank()) {
                    _productState.value = ProductState.EMPTY
                    return@launch
                }

                val snapshot = database.get().await()

                if (snapshot.exists()) {
                    val products = snapshot.children.mapNotNull { child ->
                        try {
                            val name = child.child("name").getValue(String::class.java) ?: ""
                            if (name == productName) {
                                val description = child.child("description").getValue(String::class.java) ?: ""
                                val quantity = child.child("quantity").getValue(Int::class.java) ?: 0
                                val productType = child.child("type").getValue(String::class.java) ?: ""
                                val price = child.child("price").getValue(Double::class.java) ?: 0.0
                                val vendor = child.child("vendor").getValue(String::class.java) ?: ""
                                val purchasingCost = child.child("purchasingCost").getValue(Double::class.java) ?: 0.0
                                val openingStock = child.child("openingStock").getValue(Double::class.java) ?: 0.0
                                val reorderPoint = child.child("reorderPoint").getValue(Double::class.java) ?: 0.0
                                val weightUnit = child.child("weightUnit").getValue(String::class.java) ?: Constants.WEIGHT_GRAMS

                                val rawInStore = child.child("inStore").getValue()
                                val isInStore = when (rawInStore) {
                                    is Boolean -> rawInStore
                                    is String -> rawInStore.toLowerCase() == "true"
                                    else -> false
                                }

                                val isActive = child.child("isActive").getValue(Boolean::class.java) ?: true
                                val dateAdded = child.child("dateAdded").getValue(String::class.java) ?: ""
                                val collectionMethod = child.child("collectionMethod").getValue(String::class.java) ?: Constants.COLLECTION_PICKUP
                                val paymentMethod = child.child("paymentMethod").getValue(String::class.java) ?: Constants.PAYMENT_CASH

                                ProductData(
                                    name = name,
                                    description = description,
                                    quantity = quantity,
                                    type = productType,
                                    price = price,
                                    vendor = vendor,
                                    purchasingCost = purchasingCost,
                                    openingStock = openingStock,
                                    reorderPoint = reorderPoint,
                                    weightUnit = weightUnit,
                                    isInStore = isInStore,
                                    isActive = isActive,
                                    dateAdded = dateAdded,
                                    collectionMethod = collectionMethod,
                                    paymentMethod = paymentMethod
                                )
                            } else null
                        } catch (e: Exception) {
                            null
                        }
                    }

                    _productData.value = products
                    _productState.value = if (products.isEmpty()) {
                        ProductState.EMPTY
                    } else {
                        ProductState.SUCCESS
                    }
                } else {
                    _productData.value = emptyList()
                    _productState.value = ProductState.EMPTY
                }
            } catch (e: Exception) {
                _productState.value = ProductState.ERROR(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Fetches product data from the database based on the provided type.
     *
     * This function fetches product data from the database based on the provided type.
     *
     * @param type The type of product to fetch.
     */
    fun fetchProductByType(type: String) {
        viewModelScope.launch {
            _productState.value = ProductState.LOADING
            try {
                val snapshot = database.orderByChild("type").equalTo(type).get().await()
                if (snapshot.exists()) {
                    val products = snapshot.children.mapNotNull { child ->
                        try {
                            val name = child.child("name").getValue(String::class.java) ?: ""
                            val description = child.child("description").getValue(String::class.java) ?: ""
                            val quantity = child.child("quantity").getValue(Int::class.java) ?: 0
                            val productType = child.child("type").getValue(String::class.java) ?: ""
                            val price = child.child("price").getValue(Double::class.java) ?: 0.0
                            val vendor = child.child("vendor").getValue(String::class.java) ?: ""
                            val purchasingCost = child.child("purchasingCost").getValue(Double::class.java) ?: 0.0
                            val openingStock = child.child("openingStock").getValue(Double::class.java) ?: 0.0
                            val reorderPoint = child.child("reorderPoint").getValue(Double::class.java) ?: 0.0
                            val weightUnit = child.child("weightUnit").getValue(String::class.java) ?: Constants.WEIGHT_GRAMS
                            val isInStore = child.child("inStore").getValue(Boolean::class.java) ?: true
                            val isActive = child.child("isActive").getValue(Boolean::class.java) ?: true
                            val dateAdded = child.child("dateAdded").getValue(String::class.java) ?: ""
                            val collectionMethod = child.child("collectionMethod").getValue(String::class.java) ?: Constants.COLLECTION_PICKUP
                            val paymentMethod = child.child("paymentMethod").getValue(String::class.java) ?: Constants.PAYMENT_CASH

                            ProductData(
                                name = name,
                                description = description,
                                quantity = quantity,
                                type = productType,
                                price = price,
                                vendor = vendor,
                                purchasingCost = purchasingCost,
                                openingStock = openingStock,
                                reorderPoint = reorderPoint,
                                weightUnit = weightUnit,
                                isInStore = isInStore,
                                isActive = isActive,
                                dateAdded = dateAdded,
                                collectionMethod = collectionMethod,
                                paymentMethod = paymentMethod
                            ).also {
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _productData.value = products
                    _productState.value = if (products.isEmpty()) ProductState.EMPTY else ProductState.SUCCESS
                } else {
                    _productData.value = emptyList()
                    _productState.value = ProductState.EMPTY
                }
            } catch (e: Exception) {
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
                        val productType = product.type.lowercase()

                        // Apply keyword filtering
                        val filtered = filterKeywords.any { keyword ->
                            ProductData::class.memberProperties.any { prop ->
                                val value = prop.get(product)
                                value?.toString()?.contains(keyword, ignoreCase = true) == true
                            }
                        }

                        when {
                            // Admin can see all products
                            role == "Admin" -> filtered

                            // Coop users can see their specific product types
                            role.startsWith("Coop") -> {
                                val specificType = role.removePrefix("Coop")
                                if (specificType.isBlank()) {
                                    // General Coop user can see all Coop products
                                    productType.contains("coop") && filtered
                                } else {
                                    // Specific Coop user can only see their product type
                                    productType.contains(specificType.lowercase()) && filtered
                                }
                            }

                            // Default user can see all products
                            else -> filtered
                        }
                    }

                    _productData.value = products
                    _productState.value = if (products.isEmpty()) ProductState.EMPTY else ProductState.SUCCESS
                } else {
                    _productData.value = emptyList()
                    _productState.value = ProductState.EMPTY
                }
            } catch (e: Exception) {
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
                        _productState.value =
                            ProductState.ERROR(task.exception?.message ?: "Unknown error")
                    }
                }
                .addOnFailureListener { exception ->
                    _productState.value = ProductState.ERROR(exception.message ?: "Unknown error")
                }
        }
    }

    fun deleteProduct (product: String) {
        viewModelScope.launch {
            _productState.value = ProductState.LOADING
            val query = database.child(product.lowercase())
            query.removeValue()
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

    fun updateProductQuantity(productName: String, quantity: Int) {
        viewModelScope.launch {
            try {
                val snapshot = database.get().await()
                var productFound = false
                for (product in snapshot.children) {
                    val name = product.child("name").getValue(String::class.java)

                    if (name == productName) {
                        val currentQuantity =
                            product.child("quantity").getValue(Int::class.java) ?: 0
                        val newQuantity = currentQuantity + quantity

                        product.child("quantity").ref.setValue(newQuantity).await()

                        productFound = true
                        break
                    }
                }
                if (!productFound) {
                    _productState.value = ProductState.ERROR("Product not found")
                } else {
                    fetchProducts(filter = "", role = "Farmer")
                }
            } catch (e: Exception) {
                _productState.value =
                    ProductState.ERROR(e.message ?: "Error updating product quantity")
            }
        }
    }

    fun updateFromProduct(productName: String, quantity: Int) {
        viewModelScope.launch {
            try {
                val snapshot = database.get().await()
                var productFound = false
                for (product in snapshot.children) {
                    onProductNameChange(productName)
                    val name = product.child("name").getValue(String::class.java)
                    if (name == _from.value) {
                        val fromCurrentQuantity =
                            product.child("quantity").getValue(Int::class.java) ?: 0
                        val fromNewQuantity = fromCurrentQuantity - quantity
                        product.child("quantity").ref.setValue(fromNewQuantity).await()
                        productFound = true
                        break
                    }
                }
                if (!productFound) {
                    _productState.value = ProductState.ERROR("Product not found")
                } else {
                    fetchProducts(filter = "", role = "Farmer")
                }
            } catch (e: Exception) {
                _productState.value =
                    ProductState.ERROR(e.message ?: "Error updating product quantity")
            }
        }
    }

    fun updateProductPrice(productName: String, price: Double) {
        viewModelScope.launch {
            try {
                val snapshot = database.get().await()
                var productFound = false
                for (product in snapshot.children) {
                    val name = product.child("name").getValue(String::class.java)

                    if (name == productName) {
                        product.child("priceKg").ref.setValue(price).await()
                        productFound = true
                        break
                    }
                }
                if (!productFound) {
                    _productState.value = ProductState.ERROR("Product not found")
                }
            } catch (e: Exception) {
                _productState.value =
                    ProductState.ERROR(e.message ?: "Error updating product quantity")
            }
        }
    }

    fun fetchFeaturedProducts() {
        viewModelScope.launch {
            _productState.value = ProductState.LOADING
            try {
                val snapshot = database.get().await()
                val allProducts =
                    snapshot.children.mapNotNull { it.getValue(ProductData::class.java) }

                val meatProducts = allProducts.filter { it.type.equals("Meat", ignoreCase = true) }
                val vegetableProducts =
                    allProducts.filter { it.type.equals("Vegetable", ignoreCase = true) }
                val coffeeProducts =
                    allProducts.filter { it.type.equals("Coffee", ignoreCase = true) }

                // Randomizer for coffee and vegetables, taking only 2 from each
                val randomVegetables = vegetableProducts.shuffled().take(2)
                val randomCoffee = coffeeProducts.shuffled().take(2)
                val randomMeat = meatProducts.shuffled().take(2)

                // combine featured products
                val featured = mutableListOf<ProductData>()
                featured.addAll(randomMeat)
                featured.addAll(randomVegetables)
                featured.addAll(randomCoffee)

                // shuffle the final list to randomize order
                _featuredProducts.value = featured.shuffled().take(6)
                _productState.value =
                    if (featured.isEmpty()) ProductState.EMPTY else ProductState.SUCCESS
            } catch (e: Exception) {
                _productState.value = ProductState.ERROR(e.message ?: "Unknown error")
            }
        }
    }

    fun getProductByName(productName: String): ProductData? {
        return _productData.value?.find { it.name == productName }
    }

    fun updateProduct(product: ProductData) {
        viewModelScope.launch {
            _productState.value = ProductState.LOADING
            val query = database.child(product.name.lowercase())
            query.setValue(product)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _productState.value = ProductState.SUCCESS
                    } else {
                        _productState.value =
                            ProductState.ERROR(task.exception?.message ?: "Unknown error")
                    }
                }
                .addOnFailureListener { exception ->
                    _productState.value = ProductState.ERROR(exception.message ?: "Unknown error")
                }
        }
    }

    fun updateActiveStatus(productName: String, isActive: Boolean) {
        viewModelScope.launch {
            _productState.value = ProductState.LOADING
            val query = database.child(productName.lowercase()).child("isActive")
            query.setValue(isActive)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _productState.value = ProductState.SUCCESS
                    } else {
                        _productState.value =
                            ProductState.ERROR(task.exception?.message ?: "Unknown error")
                    }
                }
                .addOnFailureListener { exception ->
                    _productState.value = ProductState.ERROR(exception.message ?: "Unknown error")
                }
        }
    }
}