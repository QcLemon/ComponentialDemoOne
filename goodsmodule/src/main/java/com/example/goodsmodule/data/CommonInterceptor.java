package com.example.goodsmodule.data;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;
import com.swgk.core.BaseAppHelper;
import com.swgk.core.BuildConfig;
import com.swgk.core.base.api.AESUtils;
import com.swgk.core.base.model.preference.BasePreferenceSource;
import com.swgk.core.util.MLog;
import com.swgk.core.util.NetWorkUtil;
import com.swgk.core.util.SharedPreferenceUtil;
import com.swgk.core.util.UiUtil;
import com.zjkj.http.entity.BaseResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import cn.hutool.core.exceptions.ExceptionUtil;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * Retrofit Interceptor(拦截器) 拦截请求并做相关处理
 */
public class CommonInterceptor implements Interceptor {

    private static final String TAG = "NetWorkEventListener";
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private Gson gson = new Gson();

    private BasePreferenceSource preferenceSource;
    private int type = 0;

    public CommonInterceptor(BasePreferenceSource preferenceSource) {
        this.preferenceSource = preferenceSource;
    }

    @Override
    public synchronized Response intercept(Chain chain) throws IOException {

        //是否需要缓存
        boolean isCache = getisCache(chain);
        //是否是刷新
        boolean isRefresh = getisRefresh(chain);
        // 创建自定义缓存工具
        CacheHttpInternal cacheHttpInternal = new CacheHttpInternal();

        // 对请求头二次处理
        Request request = rebuildRequest(chain.request(), isCache, isRefresh);
        //该方法为了处理isCache = fale不走setBeforeNetworking方法问题
         isCache  =  getPartCacheData(request, isCache);
        //当需要缓存，并且需要更新缓存的时候直接联网获取缓存数据
        if (isCache && !isRefresh) {
            //联网前对缓存逻辑处理,如果缓存处理异常，直接获取网络数据
            Response responseCache = setBeforeNetworking(request, cacheHttpInternal, isCache, isRefresh);
            if (null != responseCache) {
                return responseCache;
            }
        }

        boolean isNetWork = NetWorkUtil.isNetworkConnected(BaseAppHelper.get().getInstance());
        if (!isNetWork) {
            throw new IOException("无网络，请检查网络连接");
        }
        // 请求联网
        Response response = chain.proceed(request);

        //解密替换data
        response = getAESPayloadEntity(response, cacheHttpInternal, getisClean(chain));

        //联网后对，数据缓存逻辑处理
        setAfterNetworking(response, cacheHttpInternal, isCache, isRefresh);
        // 输出返回结果
        if (MLog.DEBUG_LOG) {
            showLogDebug(response);
        }
        return response;
    }

    /**
     * 该方法为了处理setBeforeNetworking不走的
     *
     * @param request
     * @param isCache
     */
    private boolean getPartCacheData(Request request, boolean isCache) {
        type = 0;
        // 处理这个接口,始终缓存（获取全国地理三级json）
        if (request.url().toString().contains("common/simpleArea.json")) {
            return true;
        }
        // 处理这个接口,始终缓存（获取全国地理三级json）新数据
        if (request.url().toString().contains("v1/app/common/getAreaTree")) {
            return true;
        }
        if (request.url().toString().contains("v1/app/common/getUrlSetting")) {
            type = 1;
            return true;
        }
        return isCache;
    }

    /**
     * 处理接口解密替换源解析data数据
     *
     * @param response
     * @param cleanCode 部分接口解密后对象被字符串化，用此参数去除转义符
     * @return
     */
    private Response getAESPayloadEntity(Response response, CacheHttpInternal cacheHttpInternal, boolean cleanCode) {
        MLog.d("解密 CommonInterceptor getAESPayloadEntity", "执行解密过程");
        try {
            Headers headers = response.headers();
            //从响应头中获取参数 isEncryption 的值，当为 “1”，说明接口进行了加密
            if (headers.get("isEncryption") != null && headers.get("isEncryption").equals("1")) {
                String strJson = cacheHttpInternal.getBodyString(response);
                Type type = new TypeToken<BaseResponse>() {
                }.getType();
                BaseResponse baseEntity = gson.fromJson(strJson, type);
                if (baseEntity != null &&
                        (baseEntity.getData() == null ||
                                TextUtils.isEmpty(baseEntity.getData().toString())) &&
                        !TextUtils.isEmpty(baseEntity.getPayload())
//                        &&
//                        TextUtils.equals(baseEntity.getCode(),"200")
                ) {
                    //获取加密字符
                    String payload = baseEntity.getPayload();
                    Object json = "";
                    try {
                        json = AESUtils.decodeAES(payload, headers.get("dataType"));
                        MLog.d("解密 CommonInterceptor getAESPayloadEntity 解密 == " + json);
                    } catch (Exception e) {
                        String url = ExceptionUtil.stacktraceToString(new Throwable(response.request().url() + "\n密文 == " + payload + "\n"));
                        MLog.d("解密 CommonInterceptor getAESPayloadEntity 解密失败", url + e);
//                        json = strJson;
                    }
                    baseEntity.setData(json);
                    baseEntity.setPayload("");
                    String jsonString = gson.toJson(baseEntity);
                    if (cleanCode) {
                        jsonString = cleanCode(jsonString);
                    }
                    return setResponse(response, jsonString);
                }
                if (cleanCode) {
                    String jsonString = gson.toJson(baseEntity);
                    jsonString = cleanCode(jsonString);
                    return setResponse(response, jsonString);
                }
            }
            MLog.d("解密 CommonInterceptor getAESPayloadEntity", "执行解密完成");
        } catch (Exception e) {
            String url = response.request().url().toString();
            MLog.d("解密 CommonInterceptor getAESPayloadEntity 失败", url + e.toString());
        }
        return response;
    }

    private String cleanCode(String json) {
        return json.replace("\\\"", "\"")
                .replace("\"[", "[")
                .replace("]\"", "]")
                .replace("\"{", "{")
                .replace("}\"", "}");
    }

    /**
     * 根据 原Response构建新的Response，替换原来返回数据
     *
     * @return 返回新构建的 Response
     */
    private Response setResponse(Response response, String json) {
        return setResponse(response, json.getBytes());
    }

    /**
     * 根据 原Response构建新的Response，替换原来返回数据
     *
     * @return 返回新构建的 Response
     */
    private Response setResponse(Response response, byte[] bytes) {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        ResponseBody responseBody = ResponseBody.create(bytes, mediaType);
        return new Response.Builder()
                .request(response.request())
                .protocol(response.protocol())
                .code(response.code())
                .message(response.message())
                .body(responseBody)
                .headers(response.headers())
                .handshake(response.handshake())
                .networkResponse(response.networkResponse())
                .priorResponse(response.priorResponse())
                .receivedResponseAtMillis(response.receivedResponseAtMillis())
                .sentRequestAtMillis(response.sentRequestAtMillis())
                .build();
    }


    /**
     * 联网前对缓存逻辑处理,如果缓存处理异常，直接获取网络数据
     *
     * @param response
     * @param cacheHttpInternal
     * @param isCache
     * @param isRefresh
     */
    private void setAfterNetworking(Response response, CacheHttpInternal cacheHttpInternal, boolean isCache, boolean isRefresh) {
        try {
            MLog.d(" 数据缓存", "更新缓存数据  isCache = " + isCache + " isRefresh = " + isRefresh + "  URL " + response.request().url().toString());
            if (isCache || isRefresh) {
//            Long mkvPutTimeStart = new Date().getTime();
                if (response.code() == 200) {
                    cacheHttpInternal.put(response);
                }
//            if (BuildConfig.DEBUG) {
                //缓存时间打印
//                Long mkvPutTimeEnd = new Date().getTime();
//                long timeMkvPutLon = mkvPutTimeEnd - mkvPutTimeStart;
//                String timeMkvPut = "";
//                if (timeMkvPutLon >= 400) {
//                    timeMkvPut = "\n timeMKVStr:" + timeMkvPutLon + "ms";
//                } else {
//                    timeMkvPut = " timeMKVStr:" + timeMkvPutLon + "ms";
//                }
//                MLog.d(TAG, response.request().url().toString() + timeMkvPut);
//            }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 联网之前对缓存逻辑的处理
     *
     * @param request
     * @param isCache
     * @param isRefresh
     * @return
     * @throws IOException
     */
    private Response setBeforeNetworking(Request request, CacheHttpInternal cacheHttpInternal, boolean isCache, boolean isRefresh) throws IOException {
        try {
            Response responseCache = null;
            if (isCache) {
                if (type == 1) {
                    String url = request.url().toString();
                    String str1 = url.substring(0, url.indexOf("="));
                    String str2 = url.substring(str1.length() + 1, url.length());
                    // 当链接为H5时取本地数据库数据信息
                    responseCache = cacheHttpInternal.getUrlData(request, str2);
                } else {
                    // 当需要缓存的时候，从缓存里拿出来
                    responseCache = cacheHttpInternal.get(request);
                }
            }
            return responseCache;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 联网成功后用于测试时，接口数据进行格式化打印，只在
     *
     * @param response
     */
    private void showLogDebug(Response response) {
        try {
            // 将联网数据格式化打印
            getBodyString(response);
            // 请求接口消耗时间打印
            long timeDiff = response.receivedResponseAtMillis() - response.sentRequestAtMillis();
            String timeDiffStr = "";
            if (timeDiff >= 400) {
                timeDiffStr = "\n timeDiff:" + timeDiff + "ms";
            } else {
                timeDiffStr = " timeDiff:" + timeDiff + "ms";
            }
            MLog.d(TAG + " api 接口请求时间", response.request().url() + timeDiffStr);
        } catch (Exception e) {
            MLog.d(e.getMessage(), e.toString());
        }
    }

    /**
     * 是否需要缓存
     *
     * @param chain
     * @return 默认 false，返回是否需要缓存数据
     */
    private boolean getisCache(Chain chain) {
        boolean isCache = false;
        if (!TextUtils.isEmpty(chain.request().header("isCache"))) {
            isCache = "true".equals(chain.request().header("isCache"));
        }
        return isCache;
    }

    /**
     * 是否是刷新
     *
     * @param chain
     * @return 默认 false，返回是否是刷新
     */
    private boolean getisRefresh(Chain chain) {
        boolean isRefresh = false;
        if (!TextUtils.isEmpty(chain.request().header("isRefresh"))) {
            isRefresh = "true".equals(chain.request().header("isRefresh"));
        }
        return isRefresh;
    }

    /**
     * 是否清理json转义符
     *
     * @param chain
     * @return 默认 false，返回是否是刷新
     */
    private boolean getisClean(Chain chain) {
        boolean isClean = false;
        if (!TextUtils.isEmpty(chain.request().header("clean"))) {
            isClean = "true".equals(chain.request().header("clean"));
        }
        return isClean;
    }

    /**
     * 对参数做特殊处理
     *
     * @param request   请求实体
     * @param isCache   是否缓存
     * @param isRefresh 是否更新缓存
     * @return 返回请求实体
     * @throws IOException
     */
    private Request rebuildRequest(Request request, boolean isCache, boolean isRefresh) throws IOException {
        //对请求参数进行处理
        Request newRequest = setRequest(request);
        //查找Token，并判别使用那个token
        String token = setToken(request, newRequest, isCache, isRefresh);
        //判断是否生成新票据
        String sign = AESUtils.getSignStr();
        // 可根据需求添加或修改header,
        Request.Builder builder = newRequest.newBuilder();
        if (TextUtils.isEmpty(request.header("platform"))) {
            builder.addHeader("platform", "android");
        }
        if (TextUtils.isEmpty(request.header("Content-Type"))) {
            builder.addHeader("Content-Type", "application/x-www-form-urlencoded");
        }
        builder.addHeader("type", "app-api-java");
        builder.addHeader("sign", sign);
        if (BuildConfig.DEBUG) {
            builder.addHeader("profile", "dev");
        } else {
            builder.addHeader("profile", "pro");
        }

        if (!TextUtils.isEmpty(token)) {
            builder.addHeader("Authorization", token);
        }
        builder.addHeader("versionCode", String.valueOf(UiUtil.getLocalVersion()));
        builder.addHeader("channel", UiUtil.readMetaData("channel"));
        builder.addHeader("encryption-mode", "base64");
        return builder.build();
    }

    /**
     * 处理消息请求信息
     *
     * @param request
     * @return
     */
    private Request setRequest(Request request) {
        Request newRequest;
        if ("POST".equals(request.method())) {
            newRequest = rebuildPostRequest(request);
        } else if ("GET".equals(request.method())) {
            newRequest = rebuildGetRequest(request);
        } else {
            newRequest = request;
        }
        return newRequest;
    }

    /**
     * 查找Token，并判别使用那个token
     *
     * @return
     */
    private String setToken(Request request, Request newRequest, boolean isCache, boolean isRefresh) {
        String token = "";
        String tokenType = "app token";
       /* if (preferenceSource != null) {
            token = preferenceSource.getToken();
        } else {
            token = SharedPreferenceUtil.getInstance().getSaveStringData(SharedPreferenceUtil.LOGIN_TOKEN, "");
        }*/
        //废弃使用preferenceSource存储数据
        token = SharedPreferenceUtil.getInstance().getSaveStringData(SharedPreferenceUtil.LOGIN_TOKEN, "");

        List<String> headers = request.headers("tokenType");
        if (headers != null && headers.size() > 0 && "pc".equals(headers.get(0))) {
            tokenType = "pc token";
            token = SharedPreferenceUtil.getInstance().getSaveStringData(SharedPreferenceUtil.LOGIN_PC_TOKEN, "");
        }
        List<String> headersIsLogin = request.headers("isLogin");
        if (headersIsLogin != null && headersIsLogin.size() > 0) {
            for (String v : headersIsLogin) {
                if (TextUtils.equals(v, "true")) {
                    token = "Z2N0eDpnY3R4";
                    break;
                }
            }
        }

        if (!TextUtils.isEmpty(token)) {
            token = "Bearer " + token;
        }
        if (MLog.DEBUG_LOG) {
            MLog.d("api", tokenType + " request token = " + token);
            MLog.d("api", "request Url:" +
                    "  是否走缓存isCache == " + isCache +
                    "  是否更新缓存isRefresh == " + isRefresh +
                    "  请求方式" + newRequest.method() +
                    "  请求连接Url = " + newRequest.url().toString()
            );
        }
        return token;
    }

    /**
     * 可对post请求做特殊处理
     * 比如对某个接口做参数调整和参数替换，或添加公共请求参数
     */
    private Request rebuildPostRequest(Request request) {
        RequestBody originalRequestBody = request.body();
        try {
            MLog.d("api", "request body " + getParamContent(originalRequestBody));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return request.newBuilder().method(request.method(), originalRequestBody).build();
    }

    /**
     * 对get请求做统一参数处理
     */
    private Request rebuildGetRequest(Request request) {
        String url = request.url().toString();
        int separatorIndex = url.lastIndexOf("?");
        StringBuilder sb = new StringBuilder(url);
        if (separatorIndex == -1) {
            sb.append("?");
        }
        Request.Builder requestBuilder = request.newBuilder();
        return requestBuilder.url(sb.toString()).build();
    }

    /**
     * 获取常规post请求参数
     */
    private String getParamContent(RequestBody body) throws IOException {
        Buffer buffer = new Buffer();
        body.writeTo(buffer);
        return buffer.readUtf8();
    }

    /**
     * 将联网数据格式化打印
     *
     * @param response
     * @return
     * @throws IOException
     */
    private String getBodyString(Response response) throws IOException {
        ResponseBody responseBody = response.peekBody(Long.MAX_VALUE);
        Reader jsonReader = new InputStreamReader(responseBody.byteStream(), Charset.forName("UTF-8"));
        BufferedReader reader = new BufferedReader(jsonReader);
        StringBuilder sbJson = new StringBuilder();
        String line = reader.readLine();
        do {
            sbJson.append(line);
            line = reader.readLine();
        } while (line != null);
        String bodyString = sbJson.toString();
        MLog.d("api", "response:" +
                " \n url:" + response.request().url().toString() +
                " \n headers：\n" + response.request().headers().toString() +
                " \n JSON : " + bodyString);
        if (!response.request().url().toString().contains("common/simpleArea.json") &&
                !response.request().url().toString().contains("app/common/getLocation")) {
            if (bodyString.length()<=1000000){
                // 格式化打印网络请求返回
                Logger.json(bodyString);
            }
        }
        return bodyString;
    }
}
