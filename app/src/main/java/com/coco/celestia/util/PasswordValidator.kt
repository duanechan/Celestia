package com.coco.celestia.util

fun isValidPassword(password: String): List<String> {
    val lowerCase = Regex(".*[a-z].*")
    val upperCase = Regex(".*[A-Z].*")
    val digit = Regex(".*\\d.*")
    val special = Regex(".*[!@#\$%^&*(),.?\":{}|<>].*")
    val allowed = Regex("^[A-Za-z\\d!@#\$%^&*(),.?\":{}|<>]+$")

    val response = mutableListOf<String>()

    if (password.length < 8) response.add("Must be at least 8 characters long.")
    if (!lowerCase.containsMatchIn(password)) response.add("Must contain at least one lowercase character.")
    if (!upperCase.containsMatchIn(password)) response.add("Must contain at least one uppercase character.")
    if (!digit.containsMatchIn(password)) response.add("Must contain at least one digit.")
    if (!special.containsMatchIn(password)) response.add("Must contain one special character.")
    if (!allowed.matches(password)) response.add("Must contain valid characters only.")

    return if (response.isEmpty()) listOf("Password is valid!") else response
}
