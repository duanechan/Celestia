package com.coco.celestia.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.coco.celestia.components.toast.Toast
import com.coco.celestia.screens.client.OrderItem
import com.coco.celestia.util.DateUtil
import com.coco.celestia.util.getDisplayName
import com.coco.celestia.viewmodel.CalendarViewModel
import com.coco.celestia.viewmodel.OrderState
import com.coco.celestia.viewmodel.OrderViewModel
import com.coco.celestia.viewmodel.ProductViewModel
import com.coco.celestia.viewmodel.model.CalendarUIState
import com.coco.celestia.viewmodel.model.OrderData
import java.time.YearMonth


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Calendar(
    orderViewModel: OrderViewModel,
    productViewModel: ProductViewModel,
    viewModel: CalendarViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val orderState by orderViewModel.orderState.observeAsState(OrderState.EMPTY)
    val orderData by orderViewModel.orderData.observeAsState(emptyList())
    val context = LocalContext.current
    var targetDate by remember { mutableStateOf("") }

    LaunchedEffect(key1 = targetDate) {
        orderViewModel.fetchAllOrders(
            "",
            "Admin"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text(text = "Calendar")}
            )
        }
    ) { padding ->
        Surface (
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            CalendarWidget(
                days = DateUtil.daysOfWeek,
                yearMonth = uiState.yearMonth,
                dates = uiState.dates,
                onPreviousMonthButtonClicked = { prevMonth ->
                    viewModel.toPreviousMonth(prevMonth)
                },
                onNextMonthButtonClicked =  { nextMonth ->
                    viewModel.toNextMonth(nextMonth)
                },
                onDateClickListener = { selectedDate ->
                    val fullDate = "${uiState.yearMonth.year}-${uiState.yearMonth.monthValue.toString().padStart(2, '0')}-${selectedDate.dayOfMonth.padStart(2, '0')}"
                    targetDate = fullDate
                }
            )

            when (orderState) {
                is OrderState.LOADING -> {
                    CircularProgressIndicator()
                }
                is OrderState.SUCCESS -> {
                    val sameDate = orderData.filter { it.orderDate == targetDate }
                    if (sameDate.isNotEmpty()) {
                        sameDate.forEach { order ->
                            com.coco.celestia.screens.OrderItem(order = order)
                        }
                    } else {
                        Text("No orders found for this date.")
                    }

                }
                OrderState.EMPTY -> {
                    Text("No orders found for this date.")
                }
                is OrderState.ERROR -> {
                    Text("Error: ${(orderState as OrderState.ERROR).message}")
                }
            }
        }
    }
}

@Composable
fun OrderItem(order: OrderData) {
    Column {
        Text("Order ID: ${order.orderId}")
        Text("Client: ${order.client}")
        Text("Status: ${order.status}")
        // Add more details as needed
    }
}

@Composable
fun CalendarWidget(
    days:Array<String>,
    yearMonth: YearMonth,
    dates: List<CalendarUIState.Date>,
    onPreviousMonthButtonClicked: (YearMonth) -> Unit,
    onNextMonthButtonClicked: (YearMonth) -> Unit,
    onDateClickListener: (CalendarUIState.Date) -> Unit
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
                DayItem(item, modifier = Modifier.weight(1f))
            }
        }
        Content(
            dates = dates,
            onDateClickListener = onDateClickListener
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
    dates: List<CalendarUIState.Date>,
    onDateClickListener: (CalendarUIState.Date) -> Unit
) {
    Column {
        var index = 0
        repeat(6) {
            if (index >= dates.size) return@repeat
            Row {
                repeat(7){
                    val item = if (index < dates.size) dates[index] else CalendarUIState.Date.Empty
                    ContentItem(
                        date = item,
                        onClickListener = onDateClickListener,
                        modifier = Modifier.weight(1f)
                    )
                    index++
                }
            }
        }
    }
}

@Composable
fun ContentItem(
    date: CalendarUIState.Date,
    onClickListener: (CalendarUIState.Date) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (date.isSelected) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    Color.Transparent
                }
            )
            .clickable {
                onClickListener(date)
            }
    ) {
        Text(
            text = date.dayOfMonth,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(10.dp)
        )
    }
}