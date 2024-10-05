package cn.accenture.mobiletest.booking.cache

import cn.accenture.mobiletest.App
import cn.accenture.mobiletest.util.NetworkUtils
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 缓存策略拦截器：
 *
 * 处理无网络的时候能使用缓存数据，无论缓存是否过期
 */
class CacheStrategyInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (!NetworkUtils.isConnected(App.getInstance())
            && !request.cacheControl.noCache
            && !request.cacheControl.noStore
        ) {
            // 如果没有网络，则启用 FORCE_CACHE 策略，从缓存中读取数据
            // 而且这里排除了 FORCE_NETWORK 的情况
            val request2 = request.newBuilder()
                .cacheControl(CacheControl.FORCE_CACHE)
                .build()
            val response = chain.proceed(request2)
            return response.newBuilder()
                .removeHeader("Pragma")
                .removeHeader("Cache-Control")
                .header("Cache-Control", request.cacheControl.toString())
                .build()
        }
        return chain.proceed(request)
    }
}


