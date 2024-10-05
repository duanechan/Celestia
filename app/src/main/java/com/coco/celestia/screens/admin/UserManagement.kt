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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.DarkBlue
import com.coco.celestia.ui.theme.Gray
import com.coco.celestia.ui.theme.mintsansFontFamily
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.UserData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagement(userViewModel: UserViewModel, navController: NavController) {
    var text by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val selectedUsers by userViewModel.selectedUsers.observeAsState(emptyList())
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
                .verticalScroll(rememberScrollState()) // Scrollable column
        ) {
            // Search Bar
            Spacer(modifier = Modifier.height(110.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .height(70.dp)
                    .background(DarkBlue)
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
                            fontSize = 15.sp,
                            fontFamily = mintsansFontFamily

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

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Gray)
                ) {

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
    TopBar("User Management")
    Spacer(modifier = Modifier.height(15.dp))
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
                    fontFamily = mintsansFontFamily,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "NAME",
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "ROLE",
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth(),
                    fontWeight = FontWeight.Bold,
                    fontFamily = mintsansFontFamily,
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
                        checked = user.isChecked,
                        onCheckedChange = { checked ->
                            user.isChecked = checked
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
                        textAlign = TextAlign.Center,
                        fontFamily = mintsansFontFamily
                    )
                    Text(
                        text = user.firstname + " " + user.lastname,
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontFamily = mintsansFontFamily
                    )
                    Text(
                        text = user.role,
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontFamily = mintsansFontFamily
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
fun ActionButtons(onClearSelect: () -> Unit) {
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
                        onClearSelect() // Callback to clear selected users
                        // TODO: Edit selected users
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
                        onClearSelect() // Callback to clear selected users
                        // TODO: Delete selected users
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkBlue)
    ) {
        TopAppBar(
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontFamily = mintsansFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White
            ),
            modifier = Modifier
                .background(Color.Transparent)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUserForm(
    navController: NavController,
    email: String,
    role: String,
    newEmail: (String) -> Unit,
    newRole: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("Coffee", "Meat")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(50.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(
                onClick = { navController.navigate(Screen.AdminUserManagement.route) },
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Add User ✚",
                fontFamily = mintsansFontFamily,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = newEmail,
            label = { Text("Email", fontFamily = mintsansFontFamily) },
            modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                readOnly = true,
                value = role,
                onValueChange = {},
                placeholder = { Text("Select Role", fontFamily = mintsansFontFamily) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                roles.forEach { roleItem ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(roleItem) },
                        onClick = {
                            newRole(roleItem)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
