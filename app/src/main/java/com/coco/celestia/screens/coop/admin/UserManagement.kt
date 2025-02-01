package com.coco.celestia.screens.coop.admin

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontStyle
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.TransactionViewModel
import com.coco.celestia.viewmodel.model.FacilityData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagement(
    navController: NavController,
    userViewModel: UserViewModel,
    facilityViewModel: FacilityViewModel,
    transactionViewModel: TransactionViewModel
) {
    var text by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf(UserData()) }
    var selectedTab by remember { mutableStateOf(0) }
    var filterExpanded by remember { mutableStateOf(false) }
    var selectedFacility by remember { mutableStateOf<String?>(null) }
    val usersData by userViewModel.usersData.observeAsState(emptyList())
    val userState by userViewModel.userState.observeAsState(UserState.LOADING)
    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(initial = emptyList())
    var refreshTrigger by remember { mutableStateOf(0) }

    val facilityRoles = facilitiesData.map { "Coop${it.name}" }
    val filteredUsers = usersData.filter { user ->
        when (selectedTab) {
            0 -> {
                val matchesSearch = "${user.firstname} ${user.lastname}".contains(text, ignoreCase = true)
                val matchesFacility = selectedFacility?.let { facility ->
                    user.role == "Coop$facility"
                } ?: true

                user.role.startsWith("Coop") && matchesSearch && matchesFacility
            }
            1 -> user.role == "Farmer" &&
                    "${user.firstname} ${user.lastname}".contains(text, ignoreCase = true)
            else -> false
        }
    }

    LaunchedEffect(refreshTrigger) {
        userViewModel.fetchUsers()
        facilityViewModel.fetchFacilities()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Green4)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Green4)
                    .padding(5.dp, 0.dp, 0.dp, 0.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchBar(
                    query = text,
                    onQueryChange = { text = it },
                    onSearch = { query -> text = query },
                    active = false,
                    onActiveChange = {},
                    placeholder = { Text(text = "Search name...", color = Green1) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            tint = Green1
                        )
                    },
                    modifier = Modifier
                        .width(screenWidth * 0.60f)
                        .height(50.dp)
                        .offset(y = (-12).dp)
                        .semantics { testTag = "android:id/searchBar" }
                ) {}

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        navController.navigate(Screen.AdminUserManagementAuditLogs.route)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green2),
                    contentPadding = PaddingValues(8.dp),
                    modifier = Modifier
                        .padding(top = 25.dp)
                        .height(36.dp)
                        .semantics { testTag = "android:id/auditLogsButton" }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.auditlogicon),
                        tint = Color.White,
                        contentDescription = "Audit Logs",
                        modifier = Modifier
                            .size(20.dp)
                            .semantics { testTag = "android:id/auditLogIcon" }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (selectedTab == 0) {
                    Box {
                        Button(
                            onClick = { filterExpanded = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Green2
                            ),
                            contentPadding = PaddingValues(8.dp),
                            modifier = Modifier
                                .padding(top = 25.dp)
                                .height(36.dp)
                                .semantics { testTag = "android:id/filterButton" }
                        ) {
                            Icon(
                                imageVector = Icons.Default.List,
                                contentDescription = "Filter",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = filterExpanded,
                            onDismissRequest = { filterExpanded = false },
                            modifier = Modifier
                                .background(Color.White)
                                .width(200.dp)
                                .semantics { testTag = "android:id/filterMenu" }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "All Facilities",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Green1,
                                        fontFamily = mintsansFontFamily
                                    )
                                },
                                onClick = {
                                    selectedFacility = null
                                    filterExpanded = false
                                }
                            )
                            Divider(color = Green4.copy(alpha = 0.2f))
                            facilitiesData.forEach { facility ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            facility.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Green1,
                                            fontFamily = mintsansFontFamily
                                        )
                                    },
                                    onClick = {
                                        selectedFacility = facility.name
                                        filterExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Green4,
                contentColor = Green1,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Green2,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Coop Members",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                            ),
                            fontWeight = FontWeight.Bold,
                            fontFamily = mintsansFontFamily
                        )
                    },
                    modifier = Modifier.semantics { testTag = "android:id/coopMembersTab" }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        selectedFacility = null
                    },
                    text = {
                        Text(
                            "Farmer Members",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                            ),
                            fontWeight = FontWeight.Bold,
                            fontFamily = mintsansFontFamily
                        )
                    },
                    modifier = Modifier.semantics { testTag = "android:id/farmerMembersTab" }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 140.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            UserTable(
                users = filteredUsers,
                userViewModel = userViewModel,
                selectedTab = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { testTag = "android:id/userTable" },
                onEditUserClick = { user ->
                    selectedUser = user
                },
                facilityViewModel = facilityViewModel
            )
        }

        if (selectedUser != UserData()) {
            EditUser(
                userViewModel = userViewModel,
                transactionViewModel = transactionViewModel,
                facilityViewModel = facilityViewModel,
                userData = selectedUser,
                onDismiss = {
                    selectedUser = UserData()
                    refreshTrigger += 1
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserTable(
    users: List<UserData>,
    userViewModel: UserViewModel,
    selectedTab: Int,
    modifier: Modifier,
    onEditUserClick: (UserData) -> Unit,
    facilityViewModel: FacilityViewModel
) {
    val userState by userViewModel.userState.observeAsState(initial = UserState.LOADING)
    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(initial = emptyList())
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedUserId by remember { mutableStateOf<String?>(null) }
    var selectedUserName by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    if (showDeleteDialog && selectedUserId != null && selectedUserName != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                selectedUserId = null
                selectedUserName = null
            },
            title = {
                Text(
                    "Confirm Deletion",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = mintsansFontFamily,
                    color = Green1
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete $selectedUserName?",
                    fontFamily = mintsansFontFamily,
                    color = Green1
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedUserId?.let { uid ->
                            userViewModel.deleteUser(uid)
                            Toast.makeText(context, "User deleted successfully", Toast.LENGTH_SHORT).show()
                        }
                        showDeleteDialog = false
                        selectedUserId = null
                        selectedUserName = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        selectedUserId = null
                        selectedUserName = null
                    }
                ) {
                    Text("Cancel", color = Green1)
                }
            },
            containerColor = Color.White
        )
    }

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
                Text(
                    text = "NAME",
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth()
                        .offset(x = 5.dp)
                        .semantics { testTag = "android:id/userTableHeaderName" },
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    color = Green1,
                    fontFamily = mintsansFontFamily
                )
                Text(
                    text = if (selectedTab == 0) "FACILITY" else "ROLE",
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth()
                        .padding(start = 20.dp)
                        .semantics { testTag = "android:id/userTableHeaderRole" },
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    color = Green1,
                    fontFamily = mintsansFontFamily
                )
                Text(
                    text = "ACTIONS",
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth()
                        .semantics { testTag = "android:id/userTableHeaderActions" },
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = Green1,
                    fontFamily = mintsansFontFamily
                )
            }
        }

        when (userState) {
            UserState.LOADING -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            UserState.SUCCESS -> {
                itemsIndexed(users) { index, user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (index % 2 == 0) White1 else Color.White)
                            .padding(10.dp)
                            .semantics { testTag = "android:id/userRow_$index" },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${user.firstname} ${user.lastname}",
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxWidth()
                                .semantics { testTag = "android:id/userName_$index" },
                            textAlign = TextAlign.Start,
                            color = Green1,
                            fontFamily = mintsansFontFamily
                        )

                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxWidth()
                                .semantics { testTag = "android:id/userRole_$index" }
                        ) {
                            FacilityStatusCell(
                                user = user,
                                facilitiesData = facilitiesData
                            )
                        }

                        Row(
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { onEditUserClick(user) },
                                modifier = Modifier.semantics { testTag = "android:id/editButton_$index" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit User",
                                    tint = Green1,
                                    modifier = Modifier.semantics { testTag = "android:id/editIcon_$index" }
                                )
                            }

                            IconButton(
                                onClick = {
                                    userViewModel.getUserUidByEmail(user.email) { uid ->
                                        if (uid != null) {
                                            selectedUserId = uid
                                            selectedUserName = "${user.firstname} ${user.lastname}"
                                            showDeleteDialog = true
                                        }
                                    }
                                },
                                modifier = Modifier.semantics { testTag = "android:id/deleteButton_$index" }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete User",
                                    tint = Color.Red,
                                    modifier = Modifier.semantics { testTag = "android:id/deleteIcon_$index" }
                                )
                            }
                        }
                    }
                }
            }
            UserState.EMPTY -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No users found")
                    }
                }
            }
            is UserState.ERROR -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (userState as UserState.ERROR).message,
                            color = Color.Red
                        )
                    }
                }
            }
            else -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Loading users...")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
fun FacilityStatusCell(
    user: UserData,
    facilitiesData: List<FacilityData>,
    modifier: Modifier = Modifier
) {
    val facilityExists = if (user.role.startsWith("Coop") && user.role != "Coop") {
        val facilityName = user.role.removePrefix("Coop")
        facilitiesData.any { it.name == facilityName }
    } else {
        true
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = when {
                user.role == "Farmer" -> "Farmer"
                user.role == "Coop" -> "No Facility"
                else -> user.role.removePrefix("Coop")
            },
            color = if (!facilityExists) Color.Red else Green1,
            fontFamily = mintsansFontFamily,
            fontStyle = if (!facilityExists) FontStyle.Italic else FontStyle.Normal
        )

        if (!facilityExists) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Facility no longer exists",
                tint = Color.Red,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}