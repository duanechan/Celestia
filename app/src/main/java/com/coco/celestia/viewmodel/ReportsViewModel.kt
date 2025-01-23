package com.coco.celestia.viewmodel

import android.util.Log
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

    fun generateReport(reportType: String, dateRange: String, transactions: List<TransactionData>) {
        viewModelScope.launch {
            _reportState.value = ReportState.LOADING
            try {
                val filteredTransactions = filterTransactions(reportType, dateRange, transactions)
                val report = when (reportType) {
                    "Sales by Customer" -> generateSalesByCustomerReport(filteredTransactions)
                    "Sales by Item" -> generateSalesByItemReport(filteredTransactions)
                    else -> throw IllegalArgumentException("Unknown report type")
                }

                val reportData = ReportData(
                    reportType = reportType,
                    dateRange = dateRange,
                    content = report,
                    totalAmount = calculateTotalAmount(filteredTransactions)
                )

                saveReport(reportData)
            } catch (e: Exception) {
                _reportState.value = ReportState.ERROR(e.message ?: "Unknown error")
            }
        }
    }

    private fun filterTransactions(
        reportType: String,
        dateRange: String,
        transactions: List<TransactionData>
    ): List<TransactionData> {
        val formatters = listOf(
            DateTimeFormatter.ofPattern("dd MMM yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
        )

        val dateFilter = when (dateRange) {
            "This Week" -> LocalDate.now().minusWeeks(1)
            "This Month" -> LocalDate.now().withDayOfMonth(1)
            "Last 3 Months" -> LocalDate.now().minusMonths(3)
            "This Year" -> LocalDate.now().withDayOfYear(1)
            else -> LocalDate.now().minusYears(100)
        }

        return transactions.filter { transaction ->
            try {
                val transactionDate = formatters.firstNotNullOfOrNull { formatter ->
                    try {
                        LocalDate.parse(transaction.date, formatter)
                    } catch (e: Exception) {
                        null
                    }
                } ?: throw IllegalArgumentException("Unable to parse date: ${transaction.date}")

                transactionDate.isAfter(dateFilter)
            } catch (e: Exception) {
                println("Date parsing error for ${transaction.date}: ${e.message}")
                false
            }
        }.let { filtered ->
            when (reportType) {
                "Sales by Customer" -> filtered.sortedBy { it.date }
                "Sales by Item" -> filtered.sortedBy { it.productName }
                else -> filtered
            }
        }
    }

    private fun generateSalesByCustomerReport(transactions: List<TransactionData>): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        return buildString {
            appendLine("Sales by Customer Report")
            appendLine("Generated on: ${LocalDate.now().format(formatter)}")
            appendLine()
            // Only include sales transactions
            val salesTransactions = transactions.filter { it.type == "Online Sale" }

            appendLine("Transaction ID,Date,Product Name,Status,Description,Type")
            salesTransactions.forEach { transaction ->
                appendLine(
                    "${transaction.transactionId}," +
                            "${transaction.date}," +
                            "${transaction.productName}," +
                            "${transaction.status}," +
                            "${transaction.description}," +
                            transaction.type
                )
            }

            appendLine()
            appendLine("Summary")
            appendLine("Total Sales Transactions: ${salesTransactions.size}")
        }
    }

    private fun generateSalesByItemReport(transactions: List<TransactionData>): String {
        val salesTransactions = transactions.filter { it.type == "Online Sale" }
        return buildString {
            appendLine("Sales by Item Report")
            appendLine("Generated on: ${LocalDate.now()}")
            appendLine()

            val itemSummary = salesTransactions
                .groupBy { it.productName }
                .mapValues { entry ->
                    val count = entry.value.size
                    "Count: $count"
                }

            appendLine("Product Name,Number of Sales")
            itemSummary.forEach { (item, count) ->
                appendLine("$item,$count")
            }

            // Add overall summary
            appendLine()
            appendLine("Overall Summary")
            appendLine("Total Products: ${itemSummary.size}")
            appendLine("Total Sales: ${salesTransactions.size}")
        }
    }

    private fun calculateTotalAmount(transactions: List<TransactionData>): Double {
        return transactions.filter { it.type == "Online Sale" }.size.toDouble()
    }

    private fun saveReport(reportData: ReportData) {
        val reportId = database.push().key ?: return
        val currentData = _reportData.value ?: hashMapOf()
        val reportsList = currentData[reportData.reportType]?.toMutableList() ?: mutableListOf()
        reportsList.add(reportData)
        currentData[reportData.reportType] = reportsList
        _reportData.value = currentData

        database.child(reportId).setValue(reportData)
            .addOnSuccessListener {
                Log.d("Reports", "Report saved successfully")
                _reportState.value = ReportState.SUCCESS
            }
            .addOnFailureListener { exception ->
                Log.e("Reports", "Failed to save report", exception)
                _reportState.value = ReportState.ERROR(exception.message ?: "Unknown error")
            }
    }
}