package org.penakelex.obscura.presentation.util.filesize

data class FormattedFileSize(
    val value: Double,
    val unit: SizeUnit,
    val originalBytes: Long,
) {
    val isWholeNumber: Boolean
        get() = unit == SizeUnit.BYTES
                || value == value.toLong().toDouble()
}