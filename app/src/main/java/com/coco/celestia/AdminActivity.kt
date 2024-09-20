package com.coco.celestia

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.coco.celestia.ui.theme.CelestiaTheme
import com.coco.celestia.ui.theme.DarkBlue

class AdminActivity: ComponentActivity(){
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CelestiaTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                        color = DarkBlue// Hex color))
                ) {
                    val navController = rememberNavController()
//                    NavGraph(
//                        navController = navController,
//                        startDestination = Screen.Admin.route
//                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AdminDashboard() {
    Image(painter = painterResource(id = R.drawable.dashboardmock), contentDescription = "Login Image",
        modifier = Modifier.size(1000.dp)
            .background(DarkBlue))

    Spacer(modifier = Modifier.height(50.dp))

    Text(text = "For Testing", fontSize = 50.sp, modifier =  Modifier.padding(50.dp,350.dp))
}

