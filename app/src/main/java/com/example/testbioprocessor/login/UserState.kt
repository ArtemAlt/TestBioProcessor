package com.example.testbioprocessor.login


data class UserState(
    val login: String = "",
    val isLoginSaved: Boolean = false,
    val vectorSaved: UserVectorState = UserVectorState()
)
