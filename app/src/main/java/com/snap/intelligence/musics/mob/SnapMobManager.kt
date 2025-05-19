package com.snap.intelligence.musics.mob
import android.app.Activity
import android.content.Intent
import android.text.TextUtils
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.gson.Gson
import com.snap.intelligence.musics.App
import com.snap.intelligence.musics.App.Companion.isClicked
import com.snap.intelligence.musics.App.Companion.ouUserIsCallBack
import com.snap.intelligence.musics.BasicView
import com.snap.intelligence.musics.beans.MobRequestData
import com.snap.intelligence.musics.beans.CancelEvent
import com.snap.intelligence.musics.beans.InitBanner
import com.snap.intelligence.musics.constants.SnapPosManager
import com.snap.intelligence.musics.constants.SnapPosManager.cyIsShowingFLag
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_APP_AD_CACHE_TM
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_APP_AD_DATA
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_IS_M_USER
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_JK_CLICK_CT
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_JK_CLICK_UNIT
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_JK_SHOW_CT
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_JK_SHOW_ST
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_JK_SHOW_UNIT
import com.snap.intelligence.musics.constants.NativeWeavesListener
import com.snap.intelligence.musics.constants.ShowListener
import com.snap.intelligence.musics.constants.StartUpListener
import com.snap.intelligence.musics.constants.TemTranslateIntersLis
import com.snap.intelligence.musics.constants.albumDetBanner
import com.snap.intelligence.musics.constants.albumDetInter
import com.snap.intelligence.musics.constants.artistDetBanner
import com.snap.intelligence.musics.constants.artistDetInter
import com.snap.intelligence.musics.constants.collectBanner
import com.snap.intelligence.musics.constants.finishPageInterFor
import com.snap.intelligence.musics.constants.finishPageInterOne
import com.snap.intelligence.musics.constants.finishPageInterThr
import com.snap.intelligence.musics.constants.finishPageInterTwo
import com.snap.intelligence.musics.constants.homeBanner
import com.snap.intelligence.musics.constants.homeReleaseDetailBanner
import com.snap.intelligence.musics.constants.musColdStart
import com.snap.intelligence.musics.constants.musHotStart
import com.snap.intelligence.musics.constants.playViewBanner
import com.snap.intelligence.musics.constants.playViewsInter
import com.snap.intelligence.musics.constants.queueListBanner
import com.snap.intelligence.musics.constants.searchInter
import com.snap.intelligence.musics.constants.searchResultBanner
import com.snap.intelligence.musics.db.CommonTools.valueUpload
import com.snap.intelligence.musics.local.LocalHomeActivity
import com.snap.intelligence.musics.ui.start.HomeActivity
import com.snap.intelligence.musics.ui.start.HomeActivity.Companion.playerConnection
import com.tencent.mmkv.MMKV
import org.greenrobot.eventbus.EventBus

class SnapMobManager {
    private fun nativeWeavesIsLoading(status: Boolean): Boolean {
        if (status){
            if (nativeWeavesStatus){
                LogUtils.e("result ad is loading")
                return true
            }
        }else{
            if (musWeavesNaIsLoading){
                LogUtils.e( "banner ad is loading")
                return true
            }
        }
        return false
    }


    private fun notifyNative(vos: Boolean, status1 : Boolean) {
        if (vos &&  nativeWeavesStatus){
            nativeWeavesStatus = status1
        }else if (!vos &&  musWeavesNaIsLoading){
            musWeavesNaIsLoading = status1
        }
    }


    private var dataBean: MutableList<MobRequestData.YmaeloBean.RfqhildkpkksBean.UfnjdfheBean.YjolftqvviizBean.WnsouBean>? =
        null
    var currentPos: Long = -1

    private var tempStatus: Boolean = false



    private fun checkNativeP(adPos: Long): Boolean {
        return false
    }


    private fun isUmeng() : Boolean{
        return  App.ouUserIsCallBack || GoogleMobileAdsConsentManager.getInstance(
            App.instance
        ).canRequestAds()

    }

    fun createInter(
        interPos: Long,
        tperklyTranslateIntersLis: TemTranslateIntersLis? = null
    ) {
        try {
            var adId = ""
            // 缓存拦截 插页成功回调
            if (interPoolsIsLoading()) {
                LogUtils.e(
                    "inter is loading return=====" + SnapPosManager.basicInfoLog(
                        interPos
                    ) + ""
                )
                return
            }
            if (checkInterExists()) {
                LogUtils.e(
                    "inter cached has ads return=====" + SnapPosManager.basicInfoLog(
                        interPos
                    ) + ""
                )
                tperklyTranslateIntersLis?.interIsWeavesSuc(true)
                return
            }
            interStatus = true
            interWeavesId?.run { adId = this }
            if (TextUtils.isEmpty(adId)) {
                LogUtils.e("inter id empty=====" + SnapPosManager.basicInfoLog(interPos) + "")
                notifyCySts(false)
                return
            }

            val adRequest = AdRequest.Builder().build()
            val context = App.instance ?: return
            InterstitialAd.load(context, adId, adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        LogUtils.e(
                            "inter ad load suc =====" + SnapPosManager.basicInfoLog(
                                interPos
                            ) + ""
                        )

                        uniqueInter = interstitialAd
                        interWeavesTimes = System.currentTimeMillis()
                        notifyCySts(false)
                        tperklyTranslateIntersLis?.interIsWeavesSuc(true)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        // Handle the error
                        LogUtils.e(
                            "inter ad load error =====" + SnapPosManager.basicInfoLog(
                                interPos
                            ) + ""
                        )
                        notifyCySts(false)
                        tperklyTranslateIntersLis?.interIsWeavesSuc(false)
                    }
                })
        } catch (e: Exception) {
            notifyCySts(false)
            e.printStackTrace()
        }
    }





    fun showWeaveNatives(nativePos: Long, context: Activity): NativeAd? {
        val nativeAd: NativeAd?
        if (context.isDestroyed || context.isFinishing) return null
        if (SnapPosManager.posIsCanShow(nativePos)){
            LogUtils.e("native  limit ------" + SnapPosManager.basicInfoLog(nativePos))
            return null
        }
        if (cyIsShowingFLag){
            return null
        }
        currentPos = nativePos
        if (intellNatives != null){
            if (checkMusWeavesNaTimes(nativeWeavesTimes)){
                nativeWeavesTimes  = 0
                LogUtils.e( "curr pos${SnapPosManager.basicInfoLog(nativePos)}more than sixty mins ,result cache pools")
                intellNatives = null
                return null
            }
            tempStatus = true
            nativeAd = intellNatives
            intellNatives = null
            return nativeAd
        }else if(intellBanners != null){
            if (checkMusWeavesNaTimes(banWeavesTimes)){
                banWeavesTimes  = 0
                LogUtils.e( "curr pos${SnapPosManager.basicInfoLog(nativePos)}more than sixty mins,banner cache pools")
                intellBanners = null
                return null
            }
            tempStatus = false
            nativeAd = intellBanners
            intellBanners = null
            return nativeAd
        }
        return null
    }

    private var nativeWeavesTimes: Long = 0

    private var nativeWeavesStatus: Boolean = false

    private var banWeavesIds: String? = null

    private var interWeavesId: String? = null


    private fun musWeavesTimesToLoadConfig() {
        musWeavesLoadNatives()
//        if (CommonStudyUtils.interIsCanLoad()) {
//            createInter(trackListInter())
//        }
    }
    private fun notifyCySts(sts: Boolean) {
        interStatus = sts
    }

    private var interStatus: Boolean = false

    var uniqueInter: InterstitialAd? = null

    private fun snapConfigicNatCheckExists(transSt: Boolean): Boolean {
        if (transSt && intellNatives != null && !checkMusWeavesNaTimes(nativeWeavesTimes)){
            LogUtils.e( "result cache pools has cache,stop cac")
            return true
        }else if(!transSt && intellBanners != null && !checkMusWeavesNaTimes(banWeavesTimes)){
            LogUtils.e( "banner cache pools has cache,stop cac")
            return true
        }
        return false
    }



    private var musKpIds: String? = null

    private var intellBanners: NativeAd? = null

    private var intellNatives: NativeAd? = null

    private var musWeavesNaIsLoading: Boolean = false





    fun initStartAds(baseTrans: BasicView?) {
        if (SnapPosManager.intellIsCanLoadStart()) { //kp can load
            KepingValueTools.intellTools.configStartIds(musKpIds)
            ThreadUtils.runOnUiThread {
                if (KepingValueTools.intellTools.intellObj == null) { // null -> load
                    KepingValueTools.intellTools.loadStartAds(baseTrans, object :
                        StartUpListener {
                        override fun loadSuc() {
//                            initLoadInter(baseTrans)
                            if (baseTrans == null || baseTrans.isDestroyed || baseTrans.isFinishing) {
                                return
                            }
                            showStartWeaves(baseTrans)
                            //to load inter
                        }

                        override fun LoadedError() {
//                            initLoadInter(baseTrans)
                            //to load inter

                        }
                    })
                } else {
//                    initLoadInter(baseTrans)
                    // has ad -> show
                    showStartWeaves(baseTrans)
                    //to load inter
                }
            }
        }
    }

    private var banWeavesTimes: Long = 0

    private var interWeavesTimes: Long = 0





    companion object {
        val homeStudy: SnapMobManager by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            SnapMobManager()
        }
    }


    fun musWeavesLoadNatives() {
        if (!SnapPosManager.posIsCanShow(homeBanner())) {
            initLoadBanners(
                homeBanner(),
                object : NativeWeavesListener {
                    override fun nativeIsWeavesSuc(sucFlag: Boolean) {
                        if (sucFlag) {
                            EventBus.getDefault().postSticky(
                                InitBanner(
                                    true
                                )
                            )
                        }
                    }

                })
        }
    }

    private var musNativesId: String? = null

    private fun checkMusWeavesNaTimes(nativeHt : Long): Boolean {
        if (System.currentTimeMillis() - nativeHt   > 60 * 60 * 1000) {
            return true
        }
        return false
    }

    private fun checkInterExists(): Boolean {
        //
        if (uniqueInter != null && !SnapPosManager.checkTimes(interWeavesTimes)) {
            return true
        }
        return false
    }


    private fun loadInterWeaves(
        interstitialAd: InterstitialAd?,
        pos: Long,
    ) {
        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            var currentClicked = false
            override fun onAdClicked() {
                try {
                    if (!currentClicked) {
                        currentClicked = true
                        SnapPosManager.appendClick(pos)
                    }
                    isClicked = true
//                    if (playerConnection?.isPlaying?.value == true){
//                        ThreadUtils.runOnUiThreadDelayed({ ActivityUtils.finishAllActivities()},500)
//                    }
                }catch (e : Exception){
                    e.printStackTrace()
                }

            }

            override fun onAdDismissedFullScreenContent() {
                cyIsShowingFLag = false
            }

            override fun onAdFailedToShowFullScreenContent(fa: AdError) {
                // Called when ad fails to show.
                cyIsShowingFLag = false

            }

            override fun onAdImpression() {
                // Called when an impression is recorded for an ad.
                cyIsShowingFLag = true
            }

            override fun onAdShowedFullScreenContent() {
                // Called when ad is shown.
                cyIsShowingFLag = true
                SnapPosManager.showCountAdd(pos)
//                ThreadUtils.runOnUiThreadDelayed({
//                    LogUtils.e(CommonStudyUtils.basicInfoLog(pos) + "load a new cy ad")
//                    createInter(pos)
//                }, 1500)
            }
        }
        interstitialAd?.setOnPaidEventListener {
            valueUpload(it,"Interstitial", interstitialAd.adUnitId,pos)
        }

    }







    fun initMusConfig(
        activity: BasicView?, load: Boolean = false
    ) {
        try {
            if (TextUtils.isEmpty(
                    MMKV.defaultMMKV().getString(MUSIC_CONSTANT_APP_AD_DATA, "")
                )
            ) {
                MMKV.defaultMMKV().putLong(MUSIC_CONSTANT_APP_AD_CACHE_TM, 0)
                return
            }

            val mobRequestData = Gson().fromJson(
                MMKV.defaultMMKV().getString(MUSIC_CONSTANT_APP_AD_DATA, ""),
                MobRequestData::class.java
            ) ?: return

            if (mobRequestData?.ymaelo?.firstOrNull()?.rfqhildkpkks?.ufnjdfhe?.firstOrNull()?.yjolftqvviiz?.wnsou != null) {
                dataBean = mobRequestData?.ymaelo?.firstOrNull()?.rfqhildkpkks?.ufnjdfhe?.firstOrNull()?.yjolftqvviiz?.wnsou
            }
            if (dataBean.isNullOrEmpty()) {
                return
            }
           dataBean?.forEach {
               mmkvInit(it)
               when (it.bzoz) {
                   musColdStart(), musHotStart()
                   -> {
                       if (!TextUtils.isEmpty(it.ruup)) {
                           musKpIds = it.ruup
                       }
                   }

                   homeBanner(), searchResultBanner(), albumDetBanner(),
                   queueListBanner(),  artistDetBanner(), playViewBanner(), collectBanner(), homeReleaseDetailBanner(),

                   -> {
                       if (!TextUtils.isEmpty(it.ruup)) {
                            banWeavesIds = it.ruup

                       }
                   }

                   searchInter(),  albumDetInter(),artistDetInter(),
                    playViewsInter(), finishPageInterOne(), finishPageInterTwo(), finishPageInterThr(), finishPageInterFor()
                   -> {
                       if (!TextUtils.isEmpty(it.ruup)) {

                            interWeavesId = it.ruup

                       }

                   }
               }
           }

            if (!load && (ouUserIsCallBack || GoogleMobileAdsConsentManager.getInstance(
                    App.instance
                ).canRequestAds())) {
                LogUtils.e("load um  true ----  ")
                initStartAds(activity)
            }

            if (isHomePage(load)) return
            if (!isUmeng()){
                LogUtils.e("skip  ----  ")
                return
            }
            if (!SnapPosManager.intellIsCanLoadStart()){
//                initLoadInter(activity) // can't load kp to load inter
            }
            musWeavesLoadNatives()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun initLoadBanners(
        showAdPosition: Long,
        resultAdLoadBack: NativeWeavesListener? = null
    ) {
        val snapConfigicNatPools = checkNativeP(showAdPosition)
        try {
            var postion = showAdPosition
            var unitVoxyId = ""
            if (nativeWeavesIsLoading(snapConfigicNatPools) ){
                return
            }
            if (snapConfigicNatCheckExists(snapConfigicNatPools)){
                resultAdLoadBack?.nativeIsWeavesSuc(true)
                return
            }
            if (snapConfigicNatPools){
                nativeWeavesStatus = true
                musNativesId?.let { unitVoxyId = it }
            }else{
                musWeavesNaIsLoading = true
                banWeavesIds?.let { unitVoxyId = it }
            }
            if (TextUtils.isEmpty(unitVoxyId)){
                notifyNative(snapConfigicNatPools, false)
                LogUtils.e("native id error------"+ SnapPosManager.basicInfoLog(postion))
                return
            }
            val context = App.instance ?: return
            val loader = AdLoader.Builder(context, unitVoxyId)
                .forNativeAd { nativeAd ->
                    nativeAd.setOnPaidEventListener {
                        valueUpload(it,"Native",unitVoxyId,postion)
                    }
                    if (snapConfigicNatPools) {
                        LogUtils.e( "result ad load suc${unitVoxyId}curr pos is ${SnapPosManager.basicInfoLog(postion)}")
                        intellNatives = nativeAd
                        nativeWeavesTimes = System.currentTimeMillis()
                    } else {
                        LogUtils.e( "ban ad load suc${unitVoxyId}curr pos is ${SnapPosManager.basicInfoLog(postion)}")
                        intellBanners = nativeAd
                        banWeavesTimes = System.currentTimeMillis()
                    }
                    notifyNative(snapConfigicNatPools, false)
                    resultAdLoadBack?.nativeIsWeavesSuc(true)
                }
                .withAdListener(object : AdListener() {
                    var clicked = false
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        LogUtils.e(
                            "native  onAdFailedToLoad ------" + SnapPosManager.basicInfoLog(
                                postion
                            )
                        )
                        notifyNative(snapConfigicNatPools,false)
                        resultAdLoadBack?.nativeIsWeavesSuc(false)
                    }

                    override fun onAdClicked() {
                        //前一条广告失败时position处理
                        try {

                            if (currentPos.toInt() != -1) {
                                postion = currentPos
                            }
                            if (!clicked) {
                                SnapPosManager.appendClick(postion)
                                clicked = true
                            }
                            isClicked = true
//                            if (playerConnection?.isPlaying?.value == true){
//                                ThreadUtils.runOnUiThreadDelayed({ ActivityUtils.finishAllActivities()},500)
//                            }
                        }catch (e : Exception){
                            e.printStackTrace()
                        }

                    }

                    override fun onAdClosed() {
                    }

                    override fun onAdImpression() {
                        try {
                            if (currentPos.toInt() != -1) {
                                postion = currentPos
                            }
                            SnapPosManager.showCountAdd(postion)
                            ThreadUtils.runOnUiThreadDelayed({
                                LogUtils.e(
                                    "native  load a new ad ------" + SnapPosManager.basicInfoLog(
                                        postion
                                    )
                                )
                                initLoadBanners(postion)
                            }, 1500)
                        }catch (e : Exception){
                            e.printStackTrace()
                        }

                    }

                    override fun onAdOpened() {
                    }
                })
                .build()

            loader.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            notifyNative(snapConfigicNatPools,false)
            e.printStackTrace()
        }
    }



    private fun showStartWeaves(activity: BasicView?) {
        KepingValueTools.intellTools.showStartAds(activity,
            object : ShowListener {
                override fun dispathSuc() {
                    if (activity == null || activity.isFinishing || activity.isDestroyed) {
                        LogUtils.e("return --------")
                        return
                    }
                    if (MMKV.defaultMMKV().getBoolean(MUSIC_CONSTANT_IS_M_USER,false)){
                        activity.startActivity(Intent(activity, HomeActivity::class.java))

                    }else{
                        activity.startActivity(Intent(activity, LocalHomeActivity::class.java))
                    }
                    activity.finish()
                }

                override fun dispathError() {
                    if (activity == null || activity.isFinishing || activity.isDestroyed) {
                        return
                    }
                    EventBus.getDefault().post(
                        CancelEvent(
                            true
                        )
                    )
                }
            })
    }

    private fun isHomePage(
        jump: Boolean
    ): Boolean {
        if (jump) {
            ThreadUtils.runOnUiThread {
                musWeavesTimesToLoadConfig()
            }
            return true
        }
        return false
    }





    fun getShowCyAds(recAds: Long, context: Activity) {
        if (SnapPosManager.posIsCanShow(recAds)) {
            LogUtils.e("inter flag limit ------" + SnapPosManager.basicInfoLog(recAds))
            return
        }
        if (uniqueInter != null) {
            if (SnapPosManager.checkTimes(interWeavesTimes)) {
                LogUtils.e("inter flag limit ------" + SnapPosManager.basicInfoLog(recAds))
                uniqueInter = null
                interWeavesTimes = 0
                return
            }
            cyIsShowingFLag = true
            uniqueInter?.show(context)
            LogUtils.e("inter showing current pos ===== " + SnapPosManager.basicInfoLog(recAds))
            loadInterWeaves(uniqueInter, recAds)
            uniqueInter = null
        } else {
            LogUtils.e("inter cache no to load new  ===== " + SnapPosManager.basicInfoLog(recAds))
            createInter(recAds, object : TemTranslateIntersLis {
                override fun interIsWeavesSuc(inSucFlag: Boolean) {
                    if (inSucFlag) {
                        LogUtils.e(
                            "inter cache no to load new suc  ===== " + SnapPosManager.basicInfoLog(
                                recAds
                            )
                        )
                    } else {
                        LogUtils.e(
                            "inter cache no to load new  error  ===== " + SnapPosManager.basicInfoLog(
                                recAds
                            )
                        )
                    }
                }

            })
        }

    }



    fun showCachedInter(context: Activity) {
        if (uniqueInter != null) {
            uniqueInter?.show(context)
            LogUtils.e("inter showing cache current pos ===== " )
            uniqueInter = null
        }
    }

     fun backLimit(): Boolean {
        if (SnapPosManager.posIsCanShow(finishPageInterOne()) && SnapPosManager.posIsCanShow(
                finishPageInterTwo()
            ) && SnapPosManager.posIsCanShow(finishPageInterThr()) && SnapPosManager.posIsCanShow(
                finishPageInterFor()
            )
        ) {
            LogUtils.e("inter cache flag limit ------")
            return true
        }
        return false
    }


    private fun interPoolsIsLoading(): Boolean {
        if (interStatus) {
            return true
        }
        return false
    }


    private fun mmkvInit(wnsouBean: MobRequestData.YmaeloBean.RfqhildkpkksBean.UfnjdfheBean.YjolftqvviizBean.WnsouBean) {
        MMKV.mmkvWithID("mmkv_music_app").putBoolean("$MUSIC_CONSTANT_JK_SHOW_ST-${wnsouBean.bzoz}",
            "0" == wnsouBean.ghcuxz
        )
        MMKV.mmkvWithID("mmkv_music_app").putInt("$MUSIC_CONSTANT_JK_CLICK_CT-${wnsouBean.bzoz}", wnsouBean.ccquxo)
        MMKV.mmkvWithID("mmkv_music_app").putString("$MUSIC_CONSTANT_JK_CLICK_UNIT-${wnsouBean.bzoz}", wnsouBean.grgkw)


        MMKV.mmkvWithID("mmkv_music_app").putInt("$MUSIC_CONSTANT_JK_SHOW_CT-${wnsouBean.bzoz}", wnsouBean.rrz)
        MMKV.mmkvWithID("mmkv_music_app").putString("$MUSIC_CONSTANT_JK_SHOW_UNIT-${wnsouBean.bzoz}", wnsouBean.cfd)
    }





}