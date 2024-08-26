package com.coco.celestia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.ui.theme.CelestiaTheme
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CelestiaTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF2E3DB)) // Hex color))
                ) {
                    HomeScreen()
//                    startActivity(Intent(this, LoginActivity::class.java))
                }
            }
        }
    }
}



@Composable
fun HomeScreen() {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userViewModel: UserViewModel = viewModel()
    val userData by userViewModel.userData.observeAsState()
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)

    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            userViewModel.fetchUser(currentUser.uid)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        when (userState) {
            is UserState.LOADING -> Text("Loading...", fontSize = 20.sp, fontFamily = FontFamily.Serif)
            is UserState.SUCCESS -> Text("Welcome ${(userData?.firstname + " " + userData?.lastname)}!", fontSize = 20.sp, fontFamily = FontFamily.Serif)
            is UserState.EMPTY -> Text("User data not found.", fontSize = 20.sp, fontFamily = FontFamily.Serif)
            is UserState.ERROR -> Text("Failed to load user data. (Error: ${(userState as UserState.ERROR).message})", fontSize = 20.sp, fontFamily = FontFamily.Serif)
            is UserState.LOGIN_SUCCESS -> Text("Welcome ${(userData?.firstname + " " + userData?.lastname)}!", fontSize = 20.sp, fontFamily = FontFamily.Serif)
        }
    }
}