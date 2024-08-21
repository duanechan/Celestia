package com.coco.celestia

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.ui.theme.CelestiaTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.initialize

class LoginActivity : ComponentActivity() {
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.initialize(this)
        setContent {
            CelestiaTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF2E3DB))
                ) {
                    databaseReference = FirebaseDatabase.getInstance().getReference().child("users")
                    LoginScreen(loginUser = ::loginUser)
                }
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Login Failed", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this@LoginActivity, "Login Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

@Composable
fun LoginScreen(loginUser: (String, String) -> Unit) {

    val maxCharacters = 25
    var showDialog by remember { mutableStateOf(false) }
    var errorDialogMessage by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var showRegisterDialog by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
                if (it.length <= maxCharacters) {
                    email = it
                }
            },
            label = { Text(text = "Username") },
            singleLine = true,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(2.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                if (it.length <= 16) {
                    password = it
                }
            },
            label = { Text(text = "Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            maxLines = 1
        )


        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = {
                if (email.isEmpty() || password.isEmpty()) {
                    errorDialogMessage = "Failed"
                    showDialog = true
                } else {
                    loginUser(email, password)
                }
            },
            modifier = Modifier
                .width(285.dp)
                .height(50.dp)) {
            Text(text = "Login")
        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = {
                    Text(text = if (errorDialogMessage.isNotEmpty()) "Login Failed" else "Login Successful!")
                },
                text = {
                    Text(text = if (errorDialogMessage.isNotEmpty()) "Try again" else "Welcome back!")
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
        Text(text = "Forgot Password?", fontSize = 13.sp, modifier = Modifier.clickable {  })
        Spacer(modifier = Modifier.height(85.dp))
        Text(text = "Don't have an account?", fontSize = 15.sp, modifier = Modifier.clickable {  })
        val context = LocalContext.current
        Text(
            text = "Register Now!",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable {
                showRegisterDialog = true // Show the register dialog
            })
        if (showRegisterDialog) {
            AlertDialog(
                onDismissRequest = { showRegisterDialog = false },
                title = { Text(text = "Register As") },
                text = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "Choose your role")

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(
                                    onClick = {
                                        showRegisterDialog = false
                                        val intent = Intent(context, RegisterActivity::class.java)
                                        context.startActivity(intent)
                                        (context as? ComponentActivity)?.finish()
                                    }
                                ) {
                                    Text(text = "Farmer")
                                }

                                Button(
                                    onClick = {
                                        showRegisterDialog = false
                                        val intent = Intent(context, RegisterActivity::class.java)
                                        context.startActivity(intent)
                                        (context as? ComponentActivity)?.finish()
                                    }
                                ) {
                                    Text(text = "Client")
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    // Empty/null
                },
                dismissButton = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(
                            onClick = { showRegisterDialog = false }
                        ) {
                            Text(text = "Cancel")
                        }
                    }
                }
            )
        }





    }
}

