package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.viewmodel.model.Constants
import com.coco.celestia.viewmodel.model.PriceUpdate
import com.coco.celestia.viewmodel.model.ProductData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
    private val _notes = MutableLiveData<String>()
    private val _from = MutableLiveData<String>()
    private val _currentSortFilter = MutableLiveData<String>()

    val userViewModel: UserViewModel by lazy { UserViewModel() }
    val productData: LiveData<List<ProductData>> = _productData
    val productState: LiveData<ProductState> = _productState
    val productName: LiveData<String> = _productName
    val description: LiveData<String> = _description
    val vendor: LiveData<String> = _vendor
    val from: LiveData<String> = _from
    val currentSortFilter: LiveData<String> = _currentSortFilter

    private val _featuredProducts = MutableLiveData<List<ProductData>>()
    val featuredProducts: LiveData<List<ProductData>> = _featuredProducts

    private var productCount = 0

    init {
        initializeProductCount()
    }

    private fun initializeProductCount() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                val snapshot = database.get().await()
                if (snapshot.exists()) {
                    productCount = snapshot.children.maxOfOrNull { child ->
                        val productId = child.child("productId").getValue(String::class.java) ?: ""
                        productId.split("-").lastOrNull()?.toIntOrNull() ?: 0
                    } ?: 0
                }
            } catch (e: Exception) {
                productCount = 0
            }
        }
    }

    fun getProductCount(): Int = productCount

    private fun incrementProductCount() {
        productCount++
    }

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

    fun updateNotes(notes: String) {
        _notes.value = notes
    }

    fun fetchProduct(productId: String) {
        viewModelScope.launch {
            _productState.value = ProductState.LOADING
            try {
                if (productId.isBlank()) {
                    _productState.value = ProductState.EMPTY
                    return@launch
                }

                val snapshot = database.get().await()

                if (snapshot.exists()) {
                    val products = snapshot.children.mapNotNull { child ->
                        try {
                            val id = child.child("productId").getValue(String::class.java) ?: ""
                            if (id == productId) {
                                val timestamp = child.child("timestamp").getValue(String::class.java) ?: ""
                                val name = child.child("name").getValue(String::class.java) ?: ""
                                val description = child.child("description").getValue(String::class.java) ?: ""
                                val notes = child.child("notes").getValue(String::class.java) ?: ""
                                val quantity = child.child("quantity").getValue(Int::class.java) ?: 0
                                val type = child.child("type").getValue(String::class.java) ?: ""
                                val price = child.child("price").getValue(Double::class.java) ?: 0.0
                                val vendor = child.child("vendor").getValue(String::class.java) ?: ""
                                val totalPurchases = child.child("totalPurchases").getValue(Double::class.java) ?: 0.0
                                val totalQuantitySold = child.child("totalQuantitySold").getValue(Double::class.java) ?: 0.0
                                val committedStock = child.child("committedStock").getValue(Double::class.java) ?: 0.0
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

                                val priceHistory = child.child("priceHistory").children.mapNotNull { priceUpdate ->
                                    try {
                                        PriceUpdate(
                                            price = priceUpdate.child("price").getValue(Double::class.java) ?: 0.0,
                                            previousPrice = priceUpdate.child("previousPrice").getValue(Double::class.java) ?: 0.0,
                                            dateTime = priceUpdate.child("dateTime").getValue(String::class.java) ?: "",
                                            updatedBy = priceUpdate.child("updatedBy").getValue(String::class.java) ?: ""
                                        )
                                    } catch (e: Exception) {
                                        null
                                    }
                                }

                                ProductData(
                                    productId = id,
                                    timestamp = timestamp,
                                    name = name,
                                    description = description,
                                    notes = notes,
                                    quantity = quantity,
                                    type = type,
                                    price = price,
                                    vendor = vendor,
                                    totalPurchases = totalPurchases,
                                    totalQuantitySold = totalQuantitySold,
                                    committedStock = committedStock,
                                    reorderPoint = reorderPoint,
                                    weightUnit = weightUnit,
                                    isInStore = isInStore,
                                    isActive = isActive,
                                    dateAdded = dateAdded,
                                    priceHistory = priceHistory
                                )
                            } else null
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

    fun fetchProductByType(type: String) {
        viewModelScope.launch {
            _productState.value = ProductState.LOADING
            try {
                val snapshot = database.orderByChild("type").equalTo(type).get().await()
                if (snapshot.exists()) {
                    val products = snapshot.children.mapNotNull { child ->
                        try {
                            val productId = child.child("productId").getValue(String::class.java) ?: ""
                            val timestamp = child.child("timestamp").getValue(String::class.java) ?: ""
                            val name = child.child("name").getValue(String::class.java) ?: ""
                            val description = child.child("description").getValue(String::class.java) ?: ""
                            val notes = child.child("notes").getValue(String::class.java) ?: ""
                            val quantity = child.child("quantity").getValue(Int::class.java) ?: 0
                            val type = child.child("type").getValue(String::class.java) ?: ""
                            val price = child.child("price").getValue(Double::class.java) ?: 0.0
                            val vendor = child.child("vendor").getValue(String::class.java) ?: ""
                            val totalPurchases = child.child("totalPurchases").getValue(Double::class.java) ?: 0.0
                            val totalQuantitySold = child.child("totalQuantitySold").getValue(Double::class.java) ?: 0.0
                            val committedStock = child.child("committedStock").getValue(Double::class.java) ?: 0.0
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

                            ProductData(
                                productId = productId,
                                timestamp = timestamp,
                                name = name,
                                description = description,
                                notes = notes,
                                quantity = quantity,
                                type = type,
                                price = price,
                                vendor = vendor,
                                totalPurchases = totalPurchases,
                                totalQuantitySold = totalQuantitySold,
                                committedStock = committedStock,
                                reorderPoint = reorderPoint,
                                weightUnit = weightUnit,
                                isInStore = isInStore,
                                isActive = isActive,
                                dateAdded = dateAdded
                            )
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

    fun fetchProducts(
        filter: String,
        role: String
    ) {
        viewModelScope.launch {
            _productState.value = ProductState.LOADING
            try {
                val snapshot = database.orderByChild("type").get().await()
                val filterKeywords = if (filter in listOf("Price: Low to High", "Price: High to Low")) {
                    emptyList()
                } else {
                    filter.split(",").map { it.trim() }
                }

                if (snapshot.exists()) {
                    var products = snapshot.children.mapNotNull { child ->
                        try {
                            val productId = child.child("productId").getValue(String::class.java) ?: ""
                            val timestamp = child.child("timestamp").getValue(String::class.java) ?: ""
                            val name = child.child("name").getValue(String::class.java) ?: ""
                            val description = child.child("description").getValue(String::class.java) ?: ""
                            val notes = child.child("notes").getValue(String::class.java) ?: ""
                            val quantity = child.child("quantity").getValue(Long::class.java)?.toInt() ?: 0
                            val type = child.child("type").getValue(String::class.java) ?: ""
                            val price = child.child("price").getValue(Double::class.java) ?: 0.0
                            val vendor = child.child("vendor").getValue(String::class.java) ?: ""
                            val totalPurchases = child.child("totalPurchases").getValue(Double::class.java) ?: 0.0
                            val totalQuantitySold = child.child("totalQuantitySold").getValue(Double::class.java) ?: 0.0
                            val committedStock = child.child("committedStock").getValue(Double::class.java) ?: 0.0
                            val reorderPoint = child.child("reorderPoint").getValue(Double::class.java) ?: 0.0
                            val weightUnit = child.child("weightUnit").getValue(String::class.java) ?: Constants.WEIGHT_GRAMS

                            val rawInStore = child.child("inStore").getValue()
                            val isInStore = when (rawInStore) {
                                is Boolean -> rawInStore
                                is String -> rawInStore.toLowerCase() == "true"
                                else -> false
                            }

                            val rawIsActive = child.child("isActive").getValue()
                            val isActive = when (rawIsActive) {
                                is Boolean -> rawIsActive
                                is String -> rawIsActive.toLowerCase() == "true"
                                else -> true
                            }

                            val dateAdded = child.child("dateAdded").getValue(String::class.java) ?: ""

                            val product = ProductData(
                                productId = productId,
                                timestamp = timestamp,
                                name = name,
                                description = description,
                                notes = notes,
                                quantity = quantity,
                                type = type,
                                price = price,
                                vendor = vendor,
                                totalPurchases = totalPurchases,
                                totalQuantitySold = totalQuantitySold,
                                committedStock = committedStock,
                                reorderPoint = reorderPoint,
                                weightUnit = weightUnit,
                                isInStore = isInStore,
                                isActive = isActive,
                                dateAdded = dateAdded
                            )

                            val matchesFilter = if (filterKeywords.isEmpty()) {
                                true
                            } else {
                                filterKeywords.any { keyword ->
                                    product.name.contains(keyword, ignoreCase = true) ||
                                            product.productId.contains(keyword) ||
                                            product.type.contains(keyword, ignoreCase = true) ||
                                            product.description.contains(keyword, ignoreCase = true) ||
                                            product.vendor.contains(keyword, ignoreCase = true)
                                }
                            }

                            val matchesRole = when {
                                role == "Admin" -> true
                                role.startsWith("Coop") -> {
                                    val specificType = role.removePrefix("Coop")
                                    if (specificType.isBlank()) {
                                        product.type.contains("coop", ignoreCase = true)
                                    } else {
                                        product.type.contains(specificType, ignoreCase = true)
                                    }
                                }
                                else -> true
                            }

                            if (matchesFilter && matchesRole) product else null
                        } catch (e: Exception) {
                            null
                        }
                    }

                    // Apply sorting based on filter
                    products = when (filter) {
                        "Price: Low to High" -> products.sortedBy { it.price }
                        "Price: High to Low" -> products.sortedByDescending { it.price }
                        else -> products
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
            try {
                val query = database.child(product.productId)
                query.setValue(product).await()
                incrementProductCount()
                _productState.value = ProductState.SUCCESS
            } catch (exception: Exception) {
                _productState.value = ProductState.ERROR(exception.message ?: "Unknown error")
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch(Dispatchers.Main) {
            _productState.value = ProductState.LOADING
            try {
                suspendCancellableCoroutine<Unit> { continuation ->
                    val query = database.child(productId)
                    query.removeValue()
                        .addOnSuccessListener {
                            _productState.value = ProductState.SUCCESS
                            continuation.resume(Unit)
                        }
                        .addOnFailureListener { exception ->
                            _productState.value = ProductState.ERROR(exception.message ?: "Unknown error")
                            continuation.resumeWithException(exception)
                        }
                }
            } catch (e: Exception) {
                _productState.value = ProductState.ERROR(e.message ?: "Unknown error")
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
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            try {
                userViewModel.fetchUser(currentUserId)

                var attempts = 0
                while (attempts < 10 && userViewModel.userState.value !is UserState.SUCCESS) {
                    delay(100)
                    attempts++
                }

                val userName = when (userViewModel.userState.value) {
                    is UserState.SUCCESS -> {
                        val userData = userViewModel.userData.value
                        "${userData?.firstname} ${userData?.lastname}".trim()
                    }
                    else -> "Unknown User"
                }

                database.child(product.productId).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val existingProduct = snapshot.getValue(ProductData::class.java)
                            existingProduct?.let {
                                val existingPrice = it.price
                                val newPrice = product.price

                                if (existingPrice != newPrice) {
                                    val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy h:mma")
                                    val currentDateTime = LocalDateTime.now().format(formatter)

                                    val newPriceUpdate = PriceUpdate(
                                        price = newPrice,
                                        previousPrice = existingPrice,
                                        dateTime = currentDateTime,
                                        updatedBy = userName
                                    )

                                    val existingHistory = it.priceHistory
                                    val updatedHistory = existingHistory + newPriceUpdate

                                    val productWithHistory = product.copy(
                                        priceHistory = updatedHistory
                                    )

                                    updateProductWithHistory(productWithHistory)
                                } else {
                                    updateProductWithHistory(product)
                                }
                            }
                        } else {
                            updateProductWithHistory(product)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        _productState.value = ProductState.ERROR(error.message)
                    }
                })
            } catch (e: Exception) {
                _productState.value = ProductState.ERROR(e.message ?: "Unknown error")
            }
        }
    }

    private fun updateProductWithHistory(product: ProductData) {
        database.child(product.productId).setValue(product)
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

    fun updateActiveStatus(productId: String, isActive: Boolean) {
        viewModelScope.launch {
            _productState.value = ProductState.LOADING
            val query = database.child(productId).child("isActive")
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
    fun updateProductTotalSold(productName: String, soldQuantity: Int) {
        viewModelScope.launch {
            try {
                val snapshot = database.get().await()

                if (snapshot.exists()) {
                    val productSnapshot = snapshot.children.find { child ->
                        child.child("name").getValue(String::class.java) == productName
                    }

                    if (productSnapshot != null) {
                        val currentTotalSold = productSnapshot.child("totalQuantitySold").getValue(Double::class.java) ?: 0.0
                        val currentTotalPurchases = productSnapshot.child("totalPurchases").getValue(Double::class.java) ?: 0.0

                        val price = productSnapshot.child("price").getValue(Double::class.java) ?: 0.0
                        val newTotalSold = currentTotalSold + soldQuantity
                        val newTotalPurchases = currentTotalPurchases + (soldQuantity * price)

                        val updates = hashMapOf<String, Any>(
                            "totalQuantitySold" to newTotalSold,
                            "totalPurchases" to newTotalPurchases
                        )

                        productSnapshot.ref.updateChildren(updates).await()

                        _productState.value = ProductState.SUCCESS
                        fetchProducts("", "")
                    } else {
                        _productState.value = ProductState.ERROR("Product not found")
                    }
                } else {
                    _productState.value = ProductState.ERROR("No products exist in database")
                }
            } catch (e: Exception) {
                _productState.value = ProductState.ERROR(e.message ?: "Failed to update total sales")
            }
        }
    }
    fun checkDuplicateProduct(productName: String, isInStore: Boolean): Boolean {
        val products = productData.value ?: emptyList()
        return products.any {
            it.name.equals(productName, ignoreCase = true) &&
                    it.isInStore == isInStore &&
                    it.isActive
        }
    }
}