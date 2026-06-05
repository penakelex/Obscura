package org.penakelex.obscura.presentation.util.event

sealed interface SnackbarAction {
    data object Undo : SnackbarAction
    data object Retry : SnackbarAction
    data object OpenDetails : SnackbarAction
}