package com.example.componentialdemo

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val preferencesDataStore by lazy {
        PreferencesDataStore(this.dataStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
            updateUserName(binding.edDatastore.text.toString())
            initData()
        }

        initData()
    }

    fun initData() {
        // 获取用户名的示例
        lifecycleScope.launch {
            withContext(Dispatchers.Main) {
                preferencesDataStore.userNameFlow.collect {
                               // 在此处处理用户名数据
                    binding.tvDatastore.text = "用户名：" + it
                }
            }
        }
    }

    // 更新用户名的示例
    fun updateUserName(username: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                preferencesDataStore.updateUserName(username)
            }
            withContext(Dispatchers.IO) {
                delay(50L)
                preferencesDataStore.updateUserName(username + "50")
            }
            withContext(Dispatchers.IO) {
                delay(100L)
                preferencesDataStore.updateUserName(username + "100")
            }
        }
    }
}