package com.example.testbioprocessor.login

import android.content.Context
import androidx.core.content.edit

class UserPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)

    var login: String
        get() = prefs.getString(LOGIN_KEY, null).orEmpty()
        set(value) {
            prefs.edit(commit = true) { putString(LOGIN_KEY, value) }
        }

    companion object {
        private const val USER_PREFS = "USER_PREFS"
        private const val LOGIN_KEY = "login"
    }
}
