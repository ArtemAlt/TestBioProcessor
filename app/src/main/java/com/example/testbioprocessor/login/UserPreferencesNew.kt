package com.example.testbioprocessor.login

import android.content.Context
import androidx.core.content.edit

class UserPreferencesNew(context: Context) {

    private val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)

    // Ключи для SharedPreferences
    companion object {
        private const val KEY_LOGIN = "user_login"
        private const val KEY_IS_LOGIN_SAVED = "is_login_saved"
        private const val KEY_VECTOR_IS_SAVED = "vector_is_saved"
        private const val KEY_VECTOR_LAST_UPDATED = "vector_data"
    }

    // Сохранить полное состояние пользователя
    fun saveUserState(userState: UserState) {
        sharedPreferences.edit().apply {
            putString(KEY_LOGIN, userState.login)
            putBoolean(KEY_IS_LOGIN_SAVED, userState.isLoginSaved)
            putBoolean(KEY_VECTOR_IS_SAVED, userState.vectorSaved.isSaved)
            putString(KEY_VECTOR_LAST_UPDATED, userState.vectorSaved.data)
            apply()
        }
    }

    // Получить полное состояние пользователя
    fun getUserState(): UserState {
        return UserState(
            login = sharedPreferences.getString(KEY_LOGIN, "") ?: "",
            isLoginSaved = sharedPreferences.getBoolean(KEY_IS_LOGIN_SAVED, false),
            vectorSaved = UserVectorState(
                isSaved = sharedPreferences.getBoolean(KEY_VECTOR_IS_SAVED, false),
                data = sharedPreferences.getString(KEY_VECTOR_LAST_UPDATED, "") ?: ""
            )
        )
    }

    // Отдельные методы для каждого поля

    fun saveLogin(login: String) {
        sharedPreferences.edit().putString(KEY_LOGIN, login).apply()
    }

    fun getLogin(): String {
        return sharedPreferences.getString(KEY_LOGIN, "") ?: ""
    }

    fun saveLoginStatus(isSaved: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_IS_LOGIN_SAVED, isSaved).apply()
    }

    fun getLoginStatus(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGIN_SAVED, false)
    }

    fun saveVectorState(vectorState: UserVectorState) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_VECTOR_IS_SAVED, vectorState.isSaved)
            putString(KEY_VECTOR_LAST_UPDATED, vectorState.data)
            apply()
        }
    }

    fun getVectorState(): UserVectorState {
        return UserVectorState(
            isSaved = sharedPreferences.getBoolean(KEY_VECTOR_IS_SAVED, false),
            data = sharedPreferences.getString(KEY_VECTOR_LAST_UPDATED, "") ?: ""
        )
    }

    // Очистить все данные пользователя
    fun clearUserData() {
        sharedPreferences.edit().clear().apply()
    }

    // Проверка есть ли сохраненный пользователь
    fun hasSavedUser(): Boolean {
        return getLogin().isNotEmpty() && getLoginStatus()
    }

    fun drop() {
        sharedPreferences.edit().clear().apply()
    }

}
