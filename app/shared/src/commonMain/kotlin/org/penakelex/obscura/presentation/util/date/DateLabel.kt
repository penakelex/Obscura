package org.penakelex.obscura.presentation.util.date

sealed interface DateLabel {
    data class Today(val time: String) : DateLabel
    data class Yesterday(val time: String) : DateLabel
    data class ThisWeek(
        val dayOfMonth: Int,
        val monthNumber: Int,
        val time: String,
    ) : DateLabel
    data class ThisYear(
        val dayOfMonth: Int,
        val monthNumber: Int,
    ) : DateLabel
    data class Older(
        val dayOfMonth: Int,
        val monthNumber: Int,
        val year: Int,
    ) : DateLabel
    data object JustNow : DateLabel
    data class MinutesAgo(val minutes: Int) : DateLabel
    data class HoursAgo(val hours: Int) : DateLabel
    data class DaysAgo(val days: Int) : DateLabel
    data class InMinutes(val minutes: Int) : DateLabel
    data class InHours(val hours: Int) : DateLabel
    data class InDays(val days: Int) : DateLabel
}