package com.coco.celestia

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coco.celestia.ui.theme.BgColor
import com.coco.celestia.ui.theme.CelestiaTheme
import java.util.*

class FarmerAddProductActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CelestiaTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgColor)
                ) {
                    FarmerAddProductScreen()
                }
            }
        }
    }
}

@Composable
fun FarmerAddProductScreen() {
    val context = LocalContext.current

    // Form states
    var productName by remember { mutableStateOf("") }
    var farmerName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var dateOfDelivery by remember { mutableStateOf("") }
    var harvestDate by remember { mutableStateOf("") }
    var shelfLifeYears by remember { mutableStateOf("") }
    var shelfLifeMonths by remember { mutableStateOf("") }
    var shelfLifeDays by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePicker = { onDatePicked: (String) -> Unit ->
        DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
            onDatePicked("$selectedDay/${selectedMonth + 1}/$selectedYear")
        }, year, month, day).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "Add Product", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text("Product Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = farmerName,
            onValueChange = { farmerName = it },
            label = { Text("Farmer's Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )

        // Date of Delivery
        OutlinedTextField(
            value = dateOfDelivery,
            onValueChange = { dateOfDelivery = it },
            label = { Text("Date of Delivery") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = {
                    datePicker { selectedDate -> dateOfDelivery = selectedDate }
                }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                }
            }
        )

        // Harvest Date
        OutlinedTextField(
            value = harvestDate,
            onValueChange = { harvestDate = it },
            label = { Text("Harvest Date") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = {
                    datePicker { selectedDate -> harvestDate = selectedDate }
                }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                }
            }
        )

        // Shelf Life
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = shelfLifeYears,
                onValueChange = { shelfLifeYears = it },
                label = { Text("Year/s") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = shelfLifeMonths,
                onValueChange = { shelfLifeMonths = it },
                label = { Text("Month/s") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = shelfLifeDays,
                onValueChange = { shelfLifeDays = it },
                label = { Text("Day/s") },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Weight") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = unit,
            onValueChange = { unit = it },
            label = { Text("Unit") },
            modifier = Modifier.fillMaxWidth()
        )

        // Save Button
        Button(
            onClick = {
                saveProduct(
                    productName,
                    farmerName,
                    address,
                    dateOfDelivery,
                    harvestDate,
                    shelfLifeYears,
                    shelfLifeMonths,
                    shelfLifeDays,
                    weight,
                    unit,
                    context
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}

fun saveProduct(
    productName: String,
    farmerName: String,
    address: String,
    dateOfDelivery: String,
    harvestDate: String,
    shelfLifeYears: String,
    shelfLifeMonths: String,
    shelfLifeDays: String,
    weight: String,
    unit: String,
    context: Context
) {
    // code for saving product
    Toast.makeText(context, "Product Saved", Toast.LENGTH_SHORT).show()
}
