package com.coco.celestia.screens.admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.UserState
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.UserData
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.navigation.NavController
import com.coco.celestia.screens.`object`.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagement(navController: NavController, userViewModel: UserViewModel) {
    var text by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf(UserData()) }
    val usersData by userViewModel.usersData.observeAsState(emptyList())
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)
    val users: MutableList<UserData> = mutableListOf()

    LaunchedEffect(userState) {
        userViewModel.fetchUsers()
    }
    usersData.forEach{ user ->
        //Add Conditional Statement for Coop Members Only not all Users
        if (user.role == "CoopCoffee" || user.role == "CoopMeat") {
            users.add(user)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(BlueGradientBrush)
                .verticalScroll(rememberScrollState()) // Scrollable column
        ) {

            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BlueGradientBrush)
                    .padding(5.dp, 0.dp, 0.dp, 0.dp),
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
                        .width(screenWidth * 0.75f)
                        .offset(y = 75.dp)
                        .semantics { testTag = "searchBar" }
                ) {}
                Spacer(modifier = Modifier.width(5.dp))
                Button(
                    onClick = {
                        navController.navigate(Screen.AdminUserManagementAuditLogs.route)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightBlue
                    ),
                    modifier = Modifier
                        .padding(top = 120.dp)
                        .semantics { testTag = "auditLogsButton" }
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        tint = Color.White,
                        contentDescription = "Audit Logs",
                        modifier = Modifier
                            .size(35.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Gray)
                    .semantics { testTag = "roleDropdownMenu" }
            ) {
                // Dropdown items can be added here
            }
        }

        UserTable(
            users = users,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 200.dp)
                .semantics { testTag = "userTable" },
            onEditUserClick = { user ->
                selectedUser = user
            }
        )
        Spacer(modifier = Modifier.height(100.dp))

        if(selectedUser != UserData()) {
            EditUser(
                userViewModel = userViewModel,
                userData = selectedUser,
                onDismiss = {
                    selectedUser = UserData()
                }
            )
        }
    }
}

fun DropdownMenuItem(onClick: () -> Unit, interactionSource: @Composable () -> Unit) {
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserTable(users: List<UserData?>, modifier: Modifier, onEditUserClick: (UserData) -> Unit) {
    var role by remember { mutableStateOf("") }
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
                Spacer(modifier = Modifier.width(15.dp))
                Text(
                    text = "NAME",
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth()
                        .offset(x = 5.dp),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "ROLE",
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth()
                        .padding(start = 20.dp),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = "EDIT",
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth(),
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
        itemsIndexed(users) { index, user ->
            if (user != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (index % 2 == 0) PaleBlue else Color.White)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.firstname + " " + user.lastname,
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Start,
                    )
                    role = if (user.role == "CoopCoffee") {
                        "Coffee"
                    } else {
                        "Meat"
                    }

                    Text(
                        text = role,
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Start,
                    )
                    IconButton(
                        onClick = {
                            onEditUserClick(user)
                        },
                        modifier = Modifier.offset(x = (-30).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit User",
                            modifier = Modifier
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}