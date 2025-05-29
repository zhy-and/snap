package com.snap.intelligence.musics.work

import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.util.Log
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.JsonUtils
import com.blankj.utilcode.util.LogUtils
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_APP_AD_CACHE_TM
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_APP_AD_DATA
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_APP_APP_CACHE_TM
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_APP_REFER_INFO
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_APP_REFRESH
import com.snap.intelligence.musics.constants.MMKV_COEFS
import com.snap.intelligence.musics.constants.MMKV_SYS
import com.snap.intelligence.musics.constants.MMKV_LINES
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_AF_CAMPAIGN
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_AF_MEDIA_SOURCE
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_IS_M_USER
import com.google.android.gms.ads.AdValue
import com.google.gson.Gson
import com.snap.intelligence.musics.App
import com.snap.intelligence.musics.App.Companion.ouUserIsCallBack
import com.snap.intelligence.musics.BasicView
import com.snap.intelligence.musics.R
import com.snap.intelligence.musics.beans.MobRequestData
import com.snap.intelligence.musics.beans.AppConfigBean
import com.snap.intelligence.musics.beans.JsonInfoArgs
import com.snap.intelligence.musics.beans.LauArgs
import com.snap.intelligence.musics.beans.PageArgs
import com.snap.intelligence.musics.constants.SnapPosManager
import com.snap.intelligence.musics.constants.SnapPosManager.getInstallSys
import com.snap.intelligence.musics.constants.MMKV_HOME_BACK_NO_FINISH
import com.snap.intelligence.musics.constants.MMKV_HOME_BTN_NO_FINISH
import com.snap.intelligence.musics.constants.MMKV_LAUCH_TIME
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_AF_USER
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_CURRENT_PAGE
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_GOOGLE_REFER_SOURCE
import com.snap.intelligence.musics.db.CommonTools.getCountryInfo
import com.snap.intelligence.musics.db.CommonTools.loadIds
import com.snap.intelligence.musics.extensions.logClickEvents
import com.snap.intelligence.musics.local.LocalHomeActivity
import com.snap.intelligence.musics.mob.GoogleMobileAdsConsentManager
import com.snap.intelligence.musics.mob.SnapMobManager
import com.snap.intelligence.musics.ui.start.HomeActivity
import com.tencent.mmkv.MMKV

class SnapBasicInterfaces {
    companion object{
        @JvmStatic
        fun commonConfig(): String {
            val ctx = App.instance ?: return ""
            return ctx.getString(R.string.uni_y)
        }

        private var  common_basic_info=
            "${commonConfig()}/wretq/nnfhv/kjtow/revision1/uks?ivicy=${
                loadIds(
                    App.instance
                )
            }&bmr=${commonUniIds()}"

        var value_info =
            "${commonConfig()}/tesx/mtjujg/eqsvpm/vera2/aec"


        val app_info =
            "${commonConfig()}/mexg/wqfn/prjdcg/release1/lbroo/kcauy?xixqx=${loadIds(
                App.instance
            )}&gawpn=${commonUniIds()}"


        @JvmStatic
        fun commonKys(): String {
            val ctx = App.instance ?: return ""
            return ctx.getString(R.string.uni_ky)
        }

        @JvmStatic
        fun commonUniIds(): String {
            val ctx = App.instance ?: return ""
            return ctx.getString(R.string.uni_id)

        }

        fun loadBaseInfos(view: BasicView, flag : Boolean = false) {
            if (System.currentTimeMillis() - MMKV.defaultMMKV()
                    .getLong(MUSIC_CONSTANT_APP_AD_CACHE_TM, 0) > 60 * 60 * 1000
            ) {
                HttpManager.get().requestConfigInfo(
                    HashMap(),
                    common_basic_info,
                    object : SnapInterceptor.BasicRequestListener {

                        override fun loadFailed(message: String) {
                        }

                        override fun loadSuccessConfig(configInfo: String) {
                            try {
                                Gson().fromJson(
                                    configInfo,
                                    MobRequestData::class.java
                                )?.run {
//                                    Log.e("vense succcc","vense suc ----- $configInfo")
                                    if (ymaelo?.firstOrNull()?.rfqhildkpkks?.ufnjdfhe?.firstOrNull()?.yjolftqvviiz?.sswk == 200) {
                                        if (ymaelo?.firstOrNull()?.rfqhildkpkks?.ufnjdfhe?.firstOrNull()?.yjolftqvviiz?.wnsou.isNullOrEmpty()) {
                                            if (!flag){
                                                musToMain(view)
                                            }
                                            return@run
                                        } else {
                                            LogUtils.e("vense succcc","vense suc ----- $configInfo")
                                            MMKV.defaultMMKV()
                                                .putString(MUSIC_CONSTANT_APP_AD_DATA, configInfo)
                                            MMKV.defaultMMKV().putLong(
                                                MUSIC_CONSTANT_APP_AD_CACHE_TM,
                                                System.currentTimeMillis()
                                            )
                                        }
                                        SnapMobManager.homeStudy.initMusConfig(view,flag)
                                    } else {
                                        if (!flag){
                                            musToMain(view)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                MMKV.defaultMMKV().putLong(MUSIC_CONSTANT_APP_AD_CACHE_TM, 0)
                                e.printStackTrace()
                            }

                        }
                    })
            } else {
                if (!flag){
                    SnapMobManager.homeStudy.initMusConfig(view)
                }
            }
            getProductInfos()
        }



        private fun jsonInfo(value: AdValue, adId: String, adsType: String, fbIpValue: Double,pos: Long): String {
            var json = ""
//            val catPa: HashMap<String, String> = java.util.HashMap()
            try {
                json  = Gson().toJson(JsonInfoArgs().apply {
                    gmifru = adId.replace("/","-")
                    bfjzjt = "Show"
                    phtbta = if(SnapPosManager.lauchFlag)"Cold" else("Warm")
                    yjjyhq = adsType
                    mwy = pos.toString()
                    inahpn = value.valueMicros.toString()
                    vpst = value.currencyCode
                    cbpsy = value.precisionType.toString()
                    dui = getCountryInfo(
                        App.instance
                    ).lowercase()
                    boxc = "AdMob"
                    snapFInstall = getInstallSys().toString()
                    snapFValue = fbIpValue.toString()
                    snapFOpens = MMKV.defaultMMKV().getLong("first_open",0L).toString()
                    snapPastTm = ((System.currentTimeMillis() - getInstallSys()) / 1000).toString()
                    snapMediaSource = "mediaSource"+  MMKV.defaultMMKV().getString(
                        MUSIC_CONSTANT_AF_MEDIA_SOURCE,"")
                    snapCampaign = "campaign"+MMKV.defaultMMKV().getString(MUSIC_CONSTANT_AF_CAMPAIGN,"")
                    snapAFUser = "snapAFUser"+MMKV.defaultMMKV().getString(MUSIC_CONSTANT_AF_USER,"")
                    googleRefer =  MMKV.defaultMMKV().getString(MUSIC_CONSTANT_GOOGLE_REFER_SOURCE,"empty")
                    currentUserPage = MMKV.defaultMMKV().getString(MUSIC_CONSTANT_CURRENT_PAGE,"local")
                })
//                catPa["gmifru"] = adId.replace("/","-")
//                catPa["bfjzjt"] =  "Show"
//                catPa["phtbta"] = if(SnapPosManager.lauchFlag)"Cold" else("Warm")
//                catPa["yjjyhq"] = adsType
//                catPa["mwy"] = pos.toString()
//                catPa["inahpn"] = value.valueMicros.toString()
//                catPa["vpst"] = value.currencyCode
//                catPa["cbpsy"] = value.precisionType.toString()
//                catPa["dui"] =  getCountryInfo(
//                    App.instance
//                ).lowercase()
//                catPa["boxc"] = "AdMob"
//
//                catPa["snapFInstall"] =  getInstallSys().toString()
//                catPa["snapFValue"] = fbIpValue.toString()
//                catPa["snapFOpens"] = MMKV.defaultMMKV().getLong("first_open",0L).toString()
//                catPa["snapPastTm"] = ((System.currentTimeMillis() - getInstallSys()) / 1000).toString()
//                catPa["snapMediaSource"] = "mediaSource"+  MMKV.defaultMMKV().getString(
//                    MUSIC_CONSTANT_AF_MEDIA_SOURCE,"")
//                catPa["snapCampaign"] = "campaign"+MMKV.defaultMMKV().getString(MUSIC_CONSTANT_AF_CAMPAIGN,"")
//                return catPa.toString()
            }catch (e : Exception){
                e.printStackTrace()
            }

            return json
        }

        private fun lauArgs(): String {
           var json = ""
            try {
               json =    Gson().toJson(LauArgs().apply {
                    phtbta = if(SnapPosManager.lauchFlag)"Cold" else("Warm")
                })
            }catch (e : Exception){
                e.printStackTrace()
            }
//            val catPa: HashMap<String, String> = java.util.HashMap()
//            try {
//                catPa["phtbta"] =  if(SnapPosManager.lauchFlag)"Cold" else("Warm")
//                return catPa.toString()
//            }catch (e : Exception){
//                e.printStackTrace()
//            }

            return json
        }

        fun handleValues(adsId: String, type: String, value: AdValue, fbIpValue: Double,pos: Long){
            try {
                HttpManager.get().upValues(
                    stringHashMap(adsId, type, value, fbIpValue,pos),
                    value_info,
                    object : SnapInterceptor.BasicRequestListener {

                        override fun loadFailed(message: String) {
                            LogUtils.e(" message",message)

                        }

                        override fun loadSuccessConfig(configInfo: String) {
                            LogUtils.e(" message",configInfo)

                        }


                    })
            }catch (e : Exception){
                e.printStackTrace()
            }

        }
        private fun pageArgs(): String {
            var json = ""
            try {
                json =    Gson().toJson(PageArgs().apply {
                    snapMediaSource = "mediaSource"+  MMKV.defaultMMKV().getString(
                        MUSIC_CONSTANT_AF_MEDIA_SOURCE,"")
                    snapCampaign = "campaign"+MMKV.defaultMMKV().getString(MUSIC_CONSTANT_AF_CAMPAIGN,"")
                    snapAFUser = "snapAFUser"+MMKV.defaultMMKV().getString(MUSIC_CONSTANT_AF_USER,"")
                    currentUserPage = MMKV.defaultMMKV().getString(MUSIC_CONSTANT_CURRENT_PAGE,"local")
                    googleRefer =  MMKV.defaultMMKV().getString(MUSIC_CONSTANT_GOOGLE_REFER_SOURCE,"empty")
                })
            }catch (e : Exception){
                e.printStackTrace()
            }

            return json
        }


        private fun installEventsUpload(){
            try {
                if (!MMKV.defaultMMKV().getBoolean("install_event",false)){
                    HttpManager.get().upValues(
                        installEvents(),
                        value_info,
                        object : SnapInterceptor.BasicRequestListener {

                            override fun loadFailed(message: String) {

                            }

                            override fun loadSuccessConfig(configInfo: String) {
                                LogUtils.e(configInfo)
//                                Log.e("install","install suc ----- $configInfo")

                                try {
                                    Gson().fromJson(
                                        configInfo,
                                        MobRequestData::class.java
                                    )?.run {
                                        if (ymaelo?.firstOrNull()?.rfqhildkpkks?.ufnjdfhe?.firstOrNull()?.yjolftqvviiz?.ycet == 200) {
                                            MMKV.defaultMMKV().putBoolean("install_event",true)
                                            LogUtils.e("install_event suc -----")
                                        }
                                    }
                                }catch (e : Exception){
                                    e.printStackTrace()
                                }

                            }


                        })
                }

            }catch (e : Exception){
                e.printStackTrace()
            }

        }


         fun pageAEventsUpload(){
            try {
                if (!MMKV.defaultMMKV().getBoolean("page_event_a",false)){
                    HttpManager.get().upValues(
                        pageEvents(),
                        value_info,
                        object : SnapInterceptor.BasicRequestListener {

                            override fun loadFailed(message: String) {

                            }

                            override fun loadSuccessConfig(configInfo: String) {
                                LogUtils.e(configInfo)
//                                Log.e("install","install suc ----- $configInfo")

                                try {
                                    Gson().fromJson(
                                        configInfo,
                                        MobRequestData::class.java
                                    )?.run {
                                        if (ymaelo?.firstOrNull()?.rfqhildkpkks?.ufnjdfhe?.firstOrNull()?.yjolftqvviiz?.ycet == 200) {
                                            MMKV.defaultMMKV().putBoolean("page_event_a",true)
                                            LogUtils.e("AAAApage_event_a suc -----")
                                        }
                                    }
                                }catch (e : Exception){
                                    e.printStackTrace()
                                }

                            }


                        })
                }
                if (!MMKV.defaultMMKV().getBoolean("firebase_event_a",false)){
                    logClickEvents("app_local")
                    MMKV.defaultMMKV().putBoolean("firebase_event_a",true)
                }
            }catch (e : Exception){
                e.printStackTrace()
            }

        }
         fun pageBEventsUpload(){
            try {
                if (!MMKV.defaultMMKV().getBoolean("page_event_b",false)){
                    HttpManager.get().upValues(
                        pageEvents(),
                        value_info,
                        object : SnapInterceptor.BasicRequestListener {

                            override fun loadFailed(message: String) {

                            }

                            override fun loadSuccessConfig(configInfo: String) {
                                LogUtils.e(configInfo)
//                                Log.e("install","install suc ----- $configInfo")

                                try {
                                    Gson().fromJson(
                                        configInfo,
                                        MobRequestData::class.java
                                    )?.run {
                                        if (ymaelo?.firstOrNull()?.rfqhildkpkks?.ufnjdfhe?.firstOrNull()?.yjolftqvviiz?.ycet == 200) {
                                            MMKV.defaultMMKV().putBoolean("page_event_b",true)
                                            LogUtils.e("BBBBpage_event_b suc -----")
                                        }
                                    }
                                }catch (e : Exception){
                                    e.printStackTrace()
                                }

                            }


                        })
                }
                if (!MMKV.defaultMMKV().getBoolean("firebase_event_b",false)){
                    logClickEvents("app_snap")
                    MMKV.defaultMMKV().putBoolean("firebase_event_b",true)
                }
            }catch (e : Exception){
                e.printStackTrace()
            }

        }


        fun lauchEventsUpload(){
            try {
                    HttpManager.get().upValues(
                        lauchEvents(),
                        value_info,
                        object : SnapInterceptor.BasicRequestListener {

                            override fun loadFailed(message: String) {

                            }

                            override fun loadSuccessConfig(configInfo: String) {
                            }


                        })

            }catch (e : Exception){
                e.printStackTrace()
            }

        }
        private fun stringHashMap(
            adsId: String,
            adsType: String,
            value: AdValue, fbIpValue: Double,pos: Long
        ): HashMap<String, String> {
            val stringStringHashMap: HashMap<String, String> = java.util.HashMap()
            try {
                stringStringHashMap["sisdkn"] = AppUtils.getAppName()
                stringStringHashMap["apwm"] = "Android"
                stringStringHashMap["gdd"] = Build.BRAND
                stringStringHashMap["okdo"] = Build.MODEL
                stringStringHashMap["zph"] = "10001"
                stringStringHashMap["yrcjg"] = ""+System.currentTimeMillis()
                if (!TextUtils.isEmpty(ActivityUtils.getTopActivity().localClassName )){
                    stringStringHashMap["eqr"] = ActivityUtils.getTopActivity().localClassName
                }
                if(!TextUtils.isEmpty(MMKV.defaultMMKV().getString(
                        MUSIC_CONSTANT_APP_REFER_INFO,"")+"")){
                    stringStringHashMap["rpu"] = MMKV.defaultMMKV().getString(
                        MUSIC_CONSTANT_APP_REFER_INFO,"")+""
                }
                stringStringHashMap["ebdf"] =  JsonUtils.formatJson(jsonInfo(
                    value,
                    adsId,
                    adsType,
                    fbIpValue,pos
                ))
                stringStringHashMap["cmpzhu"] = commonUniIds()
                stringStringHashMap["rntqxe"] = "" +   loadIds(
                    App.instance
                )
                stringStringHashMap["vrqp"] = Build.VERSION.RELEASE
                stringStringHashMap["pxwaw"] = AppUtils.getAppVersionName()
                if(!TextUtils.isEmpty(MMKV.defaultMMKV().getString("ad_id",""))){
                    stringStringHashMap["izzolj"] = MMKV.defaultMMKV().getString("ad_id","")+""
                }
                LogUtils.e(stringStringHashMap.toString())
                return stringStringHashMap
            }catch (e : Exception){
                e.printStackTrace()
            }
            return stringStringHashMap
        }


        private fun installEvents(

        ): HashMap<String, String> {
            val stringStringHashMap: HashMap<String, String> = java.util.HashMap()
            try {
                stringStringHashMap["sisdkn"] = AppUtils.getAppName()
                stringStringHashMap["apwm"] = "Android"
                stringStringHashMap["gdd"] = Build.BRAND
                stringStringHashMap["okdo"] = Build.MODEL
                stringStringHashMap["zph"] = "10003"
                stringStringHashMap["yrcjg"] = ""+System.currentTimeMillis()
                if (!TextUtils.isEmpty(ActivityUtils.getTopActivity().localClassName )){
                    stringStringHashMap["eqr"] = ActivityUtils.getTopActivity().localClassName
                }
                if(!TextUtils.isEmpty(MMKV.defaultMMKV().getString(
                        MUSIC_CONSTANT_APP_REFER_INFO,"")+"")){
                    stringStringHashMap["rpu"] = MMKV.defaultMMKV().getString(
                        MUSIC_CONSTANT_APP_REFER_INFO,"")+""
                }
                stringStringHashMap["cmpzhu"] = commonUniIds()
                stringStringHashMap["rntqxe"] = "" +   loadIds(
                    App.instance
                )
                stringStringHashMap["vrqp"] = Build.VERSION.RELEASE
                stringStringHashMap["pxwaw"] = AppUtils.getAppVersionName()
                if(!TextUtils.isEmpty(MMKV.defaultMMKV().getString("ad_id",""))){
                    stringStringHashMap["izzolj"] = MMKV.defaultMMKV().getString("ad_id","")+""
                }
                LogUtils.e(stringStringHashMap.toString())
            }catch (e : Exception){
                e.printStackTrace()
            }
            return stringStringHashMap
        }



        private fun pageEvents(

        ): HashMap<String, String> {
            val stringStringHashMap: HashMap<String, String> = java.util.HashMap()
            try {
                stringStringHashMap["sisdkn"] = AppUtils.getAppName()
                stringStringHashMap["apwm"] = "Android"
                stringStringHashMap["gdd"] = Build.BRAND
                stringStringHashMap["okdo"] = Build.MODEL
                stringStringHashMap["zph"] = "10009"
                stringStringHashMap["yrcjg"] = ""+System.currentTimeMillis()
                if (!TextUtils.isEmpty(ActivityUtils.getTopActivity().localClassName )){
                    stringStringHashMap["eqr"] = ActivityUtils.getTopActivity().localClassName
                }
                if(!TextUtils.isEmpty(MMKV.defaultMMKV().getString(
                        MUSIC_CONSTANT_APP_REFER_INFO,"")+"")){
                    stringStringHashMap["rpu"] = MMKV.defaultMMKV().getString(
                        MUSIC_CONSTANT_APP_REFER_INFO,"")+""
                }
                stringStringHashMap["cmpzhu"] = commonUniIds()
                stringStringHashMap["rntqxe"] = "" +   loadIds(
                    App.instance
                )
                stringStringHashMap["vrqp"] = Build.VERSION.RELEASE
                stringStringHashMap["pxwaw"] = AppUtils.getAppVersionName()
                if(!TextUtils.isEmpty(MMKV.defaultMMKV().getString("ad_id",""))){
                    stringStringHashMap["izzolj"] = MMKV.defaultMMKV().getString("ad_id","")+""
                }
                stringStringHashMap["ebdf"] =  pageArgs()

                stringStringHashMap["smxly"] =  System.currentTimeMillis().toString()+ loadIds(App.instance)
                LogUtils.e(stringStringHashMap.toString())
            }catch (e : Exception){
                e.printStackTrace()
            }
            return stringStringHashMap
        }


        private fun lauchEvents(

        ): HashMap<String, String> {
            val stringStringHashMap: HashMap<String, String> = java.util.HashMap()
            try {
                stringStringHashMap["sisdkn"] = AppUtils.getAppName()
                stringStringHashMap["apwm"] = "Android"
                stringStringHashMap["gdd"] = Build.BRAND
                stringStringHashMap["okdo"] = Build.MODEL
                stringStringHashMap["zph"] = "10005"
                stringStringHashMap["yrcjg"] = ""+System.currentTimeMillis()
                if (!TextUtils.isEmpty(ActivityUtils.getTopActivity().localClassName )){
                    stringStringHashMap["eqr"] = ActivityUtils.getTopActivity().localClassName
                }
                if(!TextUtils.isEmpty(MMKV.defaultMMKV().getString(
                        MUSIC_CONSTANT_APP_REFER_INFO,"")+"")){
                    stringStringHashMap["rpu"] = MMKV.defaultMMKV().getString(
                        MUSIC_CONSTANT_APP_REFER_INFO,"")+""
                }
                stringStringHashMap["cmpzhu"] = commonUniIds()
                stringStringHashMap["rntqxe"] = "" +   loadIds(
                    App.instance
                )
                stringStringHashMap["vrqp"] = Build.VERSION.RELEASE
                stringStringHashMap["pxwaw"] = AppUtils.getAppVersionName()
                if(!TextUtils.isEmpty(MMKV.defaultMMKV().getString("ad_id",""))){
                    stringStringHashMap["izzolj"] = MMKV.defaultMMKV().getString("ad_id","")+""
                }
                stringStringHashMap["ebdf"] =  lauArgs()

                stringStringHashMap["smxly"] =  System.currentTimeMillis().toString()+ loadIds(App.instance)
                LogUtils.e(stringStringHashMap.toString())
            }catch (e : Exception){
                e.printStackTrace()
            }
            return stringStringHashMap
        }


        fun musToMain(infoCtx: BasicView?) {
            try {
                if (infoCtx == null || infoCtx.isFinishing || infoCtx.isDestroyed){
                    return
                }
                if ((!ouUserIsCallBack && !GoogleMobileAdsConsentManager.getInstance(
                        App.instance
                    ).canRequestAds())
                ){
                    LogUtils.e("---- canRequestAds ---- error")
                    return
                }
                infoCtx.runOnUiThread {
                    if (MMKV.defaultMMKV().getBoolean(MUSIC_CONSTANT_IS_M_USER,false)){
                        infoCtx.startActivity(Intent(infoCtx, HomeActivity::class.java))
                    }else{
                        infoCtx.startActivity(Intent(infoCtx, LocalHomeActivity::class.java))
                    }
                    infoCtx.finish()
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }



        private fun cleanBasicInfo() {
            MMKV.mmkvWithID("mmkv_music_app").clear()
            MMKV.defaultMMKV()
                .putString(MUSIC_CONSTANT_APP_AD_DATA, "")
            MMKV.defaultMMKV().putLong(
                MUSIC_CONSTANT_APP_AD_CACHE_TM,
                0
            )
        }

        private fun getProductInfos() {
            if (System.currentTimeMillis() - MMKV.defaultMMKV()
                    .getLong(MUSIC_CONSTANT_APP_APP_CACHE_TM, 0) > 60 * 60 * 1000
            ) {
                HttpManager.get().requestConfigInfo(
                    HashMap(),
                    app_info,
                    object : SnapInterceptor.BasicRequestListener {

                        override fun loadFailed(message: String) {
                            LogUtils.e(message)

                        }

                        override fun loadSuccessConfig(configInfo: String) {
                            try {
                                LogUtils.e(configInfo)
                                Gson().fromJson(
                                    configInfo,
                                    AppConfigBean::class.java
                                )?.run {
                                    LogUtils.e("product info before","product ----- $configInfo")
                                    if (ymaelo?.firstOrNull()?.rfqhildkpkks?.ufnjdfhe?.firstOrNull()?.yjolftqvviiz?.mzq  == 200
                                        ) {
                                        if (ymaelo?.firstOrNull()?.rfqhildkpkks?.ufnjdfhe?.firstOrNull()?.yjolftqvviiz?.xczkh != null) {
                                            ymaelo?.firstOrNull()?.rfqhildkpkks?.ufnjdfhe?.firstOrNull()?.yjolftqvviiz?.xczkh?.gqf?.let {
                                                MMKV.defaultMMKV()
                                                    .putInt(MUSIC_CONSTANT_APP_REFRESH, it)
                                            }

                                            MMKV.defaultMMKV().putLong(
                                                MUSIC_CONSTANT_APP_APP_CACHE_TM,
                                                System.currentTimeMillis()
                                            )
                                            ymaelo?.firstOrNull()?.rfqhildkpkks?.ufnjdfhe?.firstOrNull()?.yjolftqvviiz?.xczkh?.ypqi?.apply {
                                                if (isNullOrEmpty()){
                                                    return@apply
                                                }
                                                for (i in this){

                                                    if ("standard_line" == i.fctaig){
                                                        MMKV.defaultMMKV()
                                                            .putString(MMKV_LINES, i.dbpfqp)
                                                    }
                                                    if ("upload_coef" == i.fctaig){
                                                        MMKV.defaultMMKV()
                                                            .putString(MMKV_COEFS, i.dbpfqp)
                                                    }
                                                    if ("window_period_time" == i.fctaig){
                                                        MMKV.defaultMMKV()
                                                            .putString(MMKV_SYS, i.dbpfqp)
                                                    }
                                                    if ("lauch_time" == i.fctaig){
                                                        MMKV.defaultMMKV()
                                                            .putInt(MMKV_LAUCH_TIME, i.dbpfqp.toInt())
                                                    }
//                                                    if ("back_btn_no_finish" == i.fctaig){
//                                                        MMKV.defaultMMKV()
//                                                            .putBoolean(MMKV_HOME_BACK_NO_FINISH, i.dbpfqp == "1")
//                                                    }
//                                                    if ("home_btn_no_finish" == i.fctaig){
//                                                        MMKV.defaultMMKV()
//                                                            .putBoolean(MMKV_HOME_BTN_NO_FINISH, i.dbpfqp == "1")
//                                                    }
                                                    LogUtils.e("-----line", ""+ MMKV.defaultMMKV()
                                                        .getString(MMKV_LINES, ""))
                                                    LogUtils.e("-----coef", ""+ MMKV.defaultMMKV()
                                                        .getString(MMKV_COEFS, ""))
                                                    LogUtils.e("-----_time", ""+ MMKV.defaultMMKV()
                                                        .getString(MMKV_SYS, ""))
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                MMKV.defaultMMKV().putLong(MUSIC_CONSTANT_APP_APP_CACHE_TM, 0)
                                e.printStackTrace()
                            }

                        }
                    })
            }
            installEventsUpload()
        }


    }

}