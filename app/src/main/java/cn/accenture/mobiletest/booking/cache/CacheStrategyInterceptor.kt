package cn.accenture.mobiletest.booking.cache

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
        if (!request.cacheControl.noCache
            && !request.cacheControl.noStore
        ) {
            // 启用 FORCE_CACHE 策略，从缓存中读取数据
            val request2 = request.newBuilder()
                .cacheControl(CacheControl.FORCE_CACHE)
                .build()
            val response = chain.proceed(request2)
            if (response.isSuccessful) {
                // 有本地缓存，先返回缓存(无论是否过期)
                return response.newBuilder()
                    .removeHeader("Pragma")
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", request.cacheControl.toString())
                    .build()
            }
        }
        return chain.proceed(request)
    }
}


