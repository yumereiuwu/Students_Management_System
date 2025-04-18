// data/preferences/AppPreferences.kt
package com.example.students_system.data.preferences

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val PREFS_NAME = "StudentManagerPrefs"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"
    private const val KEY_LOGGED_IN_USER_ID = "loggedInUserId"
    private const val KEY_REMEMBER_ME = "rememberMe"
    private const val KEY_REMEMBERED_USERNAME = "rememberedUsername" // Lưu username nếu remember me

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setLoggedIn(context: Context, isLoggedIn: Boolean, userId: Long = -1) {
        getPrefs(context).edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
            if (isLoggedIn) {
                putLong(KEY_LOGGED_IN_USER_ID, userId)
            } else {
                remove(KEY_LOGGED_IN_USER_ID) // Xóa id khi logout
            }
            apply()
        }
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getLoggedInUserId(context: Context): Long {
        return getPrefs(context).getLong(KEY_LOGGED_IN_USER_ID, -1)
    }

    fun setRememberMe(context: Context, remember: Boolean, username: String = "") {
        getPrefs(context).edit().apply {
            putBoolean(KEY_REMEMBER_ME, remember)
            if (remember) {
                putString(KEY_REMEMBERED_USERNAME, username)
            } else {
                remove(KEY_REMEMBERED_USERNAME)
            }
            apply()
        }
    }

    fun shouldRememberMe(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_REMEMBER_ME, false)
    }

    fun getRememberedUsername(context: Context): String? {
        return getPrefs(context).getString(KEY_REMEMBERED_USERNAME, null)
    }

    fun clearLoginData(context: Context) {
        getPrefs(context).edit().apply {
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_LOGGED_IN_USER_ID)
            // Giữ lại remember me và username nếu muốn
            // remove(KEY_REMEMBER_ME)
            // remove(KEY_REMEMBERED_USERNAME)
            apply()
        }
    }
    fun clearRememberMe(context: Context) {
        getPrefs(context).edit().apply {
            remove(KEY_REMEMBER_ME) // Xóa trạng thái remember me
            remove(KEY_REMEMBERED_USERNAME) // Xóa username đã lưu
            apply() // Áp dụng thay đổi
        }
    }
}