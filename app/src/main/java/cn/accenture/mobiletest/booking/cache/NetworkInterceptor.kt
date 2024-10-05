package cn.accenture.mobiletest.booking.cache

import cn.accenture.mobiletest.App
import cn.accenture.mobiletest.util.NetworkUtils
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 网络拦截器：
 *
 * 处理服务端返回的数据，添加Cache-Control
 */
class NetworkInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val response = chain.proceed(request)
        if (NetworkUtils.isConnected(App.getInstance())) {
            val builder = response.newBuilder()
                .removeHeader("Pragma") //清除响应体对Cache有影响的信息
                .removeHeader("Cache-Control") //清除响应体对Cache有影响的信息
            if (request.cacheControl.maxAgeSeconds > 0 || request.cacheControl.onlyIfCached) {
                builder.header("Cache-Control", request.cacheControl.toString())
            } else {
                builder.header("Cache-Control", "max-age=300") // 默认5分钟
            }
            return builder.build()
        }
        return response
    }
}


