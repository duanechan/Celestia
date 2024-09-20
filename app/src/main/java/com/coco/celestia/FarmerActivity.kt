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
import com.coco.celestia.ui.theme.BgColor
import com.coco.celestia.ui.theme.CelestiaTheme

class FarmerActivity: ComponentActivity(){
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CelestiaTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgColor) // Hex color))
                ) {
                    val navController = rememberNavController()
//                    NavGraph(
//                        navController = navController,
//                        startDestination = Screen.Farmer.route
//                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun FarmerDashboard() {
    Image(painter = painterResource(id = R.drawable.clientdashboardmock), contentDescription = "Login Image",
        modifier = Modifier.size(1000.dp))

    Spacer(modifier = Modifier.height(50.dp))

    Text(text = "Farmer Dashboard Test", fontSize = 50.sp, modifier =  Modifier.padding(50.dp,350.dp))
}


