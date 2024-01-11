package com.example.componentialdemo

import android.R.attr.value
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.alibaba.android.arouter.launcher.ARouter
import com.example.commonl.PreferencesDataStore
import com.example.componentialdemo.databinding.ActivityMainBinding
import com.example.componentialdemo.ui.ProtoDataStore
import com.example.componentialdemo.ui.SettingsSerializer
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

val Context.settingsDataStore: DataStore<UserSettings> by dataStore(
    fileName = "settings.pb",
    serializer = SettingsSerializer
)

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val preferencesDataStore by lazy {
        PreferencesDataStore(this.dataStore)
    }

    //34
//    val multiProcessDataStore by lazy {
//        ProtoDataStore(MultiProcessDataStoreFactory.create(
//            serializer = SettingsSerializer,
//            produceFile = { File("settings.pb") }
//        ))
//    }

    private val protoDataStore by lazy {
        ProtoDataStore(this.settingsDataStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MMKV.initialize(application)
   //     ARouter.init(application)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        binding.btnNav.setOnClickListener {
            ARouter.getInstance().build("/commonl/CommonLActivity").withString("orderId", "aaa").navigation()
        }

        binding.btnConfirm.setOnClickListener {
            updatePreferencesUserName(binding.edDatastore.text.toString())
            updateProtoUserName(binding.edDatastore.text.toString())
            initPreferencesData()
            initProtoData()
        }

        initPreferencesData()
        initProtoData()
    }

    fun initMMkv() {
        val mmkv = MMKV.defaultMMKV()
        mmkv?.encode("key", "value") // 存储数据，value可以是 boolean、int、float、double、String等类型
//        val result = mmkv!!.decodeBool("key") // 读取布尔值
//
//        val result = mmkv!!.decodeInt("key") // 读取整数
//
//        val result = mmkv!!.decodeFloat("key") // 读取浮点数
    }

    fun initPreferencesData() {
        // 获取用户名的示例
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                preferencesDataStore.userNameFlow.collect {
                    // 在此处处理用户名数据
                    binding.tvDatastore.text = "preferences用户名：" + it
                }
            }

            withContext(Dispatchers.Main) {
                delay(520)
                preferencesDataStore.userNameFlow.collect {
                    // 在此处处理用户名数据
                    binding.tvDatastore.text = "preferences用户名：520 s" + it
                }
            }
        }
    }

    fun initProtoData() {
        // 获取用户名的示例
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                protoDataStore.userNameFlow.collect {
                    // 在此处处理用户名数据
                    binding.tvDatastore2.text = "proto用户名：" + it
                }
            }
        }
    }

    // 更新用户名的示例
    fun updatePreferencesUserName(username: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                preferencesDataStore.updateUserName(username)
            }
            withContext(Dispatchers.IO) {
                delay(500L)
                preferencesDataStore.updateUserName(username + "50")
            }
            withContext(Dispatchers.IO) {
                delay(1000L)
                preferencesDataStore.updateUserName(username + "100")
            }
        }
    }

    // 更新用户名的示例
    fun updateProtoUserName(username: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                protoDataStore.updateUserName(username)
            }
            withContext(Dispatchers.IO) {
                protoDataStore.updateUserName(username)
            }
            withContext(Dispatchers.IO) {
                delay(50L)
                protoDataStore.updateUserName(username + "50")
            }
            withContext(Dispatchers.IO) {
                delay(100L)
                protoDataStore.updateUserName(username + "100")
            }
        }
    }
}