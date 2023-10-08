package com.example.goodsmodule

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter

@Route(path = "/goodsmodule/GoodsMainActivity")
class GoodsMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.goods_activity_goods_main)
        ARouter.init(application)
        ARouter.getInstance().inject(this)
    }
}