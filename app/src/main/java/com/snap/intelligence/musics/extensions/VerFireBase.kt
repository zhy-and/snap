package com.snap.intelligence.musics.extensions

import android.util.Log
import com.blankj.utilcode.util.LogUtils
import com.google.firebase.analytics.FirebaseAnalytics
import com.snap.intelligence.musics.App

fun logClickEvents(eventName: String){
    try {
        App?.instance?.let {
//            LogUtils.e(eventName)
            FirebaseAnalytics.getInstance(it).logEvent(eventName,null)
        }
    }catch (e : Exception){
        e.printStackTrace()
    }

}