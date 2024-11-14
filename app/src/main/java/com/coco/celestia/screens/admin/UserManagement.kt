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
import androidx.compose.ui.res.painterResource
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
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagement(
    navController: NavController,
    userViewModel: UserViewModel,
    transactionViewModel: TransactionViewModel
) {
    var text by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf(UserData()) }
    val usersData by userViewModel.usersData.observeAsState(emptyList())
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)

    // Filter users by role and search text
    val filteredUsers = usersData.filter {
        (it.role == "CoopCoffee" || it.role == "CoopMeat") &&
                ("${it.firstname} ${it.lastname}".contains(text, ignoreCase = true))
    }

    LaunchedEffect(userState) {
        userViewModel.fetchUsers()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(BlueGradientBrush)
                .verticalScroll(rememberScrollState())
        ) {
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
                    onSearch = { query -> text = query },
                    active = false,
                    onActiveChange = {},
                    placeholder = { Text(text = "Search name...", color = DarkBlue) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon", tint = DarkBlue) },
                    modifier = Modifier
                        .width(screenWidth * 0.75f)
                        .offset(y = (-20).dp)
                        .semantics { testTag = "android:id/searchBar" }
                ) {}

                Spacer(modifier = Modifier.width(5.dp))

                Button(
                    onClick = {
                        navController.navigate(Screen.AdminUserManagementAuditLogs.route)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LightBlue),
                    modifier = Modifier
                        .padding(top = 25.dp)
                        .semantics { testTag = "android:id/auditLogsButton" }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.auditlogicon),
                        tint = Color.White,
                        contentDescription = "Audit Logs",
                        modifier = Modifier
                            .size(30.dp)
                            .semantics { testTag = "android:id/auditLogIcon" }
                    )
                }
            }

//            Spacer(modifier = Modifier.height(8.dp))
//
//            DropdownMenu(
//                expanded = expanded,
//                onDismissRequest = { expanded = false },
//                modifier = Modifier
//                    .background(Gray)
//                    .semantics { testTag = "android:id/roleDropdownMenu" }
//            ) {
//                // Dropdown items can be added here
//            }
        }

        UserTable(
            users = filteredUsers,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 100.dp)
                .semantics { testTag = "android:id/userTable" },
            onEditUserClick = { user ->
                selectedUser = user
            }
        )

        Spacer(modifier = Modifier.height(100.dp))

        if (selectedUser != UserData()) {
            EditUser(
                userViewModel = userViewModel,
                transactionViewModel = transactionViewModel,
                userData = selectedUser,
                onDismiss = {
                    selectedUser = UserData()
                }
            )
        }
    }
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
                        .offset(x = 5.dp)
                        .semantics { testTag = "android:id/userTableHeaderName" },
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    color = DarkBlue
                )
                Text(
                    text = "ROLE",
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth()
                        .padding(start = 20.dp)
                        .semantics { testTag = "android:id/userTableHeaderRole" },
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    color = DarkBlue
                )
                Text(
                    text = "EDIT",
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/userTableHeaderEdit" },
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = DarkBlue
                )
            }
        }
        itemsIndexed(users) { index, user ->
            if (user != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (index % 2 == 0) PaleBlue else Color.White)
                        .padding(10.dp)
                        .semantics { testTag = "android:id/userRow_$index" },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user.firstname + " " + user.lastname,
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxWidth()
                            .semantics { testTag = "android:id/userName_$index" },
                        textAlign = TextAlign.Start,
                        color = DarkBlue
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
                            .fillMaxWidth()
                            .semantics { testTag = "android:id/userRole_$index" },
                        textAlign = TextAlign.Start,
                        color = DarkBlue
                    )
                    IconButton(
                        onClick = {
                            onEditUserClick(user)
                        },
                        modifier = Modifier
                            .offset(x = (-30).dp)
                            .semantics { testTag = "android:id/editButton_$index" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit User",
                            tint = DarkBlue,
                            modifier = Modifier.semantics { testTag = "android:id/editIcon_$index" }
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