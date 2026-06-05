package org.penakelex.obscura.presentation.util.date

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.Instant

object DateFormatter {
    fun formatListLabel(timestampMillis: Long): DateLabel {
        val now = Clock.System.now()
        val instant = Instant.fromEpochMilliseconds(timestampMillis)
        val timeZone = TimeZone.currentSystemDefault()

        val nowLocal = now.toLocalDateTime(timeZone)
        val targetLocal = instant.toLocalDateTime(timeZone)

        val nowDate = LocalDate(nowLocal.year, nowLocal.month, nowLocal.day)
        val targetDate = LocalDate(targetLocal.year, targetLocal.month, targetLocal.day)
        val daysDiff = abs(nowDate.daysUntil(targetDate))

        val time = formatTime(targetLocal.hour, targetLocal.minute)

        return when {
            daysDiff == 0 -> DateLabel.Today(time)
            daysDiff == 1 -> DateLabel.Yesterday(time)
            daysDiff < 7L -> DateLabel.ThisWeek(
                dayOfMonth = targetLocal.day,
                monthNumber = targetLocal.month.number,
                time = time,
            )
            targetLocal.year == nowLocal.year -> DateLabel.ThisYear(
                dayOfMonth = targetLocal.day,
                monthNumber = targetLocal.month.number,
            )
            else -> DateLabel.Older(
                dayOfMonth = targetLocal.day,
                monthNumber = targetLocal.month.number,
                year = targetLocal.year,
            )
        }
    }

    data class FullDate(
        val dayOfMonth: Int,
        val monthNumber: Int,
        val year: Int,
        val time: String,
    )

    fun formatFull(timestampMillis: Long): FullDate {
        val instant = Instant.fromEpochMilliseconds(timestampMillis)
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return FullDate(
            dayOfMonth = local.day,
            monthNumber = local.month.number,
            year = local.year,
            time = formatTime(local.hour, local.minute),
        )
    }

    fun formatRelative(timestampMillis: Long): DateLabel {
        val nowMillis = Clock.System.now().toEpochMilliseconds()
        val diffMillis = timestampMillis - nowMillis
        val isFuture = diffMillis > 0
        val absDiffMillis = abs(diffMillis)

        val diffSeconds = absDiffMillis / 1000
        val diffMinutes = diffSeconds / 60
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24

        return when {
            diffSeconds < 60 -> DateLabel.JustNow
            diffMinutes < 60 ->
                if (isFuture) DateLabel.InMinutes(diffMinutes.toInt())
                else DateLabel.MinutesAgo(diffMinutes.toInt())
            diffHours < 24 ->
                if (isFuture) DateLabel.InHours(diffHours.toInt())
                else DateLabel.HoursAgo(diffHours.toInt())
            diffDays < 7 ->
                if (isFuture) DateLabel.InDays(diffDays.toInt())
                else DateLabel.DaysAgo(diffDays.toInt())
            else -> formatListLabel(timestampMillis)
        }
    }

    private fun formatTime(hour: Int, minute: Int): String =
        "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}