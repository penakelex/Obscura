package org.penakelex.obscura.presentation.util.date

import androidx.compose.runtime.Composable
import obscura.app.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun DateLabel.toDisplayString(): String = when (this) {
    is DateLabel.Today -> time
    is DateLabel.Yesterday ->
        stringResource(Res.string.date_yesterday_with_time, time)
    is DateLabel.ThisWeek -> {
        val month = monthShort(monthNumber)
        "$dayOfMonth $month, $time"
    }
    is DateLabel.ThisYear -> {
        val month = monthShort(monthNumber)
        "$dayOfMonth $month"
    }
    is DateLabel.Older -> {
        val month = monthShort(monthNumber)
        "$dayOfMonth $month $year"
    }
    DateLabel.JustNow ->
        stringResource(Res.string.date_just_now)
    is DateLabel.MinutesAgo ->
        stringResource(Res.string.date_minutes_ago, minutes)
    is DateLabel.HoursAgo ->
        stringResource(Res.string.date_hours_ago, hours)
    is DateLabel.DaysAgo ->
        stringResource(Res.string.date_days_ago, days)
    is DateLabel.InMinutes ->
        stringResource(Res.string.date_in_minutes, minutes)
    is DateLabel.InHours ->
        stringResource(Res.string.date_in_hours, hours)
    is DateLabel.InDays ->
        stringResource(Res.string.date_in_days, days)
}

@Composable
private fun monthShort(month: Int): String = when (month) {
    1 -> stringResource(Res.string.month_short_1)
    2 -> stringResource(Res.string.month_short_2)
    3 -> stringResource(Res.string.month_short_3)
    4 -> stringResource(Res.string.month_short_4)
    5 -> stringResource(Res.string.month_short_5)
    6 -> stringResource(Res.string.month_short_6)
    7 -> stringResource(Res.string.month_short_7)
    8 -> stringResource(Res.string.month_short_8)
    9 -> stringResource(Res.string.month_short_9)
    10 -> stringResource(Res.string.month_short_10)
    11 -> stringResource(Res.string.month_short_11)
    12 -> stringResource(Res.string.month_short_12)
    else -> ""
}

@Composable
private fun monthLong(month: Int): String = when (month) {
    1 -> stringResource(Res.string.month_long_1)
    2 -> stringResource(Res.string.month_long_2)
    3 -> stringResource(Res.string.month_long_3)
    4 -> stringResource(Res.string.month_long_4)
    5 -> stringResource(Res.string.month_long_5)
    6 -> stringResource(Res.string.month_long_6)
    7 -> stringResource(Res.string.month_long_7)
    8 -> stringResource(Res.string.month_long_8)
    9 -> stringResource(Res.string.month_long_9)
    10 -> stringResource(Res.string.month_long_10)
    11 -> stringResource(Res.string.month_long_11)
    12 -> stringResource(Res.string.month_long_12)
    else -> ""
}