package com.example.commonl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesDataStore(private val dataStore: DataStore<Preferences>) {

    // 定义偏好设置键值对
    private object PreferencesKeys {
        val USERNAME = stringPreferencesKey("username")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        // 添加其他偏好设置键值对
    }

    // 定义获取用户名的流
    val userNameFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USERNAME] ?: ""
    }

    // 定义获取夜间模式设置的流
    val isDarkModeFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.IS_DARK_MODE] ?: false
    }

    // 更新用户名
    suspend fun updateUserName(username: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USERNAME] = username
        }
    }

    // 更新夜间模式设置
    suspend fun updateDarkMode(isDarkMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = isDarkMode
        }
    }
    // 其他偏好设置的更新方法可根据需要添加
}