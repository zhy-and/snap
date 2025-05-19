package com.snap.intelligence.musics.work
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody


class SnapInterceptor : Interceptor {


    interface BasicRequestListener {
        fun loadFailed(message: String)
        fun loadSuccessConfig(configInfo: String)
    }


    override fun intercept(chain: Interceptor.Chain): Response {
        snapConfigIntersRes = ""
        val snapConfigRequest = chain.request().newBuilder().build()
        val res = chain.proceed(snapConfigRequest)
        try {
            res.body?.let {
                snapConfigIntersRes = it.string()
                snapConfigType = it.contentType()
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        return res.newBuilder().body(snapConfigIntersRes.toResponseBody(snapConfigType)).build()
    }
    private var snapConfigType : MediaType?= null
    private var snapConfigIntersRes = ""
}