package com.coco.celestia.util

import com.coco.celestia.viewmodel.model.MonthlyInventory
import com.coco.celestia.viewmodel.model.OrderData
import com.coco.celestia.viewmodel.model.ProductData
import java.time.LocalDate
import java.time.YearMonth

fun calculateMonthlyInventory(orderData: List<OrderData>, productData: List<ProductData>): List<MonthlyInventory> {
    val currentMonth = YearMonth.now()
    val thisMonthOrders = orderData.filter {
        if (it.orderDate.isEmpty()) {
            false
        } else {
            try {
                val orderYearMonth = YearMonth.from(LocalDate.parse(it.orderDate))
                val isCurrentMonth = orderYearMonth == currentMonth
                isCurrentMonth
            } catch (e: Exception) {
                false
            }
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
