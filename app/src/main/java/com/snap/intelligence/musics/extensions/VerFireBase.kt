package com.snap.intelligence.musics.extensions

import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.snap.intelligence.musics.App

fun logClickEvents(eventName: String){
    try {
//        Log.e("-events-","$eventName")
        App?.instance?.let {
            FirebaseAnalytics.getInstance(it).logEvent(eventName,null)
        }
    }catch (e : Exception){
        e.printStackTrace()
    }

}