package com.coco.celestia.util

import java.text.SimpleDateFormat
import java.util.Locale

fun formatDate(dateString: String): String? {
    return try {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'Z yyyy", Locale.ENGLISH)
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("M/dd/yy - h:mm a", Locale.ENGLISH)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}