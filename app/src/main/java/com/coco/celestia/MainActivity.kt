package com.coco.celestia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.coco.celestia.ui.theme.CelestiaTheme
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CelestiaTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFF2E3DB)) // Hex color))
                ) {
                    LoginScreen()
                }
            }
        }
    }
}

@Preview
@Composable
fun LoginScreen(){

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painter = painterResource(id = R.drawable.a), contentDescription = "Login Image",
            modifier = Modifier.size(195.dp))

        Text(text = "CoCo", fontSize = 54.sp, fontWeight = FontWeight.Bold)
        Text(text = "Coop Connects", fontSize = 15.sp)

        Spacer(modifier = Modifier.height(35.dp))

        OutlinedTextField(value = "", onValueChange = {}, label = { Text(text = "Username")})

        Spacer(modifier = Modifier.height(2.dp))

        OutlinedTextField(value = "", onValueChange = {},
            label = { Text(text = "Password")},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation()
        )


        Spacer(modifier = Modifier.height(15.dp))

        Button(onClick = { },
            modifier = Modifier
                .width(285.dp)
                .height(50.dp)) {
            Text(text = "Login")
        }

        Spacer(modifier = Modifier.height(5.dp))
        Text(text = "Forgot Password?", fontSize = 13.sp, modifier = Modifier.clickable {  })
        Spacer(modifier = Modifier.height(85.dp))
        Text(text = "Don't have an account?", fontSize = 15.sp, modifier = Modifier.clickable {  })
        Text(text = "Register Now!", fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable {  })

    }
}