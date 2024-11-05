package com.coco.celestia.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.coco.celestia.util.DateUtil
import com.coco.celestia.util.getDisplayName
import com.coco.celestia.viewmodel.CalendarViewModel
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.model.CalendarUIState
import com.coco.celestia.viewmodel.model.OrderData
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.Locale
import com.coco.celestia.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Calendar(
    userRole: String,
    orderViewModel: OrderViewModel,
    viewModel: CalendarViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val orderState by orderViewModel.orderState.observeAsState(OrderState.EMPTY)
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    var targetDate by remember { mutableStateOf("") }
    val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    LaunchedEffect(Unit) {
        orderViewModel.fetchAllOrders(
            "",
            userRole
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {}
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                    userRole = userRole
                )
            }

            item {
                val textColor = if (userRole == "Admin") Color.White else Cocoa
                val backgroundColor = if (userRole == "Admin") MaterialTheme.colorScheme.primary.copy(alpha = 1f) else Sand2
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .padding(top = 15.dp, start = 16.dp, bottom = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Product Name",
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxWidth()
                            .offset(x = 5.dp),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        color = textColor
                    )
                    Text(
                        text = "Quantity",
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxWidth(),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = textColor
                    )
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
                            OrderItem(
                                order = order,
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
}

@Composable
fun EmptyOrderState(userRole: String) {
    val textColor = if (userRole == "Admin") Color.White else Cocoa
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                color = if (userRole == "Admin") MaterialTheme.colorScheme.primary.copy(alpha = 1f) else Sand
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
                color = if (userRole == "Admin") MaterialTheme.colorScheme.primary.copy(alpha = 1f) else Sand
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

@Composable
fun OrderItem(
    order: OrderData,
    rowIndex: Int,
    userRole: String,
    totalItems: Int
) {
    @Composable
    fun getBackgroundColor(index: Int): Color {
        return when {
            index % 2 == 0 -> {
                if (userRole == "Admin") MaterialTheme.colorScheme.primary.copy(alpha = 1f)
                else Sand
            }
            else -> {
                if (userRole == "Admin") MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                else Sand2
            }
        }
    }

    val textColor = if (userRole == "Admin") Color.White else Cocoa
    val backgroundColor = getBackgroundColor(rowIndex)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = backgroundColor)
                .padding(vertical = 30.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = order.orderData.name,
                color = textColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(2f)
                    .fillMaxWidth(),
                textAlign = TextAlign.Start,
            )
            Text(
                text = "${order.orderData.quantity}",
                color = textColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(2f)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
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
                            .padding(vertical = 30.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "",
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Start,
                        )
                        Text(
                            text = "",
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
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
    userRole: String
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
            userRole = userRole
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
    userRole: String
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
                        modifier = Modifier.weight(2f)
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
    modifier: Modifier = Modifier
) {
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    LaunchedEffect(Unit) {
        orderViewModel.fetchAllOrders("", userRole)
    }

    val targetDates = orderData
        .filter { it.status != "PENDING" && it.status != "CANCELLED" }
        .mapNotNull { order ->
            val parsedDate = inputFormat.parse(order.targetDate)
            parsedDate?.let { outputFormat.format(it) }
        }
        .distinct()

    val backgroundColor = when {
        date.isSelected -> MaterialTheme.colorScheme.secondaryContainer
        targetDates.any { it == date.fullDate.toString() } -> {
            if (userRole == "Admin") MaterialTheme.colorScheme.primary
            else Sand2
        }
        else -> Color.Transparent
    }

    Row(
        modifier = modifier
            .background(color = backgroundColor)
            .clickable { onClickListener(date) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = date.dayOfMonth,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .weight(2f)
                .padding(10.dp)
        )
    }
}