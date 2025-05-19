package com.snap.intelligence.musics.constants

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import com.snap.intelligence.musics.App
import com.tencent.mmkv.MMKV
import java.util.Calendar

object SnapPosManager  {


    fun showCountAdd(postion: Long) {
        mmkvClickRules()?.run {
            var showCount = getInt("$MUSIC_CONSTANT_BD_SHOW_CT-${postion}", 0)
            showCount += 1
            LogUtils.e(basicInfoLog(postion)+"show  count ====="+showCount)
            putInt("$MUSIC_CONSTANT_BD_SHOW_CT-${postion}", showCount)
        }
    }

    fun appendClick(pos: Long) {
        mmkvClickRules()?.run {
            var count = getInt("$MUSIC_CONSTANT_BD_CLICK_CT-${pos}", 0)
            count += 1
            LogUtils.e(basicInfoLog(pos)+" click count ====="+count)
            putInt("$MUSIC_CONSTANT_BD_CLICK_CT-${pos}", count)
        }
    }


    fun checkTimes(cacheTime : Long): Boolean {
        if (System.currentTimeMillis() - cacheTime   > 60 * 60 * 1000) {
            return true
        }
        return false
    }

    fun getInstallSys(): Long {
        try {
            val context = App.instance ?: return 0
            val packageManager = context.packageManager
            val packageInfo: PackageInfo =
                packageManager.getPackageInfo(AppUtils.getAppPackageName(), 0)
            //应用装时间
            return packageInfo.firstInstallTime
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }


    fun posIsCanShow(artPos: Long): Boolean {
        val snap_valuelateAnyClickCount =   MMKV.mmkvWithID("mmkv_music_app").getInt("$MUSIC_CONSTANT_JK_CLICK_CT-${artPos}",0)
        val snap_valuelateAnyShowCount = MMKV.mmkvWithID("mmkv_music_app").getInt("$MUSIC_CONSTANT_JK_SHOW_CT-${artPos}",0)
        val snap_valuelateAnyShow = MMKV.mmkvWithID("mmkv_music_app").getBoolean("$MUSIC_CONSTANT_JK_SHOW_ST-${artPos}",false)
        LogUtils.e(basicInfoLog(artPos)+"配置开关"+snap_valuelateAnyShow)
        if (!snap_valuelateAnyShow) {
            return true
        }
        mmkvClickRules()?.run {
            val nativeTranslateAnyClick =  getInt("$MUSIC_CONSTANT_BD_CLICK_CT-${artPos}",0)
            LogUtils.e(basicInfoLog(artPos)+"本地累积点击次数"+nativeTranslateAnyClick)
            if (nativeTranslateAnyClick >= snap_valuelateAnyClickCount) {
                return true
            }
        }
        mmkvClickRules()?.run {
            val nativeTranslateAnyShow = getInt("$MUSIC_CONSTANT_BD_SHOW_CT-${artPos}",0)
            LogUtils.e(basicInfoLog(artPos)+"本地累积展示次数"+nativeTranslateAnyShow)
            if (nativeTranslateAnyShow >= snap_valuelateAnyShowCount) {
                return true
            }
        }
        return false
    }

    private fun mmkvClickRules(): MMKV? {
        val mmkv = MMKV.mmkvWithID("snap_mmkv")
                val currentD = Calendar.getInstance().get(Calendar.DATE)
                val lastD = mmkv.getInt("sp_day", 0)
                if (currentD != lastD){
                    mmkv.clear()
                }
                mmkv.putInt("sp_day", currentD)

        return mmkv
    }

    @JvmStatic
    var cyIsShowingFLag = false
    @JvmStatic
    var lauchFlag = false

    fun basicInfoLog(position: Long): String {
        var name = ""
        when (position) {
            homeBanner() -> {name = "homeBanner snap_value pos :"}
            searchResultBanner() -> {name ="searchResultBanner snap_value pos :"}
            albumDetBanner() -> {name ="albumDetBanner snap_value pos :"}
            artistDetBanner() -> {name ="artistDetBanner snap_value pos :"}
            playViewBanner() -> {name ="playViewBanner snap_value pos :"}
            queueListBanner() -> {name ="queueListBanner snap_value pos :"}
            searchInter() -> {name ="searchInter snap_value pos :"}
            albumDetInter() -> {name ="albumDetInter snap_value pos :"}
            artistDetInter() -> {name ="artistDetInter snap_value pos :"}
            playViewsInter() -> {name ="playViewsInter snap_value pos :"}
            queueInter() -> {name ="queueInter snap_value pos :"}
            finishPageInterOne() -> {name ="finishPageInterOne snap_value pos :"}
            finishPageInterTwo() -> {name ="finishPageInterTwo snap_value pos :"}
            musColdStart() -> {name ="snap_valueKepCold snap_value pos :"}
            musHotStart() -> {name ="snap_valueKepHot snap_value pos :"}
        }
        return name
    }

    fun intellIsCanLoadStart(): Boolean {
        return !posIsCanShow(musColdStart())
                || !posIsCanShow(musHotStart())
    }



}