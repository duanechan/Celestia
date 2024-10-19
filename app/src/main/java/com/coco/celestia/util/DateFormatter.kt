package com.coco.celestia.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun formatDate(dateString: String): String {
    return try {
        val calendar = Calendar.getInstance()
        val inputFormat = SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH)
        val date = inputFormat.parse(dateString)
        calendar.time = date
        val month = inputFormat.format(date).substringBefore('/')
        val monthString = when (month) {
            "1" -> "January"
            "2" -> "February"
            "3" -> "March"
            "4" -> "April"
            "5" -> "May"
            "6" -> "June"
            "7" -> "July"
            "8" -> "August"
            "9" -> "September"
            "10" -> "October"
            "11" -> "November"
            "12" -> "December"
            else -> "ERROR FORMATTING"
        }
        return "$monthString ${calendar.get(Calendar.DAY_OF_MONTH)}, ${calendar.get(Calendar.YEAR)}"
    } catch (e: Exception) {
        e.printStackTrace()
        "Error formatting date"
    }
}