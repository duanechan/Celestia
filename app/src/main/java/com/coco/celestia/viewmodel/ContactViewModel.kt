package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.viewmodel.model.ContactData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.reflect.full.memberProperties

sealed class ContactState {
    data object LOADING : ContactState()
    data object SUCCESS : ContactState()
    data object EMPTY : ContactState()
    data class ERROR(val message: String) : ContactState()
}

class ContactViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("contacts")
    private val usersDatabase: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
    private val _contactData = MutableLiveData<List<ContactData>>()
    private val _contactState = MutableLiveData<ContactState>()
    private val _isAdmin = MutableLiveData<Boolean>()
    val contactData: LiveData<List<ContactData>> = _contactData
    val contactState: LiveData<ContactState> = _contactState
    val isAdmin: LiveData<Boolean> = _isAdmin

    init {
        checkUserRole()
    }

    fun checkUserRole() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()

        viewModelScope.launch {
            usersDatabase.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val role = snapshot.child("role").getValue(String::class.java)
                    _isAdmin.value = role?.equals("admin", ignoreCase = true) == true
                }

                override fun onCancelled(error: DatabaseError) {
                    _isAdmin.value = false
                }
            })
        }
    }

    fun fetchContacts(filter: String) {
        viewModelScope.launch {
            _contactState.value = ContactState.LOADING
            database.orderByChild("contactNumber").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val filterKeywords = filter.split(",").map { it.trim() }
                    val contacts = snapshot.children.mapNotNull { snapshot ->
                        snapshot.getValue(ContactData::class.java)?.copy(
                            contactId = snapshot.key ?: ""
                        )
                    }.filter { contact ->
                        filterKeywords.isEmpty() || filterKeywords.any { keyword ->
                            ContactData::class.memberProperties.any { prop ->
                                val value = prop.get(contact)
                                value?.toString()?.contains(keyword, ignoreCase = true) == true
                            }
                        }
                    }
                    _contactData.value = contacts
                    _contactState.value = if (contacts.isEmpty()) ContactState.EMPTY else ContactState.SUCCESS
                }

                override fun onCancelled(error: DatabaseError) {
                    _contactState.value = ContactState.ERROR(error.message)
                }
            })
        }
    }

    fun addContact(contact: ContactData) {
        viewModelScope.launch {
            try {
                _contactState.value = ContactState.LOADING

                val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                val currentDate = dateFormat.format(Date())

                database.orderByChild("contactId")
                    .startAt("CID-$currentDate")
                    .endAt("CID-$currentDate\uf8ff")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val count = snapshot.childrenCount + 1
                        val contactId = "CID-$currentDate-%03d".format(count)
                        val contactWithId = contact.copy(contactId = contactId)

                        database.child(contactId).setValue(contactWithId)
                            .addOnSuccessListener {
                                _contactState.value = ContactState.SUCCESS
                            }
                            .addOnFailureListener { exception ->
                                _contactState.value = ContactState.ERROR(
                                    exception.message ?: "Failed to add contact"
                                )
                            }
                    }
                    .addOnFailureListener { exception ->
                        _contactState.value = ContactState.ERROR(
                            exception.message ?: "Failed to generate contact ID"
                        )
                    }
            } catch (e: Exception) {
                _contactState.value = ContactState.ERROR(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    fun updateContact(contact: ContactData) {
        viewModelScope.launch {
            try {
                _contactState.value = ContactState.LOADING
                if (contact.contactId.isEmpty()) {
                    _contactState.value = ContactState.ERROR("Contact ID is missing")
                    return@launch
                }

                database.child(contact.contactId).setValue(contact)
                    .addOnSuccessListener {
                        _contactState.value = ContactState.SUCCESS
                    }
                    .addOnFailureListener { exception ->
                        _contactState.value = ContactState.ERROR(
                            exception.message ?: "Failed to update contact"
                        )
                    }
            } catch (e: Exception) {
                _contactState.value = ContactState.ERROR(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    fun deleteContact(contactId: String) {
        viewModelScope.launch {
            try {
                _contactState.value = ContactState.LOADING

                database.child(contactId).removeValue()
                    .addOnSuccessListener {
                        _contactState.value = ContactState.SUCCESS
                    }
                    .addOnFailureListener { exception ->
                        _contactState.value = ContactState.ERROR(
                            exception.message ?: "Failed to delete contact"
                        )
                    }
            } catch (e: Exception) {
                _contactState.value = ContactState.ERROR(
                    e.message ?: "An unexpected error occurred"
                )
            }
        }
    }
}