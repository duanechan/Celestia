package com.coco.celestia

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.ui.theme.LightGreen
import com.coco.celestia.ui.theme.PurpleGrey40
import com.coco.celestia.viewmodel.UserViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagement(userViewModel: UserViewModel) {
    val usersData by userViewModel.usersData.observeAsState()
    var text by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userViewModel.fetchUsers()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 75.dp)
                .verticalScroll(rememberScrollState()) // Scrollable column
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(LightGreen)
                    .padding(top = 27.dp, bottom = 8.dp, start = 25.dp, end = 16.dp)
            ) {
                Text(
                    text = "User Account Management",
                    fontSize = 31.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar and Filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .background(PurpleGrey40)
                    .padding(10.dp)
            ) {
                SearchBar(
                    query = text,
                    onQueryChange = { text = it },
                    onSearch = {},
                    active = active,
                    onActiveChange = { active = it },
                    placeholder = {
                        Text(
                            text = "Search...",
                            color = Color.Black,
                            fontSize = 15.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon"
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(35.dp)
                ){}

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Filter Icon",
                        tint = Color.White
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(onClick = { /* Handle filter option 1 */ }) {
                        Text(text = "Filter Option 1")
                    }
                    DropdownMenuItem(onClick = { /* Handle filter option 2 */ }) {
                        Text(text = "Filter Option 2")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        UserTable(
            users = sampleUsers,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 250.dp)
        )
    }
}

fun DropdownMenuItem(onClick: () -> Unit, interactionSource: @Composable () -> Unit) {

}

@Composable
fun UserTable(users: List<User>, modifier: Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "User ID",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Username",
                    modifier = Modifier.weight(2f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Roles",
                    modifier = Modifier.weight(2f),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Status",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // List items
        items(users) { user ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color.LightGray)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.id,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = user.username,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    text = user.roles,
                    modifier = Modifier.weight(2f)
                )
                Text(
                    text = user.status,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Checkbox(
                    checked = user.isChecked,
                    onCheckedChange = { checked ->
                        user.isChecked = checked
                    }
                )
            }
        }
    }
}



// Sample data class for user
data class User(
    val id: String,
    val username: String,
    val roles: String,
    val status: String,
    var isChecked: Boolean
)

// Sample data for the table
val sampleUsers = listOf(
    User("1", "user1", "Admin", "Active", false),
    User("2", "user2", "User", "Inactive", false),
    User("3", "user3", "Moderator", "Active", false)
)

