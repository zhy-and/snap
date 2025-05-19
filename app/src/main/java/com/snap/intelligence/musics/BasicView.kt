package com.snap.intelligence.musics

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.bumptech.glide.Glide
import com.snap.intelligence.musics.constants.playViewsInter
import com.snap.intelligence.musics.constants.queueInter
import com.snap.intelligence.musics.constants.searchInter
import com.gyf.immersionbar.ImmersionBar
import com.snap.intelligence.musics.constants.SnapPosManager
import com.snap.intelligence.musics.constants.finishPageInterFor
import com.snap.intelligence.musics.constants.finishPageInterOne
import com.snap.intelligence.musics.constants.finishPageInterThr
import com.snap.intelligence.musics.constants.finishPageInterTwo
import com.snap.intelligence.musics.local.posBitmap
import com.snap.intelligence.musics.mob.SnapMobManager
import com.snap.intelligence.musics.models.MediaMetadata
import com.snap.intelligence.musics.ui.music.MainPlayer
import com.snap.intelligence.musics.ui.queue.QueueActivity
import com.snap.intelligence.musics.ui.search.MusicSearchHistoryActivity
import com.snap.intelligence.musics.ui.start.HomeActivity.Companion.showed
import options

open class BasicView : AppCompatActivity() {
    fun startInterActivity(intent: Intent, pos : Long,isFinish : Boolean = true){
        showed = true
        if (!SnapPosManager.posIsCanShow(pos) && SnapMobManager.homeStudy.uniqueInter == null){

            SnapMobManager.homeStudy.createInter(pos)
            showLoadingBar()
            ThreadUtils.runOnUiThreadDelayed({
                cancelLoadingBar()
                startActivity(intent)
                if (isFinish){
                    finish()
                }
            },3000)
        }else{
            startActivity(intent)
            if (isFinish){
                finish()
            }
        }
    }
    @JvmField
    var loadingStart: AlertDialog? = null
    open fun showLoadingBar() {
        ThreadUtils.runOnUiThread {
            try {
                if (loadingStart == null){
                    loadingStart = AlertDialog.Builder(this).create()
                    loadingStart!!.window!!.setBackgroundDrawable(ColorDrawable())
                    loadingStart!!.setCancelable(false)
                    loadingStart!!.setOnKeyListener { _, keyCode, event -> keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK }
                }
                if (null != loadingStart && !loadingStart!!.isShowing && !isFinishing && !isDestroyed) {
                    loadingStart!!.show()
                    loadingStart!!.setContentView(R.layout.progress)
                    loadingStart!!.setCanceledOnTouchOutside(false)
                }

            }catch (e : Exception){
                e.printStackTrace()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (null != loadingStart && loadingStart!!.isShowing && !isFinishing && !isDestroyed) {
            loadingStart!!.dismiss()
        }
    }
    open fun cancelLoadingBar() {
        ThreadUtils.runOnUiThread {
            try {
                if (null != loadingStart && loadingStart!!.isShowing && !isFinishing && !isDestroyed) {
                    loadingStart!!.dismiss()
                }
            }catch (e : Exception){
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (localClassName.contains("LocalHomeActivity")){
            ImmersionBar.with(this).transparentStatusBar().init()
        }
        else if (localClassName.contains("SongActivity")){
            enableEdgeToEdge()
        }
        else if (localClassName.contains("AlbumDetActivity") || localClassName.contains("PlayListDetActivity") || localClassName.contains("MoodDetailsActivity") || localClassName.contains("ArtistDetActivity") || localClassName.contains("NewReleaseDetailsActivity")){
        }
        else{
            ImmersionBar.with(this).transparentStatusBar().init()

        }
    }

    open fun addUIPlayer(
        artist: TextView,
        title: TextView,
        thumbnail: ImageView,
        metadata: MediaMetadata?
    ) {
        if (!TextUtils.isEmpty(metadata?.title)){
            title.text = metadata?.title
        }
        if (!TextUtils.isEmpty(metadata?.artists?.joinToString(", ") { it.name })){
            artist.text = metadata?.artists?.joinToString(", ") { it.name }
        }
        metadata?.thumbnailUrl?.let { thumbnailUrl ->
            Glide.with(this@BasicView)
                .load(thumbnailUrl)
                .apply(options)
                .thumbnail(
                    Glide.with(this@BasicView)
                        .load(posBitmap(0))
                        .apply(options)
                )
                .into(thumbnail)
        }

    }
 open  fun startSearchPage(isClose : Boolean = false){
     startInterActivity(Intent(this, MusicSearchHistoryActivity::class.java), searchInter(),isClose)

 }
    open  fun startPlayerPage(isClose : Boolean = false){
        startInterActivity(Intent(this, MainPlayer::class.java), playViewsInter(),isClose)
    }
    open  fun startQueuePage(isClose : Boolean = false){
        startInterActivity(Intent(this, QueueActivity::class.java), queueInter(),isClose)
    }
    open  fun finishCurrent(isClose : Boolean = false){
        if (checkPageOne()){
            LogUtils.e("------------------------f one back ")
            SnapMobManager.homeStudy.getShowCyAds(finishPageInterOne(),this)
        }else if (checkPageTwo()){
            LogUtils.e("------------------------f two back ")
            SnapMobManager.homeStudy.getShowCyAds(finishPageInterTwo(),this)
        }else if (checkPageThr()){
            LogUtils.e("------------------------f thr back ")
            SnapMobManager.homeStudy.getShowCyAds(finishPageInterThr(),this)
        }else if (checkPageFor()){
            LogUtils.e("------------------------f for back ")
            SnapMobManager.homeStudy.getShowCyAds(finishPageInterFor(),this)
        }
        finish()
    }
    private fun checkPageOne(): Boolean {
        return this.localClassName.contains("MainPlayer")
    }
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            if (checkPageOne()){
                LogUtils.e("------------------------one back ")
                SnapMobManager.homeStudy.getShowCyAds(finishPageInterOne(),this)
                finish()
                return true
            }else if(checkPageTwo()){
                LogUtils.e("------------------------two back ")
                SnapMobManager.homeStudy.getShowCyAds(finishPageInterTwo(),this)
                finish()
                return true
            }else if(checkPageThr()){
                LogUtils.e("------------------------thr back ")
                SnapMobManager.homeStudy.getShowCyAds(finishPageInterThr(),this)
                finish()
                return true
            }else if(checkPageFor()){
                LogUtils.e("------------------------for back ")
                SnapMobManager.homeStudy.getShowCyAds(finishPageInterFor(),this)
                finish()
                return true
            }else{
                finish()
                return true
            }
        }
        return  super.onKeyDown(keyCode,event)
    }
    private fun checkPageTwo(): Boolean {
        return (this.localClassName.contains("MoodDetailsActivity")
                || this.localClassName.contains("NewReleaseDetailsActivity")
                || this.localClassName.contains("AlbumDetActivity")
                || this.localClassName.contains("PlayListDetActivity"))
    }
    private fun checkPageThr(): Boolean {
        return (this.localClassName.contains("QueueActivity")
                || this.localClassName.contains("ArtistDetActivity")
                || this.localClassName.contains("ArtistItemActivity"))
    }
    private fun checkPageFor(): Boolean {
        return (this.localClassName.contains("MusicSearchHistoryActivity")
                || this.localClassName.contains("MusicSearchActivity")
                || this.localClassName.contains("CollectActivity")
                || this.localClassName.contains("HistoryActivity"))
    }
}
