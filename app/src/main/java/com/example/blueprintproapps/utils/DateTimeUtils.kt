package com.example.blueprintproapps.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object DateTimeUtils {
    private val sourceTimeZone: TimeZone = TimeZone.getTimeZone("UTC")
    private val philippineTimeZone: TimeZone = TimeZone.getTimeZone("Asia/Manila")

    private val sourcePatterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss"
    )

    fun formatPhilippineTime(rawDate: String?): String {
        val value = rawDate?.trim().orEmpty()
        if (value.isEmpty()) return ""

        for (pattern in sourcePatterns) {
            val parser = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = sourceTimeZone
            }
            val parsed = runCatching { parser.parse(value) }.getOrNull() ?: continue
            return SimpleDateFormat("h:mm a", Locale.getDefault()).apply {
                timeZone = philippineTimeZone
            }.format(parsed)
        }

        return value
    }
}
