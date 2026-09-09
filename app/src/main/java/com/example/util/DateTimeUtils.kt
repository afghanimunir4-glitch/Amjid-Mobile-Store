package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
    private val fullDateTimeFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US)

    fun currentDate(): String {
        return dateFormat.format(Date())
    }

    fun currentTime(): String {
        return timeFormat.format(Date())
    }

    fun currentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return fullDateTimeFormat.format(Date(timestamp))
    }

    fun parseDateTime(dateStr: String, timeStr: String): Long {
        return try {
            val date = fullDateTimeFormat.parse("$dateStr $timeStr")
            date?.time ?: System.currentTimeMillis()
        } catch (_: Exception) {
            try {
                val dateOnly = dateFormat.parse(dateStr)
                dateOnly?.time ?: System.currentTimeMillis()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    fun getStartOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getStartOfYesterday(): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getEndOfYesterday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, -1)
        return cal.timeInMillis
    }

    fun getStartOfWeek(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getStartOfMonth(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getStartOfYear(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_YEAR, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}

object CurrencyFormatter {
    private val numberFormat = NumberFormat.getInstance(Locale.US).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }

    fun formatAfn(amount: Double): String {
        return "${numberFormat.format(amount)} AFN"
    }

    fun formatAfnPashto(amount: Double): String {
        return "${numberFormat.format(amount)} افغانۍ"
    }
}
