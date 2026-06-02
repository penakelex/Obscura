package org.penakelex.obscura.presentation.util.filesize

import androidx.compose.runtime.Composable
import obscura.app.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun FormattedFileSize.toDisplayString(): String {
    val unitStr = when (unit) {
        SizeUnit.BYTES -> stringResource(Res.string.size_bytes)
        SizeUnit.KB -> stringResource(Res.string.size_kb)
        SizeUnit.MB -> stringResource(Res.string.size_mb)
        SizeUnit.GB -> stringResource(Res.string.size_gb)
        SizeUnit.TB -> stringResource(Res.string.size_tb)
    }

    val valueStr = if (isWholeNumber) {
        value.toLong().toString()
    } else {
        "%.1f".format(value)
    }

    return "$valueStr $unitStr"
}

@Composable
fun FormattedFileSize.toDisplayWithLimit(
    limit: FormattedFileSize
): String = "${this.toDisplayString()} / ${limit.toDisplayString()}"