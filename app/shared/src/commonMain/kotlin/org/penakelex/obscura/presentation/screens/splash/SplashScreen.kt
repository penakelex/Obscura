package org.penakelex.obscura.presentation.screens.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.app_name
import obscura.app.shared.generated.resources.splash_tagline
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.penakelex.obscura.domain.model.auth.SessionState
import org.penakelex.obscura.domain.usecase.auth.ObserveSessionUseCase
import org.penakelex.obscura.presentation.navigation.NavRoute
import org.penakelex.obscura.presentation.navigation.Navigator
import org.penakelex.obscura.presentation.theme.ObscuraDimens
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    navigator: Navigator = koinInject(),
    observeSession: ObserveSessionUseCase = koinInject(),
) {
    val sessionState = observeSession().collectAsState().value
    val appName = stringResource(Res.string.app_name)

    val animationStarted = remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (animationStarted.value) 1f else 0f,
        animationSpec = tween(durationMillis = ANIMATION_DURATION_MS),
        label = "SplashAlpha",
    )
    val scale by animateFloatAsState(
        targetValue = if (animationStarted.value) 1f else 0.7f,
        animationSpec = tween(durationMillis = ANIMATION_DURATION_MS),
        label = "SplashScale",
    )

    LaunchedEffect(Unit) {
        animationStarted.value = true
    }

    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.Loading) return@LaunchedEffect

        coroutineScope {
            val minDurationJob = async {
                delay(MIN_SPLASH_DURATION_MS.milliseconds)
            }
            awaitAll(minDurationJob)
        }

        when (sessionState) {
            is SessionState.Authenticated -> {
                navigator.navigate(NavRoute.Main.NotesList) {
                    popUpTo(NavRoute.Splash) { inclusive = true }
                    launchSingleTop = true
                }
            }

            SessionState.Unauthenticated -> {
                navigator.navigate(NavRoute.Main.NotesList) {
                    popUpTo(NavRoute.Splash) { inclusive = true }
                    launchSingleTop = true
                }
            }

            SessionState.Loading -> {}
        }
    }

    SplashScreenContent(
        appName = appName,
        alpha = alpha,
        scale = scale,
    )
}

@Composable
private fun SplashScreenContent(
    appName: String,
    alpha: Float,
    scale: Float,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .alpha(alpha)
                .scale(scale),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(ObscuraDimens.Size.fabSize * 2),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(ObscuraDimens.Padding.l))

            Text(
                text = appName,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(ObscuraDimens.Padding.s))

            Text(
                text = stringResource(Res.string.splash_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private const val MIN_SPLASH_DURATION_MS = 800L
private const val ANIMATION_DURATION_MS = 600