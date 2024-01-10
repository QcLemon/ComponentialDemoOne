package com.example.commonl

import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

//class ProtoDataStore(private val dataStore: DataStore<UserSettings>) {
//
//    // 获取用户名的流
//    val userNameFlow: Flow<String> = dataStore.data.catch { exception ->
//        if (exception is IllegalStateException) {
//            emit(UserSettings.getDefaultInstance())
//        } else {
//            throw exception
//        }
//    }.map { userSettings ->
//        userSettings.username
//    }
//
//    // 获取夜间模式设置的流
//    val isDarkModeFlow: Flow<Boolean> = dataStore.data.catch { exception ->
//        if (exception is IllegalStateException) {
//            emit(UserSettings.getDefaultInstance())
//        } else {
//            throw exception
//        }
//    }.map { userSettings ->
//        userSettings.isDarkMode
//    }
//
//    // 更新用户名
//    suspend fun updateUserName(username: String) {
//        dataStore.updateData { userSettings ->
//            userSettings.toBuilder().setUsername(username).build()
//        }
//    }
//
//    // 更新夜间模式设置
//    suspend fun updateDarkMode(isDarkMode: Boolean) {
//        dataStore.updateData { userSettings ->
//            userSettings.toBuilder().setIsDarkMode(isDarkMode).build()
//        }
//    }
//
//    // 其他偏好设置的更新方法可根据需要添加
//}
//
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
//
//// 定义UserSettings的序列化器
//object UserSettingsSerializer : Serializer<UserSettings> {
//    override val defaultValue: UserSettings = UserSettings.getDefaultInstance()
//
//    override fun readFrom(input: InputStream): UserSettings {
//        try {
//            return UserSettings.parseFrom(input)
//        } catch (exception: InvalidProtocolBufferException) {
//            throw CorruptionException("Failed to parse proto.", exception)
//        }
//    }
//
//    override fun writeTo(t: UserSettings, output: OutputStream) {
//        t.writeTo(output)
//    }
//}