package com.example.testbioprocessor.login

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(context: Context) {
    companion object {
        private val USER_LOGIN_KEY = stringPreferencesKey("user_login")
    }

    private val dataStore = context.applicationContext.dataStore

    val login: Flow<String?> = dataStore.data.map { prefs ->
        prefs[USER_LOGIN_KEY]
    }

    suspend fun saveLogin(login: String) {
        dataStore.edit { prefs ->
            prefs[USER_LOGIN_KEY] = login
        }
    }

    suspend fun clearLogin() {
        dataStore.edit { prefs ->
            prefs.remove(USER_LOGIN_KEY)
        }
    }

}
