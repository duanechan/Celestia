package com.coco.celestia.screens.coop.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.components.toast.ToastStatus
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.FacilityState
import com.coco.celestia.viewmodel.FacilityViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.FacilityData

@Composable
fun AdminHome(
    navController: NavController,
    facilityViewModel: FacilityViewModel,
    userViewModel: UserViewModel,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    var currentView by remember { mutableStateOf("Dashboard") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White2)
    ) {
        // Navigation Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Green4),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NavigationTab(
                title = "Dashboard",
                iconRes = R.drawable.dashboard,
                isActive = currentView == "Dashboard",
                onClick = {
                    currentView = "Dashboard"
                }
            )
            NavigationTab(
                title = "Add Facility",
                iconRes = R.drawable.facility,
                isActive = currentView == "Add Facility",
                onClick = {
                    currentView = "Add Facility"
                }
            )
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .align(if (currentView == "Dashboard") Alignment.Start else Alignment.End),
            color = Green1,
            thickness = 2.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (currentView) {
            "Dashboard" -> {
                Facilities(facilityViewModel)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Special Requests",
                    color = Green1,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 8.dp)
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    SpecialRequestCard(title = "Pending", count = "0", onClick = { })
                    SpecialRequestCard(title = "In Progress", count = "0", onClick = { })
                    SpecialRequestCard(title = "Cancelled", count = "0", onClick = { })
                    SpecialRequestCard(title = "Turned Down", count = "0", onClick = { })
                }
            }
            "Add Facility" -> {
                AddFacilityForm(
                    navController = navController,
                    facilityViewModel = facilityViewModel,
                    userViewModel = userViewModel,
                    onEvent = { onEvent(it) })
            }
        }
    }
}

@Composable
fun Facilities(facilityViewModel: FacilityViewModel) {
    val facilitiesData by facilityViewModel.facilitiesData.observeAsState(emptyList())
    val facilityState by facilityViewModel.facilityState.observeAsState(FacilityState.LOADING)

    LaunchedEffect(Unit) {
        facilityViewModel.fetchFacilities()
    }

    Text(
        text = "My Facilities",
        color = Green1,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(vertical = 8.dp)
    )
    when (facilityState) {
        FacilityState.LOADING -> {
            Text(
                text = "Loading...",
                color = Color.Gray,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        FacilityState.EMPTY -> {
            Text(
                text = "No facilities yet.",
                color = Color.Gray,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        is FacilityState.ERROR -> {
            Text(
                text = "Error occurred: ${(facilityState as FacilityState.ERROR).message}",
                color = Color.Gray,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        FacilityState.SUCCESS -> {
            // TODO: WIP for now. Ginawa ko munang LazyRow para lang ma display.
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                itemsIndexed(facilitiesData) { _, facility ->
                    FacilityCard(facility)
                }
            }
        }
    }
}

@Composable
fun FacilityCard(facility: FacilityData) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .size(100.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = facility.icon),
                    contentDescription = null,
                    modifier = Modifier.size(25.dp)
                )
                Text(text = facility.name, color = Green1)
            }
        }
    }
}

@Composable
fun NavigationTab(
    title: String,
    iconRes: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                colorFilter = if (isActive) ColorFilter.tint(Green1) else null
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = if (isActive) Green1 else Color.Black,
                fontSize = 12.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFacilityForm(
    navController: NavController,
    userViewModel: UserViewModel,
    facilityViewModel: FacilityViewModel,
    onEvent: (Triple<ToastStatus, String, Long>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emails = remember { mutableStateListOf("") }
    var facilityFocused by remember { mutableStateOf(false) }
    var accessibleFocused by remember { mutableStateOf(false) }
    var iconPickerShown by remember { mutableStateOf(false) }
    var selectedIcon by remember { mutableIntStateOf(R.drawable.facility) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Green4),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .border(BorderStroke(2.dp, Green1), RoundedCornerShape(5.dp))
                            .size(75.dp)
                    ) {
                        if (selectedIcon == R.drawable.facility) {
                            Image(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Facility Image",
                                modifier = Modifier.size(40.dp).clickable { iconPickerShown = true }
                            )
                        } else {
                            Image(
                                painter = painterResource(id = selectedIcon),
                                contentDescription = "Facility Image",
                                modifier = Modifier.size(40.dp).clickable { iconPickerShown = true }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Facility Name",
                        color = Green1,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = {
                        Text(
                            text = "Enter name of Facility",
                            fontStyle = FontStyle.Italic,
                            color = if (facilityFocused) Color.Transparent else Color.Gray
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .onFocusChanged { focusState ->
                            facilityFocused = focusState.isFocused
                        },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = White1,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Accessible to:",
                    color = Green1,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    itemsIndexed(emails) { index, email ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            TextField(
                                value = email,
                                onValueChange = { newValue -> emails[index] = newValue },
                                placeholder = { Text(text = "Enter email address", color = if (accessibleFocused) Color.Transparent else Color.Gray) },
                                modifier = Modifier.weight(1f),
                                colors = TextFieldDefaults.textFieldColors(
                                    containerColor = White1,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    if (index == emails.size - 1) {
                                        emails.add("")
                                    } else {
                                        emails.removeAt(index)
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (index == emails.size - 1) Icons.Default.Add else Icons.Default.Clear,
                                    contentDescription = null,
                                    tint = Green1
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.isNotEmpty() && emails.all { it.isNotEmpty() }) {
                    facilityViewModel.createFacility(
                        icon = selectedIcon,
                        name = name,
                        emails = emails,
                        onComplete = {
                            onEvent(Triple(ToastStatus.SUCCESSFUL, "$name facility added.", System.currentTimeMillis()))
                            userViewModel.assignFacility(emails, name)
                            navController.navigate(Screen.Admin.route)
                        },
                        onError = { onEvent(Triple(ToastStatus.FAILED, it, System.currentTimeMillis())) }
                    )
                } else {
                    onEvent(Triple(ToastStatus.FAILED, "Please fill in all fields.", System.currentTimeMillis()))
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Green1),
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = "ADD", color = Color.White)
        }
    }

    if (iconPickerShown) {
        IconPicker(
            selected = selectedIcon,
            onDismiss = { iconPickerShown = false },
            onConfirm = {
                selectedIcon = it
                iconPickerShown = false
            }
        )
    }
}

// Icon picker for facility creation
@Composable
fun IconPicker(
    selected: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedIcon by remember { mutableIntStateOf(selected) }
    // If you wanna add icons to the icon picker, prefix the file with 'fac_icon_'.
    var icons = R.drawable::class.java.fields
        .filter { it.name.startsWith("fac_icon_") }
        .map { it.getInt(null) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = "Pick an icon for the facility:",
                fontFamily = mintsansFontFamily,
                fontSize = 16.sp,
            )
        },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(icons) { _, icon ->
                    Row {
                        IconButton(
                            onClick = { selectedIcon = icon },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = if (selectedIcon == icon) JadeGreen else Color.Transparent,
                                contentColor = if (selectedIcon == icon) White1 else Color.DarkGray
                            )
                        ) {
                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = "Cancel", fontFamily = mintsansFontFamily)
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(selectedIcon) },
                enabled = selectedIcon != selected
            ) {
                Text(text = "Confirm", fontFamily = mintsansFontFamily)
            }
        },
    )
}

@Composable
fun SpecialRequestCard(title: String, count: String, onClick: () -> Unit) {
    val imageRes = when (title) {
        "Pending" -> R.drawable.preparing
        "In Progress" -> R.drawable.progress
        "Cancelled" -> R.drawable.cancelled
        "Turned Down" -> R.drawable.cancel
        else -> R.drawable.warning
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Green4),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(min = 50.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "$title Icon",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Green1)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Green1,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = count,
                color = Green1,
                fontWeight = FontWeight.Bold
            )
        }
    }
}