package org.penakelex.obscura.presentation.screens.sessions.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.current_session
import obscura.app.shared.generated.resources.revoke
import obscura.app.shared.generated.resources.session_created
import obscura.app.shared.generated.resources.session_expires
import obscura.app.shared.generated.resources.unknown_device
import org.jetbrains.compose.resources.stringResource
import org.penakelex.obscura.domain.model.auth.SessionInfo
import org.penakelex.obscura.presentation.components.common.ButtonVariant
import org.penakelex.obscura.presentation.components.common.ObscuraButton
import org.penakelex.obscura.presentation.theme.ObscuraDimens
import org.penakelex.obscura.presentation.util.date.DateFormatter
import org.penakelex.obscura.presentation.util.date.toDisplayString

@Composable
fun SessionCard(
    session: SessionInfo,
    onRevokeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (session.isCurrent) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (session.isCurrent) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val createdAtLabel = remember(session.createdAt) {
        DateFormatter.formatRelative(session.createdAt)
    }
    val expiresAtLabel = remember(session.expiresAt) {
        DateFormatter.formatRelative(session.expiresAt)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(
            defaultElevation = ObscuraDimens.Padding.xs,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ObscuraDimens.Padding.m),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.DeviceUnknown,
                contentDescription = null,
                modifier = Modifier.size(ObscuraDimens.Size.iconLarge),
                tint = contentColor,
            )

            Spacer(Modifier.width(ObscuraDimens.Padding.m))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(
                    ObscuraDimens.Padding.xs,
                ),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        ObscuraDimens.Padding.s,
                    ),
                ) {
                    Text(
                        text = session.deviceInfo
                            ?: stringResource(Res.string.unknown_device),
                        style = MaterialTheme.typography.titleMedium,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (session.isCurrent) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = stringResource(
                                Res.string.current_session,
                            ),
                            modifier = Modifier.size(
                                ObscuraDimens.Size.iconSmall,
                            ),
                            tint = contentColor,
                        )
                    }
                }

                Text(
                    text = stringResource(
                        Res.string.session_created,
                        createdAtLabel.toDisplayString(),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f),
                )

                Text(
                    text = stringResource(
                        Res.string.session_expires,
                        expiresAtLabel.toDisplayString(),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.7f),
                )
            }

            if (!session.isCurrent) {
                Spacer(Modifier.width(ObscuraDimens.Padding.m))
                ObscuraButton(
                    text = stringResource(Res.string.revoke),
                    onClick = onRevokeClick,
                    variant = ButtonVariant.DESTRUCTIVE,
                )
            }
        }
    }
}