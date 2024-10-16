package com.coco.celestia.util

import com.coco.celestia.viewmodel.model.MonthlyInventory
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

fun calculateMonthlyInventory(orderData: List<OrderData>, productData: List<ProductData>): List<MonthlyInventory> {
    val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val currentMonth = YearMonth.now()
    val thisMonthOrders = orderData.filter {
        it.status != "PENDING" && it.targetDate.isNotEmpty() && try {
            val parsedDate = inputFormat.parse(it.targetDate)
            val formattedDate = outputFormat.format(parsedDate!!)
            val targetYearMonth = YearMonth.from(LocalDate.parse(formattedDate))
            targetYearMonth == currentMonth
        } catch (e: Exception) {
            false
        }
    }

    val orderedQuantities = thisMonthOrders
        .groupBy { it.orderData.name }
        .mapValues { entry ->
            entry.value.sumOf { it.orderData.quantity }
        }

    return productData.map { product: ProductData ->
        val totalOrdered = orderedQuantities[product.name] ?: 0
        MonthlyInventory(
            productName = product.name,
            remainingQuantity = product.quantity - totalOrdered,
            totalOrderedThisMonth = totalOrdered
        )
    }
}
