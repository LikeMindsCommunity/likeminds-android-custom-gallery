package com.likeminds.customgallery.utils

import android.content.Context
import com.likeminds.customgallery.R
import java.text.SimpleDateFormat
import java.util.*

object DateUtil {
    /**
     * @param milliseconds - This should be the UNIX timestamp
     */
    fun createDateFormat(pattern: String, milliseconds: Long): String {
        val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        return dateFormat.format(Date(milliseconds * 1000))
    }

    fun getDateTitleForGallery(context: Context, date: Long): String {
        val targetCalendar = Calendar.getInstance()
        targetCalendar.timeInMillis = date * 1000
        val calendar = Calendar.getInstance()
        return if (isCurrentWeek(calendar, targetCalendar)) {
            context.getString(R.string.recent)
        } else {
            calendar.add(Calendar.WEEK_OF_YEAR, -1)
            if (isPastWeek(calendar, targetCalendar)) {
                context.getString(R.string.last_week)
            } else {
                calendar.set(Calendar.DAY_OF_WEEK, 1)
                if (isCurrentMonth(calendar, targetCalendar)) {
                    context.getString(R.string.last_month)
                } else {
                    getMonthName(date)
                }
            }
        }
    }

    private fun isCurrentWeek(calendar: Calendar, targetCalendar: Calendar): Boolean {
        return calendar[Calendar.WEEK_OF_YEAR] == targetCalendar[Calendar.WEEK_OF_YEAR]
                && calendar[Calendar.YEAR] == targetCalendar[Calendar.YEAR]
    }

    private fun isPastWeek(calendar: Calendar, targetCalendar: Calendar): Boolean {
        return calendar[Calendar.WEEK_OF_YEAR] == targetCalendar[Calendar.WEEK_OF_YEAR]
                && calendar[Calendar.YEAR] == targetCalendar[Calendar.YEAR]
    }

    private fun isCurrentMonth(calendar: Calendar, targetCalendar: Calendar): Boolean {
        return calendar[Calendar.YEAR] == targetCalendar[Calendar.YEAR]
                && calendar[Calendar.MONTH] == targetCalendar[Calendar.MONTH]
                && calendar[Calendar.DAY_OF_MONTH] > targetCalendar[Calendar.DAY_OF_MONTH]
    }

    private fun getMonthName(date: Long): String {
        return SimpleDateFormat("MMMM", Locale.getDefault()).format(date * 1000)
    }

    fun formatSeconds(timeInSeconds: Int): String {
        val hours = timeInSeconds / 3600
        val secondsLeft = timeInSeconds - hours * 3600
        val minutes = secondsLeft / 60
        val seconds = secondsLeft - minutes * 60
        var formattedTime = ""
        if (hours in 1..9) formattedTime += "0"
        if (hours > 0)
            formattedTime += "$hours:"
        if (minutes < 10) formattedTime += "0"
        formattedTime += "$minutes:"
        if (seconds < 10) formattedTime += "0"
        formattedTime += seconds
        if (formattedTime.startsWith("00")) {
            return formattedTime.substring(1)
        }
        return formattedTime
    }
}