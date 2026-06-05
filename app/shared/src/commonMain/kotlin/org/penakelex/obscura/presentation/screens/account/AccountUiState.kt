package org.penakelex.obscura.presentation.screens.account

data class AccountUiState(
    val email: String = "",
    val isLoading: Boolean = true,
    val isPasswordDialogVisible: Boolean = false,
    val isEmailDialogVisible: Boolean = false,
    val isDeleteDialogVisible: Boolean = false,
    val isOperationInProgress: Boolean = false,
)