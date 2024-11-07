package com.coco.celestia.screens.client

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.viewmodel.model.ContactData
import com.coco.celestia.ui.theme.ClientBG
import com.coco.celestia.ui.theme.CLightGreen
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
            .background(ClientBG)
            .padding(top = 75.dp)
            .verticalScroll(rememberScrollState())
            .semantics { testTag = "android:id/ClientContactScreen" }
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp, start = 25.dp, end = 16.dp)
                .semantics { testTag = "android:id/SearchBarRow" },
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchBar(
                query = text,
                onQueryChange = { newText -> text = newText },
                onSearch = {},
                active = active,
                onActiveChange = { active = false },
                placeholder = { Text(text = "Search...", color = Color.Black, fontSize = 15.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon") },
                modifier = Modifier
                    .height(50.dp)
                    .offset(y = -13.dp)
                    .fillMaxWidth()
                    .semantics { testTag = "android:id/ContactSearchBar" }
            ) {
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (contactState) {
            is ContactState.LOADING -> {
                Text(
                    text = "Loading contacts...",
                    modifier = Modifier.semantics { testTag = "android:id/LoadingText" }
                )
            }
            is ContactState.ERROR -> {
                Text(
                    text = "Failed to load contacts: ${(contactState as ContactState.ERROR).message}",
                    modifier = Modifier.semantics { testTag = "android:id/ErrorText" }
                )
            }
            is ContactState.SUCCESS -> {
                ContactList(contactData)
            }
            is ContactState.EMPTY -> {
                Text(
                    text = "No contacts found.",
                    modifier = Modifier.semantics { testTag = "android:id/EmptyText" }
                )
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
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

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .width(500.dp)
            .height(if (expanded) 195.dp else 165.dp)
            .offset(x = (-16).dp, y = 0.dp)
            .padding(top = 0.dp, bottom = 5.dp, start = 30.dp, end = 0.dp)
            .clickable { expanded = !expanded }
            .semantics { testTag = "android:id/ContactCard_${contact.name}" },
        colors = CardDefaults.cardColors(containerColor = CLightGreen)
    ) {
        Column(
            Modifier
                .padding(16.dp)
        ) {
            Text(
                text = name,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .padding(top = 15.dp, start = 10.dp)
                    .semantics { testTag = "android:id/ContactName_${contact.name}" }
            )
            Text(
                text = role,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier
                    .padding(top = 15.dp, start = 10.dp)
                    .semantics { testTag = "android:id/ContactRole_${contact.name}" }
            )
            if (expanded) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Contact Number: $contactNumber",
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier
                        .padding(top = 10.dp, start = 10.dp)
                        .semantics { testTag = "android:id/ContactNumber_${contact.name}" }
                )
                Text(
                    text = "Email: $email",
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier
                        .padding(top = 5.dp, start = 10.dp)
                        .semantics { testTag = "android:id/ContactEmail_${contact.name}" }
                )
            }
            Text(
                text = if (expanded) "Show Less" else "Show More",
                fontSize = 16.sp,
                color = Color.LightGray,
                modifier = Modifier
                    .padding(top = 10.dp, start = 10.dp)
                    .semantics { testTag = "android:id/ExpandText_${contact.name}" }
            )
        }
    }
}