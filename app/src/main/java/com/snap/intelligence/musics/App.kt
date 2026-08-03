package com.snap.intelligence.musics

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.datastore.preferences.core.edit
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.Utils
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_AF_CAMPAIGN
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_AF_MEDIA_SOURCE
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_APP_REFER_INFO
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_IS_M_USER
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.snap.intelligence.musics.constants.SnapPosManager.lauchFlag
import com.snap.intelligence.musics.constants.ContentCountryKey
import com.snap.intelligence.musics.constants.ContentLanguageKey
import com.snap.intelligence.musics.constants.CountryCodeToName
import com.snap.intelligence.musics.constants.InnerTubeCookieKey
import com.snap.intelligence.musics.constants.LanguageCodeToName
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_AF_USER
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_GOOGLE_REFER_SOURCE

import com.snap.intelligence.musics.constants.MaxImageCacheSizeKey
import com.snap.intelligence.musics.constants.ProxyEnabledKey
import com.snap.intelligence.musics.constants.ProxyTypeKey
import com.snap.intelligence.musics.constants.ProxyUrlKey
import com.snap.intelligence.musics.constants.SYSTEM_DEFAULT
import com.snap.intelligence.musics.constants.UseLoginForBrowse
import com.snap.intelligence.musics.constants.VisitorDataKey
import com.snap.intelligence.musics.extensions.toEnum
import com.snap.intelligence.musics.extensions.toInetSocketAddress
import com.snap.intelligence.musics.utils.dataStore
import com.snap.intelligence.musics.utils.get
import com.snap.intelligence.musics.work.HttpManager
import com.tencent.mmkv.MMKV
import com.zionhuang.innertube.MainBe
import com.zionhuang.innertube.models.YouTubeLocale
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.net.Proxy
import java.util.Locale
import java.util.UUID


@HiltAndroidApp
class App : Application(), ImageLoaderFactory {
    private var referrerClient: InstallReferrerClient?= null
    private var oldTimes = 0L
    private val appScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    )

    private fun initAdIdWithTimeout() {
        if (TextUtils.isEmpty(MMKV.defaultMMKV().getString("ad_id",""))){
            appScope.launch {
                try {
                    val adId = withTimeoutOrNull(15_000) { // 15秒超时
                        withContext(Dispatchers.IO) {
                            AdvertisingIdClient.getAdvertisingIdInfo(applicationContext).id
                        }
                    } ?: generateFallbackId()
//                LogUtils.e("adId---"+adId)
                        MMKV.defaultMMKV().putString("ad_id",adId)
                } catch (e: Exception) {
//                LogUtils.e("AdIdFailed: ${e.message}")
                }
            }
        }

    }

    private fun generateFallbackId(): String {
        return UUID.randomUUID().toString()
    }

    override fun onTerminate() {
        appScope.cancel()
        super.onTerminate()
    }
    companion object{
        @JvmField
        var instance: Context? = null
        //todo
        var isLocal = false
        var isOrganic = false
        var isNextPlay = false
        var ouUserIsCallBack = false
        var referStatusCallBack = false
        var isClicked = false
    }

    private fun initManager() {
        MobileAds.initialize(this)
        HttpManager.get().initNetWork()
        initReferInfos()
        initAppFly()
    }

    private fun initAppFly() {
        LogUtils.getConfig().isLogSwitch = false
        AppsFlyerLib.getInstance().init("enpU6TQdosciudeUJJu9fS", null, this)
        AppsFlyerLib.getInstance().start(this)
        AppsFlyerLib.getInstance()
            .registerConversionListener(this, object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(conversionDataMap: Map<String, Any>) {
                    try {
//                        LogUtils.e(conversionDataMap.toString())
                        // 判断流量类型
                        val afStatus = conversionDataMap["af_status"] as String?
                        if (!TextUtils.isEmpty(afStatus)){
                            if (afStatus == "Non-organic"){
                                MMKV.defaultMMKV().putBoolean(MUSIC_CONSTANT_IS_M_USER,true)
                            }
                        }
                        // 判断流量來源
                        val media_source = conversionDataMap["media_source"] as String?
                        if (!TextUtils.isEmpty(media_source)){
                            if (media_source?.contains("facebook",true) == true){
                                MMKV.defaultMMKV().putBoolean(MUSIC_CONSTANT_IS_M_USER,true)
                            }
                        }
                        // 判断活動名稱
                        val campaign = conversionDataMap["campaign"] as String?
                        if (!TextUtils.isEmpty(campaign)){
                            if (campaign?.contains("0006") == true){
                                MMKV.defaultMMKV().putBoolean(MUSIC_CONSTANT_IS_M_USER,true)
                            }
                        }

                        MMKV.defaultMMKV().putString(MUSIC_CONSTANT_AF_MEDIA_SOURCE,conversionDataMap["media_source"] as String?)
                        MMKV.defaultMMKV().putString(MUSIC_CONSTANT_AF_CAMPAIGN,conversionDataMap["campaign"] as String?)
                        MMKV.defaultMMKV().putString(MUSIC_CONSTANT_AF_USER,conversionDataMap["af_status"] as String?)
//                        LogUtils.e("AF", "来源afStatus：$afStatus")
                    }catch (e : Exception){
                        e.printStackTrace()
                    }

                }

                override fun onConversionDataFail(errorMessage: String) {


                } // 其他回调方法省略...

                override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                }

                override fun onAttributionFailure(p0: String?) {
                }
            })

    }

    private fun initReferInfos() {
        try {
            referrerClient = InstallReferrerClient.newBuilder(this).build()
            referrerClient?.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    referStatusCallBack = true
                    when (responseCode) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            LogUtils.e("InstallReferrerClient success")
                            try {
                                referrerClient?.installReferrer?.let {
                                    val referrerUrl: String = it.installReferrer
                                    LogUtils.e("InstallReferrerClient","InstallReferrerClient success$referrerUrl")
                                    if (!TextUtils.isEmpty(referrerUrl)){
                                        MMKV.defaultMMKV().putString(MUSIC_CONSTANT_APP_REFER_INFO,referrerUrl)
                                        if (referrerUrl.contains("facebook")){
                                            MMKV.defaultMMKV().putBoolean(MUSIC_CONSTANT_IS_M_USER,true)
                                            MMKV.defaultMMKV().putString(MUSIC_CONSTANT_GOOGLE_REFER_SOURCE,"facebook")
                                        }else if (referrerUrl.contains("instagram")){
                                            MMKV.defaultMMKV().putBoolean(MUSIC_CONSTANT_IS_M_USER,true)
                                            MMKV.defaultMMKV().putString(MUSIC_CONSTANT_GOOGLE_REFER_SOURCE,"instagram")
                                        }else{
                                            MMKV.defaultMMKV().putString(MUSIC_CONSTANT_GOOGLE_REFER_SOURCE,"other")
                                        }
                                        referrerClient?.endConnection()
                                    }
                                }

                            }catch (e : Exception){
                                referrerClient?.endConnection()
                                e.printStackTrace()
                            }

                        }
                        InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                            // API not available on the current Play Store app.
                            referrerClient?.endConnection()
                        }
                        InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                            // Connection couldn't be established.
                            referrerClient?.endConnection()
                        }
                    }
                }

                override fun onInstallReferrerServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            })

            initAdIdWithTimeout()


            AppUtils.registerAppStatusChangedListener(object : Utils.OnAppStatusChangedListener{
                var count = 0
                override fun onForeground(activity: Activity?) {

                }

                override fun onBackground(activity: Activity?) {
                    lauchFlag = false
                    try {
                        var times = System.currentTimeMillis()
                        count++
                        if (count> 50){
                            return
                        }
                        if (times - oldTimes < 2000){
                            LogUtils.e("empty ----------")
                            return
                        }
                        oldTimes = times
                        LogUtils.e("enter ht ----------")
                        ThreadUtils.runOnUiThreadDelayed({
                            ActivityUtils.finishAllActivities()
                        },300)
                    }catch (e : Exception){
                        e.printStackTrace()
                    }


                }
            })
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        instance = this
        lauchFlag = true
        MMKV.initialize(this)
        if (BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }
        initManager()
        val locale = Locale.getDefault()
        val languageTag = locale.toLanguageTag().replace("-Hant", "") // replace zh-Hant-* to zh-*
        MainBe.locale = YouTubeLocale(
            gl = dataStore[ContentCountryKey]?.takeIf { it != SYSTEM_DEFAULT }
                ?: locale.country.takeIf { it in CountryCodeToName }
                ?: "US",
            hl = dataStore[ContentLanguageKey]?.takeIf { it != SYSTEM_DEFAULT }
                ?: locale.language.takeIf { it in LanguageCodeToName }
                ?: languageTag.takeIf { it in LanguageCodeToName }
                ?: "en"
        )
//        if (languageTag == "zh-TW") {
//            KuGou.useTraditionalChinese = true
//        }

        if (dataStore[ProxyEnabledKey] == true) {
            try {
                MainBe.proxy = Proxy(
                    dataStore[ProxyTypeKey].toEnum(defaultValue = Proxy.Type.HTTP),
                    dataStore[ProxyUrlKey]!!.toInetSocketAddress()
                )
            } catch (e: Exception) {
//                Toast.makeText(this, "Failed to parse proxy url.", LENGTH_SHORT).show()
//                reportException(e)
            }
        }
        //todo
        MMKV.defaultMMKV().putBoolean(MUSIC_CONSTANT_IS_M_USER,true)
//todo
        if (dataStore[UseLoginForBrowse] == true) {
            MainBe.useLoginForBrowse = true
        }

        GlobalScope.launch {
            dataStore.data
                .map { it[VisitorDataKey] }
                .distinctUntilChanged()
                .collect { visitorData ->
                    MainBe.visitorData = visitorData
                        ?.takeIf { it != "null" } // Previously visitorData was sometimes saved as "null" due to a bug
                        ?: MainBe.visitorData().getOrNull()?.also { newVisitorData ->
                            dataStore.edit { settings ->
                                settings[VisitorDataKey] = newVisitorData
                            }
                        } ?: MainBe.DEFAULT_VISITOR_DATA
                }
        }
        GlobalScope.launch {
            dataStore.data
                .map { it[InnerTubeCookieKey] }
                .distinctUntilChanged()
                .collect { cookie ->
                    MainBe.cookie = cookie
                }
        }
    }
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
//        if (level == TRIM_MEMORY_UI_HIDDEN) {
//            LogUtils.e("appppp ----")
//            ActivityUtils.finishAllActivities()
//        }
    }
    override fun newImageLoader() = ImageLoader.Builder(this)
        .crossfade(true)
        .respectCacheHeaders(false)
        .allowHardware(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        .diskCache(
            DiskCache.Builder()
                .directory(cacheDir.resolve("coil"))
                .maxSizeBytes((dataStore[MaxImageCacheSizeKey] ?: 512) * 1024 * 1024L)
                .build()
        )
        .build()
}