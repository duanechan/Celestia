package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class UserState {
    object LOADING : UserState()
    object SUCCESS : UserState()
    object EMAIL_SENT_SUCCESS: UserState()
    data object REGISTER_SUCCESS: UserState()
    data class LOGIN_SUCCESS (val role: String): UserState()
    object EMPTY : UserState()
    data class ERROR (val message: String) : UserState()
}

class UserViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _userData = MutableLiveData<UserData?>()
    private val _usersData = MutableLiveData<List<UserData?>>()
    private val _userState = MutableLiveData<UserState>()
    val userData: LiveData<UserData?> = _userData
    val usersData: LiveData<List<UserData?>> = _usersData
    val userState: LiveData<UserState> = _userState

    /**
     * Fetches user data from the database based on the provided UID.
     */
    fun fetchUser(uid: String) {
        viewModelScope.launch {
            _userState.value = UserState.LOADING
            try {
                val snapshot = database.child(uid).get().await()
                if (snapshot.exists()) {
                    val userInfo = snapshot.getValue(UserData::class.java)
                    _userData.value = userInfo
                    _userState.value = UserState.SUCCESS
                } else {
                    _userData.value = null
                    _userState.value = UserState.EMPTY
                }
            } catch (e: Exception) {
                _userData.value = null
                _userState.value = UserState.ERROR(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Fetches a list of all users from the database.
     */
    fun fetchUsers() {
        viewModelScope.launch {
            _userState.value = UserState.LOADING
            try {
                val snapshot = database.get().await()
                if (snapshot.exists()) {
                    val users = mutableListOf<UserData?>()
                    for (userSnapshot in snapshot.children) {
                        val userInfo = userSnapshot.getValue(UserData::class.java)
                        users.add(userInfo)
                    }
                    _usersData.value = users
                    _userState.value = UserState.SUCCESS
                } else {
                    _usersData.value = emptyList()
                    _userState.value = UserState.EMPTY
                }
            } catch (e: Exception) {
                _usersData.value = emptyList()
                _userState.value = UserState.ERROR(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Registers a new user with the provided email, first name, last name, and password.
     */
    fun register(email: String, firstname: String, lastname: String, password: String, role: String) {
        viewModelScope.launch {
            _userState.value = UserState.LOADING
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user
                user?.let {
                    val userData = UserData(email, firstname, lastname, role)
                    database.child(user.uid).setValue(userData).await()
                    _userState.value = UserState.REGISTER_SUCCESS
                } ?: run {
                    _userState.value = UserState.ERROR("Registration failed")
                }
            } catch (e: Exception) {
                _userState.value = UserState.ERROR(e.message ?: "Unknown error")
            } catch (e: FirebaseAuthUserCollisionException) {
                _userState.value = UserState.ERROR("Account already exists")
            }
        }
    }


    /**
     * Logs in a user with the provided email and password.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _userState.value = UserState.LOADING
            if (email.isEmpty() || password.isEmpty()) {
                _userState.value = UserState.ERROR("Fields cannot be empty")
            } else {
                try {
                    val result = auth.signInWithEmailAndPassword(email, password).await()
                    val user = result.user
                    user?.let {
                        database.child(user.uid).get().addOnSuccessListener { snapshot ->
                            val userRole = snapshot.child("role").getValue(String::class.java)
                            if (userRole != null) {
                                _userData.value = snapshot.getValue(UserData::class.java)
                                _userState.value = UserState.LOGIN_SUCCESS(userRole)
                            } else {
                                _userState.value = UserState.ERROR("Role not found")
                            }
                        }.addOnFailureListener {
                            _userState.value = UserState.ERROR("Login failed")
                        }
                    } ?: run {
                        _userState.value = UserState.ERROR("Login failed")
                    }
                } catch (e: Exception) {
                    _userState.value = UserState.ERROR(e.message ?: "Unknown error")
                }
            }
        }
    }

    /**
     * Sends a password reset email to the provided email address.
     */
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _userState.value = UserState.LOADING
            try {
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _userState.value = UserState.EMAIL_SENT_SUCCESS
                        } else {
                            _userState.value = UserState.ERROR(task.exception?.message ?: "Unknown Error")
                        }
                    }
            } catch (e: Exception) {
                _userState.value = UserState.ERROR(e.message ?: "Unknown Error")
            }
        }
    }

    /**
     * Updates a user in the database based on the provided email.
     */
    fun updateUser(
        uid: String,
        updatedUserData: UserData
    ) {
        val query = database.child(uid)

        viewModelScope.launch {
            _userState.value = UserState.LOADING
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        snapshot.ref.setValue(updatedUserData)
                            .addOnSuccessListener {
                                _userState.value = UserState.SUCCESS
                            }
                            .addOnFailureListener { exception ->
                                _userState.value = UserState.ERROR(exception.message ?: "Unknown error")
                            }
                    } else {
                        _userState.value = UserState.EMPTY
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _userState.value = UserState.ERROR(error.message)
                }
            })
        }
    }

    /**
     * Logs out the current user.
     */
    fun logout() {
        viewModelScope.launch {
            _userState.value = UserState.LOADING
            try {
                auth.signOut()
                _userData.value = null
                _userState.value = UserState.SUCCESS
            } catch (e: Exception) {
                _userState.value = UserState.ERROR(e.message ?: "Unknown error")
            }
        }
    }
}