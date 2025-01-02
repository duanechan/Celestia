@file:OptIn(ExperimentalMaterial3Api::class)

package com.coco.celestia.screens.client

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.navigation.NavController
import com.coco.celestia.R
import com.coco.celestia.screens.`object`.Screen
import com.coco.celestia.ui.theme.Green4
import com.coco.celestia.viewmodel.SpecialRequestViewModel
import com.coco.celestia.viewmodel.UserViewModel
import com.coco.celestia.viewmodel.model.ProductReq
import com.coco.celestia.viewmodel.model.ProductReqValidation
import com.coco.celestia.viewmodel.model.SpecialRequest
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

@Composable
fun DisplaySpecialReq(
    navController: NavController
) {
    Box (
        modifier = Modifier.fillMaxSize()
    ) {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(Green4)
        ){
            val filters = listOf(
                "To Review",
                "In Progress",
                "Completed",
                "Cancelled"
            )

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color.White, shape = RoundedCornerShape(12.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon (
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier.padding(start = 16.dp)
                )

                TextField(
                    value = "", //text
                    onValueChange = {}, //newText -> text = newText
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

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filters.forEach { label ->
                    Text(
                        text = label
                    )
                }
            }

            Column {
                //Column for Orders
            }
        }

        Icon (
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
fun AddSpecialReq(
    specialRequestViewModel: SpecialRequestViewModel,
    userViewModel: UserViewModel
) {
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val productRows = remember { mutableStateListOf(ProductReq()) }
    var targetDate by remember { mutableStateOf("") }
    var collectionMethod by remember { mutableStateOf("") }
    var additional by remember { mutableStateOf("") }
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val userData by userViewModel.userData.observeAsState()

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss")
    val formattedDateTime = currentDateTime.format(formatter)

    var subjectEmpty by remember { mutableStateOf(false) }
    var targetDateEmpty by remember { mutableStateOf(false) }
    val productEmpty = remember { mutableStateListOf(ProductReqValidation()) }
    var collectionMethodEmpty by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userViewModel.fetchUser(uid)
    }
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Subject",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it},
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        if (subjectEmpty) {
            Text(
                text = "Subject is Empty!",
                color = Color.Red,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

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
                // Dropdown
                OutlinedTextField(
                    value = productRow.name,
                    onValueChange = { newValue ->
                        productRows[index] = productRow.copy(name = newValue)
                        productEmpty[index] = productEmpty[index].copy(name = newValue.isEmpty())
                    },
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .weight(1f),
                    singleLine = true
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
                    color = Color.Red,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (productEmpty[index].quantity) {
                Text(
                    text = "Quantity must be greater than 0!",
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = "Add Product")
        }

        Text(
            text = "Target Date",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(vertical = 4.dp)
        )

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

        if (targetDateEmpty) {
            Text(
                text = "Target Date is Empty!",
                color = Color.Red,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

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
                selected = collectionMethod == "Pick Up",
                onClick = { collectionMethod = "Pick Up"}
            )
            Text("Pick Up")
        }

        Row (
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = collectionMethod == "Deliver",
                onClick = { collectionMethod = "Deliver"}
            )
            Text("Deliver")
        }

        if (collectionMethodEmpty) {
            Text(
                text = "Please Choose a Collection Method!",
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
                    val specialReq = SpecialRequest (
                        subject = subject,
                        description = description,
                        products = productRows,
                        targetDate = targetDate,
                        collectionMethod = collectionMethod,
                        additionalRequest = additional,
                        uid = uid,
                        status = "To Review",
                        name = "${userData?.firstname} ${userData?.lastname}",
                        dateRequested = formattedDateTime
                    )

                    specialRequestViewModel.addSpecialRequest(
                        uid,
                        specialReq
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(text = "Submit Request")
        }
    }
}