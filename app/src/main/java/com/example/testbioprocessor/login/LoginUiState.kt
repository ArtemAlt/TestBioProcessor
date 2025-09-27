package com.example.testbioprocessor.login


data class LoginUiState(
    val login: String = "",
    val isLoginSaved: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val showResetMessage: Boolean = false
)
