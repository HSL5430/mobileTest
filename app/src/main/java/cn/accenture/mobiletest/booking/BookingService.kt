package cn.accenture.mobiletest.booking

import android.os.Handler
import android.util.Log
import cn.accenture.mobiletest.App
import cn.accenture.mobiletest.booking.cache.CACHE_MAX_AGE_SECONDS
import cn.accenture.mobiletest.booking.cache.CacheStrategyInterceptor
import cn.accenture.mobiletest.booking.cache.NetworkInterceptor
import cn.accenture.mobiletest.util.NetworkUtils
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.http.RealResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * 请求数据, 并根据缓存策略处理
 * @param mainHandler 用来自动刷新数据
 * @param callback 回调函数，返回数据，返回true表示数据有效，false表示数据无效
 */
class BookingService(val mainHandler: Handler, val callback: (String?) -> Boolean) {

    private var mOkHttpClient: OkHttpClient? = null

    private fun getOkHttpClient() = if (mOkHttpClient == null) {
        val okHttpClient = OkHttpClient.Builder()
            // .connectTimeout(10_000, TimeUnit.MILLISECONDS)
            // .readTimeout(10_000, TimeUnit.MILLISECONDS)
            // .writeTimeout(10_000, TimeUnit.MILLISECONDS)
            .addNetworkInterceptor(NetworkInterceptor())
            .addInterceptor(CacheStrategyInterceptor())
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            // 自定义缓存目录:持久化缓存 Service data
            .cache(Cache(File(App.getInstance().cacheDir, "okhttp"), 1024 * 1024 * 1))
            .build()
        mOkHttpClient = okHttpClient
        okHttpClient
    } else {
        mOkHttpClient!!
    }

    /**
     * 请求数据, 并根据缓存策略处理
     * @param maxAge 缓存时间，单位秒，默认5分钟；小于等于0时，强制从网络获取数据
     */
    fun request(maxAge: Int = CACHE_MAX_AGE_SECONDS) {
        // 设置缓存策略
        val cacheControl = if (maxAge <= 0) {
            CacheControl.FORCE_NETWORK
        } else {
            CacheControl.Builder()
                .maxAge(maxAge, TimeUnit.SECONDS)
                .build()
        }
        val request = Request.Builder()
            .url("http://192.168.3.219/booking.json") // 实际开发过程中配置后端提供的api
            .cacheControl(cacheControl)
            .build()
        getOkHttpClient().newCall(request).enqueue(object : okhttp3.Callback {

            override fun onFailure(call: Call, e: IOException) {
                Log.e("Booking", "onFailure: ", e)
                callback(e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                val isRealResponseBody = response.body is RealResponseBody
                try {
                    if (call.isCanceled()) {
                        onFailure(call, IOException("Canceled!"))
                        return
                    }
                    if (response.code < 200 || response.code >= 300) {
                        onFailure(call, IOException("Response's code is : " + response.code))
                        return
                    }

                    val bodyStr = response.body?.string()
                    val str = StringBuilder()
                        .append("request: ")
                        .append(response.request.toString())
                        .append("\n\n")
                        .append("from: ")
                        .append(if (isRealResponseBody) "network" else "cache")
                        .append("\n\n")
                        .append("response: ")
                        .append('\n')
                        .append(bodyStr)
                        .toString()
                    callback(str)
                    Log.d("Booking", "onResponse: \n$bodyStr")
                } catch (e: Exception) {
                    onFailure(call, IOException(e))
                } finally {
                    try {
                        response.body?.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    // 检查缓存是否过期
                    if (response.request.cacheControl.onlyIfCached
                        && System.currentTimeMillis() - response.sentRequestAtMillis > response.cacheControl.maxAgeSeconds * 1000
                        && NetworkUtils.isConnected(App.getInstance())
                    ) {
                        // 已过期 && 有网络，重新请求数据，强制从网络获取
                        request(0)
                    } else if (isRealResponseBody) {
                        mainHandler.removeCallbacksAndMessages(null)
                        // 数据过期后，自动触发刷新机制
                        mainHandler.postDelayed({ request(0) }, 1000L * CACHE_MAX_AGE_SECONDS )
                    }
                }
            }
        })
    }
}


