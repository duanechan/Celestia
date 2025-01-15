package com.coco.celestia.viewmodel

import android.util.Log
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.viewmodel.model.BasketItem
import com.coco.celestia.viewmodel.model.Notification
import com.coco.celestia.viewmodel.model.UserData
import com.coco.celestia.viewmodel.model.toMap
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.snapshots
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class UserState {
    data object LOADING : UserState()
    data object SUCCESS : UserState()
    data object EMAIL_SENT_SUCCESS: UserState()
    data object REGISTER_SUCCESS: UserState()
    data class LOGIN_SUCCESS (val role: String): UserState()
    data object EMPTY : UserState()
    data class ERROR (val message: String) : UserState()
}

sealed class PasswordState {
    data object LOADING : PasswordState()
    data object SUCCESS : PasswordState()
    data class ERROR (val message: String) : PasswordState()
}

class UserViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _userData = MutableLiveData<UserData>()
    private val _usersData = MutableLiveData<List<UserData>>()
    private val _userState = MutableLiveData<UserState>()
    private val _passwordState = MutableLiveData<PasswordState>()
    val userData: LiveData<UserData> = _userData
    val usersData: LiveData<List<UserData>> = _usersData
    val userState: LiveData<UserState> = _userState
    val passwordState: LiveData<PasswordState> = _passwordState

    fun resetUserState() {
        _userState.value = UserState.LOADING
    }

    fun resetPasswordState() {
        _passwordState.value = PasswordState.LOADING
    }

    /**
     * Fetches user data from the database based on the provided UID.
     */
    fun fetchUser(uid: String) {
        viewModelScope.launch {
            _userState.value = UserState.LOADING
            try {
                val snapshot = database.child(uid).get().await()
                if (snapshot.exists()) {
                    _userData.value = parseUserData(snapshot)
                    _userState.value = UserState.SUCCESS
                } else {
                    _userData.value = UserData()
                    _userState.value = UserState.EMPTY
                }
            } catch (e: Exception) {
                _userData.value = UserData()
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
                    val users = mutableListOf<UserData>()
                    for (userSnapshot in snapshot.children) {
                        val userInfo = parseUserData(userSnapshot)
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

    fun fetchActiveUsers(onResult: (List<UserData>) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val query = database.orderByChild("online").equalTo(true)

            query.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        onResult(
                            snapshot.children.mapNotNull { userSnapshot ->
                                parseUserData(userSnapshot)
                            }
                        )
                    } else {
                        onResult(emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onError(error.message)
                }
            })
        }
    }

    fun getUserUidByEmail(email: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val query = database.orderByChild("email").equalTo(email)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val uid = userSnapshot.key
                            onResult(uid)
                            return
                        }
                    } else {
                        onResult(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(null)
                }
            })
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
                    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val userData = UserData(
                        email = email,
                        firstname = firstname,
                        lastname = lastname,
                        role = role,
                        registrationDate = currentDate
                    )
                    database.child(user.uid).setValue(userData).await()
                    _userState.value = UserState.REGISTER_SUCCESS
                    auth.signOut()
                } ?: run {
                    _userState.value = UserState.ERROR("Registration failed")
                }
            } catch (e: Exception) {
                _userState.value = UserState.ERROR(e.message ?: "Unknown error")
            } catch (e: FirebaseAuthUserCollisionException) {
                _userState.value = UserState.ERROR("Account already exists")
            } catch (e: FirebaseNetworkException) {
                _userState.value = UserState.ERROR("No internet connection.")
            }
        }
    }

    fun addAccount(email: String, firstname: String, lastname: String, password: String, role: String, currentPass: String) {
        viewModelScope.launch {
            _userState.value = UserState.LOADING
            val currentUser = auth.currentUser

            try {
                reAuthenticate(currentUser, currentPass)
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val newUser = result.user

                if (newUser != null) {
                    val userData = UserData(email, firstname, lastname, role)
                    database.child(newUser.uid).setValue(userData).await()
                    auth.signOut()
                    signInUser(currentUser?.email ?: "", currentPass)
                    _userState.value = UserState.REGISTER_SUCCESS
                } else {
                    _userState.value = UserState.ERROR("Registration failed. No user created.")
                }
            } catch (e: FirebaseAuthUserCollisionException) {
                _userState.value = UserState.ERROR("Account already exists.")
            } catch (e: FirebaseNetworkException) {
                _userState.value = UserState.ERROR("No internet connection.")
            } catch (e: Exception) {
                _userState.value = UserState.ERROR(e.message ?: "Unknown error occurred during registration.")
            }
        }
    }

    private suspend fun signInUser(email: String, password: String) {
        try {
            auth.signInWithEmailAndPassword(email, password).await()
        } catch (e: FirebaseAuthException) {
            _userState.value = UserState.ERROR("Sign-in failed: ${e.message}")
        }
    }

    private suspend fun reAuthenticate(currentUser: FirebaseUser?, currentPass: String) {
        currentUser?.let { user ->
            try {
                val currentEmail = user.email ?: throw Exception("Email not available for re-authentication")
                val credential = EmailAuthProvider.getCredential(currentEmail, currentPass)

                val task = user.reauthenticate(credential)
                task.await()

                if (task.isSuccessful) {
                    _userState.value = UserState.SUCCESS
                } else {
                    _userState.value = UserState.ERROR("Re-authentication failed. Please try again.")
                }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _userState.value = UserState.ERROR("Re-authentication failed. Incorrect password.")
                throw Exception("Re-authentication failed. Incorrect password.")
            } catch (e: FirebaseAuthException) {
                handleReAuthError(e)
            } catch (e: Exception) {
                _userState.value = UserState.ERROR("Re-authentication failed. ${e.message}")
            }
        } ?: run {
            _userState.value = UserState.ERROR("Re-authentication failed. Original user is null.")
        }
    }


    private fun handleReAuthError(e: FirebaseAuthException) {
        when (e.errorCode) {
            "ERROR_INVALID_PASSWORD" -> {
                _userState.value = UserState.ERROR("Re-authentication failed. Incorrect password.")
            }
            "ERROR_USER_NOT_FOUND" -> {
                _userState.value = UserState.ERROR("Re-authentication failed. User not found.")
            }
            else -> {
                _userState.value = UserState.ERROR("Re-authentication failed. ${e.message}")
            }
        }
    }

    fun changePassword (currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            val user = auth.currentUser
            _passwordState.value = PasswordState.LOADING
            if (user != null) {
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                user.reauthenticate(credential)
                    .addOnCompleteListener { reAuthTask ->
                        if (reAuthTask.isSuccessful) {
                            user.updatePassword(newPassword)
                                .addOnCompleteListener { updateTask ->
                                    if (updateTask.isSuccessful) {
                                        _passwordState.value = PasswordState.SUCCESS
                                    } else {
                                        _passwordState.value = PasswordState.ERROR("Password Update Failed")
                                    }
                                }
                        } else {
                            _passwordState.value = PasswordState.ERROR("Re-authentication failed. Wrong Password")
                        }
                    }
            } else {
                _passwordState.value = PasswordState.ERROR("User not found.")
            }
        }
    }
    /**
     * Logs in a user with the provided email and password.
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _userState.value = UserState.LOADING
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                user?.let {
                    database.child(user.uid).child("online").setValue(true)
                    database.child(user.uid).get()
                        .addOnSuccessListener { snapshot ->
                            val userRole = snapshot.child("role").getValue(String::class.java)
                            if (userRole != null) {
                                _userData.value = parseUserData(snapshot)
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
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _userState.value = UserState.ERROR("Invalid email or password.")
            } catch (e: FirebaseNetworkException) {
                _userState.value = UserState.ERROR("No internet connection.")
            }
        }
    }

    private fun parseUserData(snapshot: DataSnapshot): UserData {
        return UserData(
            email = snapshot.child("email").getValue(String::class.java) ?: "",
            firstname = snapshot.child("firstname").getValue(String::class.java) ?: "",
            lastname = snapshot.child("lastname").getValue(String::class.java) ?: "",
            role = snapshot.child("role").getValue(String::class.java) ?: "",
            basket = snapshot.child("basket").children.mapNotNull { item ->
                item.getValue(BasketItem::class.java)
            },
            phoneNumber = snapshot.child("phoneNumber").getValue(String::class.java) ?: "",
            streetNumber = snapshot.child("streetNumber").getValue(String::class.java) ?: "",
            barangay = snapshot.child("barangay").getValue(String::class.java) ?: "",
            online = snapshot.child("online").getValue(Boolean::class.java) ?: false,
            isChecked = snapshot.child("isChecked").getValue(Boolean::class.java) ?: false,
            registrationDate = snapshot.child("registrationDate").getValue(String::class.java) ?: ""
        )
    }

    fun assignFacility(emails: List<String>, facility: String) {
        viewModelScope.launch {
            _userState.value = UserState.LOADING
            try {
                database.runTransaction(object : Transaction.Handler {
                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                        currentData.children.forEach { child ->
                            var user = child.getValue(UserData::class.java) ?: UserData()
                            if (emails.contains(user.email) && user.role == "Client") {
                                user.role = "Coop${facility.replaceFirstChar { it.uppercase() }}"
                                child.value = user
                            }
                        }
                        return Transaction.success(currentData)
                    }

                    override fun onComplete(
                        error: DatabaseError?,
                        committed: Boolean,
                        currentData: DataSnapshot?
                    ) {
                        if (error != null) {
                            _userState.value = UserState.ERROR(error.message)
                        } else if (committed) {
                            _userState.value = UserState.SUCCESS
                        }
                    }

                })
            } catch (e: Exception) {
                _userState.value = UserState.ERROR(e.message ?: "Unknown Error.")
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
            } catch (e: FirebaseNetworkException) {
                _userState.value = UserState.ERROR("No internet connection.")
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
        viewModelScope.launch {
            _userState.value = UserState.LOADING
            try {
                database.child(uid).setValue(updatedUserData).await()
                database.child(uid).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            _userState.value = UserState.SUCCESS
                        } else {
                            _userState.value = UserState.EMPTY
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        _userState.value = UserState.ERROR(error.message)
                    }
                })
            } catch (e: Exception) {
                _userState.value = UserState.ERROR(e.message ?: "Unknown error")
            }
        }
    }

    fun addItem(uid: String, item: BasketItem) {
        viewModelScope.launch {
            _userState.value = UserState.LOADING
            try {
                val basketRef = database.child(uid).child("basket").child(item.id)

                val itemMap = item.toMap()

                basketRef.setValue(itemMap)
                    .addOnSuccessListener {
                        _userState.value = UserState.SUCCESS
                    }
                    .addOnFailureListener { exception ->
                        _userState.value = UserState.ERROR(exception.message ?: "Unknown error")
                    }
            } catch (e: Exception) {
                _userState.value = UserState.ERROR(e.message ?: "Unknown error")
            }
        }
    }

    fun updateCheckoutItems(uid: String, updatedBasket: List<BasketItem>) {
        viewModelScope.launch {
            _userState.value = UserState.LOADING
            try {
                val query = database.child(uid).child("basket")

                val updates = updatedBasket.associate { item ->
                    item.id to item.toMap()
                }
                query.updateChildren(updates).await()

                _userData.value = _userData.value?.copy(
                    basket = _userData.value?.basket?.filter { basketItem ->
                        !updatedBasket.any { it.id == basketItem.id }
                    } ?: emptyList()
                )
                _userState.value = UserState.SUCCESS
            } catch (e: Exception) {
                _userState.value = UserState.ERROR(e.message ?: "Unknown error")
            }
        }
    }

    fun clearCheckoutItems(items: List<BasketItem>) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        viewModelScope.launch {
            _userState.value = UserState.LOADING
            try {
                val removedItems = items.associate { item -> item.id to null }
                database.child(uid).child("basket").updateChildren(removedItems).await()
                _userData.value = _userData.value?.copy(
                    basket = _userData.value?.basket?.filter { basketItem ->
                        !items.any { it.id == basketItem.id }
                    } ?: emptyList()
                )
            } catch (e: Exception) {
                _userState.value = UserState.ERROR(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Logs out the current user.
     */
    fun logout(uid: String) {
        viewModelScope.launch {
            _userState.value = UserState.LOADING
            try {
                database.child(uid).child("online").setValue(false)
                    .addOnSuccessListener {
                        auth.signOut()
                        _userData.value = UserData()
                        _userState.value = UserState.SUCCESS
                    }.addOnFailureListener {
                        _userState.value = UserState.ERROR("Logout failed")
                    }
            } catch (e: Exception) {
                _userState.value = UserState.ERROR(e.message ?: "Unknown error")
            } catch (e: FirebaseNetworkException) {
                _userState.value = UserState.ERROR("No internet connection.")
            }
        }
    }
}