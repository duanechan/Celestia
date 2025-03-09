package com.coco.celestia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coco.celestia.viewmodel.model.ReportData
import com.coco.celestia.viewmodel.model.TransactionData
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class ReportState {
    data object LOADING : ReportState()
    data object SUCCESS : ReportState()
    data object EMPTY : ReportState()
    data class ERROR(val message: String) : ReportState()
}

class ReportsViewModel : ViewModel() {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("reports")
    private val _reportData = MutableLiveData<HashMap<String, List<ReportData>>>()
    private val _reportState = MutableLiveData<ReportState>()
    val reportData: LiveData<HashMap<String, List<ReportData>>> = _reportData
    val reportState: LiveData<ReportState> = _reportState

    private fun generateSalesByCustomerReport(transactions: List<TransactionData>): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        return buildString {
            appendLine("Sales by Customer Report")
            appendLine("Generated on: ${LocalDate.now().format(formatter)}")
            appendLine()
            appendLine()

            appendLine("----------------------------------------")
            appendLine("Transaction Details")
            appendLine("----------------------------------------")
            appendLine()

            val salesTransactions = transactions.filter { it.type == "Online Sale" }
            salesTransactions.forEach { transaction ->
                appendLine("-----------------------------------")
                appendLine("Transaction ID: ${transaction.transactionId}")
                appendLine("Date: ${transaction.date}")
                appendLine("Product Name: ${transaction.productName}")
                appendLine("Product ID: ${transaction.productId}")
                appendLine("Status: ${transaction.status}")
                appendLine("Description: ${transaction.description}")
                appendLine("-----------------------------------")
                appendLine()
            }
            appendLine()

            appendLine("----------------------------------------")
            appendLine("Product Summary")
            appendLine("----------------------------------------")
            appendLine()

            val productGroups = salesTransactions
                .groupBy { it.productName }
                .toSortedMap()

            productGroups.forEach { (product, sales) ->
                appendLine("Product Name: $product")
                appendLine("Number of Sales: ${sales.size}")
                appendLine()
            }
            appendLine()

            appendLine("----------------------------------------")
            appendLine("Overall Summary")
            appendLine("----------------------------------------")
            appendLine()
            appendLine("Total Products Sold: ${productGroups.size}")
            appendLine("Total Sales Transactions: ${salesTransactions.size}")
            appendLine()

            val dates = salesTransactions.map { it.date }
            if (dates.isNotEmpty()) {
                appendLine("First Transaction Date: ${dates.minOrNull()}")
                appendLine("Last Transaction Date: ${dates.maxOrNull()}")
            } else {
                appendLine("Date Range: No transactions found")
            }
            appendLine()
            appendLine("----------------------------------------")
        }
    }

    private fun generateSalesByItemReport(transactions: List<TransactionData>): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        val salesTransactions = transactions.filter { it.type == "Online Sale" }

        return buildString {
            appendLine("Sales by Item Report")
            appendLine("Generated on: ${LocalDate.now().format(formatter)}")
            appendLine()
            appendLine()

            appendLine("----------------------------------------")
            appendLine("Product Analysis")
            appendLine("----------------------------------------")
            appendLine()

            val productGroups = salesTransactions
                .groupBy { it.productName }
                .toSortedMap()

            if (productGroups.isEmpty()) {
                appendLine("No products found for the selected criteria")
                appendLine()
            } else {
                productGroups.forEach { (product, sales) ->
                    val dates = sales.map { it.date }
                    appendLine("-----------------------------------")
                    appendLine("Product Name: $product")
                    appendLine("Product ID: ${sales.firstOrNull()?.productId ?: ""}")
                    appendLine("Number of Sales: ${sales.size}")
                    appendLine("First Sale Date: ${dates.minOrNull() ?: ""}")
                    appendLine("Last Sale Date: ${dates.maxOrNull() ?: ""}")
                    appendLine("-----------------------------------")
                    appendLine()
                }
            }
            appendLine()

            appendLine("----------------------------------------")
            appendLine("Daily Sales Breakdown")
            appendLine("----------------------------------------")
            appendLine()

            if (salesTransactions.isEmpty()) {
                appendLine("No sales found for the selected criteria")
                appendLine()
            } else {
                salesTransactions
                    .groupBy { it.date }
                    .toSortedMap()
                    .forEach { (date, salesOnDate) ->
                        appendLine("-----------------------------------")
                        appendLine("Date: $date") // Keep original date format
                        appendLine("Number of Sales: ${salesOnDate.size}")
                        appendLine("Products Sold: ${salesOnDate.map { it.productName }.distinct().joinToString(", ")}")
                        appendLine("-----------------------------------")
                        appendLine()
                    }
            }
            appendLine()

            appendLine("----------------------------------------")
            appendLine("Overall Summary")
            appendLine("----------------------------------------")
            appendLine()
            appendLine("Total Unique Products: ${productGroups.size}")
            appendLine("Total Sales Transactions: ${salesTransactions.size}")
            appendLine()

            if (productGroups.isNotEmpty()) {
                appendLine("Total Sales by Product:")
                productGroups.forEach { (product, sales) ->
                    appendLine("$product: ${sales.size} sales")
                }
                appendLine()
            }

            val dates = salesTransactions.map { it.date }
            if (dates.isNotEmpty()) {
                appendLine("Report Period:")
                appendLine("First Transaction Date: ${dates.minOrNull()}")
                appendLine("Last Transaction Date: ${dates.maxOrNull()}")
            } else {
                appendLine("Report Period: No transactions found")
            }
            appendLine()
            appendLine("----------------------------------------")
            appendLine()
            appendLine("Sales by Item Report Summary")
            appendLine()
            appendLine("Product,Date,Status")
            if (salesTransactions.isNotEmpty()) {
                salesTransactions.forEach { transaction ->
                    appendLine("${transaction.productName},${transaction.date},${transaction.status}")
                }
            }
        }
    }

    private fun calculateTotalAmount(transactions: List<TransactionData>): Double {
        return transactions.filter { it.type == "Online Sale" }.size.toDouble()
    }

    fun generateReportWithDateRange(
        reportType: String,
        startDate: LocalDate,
        endDate: LocalDate,
        transactions: List<TransactionData>,
        productFilter: String? = null
    ) {
        viewModelScope.launch {
            _reportState.value = ReportState.LOADING
            try {
                val filteredTransactions = filterTransactionsByDateRange(
                    reportType = reportType,
                    startDate = startDate,
                    endDate = endDate,
                    transactions = transactions,
                    productFilter = productFilter
                )

                val report = when (reportType) {
                    "Sales by Customer" -> generateSalesByCustomerReport(filteredTransactions)
                    "Sales by Item" -> generateSalesByItemReport(filteredTransactions)
                    else -> throw IllegalArgumentException("Unknown report type")
                }

                val dateRangeStr = "${startDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))} - ${endDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}"

                val reportData = ReportData(
                    reportType = reportType,
                    dateRange = dateRangeStr,
                    content = report,
                    totalAmount = calculateTotalAmount(filteredTransactions)
                )

                val currentData = _reportData.value ?: hashMapOf()
                val reportsList = currentData[reportType]?.toMutableList() ?: mutableListOf()
                reportsList.add(reportData)
                currentData[reportType] = reportsList
                _reportData.value = currentData

                val reportId = database.push().key ?: return@launch
                database.child(reportId).setValue(reportData)
                    .addOnSuccessListener {
                        _reportState.value = ReportState.SUCCESS
                    }
                    .addOnFailureListener { exception ->
                        _reportState.value = ReportState.ERROR(exception.message ?: "Unknown error")
                    }
            } catch (e: Exception) {
                _reportState.value = ReportState.ERROR(e.message ?: "Unknown error")
            }
        }
    }
    private fun filterTransactionsByDateRange(
        reportType: String,
        startDate: LocalDate,
        endDate: LocalDate,
        transactions: List<TransactionData>,
        productFilter: String? = null
    ): List<TransactionData> {
        return transactions.filter { transaction ->
            if (transaction.type != "Online Sale") return@filter false

            if (productFilter != null && transaction.productName != productFilter) {
                return@filter false
            }

            try {
                val formatters = listOf(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                    DateTimeFormatter.ofPattern("dd MMM yyyy"),
                    DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                    DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                )

                val datePart = if (transaction.date.contains(" ") && transaction.date.contains(":")) {
                    transaction.date.split(" ")[0]
                } else {
                    transaction.date
                }

                val transactionDate = formatters.firstNotNullOfOrNull { formatter ->
                    try {
                        LocalDate.parse(transaction.date, formatter)
                    } catch (e: Exception) {
                        if (datePart != transaction.date) {
                            try {
                                LocalDate.parse(datePart, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            } catch (e2: Exception) {
                                null
                            }
                        } else {
                            null
                        }
                    }
                }

                if (transactionDate != null) {
                    transactionDate.isEqual(startDate) ||
                            transactionDate.isEqual(endDate) ||
                            (transactionDate.isAfter(startDate) && transactionDate.isBefore(endDate))
                } else {
                    true
                }
            } catch (e: Exception) {
                true
            }
        }
    }
}