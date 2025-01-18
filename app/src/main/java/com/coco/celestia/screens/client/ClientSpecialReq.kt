@file:OptIn(ExperimentalMaterial3Api::class)

package com.coco.celestia.screens.client

import android.app.DatePickerDialog
import android.widget.DatePicker
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.*
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.SpecialReqState
import com.coco.celestia.viewmodel.SpecialRequestViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.VegetableViewModel
import com.coco.celestia.viewmodel.model.Constants
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
import com.coco.celestia.viewmodel.model.ProductReq
import com.coco.celestia.viewmodel.model.ProductReqValidation
import com.coco.celestia.viewmodel.model.SpecialRequest
import com.coco.celestia.viewmodel.model.TrackRecord
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.UUID

@Composable
fun DisplaySpecialReq(
    navController: NavController
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val specialReqViewModel: SpecialRequestViewModel = viewModel()
    val specialReqData by specialReqViewModel.specialReqData.observeAsState(emptyList())
    val specialReqState by specialReqViewModel.specialReqState.observeAsState(SpecialReqState.LOADING)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val filters = listOf(
        "To Review",
        "In Progress",
        "Completed",
        "Cancelled"
    )

    LaunchedEffect(Unit) {
        specialReqViewModel.fetchSpecialRequests(
            filter = "",
            orderBy = "Requested",
            ascending = false
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Green4)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .background(White1, shape = RoundedCornerShape(12.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.padding(start = 16.dp)
                    )

                    TextField(
                        value = "", // text
                        onValueChange = {}, // newText -> text = newText
                        placeholder = { Text("Search") },
                        modifier = Modifier
                            .weight(1f),
                        maxLines = 1,
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }

                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 0.dp
                ) {
                    filters.forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            modifier = Modifier.background(Green4),
                            text = {
                                Text(
                                    text = label,
                                    fontFamily = mintsansFontFamily,
                                    modifier = Modifier.padding(horizontal = 5.dp),
                                    color = Green1,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                    }
                }

                when (specialReqState) {
                    SpecialReqState.LOADING -> {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    SpecialReqState.EMPTY -> {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = "No Orders",
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No Orders Found")
                            }
                        }
                    }
                    SpecialReqState.SUCCESS -> {
                        val userRequests = specialReqData
                            .filter { it.uid == uid }
                            .sortedByDescending { request ->
                                LocalDateTime.parse(
                                    request.dateRequested,
                                    DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
                                )
                            }
                        val filteredRequests = userRequests.filter { it.status == filters[selectedTabIndex] }
                        Column(modifier = Modifier.fillMaxSize()) {
                            if (filteredRequests.isNotEmpty()) {
                                for (request in filteredRequests) {
                                    OrderCard(
                                        order = request,
                                        index = request.specialRequestUID,
                                        navController = navController
                                    )
                                }
                            } else {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Icon(
                                            imageVector = Icons.Default.ShoppingCart,
                                            contentDescription = "No Orders",
                                            modifier = Modifier.size(30.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("No Orders Found")
                                    }
                                }
                            }
                        }
                    }
                    is SpecialReqState.ERROR -> {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = (specialReqState as SpecialReqState.ERROR).message,
                                color = Color.Red
                            )
                        }
                    }
                }
            }
        }

        Icon(
            painter = painterResource(R.drawable.add),
            contentDescription = "Add",
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
                .size(56.dp)
                .clickable {
                    navController.navigate(Screen.ClientAddSpecialReq.route)
                }
        )
    }
}

@Composable
fun OrderCard(
    order: SpecialRequest,
    index: String,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                navController.navigate(Screen.ClientSpecialReqDetails.createRoute(order.specialRequestUID))
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = White1
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = order.subject,
                fontWeight = FontWeight.Bold,
                color = Green1,
                fontSize = 16.sp,
                fontFamily = mintsansFontFamily
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Date Requested: ${order.dateRequested}",
                    color = Green1,
                    fontSize = 14.sp,
                    fontFamily = mintsansFontFamily
                )

                Text(
                    text = order.status,
                    fontWeight = FontWeight.Bold,
                    color = Green1,
                    fontSize = 14.sp,
                    fontFamily = mintsansFontFamily
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Target Date: ${order.targetDate}",
                color = Green1,
                fontSize = 14.sp,
                fontFamily = mintsansFontFamily
            )
        }
    }
}

@Composable
fun AddSpecialReq(
    navController: NavController,
    specialRequestViewModel: SpecialRequestViewModel,
    userViewModel: UserViewModel,
    vegetableViewModel: VegetableViewModel
) {
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val productRows = remember { mutableStateListOf(ProductReq()) }
    var targetDate by remember { mutableStateOf("") }
    var collectionMethod by remember { mutableStateOf("") }
    var additional by remember { mutableStateOf("") }
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val userData by userViewModel.userData.observeAsState()
    val vegetables by vegetableViewModel.vegData.observeAsState()
    val trackRecord = remember { mutableStateListOf<TrackRecord>() }
    var deliveryAddress by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
    val formattedDateTime = currentDateTime.format(formatter)

    val parsedDateTme = LocalDateTime.parse(formattedDateTime, formatter)
    val updatedDateTime = parsedDateTme.plusSeconds(1)
    val plusOneDateTime = updatedDateTime.format(formatter)

    var subjectEmpty by remember { mutableStateOf(false) }
    var targetDateEmpty by remember { mutableStateOf(false) }
    val productEmpty = remember { mutableStateListOf(ProductReqValidation()) }
    var collectionMethodEmpty by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vegetableViewModel.fetchVegetables()
        userViewModel.fetchUser(uid)
    }

    val vegetableList = vegetables?.map { it.name } ?: emptyList()

    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Subject",
                fontWeight = FontWeight.Bold
            )

            if (subjectEmpty) {
                Text(
                    text = "Subject is Empty!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                )
            }
        }

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it},
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        Text(
            text = "Description",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it},
            maxLines = 5,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        Row (
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Product Name",
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .weight(1f),
            )

            Text(
                text = "Quantity",
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .weight(1f),
            )
        }

        productRows.forEachIndexed { index, productRow ->
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AutocompleteTextField(
                    suggestions = vegetableList,
                    onSuggestionClick = { selectedProduct ->
                        productRows[index] = productRow.copy(name = selectedProduct)
                        productEmpty[index] = productEmpty[index].copy(name = selectedProduct.isEmpty())
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = if (productRow.quantity == 0) "" else productRow.quantity.toString(),
                    onValueChange = { newValue ->
                        val newIntValue = newValue.toIntOrNull()
                        if (newIntValue != null) {
                            productRows[index] = productRow.copy(quantity = newIntValue)
                            productEmpty[index] = productEmpty[index].copy(quantity = newIntValue <= 0)
                        }
                    },
                    label = { Text("Enter Quantity") },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (productRows.size > 1) {
                    Icon (
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier
                            .clickable {
                                productRows.removeAt(index)
                                productEmpty.removeAt(index)
                            }
                    )
                }
            }

            if (productEmpty[index].name) {
                Text(
                    text = "Product name cannot be empty!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (productEmpty[index].quantity) {
                Text(
                    text = "Quantity must be greater than 0!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        Button(
            onClick = {
                productRows.add(ProductReq())
                productEmpty.add(ProductReqValidation())
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Green4,
                contentColor = Green1
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = "Add Product")
        }

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Target Date",
                fontWeight = FontWeight.Bold
            )

            if (targetDateEmpty) {
                Text(
                    text = "Target Date is Empty!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                )
            }
        }

        OutlinedTextField(
            value = targetDate,
            onValueChange = { targetDate = it },
            singleLine = true,
            readOnly = true,
            modifier = Modifier.padding(vertical = 4.dp),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        DatePickerDialog(
                            context,
                            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                                val formattedDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                                calendar.set(year, month, dayOfMonth)
                                targetDate = formattedDate.format(calendar.time)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    }
                )
            }
        )

        Text(
            text = "Collection Method",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(vertical = 4.dp)
        )

        Row (
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = collectionMethod == Constants.COLLECTION_PICKUP,
                onClick = {
                    collectionMethod = Constants.COLLECTION_PICKUP
                    deliveryAddress = "City Vet Office, Baguio City"
                }
            )
            Text(Constants.COLLECTION_PICKUP)
        }

        Row (
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = collectionMethod == Constants.COLLECTION_DELIVERY,
                onClick = {
                    collectionMethod = Constants.COLLECTION_DELIVERY
                    deliveryAddress = "${userData?.streetNumber}, ${userData?.barangay}"
                }
            )
            Text(Constants.COLLECTION_DELIVERY)
        }

        if (collectionMethodEmpty) {
            Text(
                text = "Please Choose a Collection Method!",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Text(
            text = "Additional Requests",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(vertical = 4.dp)
        )
        OutlinedTextField(
            value = additional,
            onValueChange = { additional = it},
            maxLines = 5,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        Button(
            onClick = {
                subjectEmpty = subject.isEmpty()
                targetDateEmpty = targetDate.isEmpty()
                collectionMethodEmpty = collectionMethod.isEmpty()

                productEmpty.forEachIndexed{ index, _ ->
                    productEmpty[index] = ProductReqValidation(
                        productRows[index].name.isEmpty(),
                        productRows[index].quantity <= 0
                    )
                }

                if (!subjectEmpty && !targetDateEmpty && !collectionMethodEmpty &&
                    productEmpty.all { !it.name && !it.quantity }) {
                    val orderPlaced = TrackRecord (
                        description = "Order Request is placed.",
                        dateTime = formattedDateTime
                    )

                    val orderReview = TrackRecord(
                        description = "Order Request is being reviewed.",
                        dateTime = plusOneDateTime
                    )
                    trackRecord.add(orderPlaced)
                    trackRecord.add(orderReview)

                    val specialReq = SpecialRequest (
                        subject = subject,
                        description = description,
                        products = productRows,
                        targetDate = targetDate,
                        collectionMethod = collectionMethod,
                        additionalRequest = additional,
                        email = userData?.email.toString(),
                        uid = uid,
                        status = "To Review",
                        name = "${userData?.firstname} ${userData?.lastname}",
                        dateRequested = formattedDateTime,
                        specialRequestUID = "SR-${UUID.randomUUID()}",
                        trackRecord = trackRecord,
                    )

                    specialRequestViewModel.addSpecialRequest(
                        uid,
                        specialReq
                    )
                    navController.navigate(Screen.ClientSpecialReq.route)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Green4,
                contentColor = Green1
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = "Submit Request")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutocompleteTextField(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier
) {
    var query by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isDropdownExpanded,
        onExpandedChange = { isDropdownExpanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { newQuery ->
                query = newQuery
                isDropdownExpanded = newQuery.isNotEmpty()
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            label = { Text("Enter Product") },
            singleLine = true
        )

        ExposedDropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false },
            modifier = Modifier
                .heightIn(max = 200.dp)
        ) {
            val filteredSuggestions = suggestions.filter {
                it.contains(query, ignoreCase = true)
            }
            if (filteredSuggestions.isNotEmpty()) {
                filteredSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            query = suggestion
                            onSuggestionClick(suggestion)
                            isDropdownExpanded = false
                        }
                    )
                }
            } else {
                DropdownMenuItem(
                    text = { Text("No suggestions found") },
                    onClick = {}
                )
            }
        }
    }
}