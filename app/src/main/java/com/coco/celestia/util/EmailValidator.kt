package com.coco.celestia.util

fun isValidEmail(email: String): Boolean {
    val emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}(?:\\.[a-zA-Z]{2,})?$"
    return email.matches(emailPattern.toRegex())
}