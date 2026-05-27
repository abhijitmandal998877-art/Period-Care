package com.example.data.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private const val DATE_FORMAT = "yyyy-MM-dd"

    fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return sdf.format(Date())
    }

    fun addDaysToDate(dateString: String, days: Int): String {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return try {
            val date = sdf.parse(dateString) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, days)
            sdf.format(cal.time)
        } catch (e: Exception) {
            dateString
        }
    }

    fun getDaysDifference(startDate: String, endDate: String): Int {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return try {
            val start = sdf.parse(startDate) ?: return 0
            val end = sdf.parse(endDate) ?: return 0
            val diff = end.time - start.time
            (diff / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            0
        }
    }

    fun parseDateFriendly(dateString: String): String {
        if (dateString.isEmpty()) return "Not tracked"
        val sdfInput = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val sdfOutput = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return try {
            val date = sdfInput.parse(dateString) ?: return dateString
            sdfOutput.format(date)
        } catch (e: Exception) {
            dateString
        }
    }
}
