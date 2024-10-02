package com.coco.celestia.screens.admin

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.coco.celestia.screens.coop.DropdownField
import com.coco.celestia.ui.theme.DarkBlue
import com.coco.celestia.ui.theme.Gray
import com.coco.celestia.ui.theme.PurpleGrey40
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.UserData


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagement(userViewModel: UserViewModel, navController: NavController) {
    var text by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val selectedUsers = userViewModel.selectedUsers
    val usersData by userViewModel.usersData.observeAsState()
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)
    val users: MutableList<UserData?> = mutableListOf()

    LaunchedEffect(Unit) {
        userViewModel.fetchUsers()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(DarkBlue)
                .padding(top = 20.dp)
                .verticalScroll(rememberScrollState()) // Scrollable column
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(DarkBlue)
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

            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(DarkBlue)
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
                    modifier = Modifier.background(Gray)
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
        when (userState) {
            UserState.LOADING -> {
                CircularProgressIndicator()
            }
            UserState.SUCCESS -> {
                usersData?.forEach{ user ->
                    //Add Conditional Statement for Coop Members Only not all Users
                    users.add(user)
                }
            }
            UserState.EMPTY -> {
                Text("No Users Found")
            }
            is UserState.ERROR -> {
                Toast.makeText(navController.context, "Error: ${(userState as UserState.ERROR).message}", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }

        UserTable(
            users = users,
            selectedUsers = selectedUsers,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 200.dp),
            userViewModel = userViewModel
        )
        Spacer(modifier = Modifier.height(100.dp))
    }
}

fun DropdownMenuItem(onClick: () -> Unit, interactionSource: @Composable () -> Unit) {

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserTable(users: List<UserData?>, selectedUsers: List<UserData?>, modifier: Modifier, userViewModel: UserViewModel) {
    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(24.dp))
                Text(
                    text = "EMAIL",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "NAME",
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth(),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "ROLE",
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth(),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
        itemsIndexed(users) { index, user ->
            if (user != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (index % 2 == 0) Color.LightGray else Color.White)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = user.isChecked.value,
                        onCheckedChange = { checked ->
                            user.isChecked.value = checked
                            if (checked) {
                                if (!selectedUsers.contains(user)) {
                                    userViewModel.addSelectedUser(user)
                                }
                            } else {
                                userViewModel.removeSelectedUser(user)
                            }
                        }
                    )
                    Text(
                        text = user.email,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = user.firstname + " " + user.lastname,
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = user.role,
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(95.dp))
        }
    }
}




@Composable
fun ActionButtons() {
    var showPopUpEdit by remember { mutableStateOf(false) }
    var showPopUpDelete by remember { mutableStateOf(false) }
    var firstTextFieldValue by remember { mutableStateOf("") }
    var secondTextFieldValue by remember { mutableStateOf("") }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CircleButton(onClick = { /* TODO: Handle notification click */ }, icon = Icons.Default.Menu)
        CircleButton(onClick = { showPopUpEdit = true }, icon = Icons.Default.Edit)
        CircleButton(onClick = { showPopUpDelete = true }, icon = Icons.Default.Delete)
    }

    if (showPopUpEdit) {
        AlertDialog(
            onDismissRequest = { showPopUpEdit = false },
            title = { Text(text = "Enter your input") },
            text = {
                Column {
                    //to be changed
                    TextField(
                        value = firstTextFieldValue,
                        onValueChange = { firstTextFieldValue = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    //change to dropdown
                    TextField(
                        value = secondTextFieldValue,
                        onValueChange = { secondTextFieldValue = it },
                        label = { Text("Role") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },

            confirmButton = {
                Button(
                    onClick = {
                        showPopUpEdit = false
                    }) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showPopUpEdit = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    if (showPopUpDelete) {
        AlertDialog(
            onDismissRequest = { showPopUpDelete = false },
            title = { Text(text = "Enter...") },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to delete this user?",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },

            confirmButton = {
                Button(
                    onClick = {
                        showPopUpDelete = false
                    }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showPopUpDelete = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CircleButton(onClick: () -> Unit, icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(DarkBlue)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White)
    }
}

@Composable
fun AddUserForm(
    email: String,
    role: String,
    addEmail: (String) -> Unit,
    addRole: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Add User ✚",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = addEmail,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

    }
}


// Sample data class for user
data class User(
    val id: String,
    val username: String,
    val roles: String,
    val status: String,
    var isChecked: MutableState<Boolean>,
    var showActionButtons: Boolean = false
)

// Sample data for the table
val sampleUsers = listOf(
    User("1", "user1", "Admin", "Active", mutableStateOf(false)),
    User("2", "user2", "User", "Inactive", mutableStateOf(false)),
    User("3", "user3", "Moderator", "Active", mutableStateOf(false))
)

