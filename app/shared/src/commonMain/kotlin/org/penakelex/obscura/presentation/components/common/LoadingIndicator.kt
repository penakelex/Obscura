package org.penakelex.obscura.presentation.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import org.penakelex.obscura.presentation.theme.ObscuraDimens

enum class LoadingSize(val size: Dp) {
    SMALL(ObscuraDimens.Size.iconMedium),
    MEDIUM(ObscuraDimens.Size.iconLarge),
    LARGE(ObscuraDimens.Size.fabSize),
}

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: LoadingSize = LoadingSize.MEDIUM,
    text: String? = null,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size.size),
            strokeWidth = when (size) {
                LoadingSize.SMALL -> ObscuraDimens.Padding.xs / 2
                LoadingSize.MEDIUM -> ObscuraDimens.Padding.xs / 2
                LoadingSize.LARGE -> ObscuraDimens.Padding.xs
            },
            color = MaterialTheme.colorScheme.primary,
        )

        if (text != null) {
            Spacer(Modifier.height(ObscuraDimens.Padding.m))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun FullScreenLoading(
    modifier: Modifier = Modifier,
    text: String? = null,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        LoadingIndicator(
            size = LoadingSize.LARGE,
            text = text,
        )
    }
}