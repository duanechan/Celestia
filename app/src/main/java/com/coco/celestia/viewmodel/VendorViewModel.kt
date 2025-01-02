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

    fun fetchVendors(filter: String = "all", searchQuery: String = "", facilityName: String? = null) {
        viewModelScope.launch {
            _vendorState.value = VendorState.LOADING

            database.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val searchKeywords = searchQuery.split(" ").map { it.trim() }

                    val vendors = snapshot.children
                        .mapNotNull { it.getValue(VendorData::class.java) }
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
            })
        }
    }

    fun addVendor(vendor: VendorData, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val newVendorRef = database.push()
                newVendorRef.setValue(vendor)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Error adding vendor") }
            } catch (e: Exception) {
                onError(e.message ?: "Error adding vendor")
            }
        }
    }

    // TODO: Implement this when there is already the vendor details screen
    fun updateVendor(vendorId: String, vendor: VendorData, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                database.child(vendorId).setValue(vendor)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Error updating vendor") }
            } catch (e: Exception) {
                onError(e.message ?: "Error updating vendor")
            }
        }
    }

    // TODO: Implement this when there is already the vendor details screen
    fun deleteVendor(vendorId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                database.child(vendorId).removeValue()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.message ?: "Error deleting vendor") }
            } catch (e: Exception) {
                onError(e.message ?: "Error deleting vendor")
            }
        }
    }
}