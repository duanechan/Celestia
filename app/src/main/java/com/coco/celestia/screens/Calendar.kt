package com.coco.celestia.screens

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coco.celestia.ui.theme.BgColor
import com.coco.celestia.ui.theme.Cocoa
import com.coco.celestia.ui.theme.CompletedStatus
import com.coco.celestia.ui.theme.DeliveringStatus
import com.coco.celestia.ui.theme.DuskyBlue
import com.coco.celestia.ui.theme.GoldenYellow
import com.coco.celestia.ui.theme.PendingStatus
import com.coco.celestia.ui.theme.Sand
import com.coco.celestia.ui.theme.Sand2
import com.coco.celestia.ui.theme.mintsansFontFamily
import com.coco.celestia.util.DateUtil
import com.coco.celestia.util.getDisplayName
import com.coco.celestia.viewmodel.CalendarViewModel
import com.coco.celestia.viewmodel.FarmerItemViewModel
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.model.CalendarUIState
import com.coco.celestia.viewmodel.model.OrderData
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.Locale

@Composable
fun Calendar(
    userRole: String,
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel,
    viewModel: CalendarViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
    val orderState by orderViewModel.orderState.observeAsState(OrderState.EMPTY)
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val productData by productViewModel.productData.observeAsState(emptyList())
    val priceMap = remember { mutableMapOf<String, Double>() }
    var targetDate by remember { mutableStateOf("") }
    val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val farmerItemViewModel: FarmerItemViewModel = viewModel()
    var farmerName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        orderViewModel.fetchAllOrders(
            "",
            userRole
        )
        productViewModel.fetchProducts(
            "",
            userRole
        )
        if (orderData.isNotEmpty() && productData.isNotEmpty()) {
            orderData.forEach { order ->
                val orderedProduct = order.orderData.name
                val productPrice = productData
                    .find { product -> product.name == orderedProduct }?.priceKg
                priceMap[order.orderData.name] = productPrice!!
            }
        }
        if (uid.isNotEmpty()) {
            farmerName = farmerItemViewModel.fetchFarmerName(uid)
        }
    }

    val calendarBackgroundColor = when (userRole) {
        "Admin" -> Color.White
        "CoopMeat", "CoopCoffee" -> Color.White
        else -> BgColor
    }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(calendarBackgroundColor)
        ) {
            item {
                CalendarWidget(
                    orderViewModel = orderViewModel,
                    days = DateUtil.daysOfWeek,
                    yearMonth = uiState.yearMonth,
                    dates = uiState.dates,
                    onPreviousMonthButtonClicked = { prevMonth ->
                        viewModel.toPreviousMonth(prevMonth)
                    },
                    onNextMonthButtonClicked = { nextMonth ->
                        viewModel.toNextMonth(nextMonth)
                    },
                    onDateClickListener = { selectedDate ->
                        targetDate = selectedDate.fullDate.toString()
                    },
                    userRole = userRole,
                    farmerName = farmerName
                )
            }

            item {
                val textColor = when (userRole) {
                    "Admin" -> Color.White
                    "CoopMeat", "CoopCoffee" -> PendingStatus
                    else -> Cocoa
                }

                val backgroundColor = when (userRole) {
                    "Admin" -> DuskyBlue.copy(alpha = 1f)
                        "CoopMeat", "CoopCoffee" -> DeliveringStatus.copy(alpha = 0.5f)
                    else -> Sand2
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .padding(vertical = 15.dp, horizontal = 25.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Product Name",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        color = textColor
                    )
                    Text(
                        text = "Qty.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        color = textColor,
                        modifier = Modifier.offset(x = (-10).dp)
                    )
                    Text(
                        text = "Price",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        color = textColor,
                        modifier = Modifier.offset(x = (-25).dp)
                    )
                    Spacer(modifier = Modifier)
                }
                Divider(color = textColor, thickness = 2.dp)
            }

            when (orderState) {
                is OrderState.LOADING -> {
                    item {
                        CircularProgressIndicator()
                    }
                }

                is OrderState.SUCCESS -> {
                    val sameDate = orderData.filter { order ->
                        val targetDates = order.targetDate
                        if (targetDate.isNotEmpty() && order.status != "PENDING" &&
                            order.status != "CANCELLED") {
                            val parsedDate = inputFormat.parse(targetDates)
                            val formattedDate = parsedDate?.let { outputFormat.format(it) }
                            formattedDate == targetDate
                        } else {
                            false
                        }
                    }
                    if (sameDate.isNotEmpty()) {
                        itemsIndexed(sameDate) { index, order ->
                            val orderPrice = priceMap[order.orderData.name]!!
                            OrderItem(
                                order = order,
                                price = orderPrice,
                                rowIndex = index,
                                userRole = userRole,
                                totalItems = sameDate.size
                            )
                        }
                    } else {
                        item {
                            EmptyOrderState(userRole = userRole)
                        }
                    }
                }

                OrderState.EMPTY -> {
                    item {
                        EmptyOrderState(userRole = userRole)
                    }
                }

                is OrderState.ERROR -> {
                    item {
                        ErrorOrderState(userRole = userRole, message = (orderState as OrderState.ERROR).message)
                    }
                }
            }
        }
}

@Composable
fun EmptyOrderState(userRole: String) {
    val textColor = when (userRole) {
        "Admin" -> Color.White
        "CoopMeat", "CoopCoffee" -> PendingStatus
        else -> Cocoa
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                color = when (userRole) {
                    "Admin" -> DuskyBlue.copy(alpha = 1f)
                    "CoopMeat", "CoopCoffee" -> CompletedStatus.copy(alpha = 0.5f)
                    else -> Sand
                }
            )
            .padding(bottom = 500.dp, top = 40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No orders found for this date.",
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = textColor
        )
    }
}

@Composable
fun ErrorOrderState(userRole: String, message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (userRole == "Admin") DuskyBlue.copy(alpha = 1f) else Sand
            )
            .padding(vertical = 30.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Error: $message",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            color = if (userRole == "Admin") Color.White else Cocoa
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderItem(
    order: OrderData,
    price: Double,
    rowIndex: Int,
    userRole: String,
    totalItems: Int
) {
    var showFulfillers by remember { mutableStateOf(false) }

    @Composable
    fun getBackgroundColor(index: Int): Color {
        return when {
            index % 2 == 0 -> {
                when (userRole) {
                    "Admin" -> DuskyBlue.copy(alpha = 1f)
                    "CoopMeat", "CoopCoffee" -> CompletedStatus.copy(alpha = 0.5f)
                    else -> Sand
                }
            }
            else -> {
                when (userRole) {
                    "Admin" -> DuskyBlue.copy(alpha = 0.5f)
                    "CoopMeat", "CoopCoffee" -> DeliveringStatus.copy(alpha = 0.5f)
                    else -> Sand2
                }
            }
        }
    }

    val textColor = when (userRole) {
        "Admin" -> Color.White
        "CoopMeat", "CoopCoffee" -> PendingStatus
        else -> Cocoa
    }
    val backgroundColor = getBackgroundColor(rowIndex)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = backgroundColor)
                .padding(vertical = 30.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = order.orderData.name,
                color = textColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${order.orderData.quantity}",
                color = textColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "₱${price * order.orderData.quantity}",
                color = textColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { showFulfillers = true },
                content = {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Supplier/s",
                        tint = textColor
                    )
                },
                modifier = Modifier.size(25.dp)
            )
        }

        if (rowIndex == totalItems - 1) {
            val remainingToNextMultipleOf5 = (5 - (totalItems % 5)) % 5
            if (remainingToNextMultipleOf5 > 0) {
                repeat(remainingToNextMultipleOf5) { placeholderIndex ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = getBackgroundColor(totalItems + placeholderIndex))
                            .padding(vertical = 30.dp, horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "",
                            modifier = Modifier.weight(2f),
                            textAlign = TextAlign.Start,
                        )
                        Text(
                            text = "",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.End,
                        )
                    }
                }
            }
        }
    }

    if (showFulfillers) {
        AlertDialog(
            onDismissRequest = { showFulfillers = false },
            confirmButton = {},
            title = { Text(text = "Supplier/s", fontWeight = FontWeight.Bold, fontFamily = mintsansFontFamily) },
            text = {
                LazyColumn {
                    itemsIndexed(order.fulfilledBy) { _, fulfiller ->
                        Text(text = fulfiller, fontFamily = mintsansFontFamily)
                    }
                }
            }
        )
    }
}

@Composable
fun CalendarWidget(
    orderViewModel: OrderViewModel,
    days: Array<String>,
    yearMonth: YearMonth,
    dates: List<CalendarUIState.Date>,
    onPreviousMonthButtonClicked: (YearMonth) -> Unit,
    onNextMonthButtonClicked: (YearMonth) -> Unit,
    onDateClickListener: (CalendarUIState.Date) -> Unit,
    userRole: String,
    farmerName: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Header(
            yearMonth = yearMonth,
            onPreviousMonthButtonClicked = onPreviousMonthButtonClicked,
            onNextMonthButtonClicked = onNextMonthButtonClicked
        )
        Row {
            repeat(days.size) {
                val item = days[it]
                DayItem(item, modifier = Modifier.weight(2f))
            }
        }
        Content(
            orderViewModel = orderViewModel,
            dates = dates,
            onDateClickListener = onDateClickListener,
            userRole = userRole,
            farmerName = farmerName
        )
    }
}

@Composable
fun Header(
    yearMonth: YearMonth,
    onPreviousMonthButtonClicked: (YearMonth) -> Unit,
    onNextMonthButtonClicked: (YearMonth) -> Unit,
) {
    Row {
        IconButton(onClick = {
            onPreviousMonthButtonClicked.invoke(yearMonth.minusMonths(1))
        }) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowLeft,
                contentDescription = "Back"
            )
        }
        Text(
            text = yearMonth.getDisplayName(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        )
        IconButton(onClick = {
            onNextMonthButtonClicked.invoke(yearMonth.plusMonths(1))
        }) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Next"
            )
        }
    }
}

@Composable
fun DayItem(day: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Text(
            text = day,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(10.dp)
        )
    }
}

@Composable
fun Content(
    orderViewModel: OrderViewModel,
    dates: List<CalendarUIState.Date>,
    onDateClickListener: (CalendarUIState.Date) -> Unit,
    userRole: String,
    farmerName: String
) {
    Column {
        var index = 0
        repeat(6) {
            if (index >= dates.size) return@repeat
            Row {
                repeat(7) {
                    val item = if (index < dates.size) dates[index] else CalendarUIState.Date.Empty
                    ContentItem(
                        orderViewModel = orderViewModel,
                        date = item,
                        onClickListener = onDateClickListener,
                        userRole = userRole,
                        modifier = Modifier.weight(2f),
                        farmerName = farmerName
                    )
                    index++
                }
            }
        }
    }
}

@Composable
fun ContentItem(
    orderViewModel: OrderViewModel,
    date: CalendarUIState.Date,
    onClickListener: (CalendarUIState.Date) -> Unit,
    userRole: String,
    farmerName: String,
    modifier: Modifier = Modifier
) {
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    LaunchedEffect(Unit) {
        orderViewModel.fetchAllOrders("", userRole)
    }

    val targetDates = orderData
        .filter {
            it.status != "PENDING" && it.status != "CANCELLED" &&
                    if (userRole == "Farmer") {
                        it.fulfilledBy.contains(farmerName)
                    } else {
                        true
                    }
        }
        .mapNotNull { order ->
            val parsedDate = inputFormat.parse(order.targetDate)
            parsedDate?.let { outputFormat.format(it) }
        }
        .distinct()

    val backgroundColor = when {
        date.isSelected -> MaterialTheme.colorScheme.secondaryContainer
        targetDates.any { it == date.fullDate.toString() } -> {
            when (userRole) {
                "Admin" -> DuskyBlue.copy(alpha = 0.8f)
                "CoopMeat", "CoopCoffee" -> CompletedStatus.copy(alpha = 0.5f)
                else -> GoldenYellow
            }
        }
        else -> Color.Transparent
    }

    Row(
        modifier = modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(color = backgroundColor)
            .clickable { onClickListener(date) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = date.dayOfMonth,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(10.dp)
        )
    }
}