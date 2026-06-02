package org.penakelex.obscura.presentation.components.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.penakelex.obscura.presentation.theme.ObscuraDimens

enum class ButtonVariant {
    PRIMARY,
    SECONDARY,
    TEXT,
    DESTRUCTIVE
}

@Composable
fun ObscuraButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
) {
    val buttonModifier = modifier
        .height(ObscuraDimens.Size.buttonHeight)

    when (variant) {
        ButtonVariant.PRIMARY -> PrimaryButton(
            text = text,
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled && !loading,
            loading = loading,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
        )

        ButtonVariant.SECONDARY -> SecondaryButton(
            text = text,
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled && !loading,
            loading = loading,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
        )

        ButtonVariant.TEXT -> TextButtonVariant(
            text = text,
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled && !loading,
            loading = loading,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
        )

        ButtonVariant.DESTRUCTIVE -> DestructiveButton(
            text = text,
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled && !loading,
            loading = loading,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
        )
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    loading: Boolean,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = ObscuraDimens.Padding.l),
    ) {
        ButtonContent(
            text = text,
            loading = loading,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
        )
    }
}

@Composable
private fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    loading: Boolean,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(horizontal = ObscuraDimens.Padding.l),
    ) {
        ButtonContent(
            text = text,
            loading = loading,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
        )
    }
}

@Composable
private fun TextButtonVariant(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    loading: Boolean,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = ObscuraDimens.Padding.m),
    ) {
        ButtonContent(
            text = text,
            loading = loading,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
        )
    }
}

@Composable
private fun DestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    loading: Boolean,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        ),
        contentPadding = PaddingValues(horizontal = ObscuraDimens.Padding.l),
    ) {
        ButtonContent(
            text = text,
            loading = loading,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
        )
    }
}

@Composable
private fun ButtonContent(
    text: String,
    loading: Boolean,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
) {
    if (loading) {
        CircularProgressIndicator(
            modifier = Modifier
                .height(ObscuraDimens.Size.iconSmall)
                .width(ObscuraDimens.Size.iconSmall),
            strokeWidth = ObscuraDimens.Padding.xs / 2,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    } else {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier
                    .height(ObscuraDimens.Size.iconMedium)
                    .width(ObscuraDimens.Size.iconMedium),
            )
            Spacer(Modifier.width(ObscuraDimens.Padding.s))
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )

        if (trailingIcon != null) {
            Spacer(Modifier.width(ObscuraDimens.Padding.s))
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                modifier = Modifier
                    .height(ObscuraDimens.Size.iconMedium)
                    .width(ObscuraDimens.Size.iconMedium),
            )
        }
    }
}