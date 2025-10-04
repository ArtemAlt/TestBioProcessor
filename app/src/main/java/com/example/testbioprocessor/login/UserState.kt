package com.example.testbioprocessor.login


data class UserState(
    var login: String = "",
    val isLoginSaved: Boolean = false,
    val vectorSaved: UserVectorState = UserVectorState()
)
