package com.coco.celestia.util

object PhoneValidator {
    private val prefixMap = mapOf(
        "Globe" to listOf("817", "905", "906", "915", "916", "917", "926", "927", "935", "936", "945", "955", "956", "965", "966", "967", "975", "976", "977", "995", "997"),
        "ABS-CBN Mobile" to listOf("937"),
        "Cherry Prepaid" to listOf("996"),
        "Globe Postpaid" to listOf("9175", "9176", "9178", "9253", "9255", "9256", "9257", "9258"),
        "Smart" to listOf("813", "907", "908", "909", "910", "811", "912", "913", "914", "918", "919", "920", "921", "928", "929", "930", "938", "939", "940", "946", "947", "948", "949", "950", "951", "961", "963", "968", "969", "970", "981", "989", "992", "998", "999"),
        "Dito" to listOf("895", "896", "897", "898", "991", "992", "993", "994"),
        "Sun Cellular" to listOf("922", "923", "924", "925", "931", "932", "933", "934", "941", "942", "943", "944")
    )

    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        if (!phoneNumber.all { it.isDigit() }) return false
        if (phoneNumber.length > 10) return false

        val validPrefixes = prefixMap.values.flatten()

        val prefix = if (phoneNumber.length >= 3) phoneNumber.substring(0, 3) else ""
        val postpaidPrefix = if (phoneNumber.length >= 4) phoneNumber.substring(0, 4) else ""

        return validPrefixes.contains(prefix) || validPrefixes.contains(postpaidPrefix)
    }

}