package com.coco.celestia.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coco.celestia.NavGraph
import com.coco.celestia.R
import com.coco.celestia.ui.theme.CelestiaTheme
import com.coco.celestia.util.isValidEmail
import com.coco.celestia.viewmodel.ProductState
import com.coco.celestia.viewmodel.UserViewModel

@Preview
@Composable
fun ProfilePagePreview() {
    val firstName = "John Mclean"
    val lastName = "Doe"
    val email = "johndoe@gmail.com"
    val phoneNumber = "123-456-789"
    val houseNo = "66"
    val barangay = "Bakakeng"

    CelestiaTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2E3DB))
        ) {
            ProfileScreen(
                firstName,
                lastName,
                email,
                phoneNumber,
                houseNo,
                barangay
            )
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    firstName: String,
    lastName: String,
    email: String,
    phoneNumber: String,
    houseNo: String,
    barangay: String
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .padding(16.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.profile_icon),
            contentDescription = "Profile Icon",
            modifier = Modifier.size(100.dp)
        )
        Text(text = "$firstName $lastName", fontWeight = FontWeight.Bold, fontSize = 25.sp)

        OutlinedTextField(
            value = email,
            onValueChange = {},
            label = { Text(text = "Email") },
            singleLine = true,
            maxLines = 1,
            modifier = Modifier
        )
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {},
            label = { Text(text = "Phone Number") },
            singleLine = true,
            maxLines = 1,
            modifier = Modifier
        )
        OutlinedTextField(
            value = houseNo,
            onValueChange = {},
            label = { Text(text = "House No.") },
            singleLine = true,
            maxLines = 1,
            modifier = Modifier
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                value = barangay,
                onValueChange = {},
                label = { Text("Barangay") },
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                DropdownMenuItem(text = { Text("Pinsao") }, onClick = {})
                DropdownMenuItem(text = { Text("Trancoville") }, onClick = {})
                DropdownMenuItem(text = { Text("Quezon Hill") }, onClick = {})
                DropdownMenuItem(text = { Text("Bakakeng Sur") }, onClick = {})
                DropdownMenuItem(text = { Text("Camp 8") }, onClick = {})
            }
        }

    }
}

@Composable
fun ProfilePage(
    navController: NavController,
    userViewModel: UserViewModel
) {
    val userData by userViewModel.userData.observeAsState()
    val firstName = userData!!.firstname
    val lastName = userData!!.lastname
    val email = userData!!.email
    TODO("Add these attributes to the users database.")
    val phoneNumber = ""
    val houseNo = ""
    val barangay = ""

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ProfileScreen(
            firstName,
            lastName,
            email,
            phoneNumber,
            houseNo,
            barangay
        )
    }
}

