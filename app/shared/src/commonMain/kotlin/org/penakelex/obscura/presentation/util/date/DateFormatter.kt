package org.penakelex.obscura.presentation.util.date

import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.Instant

object DateFormatter {
    fun formatListLabel(timestampMillis: Long): DateLabel {
        val now = Clock.System.now()
        val instant = Instant.fromEpochMilliseconds(timestampMillis)
        val nowLocal = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val targetLocal = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        val daysDiff = daysBetween(
            nowLocal.year, nowLocal.month.number, nowLocal.day,
            targetLocal.year,
            targetLocal.month.number, targetLocal.day,
        )
        val time = formatTime(targetLocal.hour, targetLocal.minute)

        return when {
            daysDiff == 0L -> DateLabel.Today(time)
            daysDiff == 1L -> DateLabel.Yesterday(time)
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
        val diffMillis = abs(nowMillis - timestampMillis)
        val diffSeconds = diffMillis / 1000
        val diffMinutes = diffSeconds / 60
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24

        return when {
            diffSeconds < 60 -> DateLabel.JustNow
            diffMinutes < 60 -> DateLabel.MinutesAgo(diffMinutes.toInt())
            diffHours < 24 -> DateLabel.HoursAgo(diffHours.toInt())
            diffDays < 7 -> DateLabel.DaysAgo(diffDays.toInt())
            else -> formatListLabel(timestampMillis)
        }
    }

    private fun formatTime(hour: Int, minute: Int): String =
        "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

    private fun daysBetween(
        y1: Int, m1: Int, d1: Int,
        y2: Int, m2: Int, d2: Int,
    ): Long {
        val days1 = daysSinceEpoch(y1, m1, d1)
        val days2 = daysSinceEpoch(y2, m2, d2)
        return days1 - days2
    }

    private fun daysSinceEpoch(year: Int, month: Int, day: Int): Long {
        val y = if (month <= 2) year - 1 else year
        val m = if (month <= 2) month + 12 else month
        return (365 * y + y / 4 - y / 100 + y / 400 +
                (3 * (m + 1)) / 5 + day).toLong()
    }
}