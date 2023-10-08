//package com.example.goodsmodule.data
//
//import okhttp3.Interceptor
//import okhttp3.OkHttpClient
//import java.net.Proxy
//import java.util.concurrent.TimeUnit
//
//object GoodsRetrofit {
//    private const val BASE_URL = "https://api.jybtech.cn/zjkj-app/"
//
//    //retrofit对象的实例化
//    val retrofit = Retrofit.Builder()
//            .baseUrl(BASE_URL)
//            .client(getOkHttpClient(150, null))
//            .addConverterFactory(CryptoConverterFactory.create())
//            .build()
//
//    //调用apiService   //创建网络请求接口对象实例
//    val apiService: GoodsApiService = createApi()
//
//    //已定义好了retrofit对象实例
//    inline fun <reified T> createApi(): T {
//        return retrofit.create(T::class.java)
//    }
//
//    /**
//     * 自定义请求体的超时时间和添加拦截器
//     *
//     * @param timeOut 超时时间
//     * @return
//     */
//    private fun getOkHttpClient(timeOut: Int, interceptorList: List<Interceptor>?): OkHttpClient? {
//        val okHttpClientBuilder = initOkHttpClient(timeOut)
//        if (!MLog.DEBUG_LOG) {
//            okHttpClientBuilder.proxy(Proxy.NO_PROXY)
//        }
//        /* okHttpClientBuilder.addInterceptor(new BaseUrlInterceptor());*/
//        okHttpClientBuilder.addInterceptor(CommonInterceptor(null))
//        //            okHttpClientBuilder.addInterceptor(new UserInfoCommonInterceptor());
//        return okHttpClientBuilder.build()
//    }
//
//    /**
//     * 初始化  OkHttpClient
//     */
//    private fun initOkHttpClient(timeOut: Int): OkHttpClient.Builder {
//        return OkHttpClient.Builder()
//                .connectTimeout(timeOut.toLong(), TimeUnit.SECONDS)
//                .readTimeout(timeOut.toLong(), TimeUnit.SECONDS)
//                .writeTimeout(timeOut.toLong(), TimeUnit.SECONDS) //解决Android 10上获取相关信任凭证
//                .sslSocketFactory(SSLSocketClient.getSSLSocketFactory(), SSLSocketClient.getX509TrustManager())
//    }
//}