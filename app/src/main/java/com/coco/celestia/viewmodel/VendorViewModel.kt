package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.viewmodel.model.VendorData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlin.reflect.full.memberProperties

sealed class VendorState {
    data object LOADING : VendorState()
    data object SUCCESS : VendorState()
    data object EMPTY : VendorState()
    data class ERROR(val message: String) : VendorState()
}

class VendorViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("vendors")
    private val _vendorData = MutableLiveData<List<VendorData>>()
    private val _vendorState = MutableLiveData<VendorState>()
    val vendorData: LiveData<List<VendorData>> = _vendorData
    val vendorState: LiveData<VendorState> = _vendorState

    private var currentListener: ValueEventListener? = null

    fun fetchVendors(filter: String = "all", searchQuery: String = "", facilityName: String? = null) {
        viewModelScope.launch {
            _vendorState.value = VendorState.LOADING

            currentListener?.let {
                database.removeEventListener(it)
            }

            currentListener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val searchKeywords = searchQuery.split(" ").map { it.trim() }
                    val vendors = snapshot.children
                        .mapNotNull { childSnapshot ->
                            try {
                                val data = childSnapshot.value as? Map<*, *>
                                if (data != null) {
                                    VendorData(
                                        firstName = data["firstName"] as? String ?: "",
                                        lastName = data["lastName"] as? String ?: "",
                                        companyName = data["companyName"] as? String ?: "",
                                        email = data["email"] as? String ?: "",
                                        phoneNumber = data["phoneNumber"] as? String ?: "",
                                        address = data["address"] as? String ?: "",
                                        remarks = data["remarks"] as? String ?: "",
                                        facility = data["facility"] as? String ?: "",
                                        isActive = data["active"] as? Boolean ?: true
                                    )
                                } else {
                                    childSnapshot.getValue(VendorData::class.java)
                                }
                            } catch (e: Exception) {
                                null
                            }
                        }
                        .filter { vendor ->
                            if (facilityName != null) {
                                vendor.facility == facilityName
                            } else true
                        }
                        .filter { vendor ->
                            if (searchQuery.isNotEmpty()) {
                                searchKeywords.any { keyword ->
                                    VendorData::class.memberProperties.any { prop ->
                                        val value = prop.get(vendor)
                                        value?.toString()?.contains(keyword, ignoreCase = true) == true
                                    }
                                }
                            } else true
                        }
                        .filter { vendor ->
                            when (filter.lowercase()) {
                                "active" -> vendor.isActive
                                "inactive" -> !vendor.isActive
                                else -> true
                            }
                        }
                    _vendorData.value = vendors
                    _vendorState.value = if (vendors.isEmpty()) VendorState.EMPTY else VendorState.SUCCESS
                }

                override fun onCancelled(error: DatabaseError) {
                    _vendorState.value = VendorState.ERROR(error.message)
                }
            }

            database.addValueEventListener(currentListener!!)
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentListener?.let {
            database.removeEventListener(it)
        }
    }

    fun fetchVendorByEmail(email: String, onSuccess: (VendorData?) -> Unit) {
        viewModelScope.launch {
            try {
                database.orderByChild("email").equalTo(email).get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            try {
                                val data = snapshot.children.first().value as? Map<*, *>
                                if (data != null) {
                                    val vendor = VendorData(
                                        firstName = data["firstName"] as? String ?: "",
                                        lastName = data["lastName"] as? String ?: "",
                                        companyName = data["companyName"] as? String ?: "",
                                        email = data["email"] as? String ?: "",
                                        phoneNumber = data["phoneNumber"] as? String ?: "",
                                        address = data["address"] as? String ?: "",
                                        remarks = data["remarks"] as? String ?: "",
                                        facility = data["facility"] as? String ?: "",
                                        isActive = data["active"] as? Boolean ?: true
                                    )
                                    onSuccess(vendor)
                                } else {
                                    val vendor = snapshot.children.first().getValue(VendorData::class.java)
                                    onSuccess(vendor)
                                }
                            } catch (e: Exception) {
                                onSuccess(null)
                            }
                        } else {
                            onSuccess(null)
                        }
                    }
                    .addOnFailureListener {
                        onSuccess(null)
                    }
            } catch (e: Exception) {
                onSuccess(null)
            }
        }
    }

    fun addVendor(vendor: VendorData, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val vendorMap = mapOf(
                    "firstName" to vendor.firstName,
                    "lastName" to vendor.lastName,
                    "companyName" to vendor.companyName,
                    "email" to vendor.email,
                    "phoneNumber" to vendor.phoneNumber,
                    "address" to vendor.address,
                    "remarks" to vendor.remarks,
                    "facility" to vendor.facility,
                    "active" to vendor.isActive
                )

                val newVendorRef = database.push()
                newVendorRef.setValue(vendorMap)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Error adding vendor") }
            } catch (e: Exception) {
                onError(e.message ?: "Error adding vendor")
            }
        }
    }

    fun updateVendor(email: String, vendor: VendorData, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val vendorMap = mapOf(
                    "firstName" to vendor.firstName,
                    "lastName" to vendor.lastName,
                    "companyName" to vendor.companyName,
                    "email" to vendor.email,
                    "phoneNumber" to vendor.phoneNumber,
                    "address" to vendor.address,
                    "remarks" to vendor.remarks,
                    "facility" to vendor.facility,
                    "active" to vendor.isActive
                )

                database.orderByChild("email").equalTo(email).get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            val vendorKey = snapshot.children.first().key!!
                            database.child(vendorKey).setValue(vendorMap)
                                .addOnSuccessListener { onSuccess() }
                                .addOnFailureListener { onError(it.message ?: "Error updating vendor") }
                        } else {
                            onError("Vendor not found")
                        }
                    }
                    .addOnFailureListener {
                        onError(it.message ?: "Error finding vendor")
                    }
            } catch (e: Exception) {
                onError(e.message ?: "Error updating vendor")
            }
        }
    }

    fun toggleVendorStatus(
        email: String,
        currentVendor: VendorData,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Update the local list immediately
                _vendorData.value = _vendorData.value?.map { vendor ->
                    if (vendor.email == email) {
                        vendor.copy(isActive = !vendor.isActive)
                    } else {
                        vendor
                    }
                }

                database.orderByChild("email").equalTo(email).get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            val vendorKey = snapshot.children.first().key!!
                            database.child(vendorKey).child("active").setValue(!currentVendor.isActive)  // Use 'active' in Firebase
                                .addOnSuccessListener { onSuccess() }
                                .addOnFailureListener { error ->
                                    _vendorData.value = _vendorData.value?.map { vendor ->
                                        if (vendor.email == email) {
                                            vendor.copy(isActive = currentVendor.isActive)
                                        } else {
                                            vendor
                                        }
                                    }
                                    onError(error.message ?: "Error updating vendor status")
                                }
                        } else {
                            _vendorData.value = _vendorData.value?.map { vendor ->
                                if (vendor.email == email) {
                                    vendor.copy(isActive = currentVendor.isActive)
                                } else {
                                    vendor
                                }
                            }
                            onError("Vendor not found")
                        }
                    }
                    .addOnFailureListener { error ->
                        _vendorData.value = _vendorData.value?.map { vendor ->
                            if (vendor.email == email) {
                                vendor.copy(isActive = currentVendor.isActive)
                            } else {
                                vendor
                            }
                        }
                        onError(error.message ?: "Error finding vendor")
                    }
            } catch (e: Exception) {
                _vendorData.value = _vendorData.value?.map { vendor ->
                    if (vendor.email == email) {
                        vendor.copy(isActive = currentVendor.isActive)
                    } else {
                        vendor
                    }
                }
                onError(e.message ?: "Error toggling vendor status")
            }
        }
    }

    fun deleteVendor(email: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                database.orderByChild("email").equalTo(email).get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            val vendorKey = snapshot.children.first().key!!
                            database.child(vendorKey).removeValue()
                                .addOnSuccessListener { onSuccess() }
                                .addOnFailureListener { onError(it.message ?: "Error deleting vendor") }
                        } else {
                            onError("Vendor not found")
                        }
                    }
                    .addOnFailureListener {
                        onError(it.message ?: "Error finding vendor")
                    }
            } catch (e: Exception) {
                onError(e.message ?: "Error deleting vendor")
            }
        }
    }
}