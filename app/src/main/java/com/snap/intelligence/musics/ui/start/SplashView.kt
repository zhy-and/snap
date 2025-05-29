package com.snap.intelligence.musics.ui.start

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.snap.intelligence.musics.App
import com.snap.intelligence.musics.App.Companion.ouUserIsCallBack
import com.snap.intelligence.musics.BasicView
import com.snap.intelligence.musics.beans.CancelEvent
import com.snap.intelligence.musics.constants.MMKV_LAUCH_TIME
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_IS_M_USER
import com.snap.intelligence.musics.constants.SnapPosManager
import com.snap.intelligence.musics.constants.checkOUsers
import com.snap.intelligence.musics.databinding.ViewSplashBinding
import com.snap.intelligence.musics.local.LocalHomeActivity
import com.snap.intelligence.musics.mob.GoogleMobileAdsConsentManager
import com.snap.intelligence.musics.mob.SnapMobManager
import com.snap.intelligence.musics.work.SnapBasicInterfaces.Companion.lauchEventsUpload
import com.snap.intelligence.musics.work.SnapBasicInterfaces.Companion.loadBaseInfos
import com.tencent.mmkv.MMKV
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.atomic.AtomicBoolean


class SplashView : BasicView() {
    private lateinit var binding: ViewSplashBinding
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       binding = ViewSplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            if (!EventBus.getDefault().isRegistered(this)) {
                EventBus.getDefault().register(this)
            }
            if (MMKV.defaultMMKV().getLong("first_open",0L) == 0L){
                MMKV.defaultMMKV().putLong("first_open",System.currentTimeMillis())
            }
            if (!checkOUsers()){
                ouUserIsCallBack = true
            }

            startInit()
            if (checkOUsers()){
                createView()
            }else{
                toHomePage()
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    private val adsInitializeCalled = AtomicBoolean(false)

    private fun createView() {

        val googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(
            App.instance
        )
        googleMobileAdsConsentManager?.gatherConsent(
            this
        ) { consentError ->
            if (consentError != null) {
                // Consent not obtained in current session.
                LogUtils.e(
                    java.lang.String.format(
                        "%s: %s",
                        consentError.getErrorCode(),
                        consentError.getMessage()
                    )
                )
            }
            ouUserIsCallBack = true
            LogUtils.e("oumeng load  flag -- "+googleMobileAdsConsentManager?.canRequestAds())
            initializeMobileAdsSdk()
        }
        if (googleMobileAdsConsentManager?.canRequestAds() == true) {
            LogUtils.e("oumeng err 222 ---- ")

            initializeMobileAdsSdk()
        }else{
            LogUtils.e("oumeng err  ---- ")
        }
    }

    private fun initializeMobileAdsSdk() {
        if (adsInitializeCalled.getAndSet(true)) {
//            LogUtils.e("UM TAG ", "getAndSet")
            return
        }
        if (messManager == null){
            LogUtils.e("jumped   err---- ")
            return
        }


        // Initialize the Mobile Ads SDK.
         SnapMobManager.homeStudy.initStartAds(this)
        ThreadUtils.runOnUiThread {
            if (!SnapPosManager.intellIsCanLoadStart()){
//                MainHomeStudy.homeStudy.initLoadInter(this) // can't load kp to load inter
            }
            SnapMobManager.homeStudy.musWeavesLoadNatives()
        }
        toHomePage()
    }


    private var messManager : Handler?= null


    private fun toHomePage() {
       val lauchTime =  MMKV.defaultMMKV().getInt(MMKV_LAUCH_TIME,15)
        messManager?.postDelayed({
            if (MMKV.defaultMMKV().getBoolean(MUSIC_CONSTANT_IS_M_USER,false)){
                startActivity(Intent(this, HomeActivity::class.java))
            }else{
                startActivity(Intent(this, LocalHomeActivity::class.java))
            }
            finish()
        }, lauchTime * 1000L)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        messManager?.removeCallbacksAndMessages(null)
        binding?.anims?.cancelAnimation()
    }
    private fun startInit() {
        try {
            messManager =  Handler(Looper.getMainLooper())

            loadBaseInfos(this,false)
            lauchEventsUpload()
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    private fun destroyHd() {
        try {
            messManager?.removeCallbacksAndMessages(null)
            messManager = null
        }catch (e : Exception){
            e.printStackTrace()
        }

    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun remove(re: CancelEvent?) {
        LogUtils.e(" EventAdvenseRemove------")
        if (re?.isShow == true) {
            destroyHd()
        }
    }

    override fun onPause() {
        super.onPause()
    }

}