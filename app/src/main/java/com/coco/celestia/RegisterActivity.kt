package com.coco.celestia

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.ui.theme.CelestiaTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class RegisterActivity : ComponentActivity() {
    private lateinit var databaseReference : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CelestiaTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF2E3DB))
                ) {
                    databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    RegisterScreen(
                        registerUser = ::registerUser,
                        showMessage = { message ->
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        })
                }
            }
        }
    }

    private fun registerUser(email: String, username: String, firstname: String, lastname: String, password: String) {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userData = UserData(email, username, firstname, lastname, password)

                        databaseReference.push().setValue(userData)
                            .addOnCompleteListener{
                                Toast.makeText(this@RegisterActivity, "Register Successful", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                                finish()
                            }
                            .addOnFailureListener{
                                Toast.makeText(this@RegisterActivity, "error ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this@RegisterActivity, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this@RegisterActivity, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}


@Composable
fun RegisterScreen(registerUser: (String, String, String, String, String) -> Unit, showMessage: (String) -> Unit) {

    val maxChar = 25
    var showDialog by remember { mutableStateOf(false) }
    val errorDialogMessage by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.a), contentDescription = "Login Image",
            modifier = Modifier.size(195.dp))

        Text(text = "CoCo", fontSize = 54.sp, fontWeight = FontWeight.Bold)
        Text(text = "Coop Connects", fontSize = 15.sp)
        Spacer(modifier = Modifier.height(35.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                if (it.length <= maxChar) {
                    email = it
                }
            },
            label = { Text(text = "Email") },
            singleLine = true,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(2.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                if (it.length <= maxChar) {
                    username = it
                }
            },
            label = { Text(text = "Username") },
            singleLine = true,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(2.dp))

        OutlinedTextField(
            value = firstName,
            onValueChange = {
                if (it.length <= maxChar) {
                    firstName = it
                }
            },
            label = { Text(text = "First Name") },
            singleLine = true,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(2.dp))

        OutlinedTextField(
            value = lastName,
            onValueChange = {
                if (it.length <= maxChar) {
                    lastName = it
                }
            },
            label = { Text(text = "Last Name") },
            singleLine = true,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(2.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                if (it.length <= maxChar) {
                    password = it
                }
            },
            label = { Text(text = "Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(2.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                if (it.length <= maxChar) {
                    confirmPassword = it
                }
            },
            label = { Text(text = "Confirm Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = {
                if (email.isNotEmpty() && username.isNotEmpty() && firstName.isNotEmpty() && lastName.isNotEmpty() && password.isNotEmpty()) {
                    registerUser(email, username, firstName, lastName, password)
                } else {
                    showMessage("All fields must be filled.")
                }
            },
            modifier = Modifier
                .width(285.dp)
                .height(50.dp)) {
            Text(text = "Register")
        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(text = if (errorDialogMessage.isNotEmpty()) "Registration Failed" else "Registration Successful!")
                },
                text = {
                    Text(text = if (errorDialogMessage.isNotEmpty()) "Try again" else "Welcome, $username!")
                },
                confirmButton = {
                    Button(
                        onClick = { showDialog = false }
                    ) {
                        Text(if (errorDialogMessage.isNotEmpty()) "Retry" else "Let's Go!")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(5.dp))
    }
}
