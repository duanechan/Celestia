package com.coco.celestia.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.viewmodel.model.FacilityData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

sealed class FacilityState {
    data object LOADING : FacilityState()
    data object SUCCESS : FacilityState()
    data object EMPTY : FacilityState()
    data class ERROR (val message: String) : FacilityState()
}

class FacilityViewModel: ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("facilities")
    private val _facilityData = MutableLiveData<FacilityData>()
    private val _facilitiesData = MutableLiveData<List<FacilityData>>()
    private val _facilityState = MutableLiveData<FacilityState>()
    val facilityData: LiveData<FacilityData> = _facilityData
    val facilitiesData: LiveData<List<FacilityData>> = _facilitiesData
    val facilityState: LiveData<FacilityState> = _facilityState

    fun fetchFacilities() {
        _facilityState.value = FacilityState.LOADING

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val facilities = snapshot.children.mapNotNull { child ->
                        try {
                            if (child.getValue() is Boolean) {
                                null
                            } else {
                                // Add these debug logs
                                Log.d("FacilityViewModel", "Raw data for ${child.key}: ${child.getValue()}")
                                val facility = child.getValue(FacilityData::class.java)
                                // Log the converted facility data
                                facility?.let {
                                    Log.d("FacilityViewModel", """
                                    Converted Facility Data for ${it.name}:
                                    isPickupEnabled: ${it.isPickupEnabled}
                                    isCashEnabled: ${it.isCashEnabled}
                                    pickupLocation: ${it.pickupLocation}
                                    cashInstructions: ${it.cashInstructions}
                                """.trimIndent())
                                }
                                facility
                            }
                        } catch (e: Exception) {
                            Log.e("FacilityViewModel", "Error converting facility data: ${e.message}")
                            null
                        }
                    }
                    _facilitiesData.value = facilities
                    _facilityState.value = if (facilities.isEmpty()) FacilityState.EMPTY else FacilityState.SUCCESS
                } else {
                    _facilitiesData.value = emptyList()
                    _facilityState.value = FacilityState.EMPTY
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _facilityState.value = FacilityState.ERROR(error.message)
            }
        })
    }

    fun createFacility(
        icon: Int,
        name: String,
        emails: MutableList<String>,
        onComplete: () -> Unit,
        onError: (String) -> Unit
    ) {
        _facilityState.value = FacilityState.LOADING
        viewModelScope.launch {
            try {
                val facilityRef = database.child(name.lowercase())

                val facilityData = mutableMapOf<String, Any>(
                    "icon" to icon,
                    "name" to name,
                    "emails" to emails.toList(),
                    "isPickupEnabled" to false,
                    "pickupLocation" to "",
                    "isDeliveryEnabled" to false,
                    "deliveryDetails" to "",
                    "isCashEnabled" to false,
                    "cashInstructions" to "",
                    "isGcashEnabled" to false,
                    "gcashNumbers" to ""
                )

                facilityRef.setValue(facilityData)
                    .addOnSuccessListener {
                        onComplete()
                        _facilityState.value = FacilityState.SUCCESS
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Failed to create facility")
                        _facilityState.value = FacilityState.ERROR(e.message ?: "Failed to create facility")
                    }

            } catch (e: Exception) {
                Log.e("FacilityViewModel", "Error creating facility: ${e.message}", e)
                onError(e.message ?: "Unknown error occurred")
                _facilityState.value = FacilityState.ERROR(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun updateFacilityEmails(
        oldRole: String?,
        newRole: String,
        userEmail: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (oldRole != null && oldRole.startsWith("Coop") && oldRole != "Coop") {
                    val oldFacilityName = oldRole.removePrefix("Coop").lowercase()
                    database.child(oldFacilityName).runTransaction(object : Transaction.Handler {
                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                            val facility = currentData.getValue(FacilityData::class.java)
                            if (facility != null) {
                                facility.emails.remove(userEmail)
                                currentData.value = facility
                                return Transaction.success(currentData)
                            }
                            return Transaction.abort()
                        }

                        override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                            if (error != null) {
                                _facilityState.value = FacilityState.ERROR(error.message)
                            }
                        }
                    })
                }

                if (newRole != "Coop") {
                    val newFacilityName = newRole.removePrefix("Coop").lowercase()
                    database.child(newFacilityName).runTransaction(object : Transaction.Handler {
                        override fun doTransaction(currentData: MutableData): Transaction.Result {
                            val facility = currentData.getValue(FacilityData::class.java)
                            if (facility != null) {
                                if (!facility.emails.contains(userEmail)) {
                                    facility.emails.add(userEmail)
                                }
                                currentData.value = facility
                                return Transaction.success(currentData)
                            }
                            return Transaction.abort()
                        }

                        override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                            when {
                                error != null -> {
                                    onError(error.message)
                                    _facilityState.value = FacilityState.ERROR(error.message)
                                }
                                committed -> {
                                    onSuccess()
                                    _facilityState.value = FacilityState.SUCCESS
                                }
                                else -> {
                                    onError("Failed to update facility")
                                    _facilityState.value = FacilityState.ERROR("Failed to update facility")
                                }
                            }
                        }
                    })
                } else {
                    onSuccess()
                    _facilityState.value = FacilityState.SUCCESS
                }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
                _facilityState.value = FacilityState.ERROR(e.message ?: "Unknown error occurred")
            }
        }
    }

    private fun removeEmailFromFacility(facilityName: String, email: String) {
        database.child(facilityName).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val facility = currentData.getValue(FacilityData::class.java) ?: return Transaction.abort()
                facility.emails.remove(email)
                currentData.value = facility
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                if (error != null) {
                    _facilityState.value = FacilityState.ERROR(error.message)
                }
            }
        })
    }

    private fun addEmailToFacility(
        facilityName: String,
        email: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        database.child(facilityName).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val facility = currentData.getValue(FacilityData::class.java)
                if (facility == null) {
                    return Transaction.abort()
                }
                if (!facility.emails.contains(email)) {
                    facility.emails.add(email)
                }
                currentData.value = facility
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                when {
                    error != null -> {
                        onError(error.message)
                        _facilityState.value = FacilityState.ERROR(error.message)
                    }
                    committed -> {
                        onSuccess()
                        _facilityState.value = FacilityState.SUCCESS
                    }
                    else -> {
                        onError("Failed to update facility")
                        _facilityState.value = FacilityState.ERROR("Failed to update facility")
                    }
                }
            }
        })
    }

    fun updateFacilitySettings(
        facilityName: String,
        pickupLocation: String,
        deliveryDetails: String,
        cashInstructions: String,
        gcashNumbers: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val updates = hashMapOf<String, Any>(
                    // Collection Methods
                    "isPickupEnabled" to (pickupLocation.isNotEmpty()),
                    "isDeliveryEnabled" to (deliveryDetails.isNotEmpty()),
                    "pickupLocation" to pickupLocation,
                    "deliveryDetails" to deliveryDetails,
                    // Payment Methods
                    "isCashEnabled" to (cashInstructions.isNotEmpty()),
                    "isGcashEnabled" to (gcashNumbers.isNotEmpty()),
                    "cashInstructions" to cashInstructions,
                    "gcashNumbers" to gcashNumbers
                )

                database.child(facilityName.lowercase())
                    .updateChildren(updates)
                    .addOnSuccessListener {
                        onSuccess()
                        _facilityState.value = FacilityState.SUCCESS
                    }
                    .addOnFailureListener { e ->
                        onError(e.message ?: "Failed to update settings")
                        _facilityState.value = FacilityState.ERROR(e.message ?: "Failed to update settings")
                    }
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error occurred")
                _facilityState.value = FacilityState.ERROR(e.message ?: "Unknown error occurred")
            }
        }
    }
    fun FacilityData.toMap(): Map<String, Any> {
        return mapOf(
            "icon" to icon,
            "name" to name,
            "emails" to ArrayList(emails),
            // Collection Methods
            "isPickupEnabled" to isPickupEnabled,
            "isDeliveryEnabled" to isDeliveryEnabled,
            "pickupLocation" to pickupLocation,
            "deliveryDetails" to deliveryDetails,
            // Payment Methods
            "isCashEnabled" to isCashEnabled,
            "isGcashEnabled" to isGcashEnabled,
            "cashInstructions" to cashInstructions,
            "gcashNumbers" to gcashNumbers
        )
    }
}