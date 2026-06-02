package org.penakelex.obscura.presentation.util.filesize

import kotlin.math.log10
import kotlin.math.pow

object FileSizeFormatter {
    private val units = SizeUnit.entries.toTypedArray()

    fun format(bytes: Long): FormattedFileSize {
        if (bytes <= 0) {
            return FormattedFileSize(
                value = 0.0,
                unit = SizeUnit.BYTES,
                originalBytes = 0L,
            )
        }

        val digitGroups =
            (log10(bytes.toDouble()) / log10(1024.0)).toInt()
                .coerceAtMost(units.size - 1)

        val value = bytes / 1024.0.pow(digitGroups.toDouble())

        return FormattedFileSize(
            value = value,
            unit = units[digitGroups],
            originalBytes = bytes,
        )
    }
}