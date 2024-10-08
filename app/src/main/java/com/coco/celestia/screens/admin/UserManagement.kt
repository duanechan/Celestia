package com.coco.celestia.screens.admin

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.ui.theme.DarkBlue
import com.coco.celestia.ui.theme.Gray
import com.coco.celestia.ui.theme.mintsansFontFamily
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.UserData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagement(userViewModel: UserViewModel, onEditUserClick: () -> Unit) {
    var text by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val selectedUsers by userViewModel.selectedUsers.observeAsState(emptyList())
    val usersData by userViewModel.usersData.observeAsState()
    val userState by userViewModel.userState.observeAsState()
    val users: MutableList<UserData?> = mutableListOf()

    LaunchedEffect(userState) {
        userViewModel.fetchUsers()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(DarkBlue)
                .verticalScroll(rememberScrollState()) // Scrollable column
        ) {
            TopBarAdmin("User Management")

            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBlue)
                    .padding(5.dp, 0.dp, 5.dp, 0.dp),
                        horizontalArrangement = Arrangement.Center
            ) {
                SearchBar(
                    query = text,
                    onQueryChange = { text = it },
                    onSearch = { text = it },
                    active = false,
                    onActiveChange = {},
                    placeholder = { Text(text = "Search", color = DarkBlue) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon") },
                    modifier = Modifier
                        .width(screenWidth * 0.9f) // will make the searchbar 90% of the screen width
                        .offset(y=(-50.dp))
                ) {}
            }
            Spacer(modifier = Modifier.height(8.dp))

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(Gray)
                ) {

                }
            }
        }

        usersData?.forEach{ user ->
            //Add Conditional Statement for Coop Members Only not all Users
            users.add(user)
        }

        UserTable(
            users = users,
            selectedUsers = selectedUsers,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 200.dp),
            userViewModel = userViewModel, onEditUserClick = {onEditUserClick()}
        )
        Spacer(modifier = Modifier.height(100.dp))
    }

fun DropdownMenuItem(onClick: () -> Unit, interactionSource: @Composable () -> Unit) {
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UserTable(users: List<UserData?>, selectedUsers: List<UserData?>, modifier: Modifier, userViewModel: UserViewModel, onEditUserClick: () -> Unit) {
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
                    IconButton(
                        onClick = {
                            // Set the user as the selected user and trigger the edit popup
                            onEditUserClick()
                            userViewModel.addSelectedUser(user)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit User"
                        )
                    }
                    Text(
                        text = user.email,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = user.firstname + " " + user.lastname,
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = user.role,
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarAdmin(title: String) {
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