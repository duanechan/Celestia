package com.coco.celestia.screens.client

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.viewmodel.ContactData
import com.coco.celestia.R
import com.coco.celestia.ui.theme.VeryDarkPurple
import com.coco.celestia.ui.theme.VeryDarkGreen
import com.coco.celestia.viewmodel.ContactState
import com.coco.celestia.viewmodel.ContactViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientContact(contactViewModel: ContactViewModel) {
    val contactData by contactViewModel.contactData.observeAsState(emptyList())
    val contactState by contactViewModel.contactState.observeAsState(ContactState.LOADING)
    var text by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    LaunchedEffect(text) {
        contactViewModel.fetchContacts(
            filter = text
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 75.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(VeryDarkGreen)
                .padding(top = 27.dp, bottom = 8.dp, start = 25.dp, end = 16.dp)
        ) {
            Text(text = "Contact Inquiry", fontSize = 31.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { /* Handle notification click */ }) {
                Image(
                    painter = painterResource(id = R.drawable.notification_icon),
                    contentDescription = "Notification Icon",
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(VeryDarkPurple)
                .padding(top = 10.dp, bottom = 15.dp, start = 25.dp, end = 16.dp)
        ) {
            SearchBar(
                query = text,
                onQueryChange = { newText -> text = newText },
                onSearch = { /* Handle search action */ },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text(text = text, color = Color.Black, fontSize = 15.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon") },
                modifier = Modifier
                    .width(225.dp)
                    .height(35.dp)
            ) {
                //TODO
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (contactState) {
            is ContactState.LOADING -> {
                Text("Loading contacts...")
            }
            is ContactState.ERROR -> {
                Text("Failed to load contacts: ${(contactState as ContactState.ERROR).message}")
            }
            is ContactState.SUCCESS -> {
                ContactList(contactData)
            }
            is ContactState.EMPTY -> {
                Text("No contacts found.")
            }
        }
    }
}

@Composable
fun ContactList(contactList: List<ContactData>) {
    contactList.forEach { contact ->
        ItemCards(contact)
    }
}

@Composable
fun ItemCards(contact: ContactData) {
    val name = contact.name
    val role = contact.role
    val contactNumber = contact.contactNumber
    val email = contact.email

    Card(
        modifier = Modifier
            .width(500.dp)
            .height(200.dp)
            .offset(x = (-16).dp, y = 0.dp)
            .padding(top = 0.dp, bottom = 5.dp, start = 30.dp, end = 0.dp),
        colors = CardDefaults.cardColors(containerColor = VeryDarkGreen)
    ) {
        var expanded by remember { mutableStateOf(false) }
        Column(
            Modifier
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Text(
                text = name,
                fontSize = 35.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 15.dp, start = 10.dp)
            )
            Text(
                text = role,
                fontSize = 25.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                modifier = Modifier.padding(top = 15.dp, start = 10.dp)
            )
            // Optional: Display more details if expanded
            if (expanded) {
                Text(
                    text = "Contact Number: $contactNumber",
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier.padding(top = 10.dp, start = 10.dp)
                )
                Text(
                    text = "Email: $email",
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier.padding(top = 5.dp, start = 10.dp)
                )
            }
        }
    }
}