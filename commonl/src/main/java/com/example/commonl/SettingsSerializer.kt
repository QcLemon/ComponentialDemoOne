package com.example.commonl

import androidx.datastore.core.Serializer

//object SettingsSerializer : Serializer<UserSettings> {
//    override val defaultValue: Settings = Settings.getDefaultInstance()
//
//    override suspend fun readFrom(input: InputStream): Settings {
//        try {
//            return Settings.parseFrom(input)
//        } catch (exception: InvalidProtocolBufferException) {
//            throw CorruptionException("Cannot read proto.", exception)
//        }
//    }
//
//    override suspend fun writeTo(
//        t: Settings,
//        output: OutputStream) = t.writeTo(output)
//}
//
//val Context.settingsDataStore: DataStore<Settings> by dataStore(
//    fileName = "settings.pb",
//    serializer = SettingsSerializer
//)