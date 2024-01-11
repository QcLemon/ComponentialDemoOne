package com.example.componentialdemo.ui

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import com.example.componentialdemo.UserSettings
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream

class ProtoDataStore(private val dataStore: DataStore<UserSettings>) {

    // 获取用户名的流
    val userNameFlow: Flow<String> = dataStore.data.catch { exception ->
        if (exception is IllegalStateException) {
            emit(UserSettings.getDefaultInstance())
        } else {
            throw exception
        }
    }.map { userSettings ->
        userSettings.username
    }

    // 获取夜间模式设置的流
    val isDarkModeFlow: Flow<Boolean> = dataStore.data.catch { exception ->
        if (exception is IllegalStateException) {
            emit(UserSettings.getDefaultInstance())
        } else {
            throw exception
        }
    }.map { userSettings ->
        userSettings.isDarkMode
    }

    // 更新用户名
    suspend fun updateUserName(username: String) {
        dataStore.updateData { userSettings ->
            userSettings.toBuilder().setUsername(username).build()
        }
    }

    // 更新夜间模式设置
    suspend fun updateDarkMode(isDarkMode: Boolean) {
        dataStore.updateData { userSettings ->
            userSettings.toBuilder().setIsDarkMode(isDarkMode).build()
        }
    }

    // 其他偏好设置的更新方法可根据需要添加
}

//// 创建ProtoDataStore实例
//fun createProtoDataStore(): ProtoDataStore {
//    return ProtoDataStoreBuilder(UserSettings.getDefaultInstance().toByteArray())
//        .setSerializer(UserSettingsSerializer)
//        .setErrorHandlers(
//            ReplaceFileCorruptionHandler<UserSettings>(),
//            WellKnownIOExceptionHandler
//        )
//        .build()
//}
