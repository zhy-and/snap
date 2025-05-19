package com.snap.intelligence.musics.ui.album

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.bumptech.glide.Glide
import com.google.android.gms.ads.nativead.NativeAd
import com.gyf.immersionbar.ImmersionBar
import com.snap.intelligence.musics.BasicView
import com.snap.intelligence.musics.R
import com.snap.intelligence.musics.SongListAdapter
import com.snap.intelligence.musics.constants.SnapPosManager
import com.snap.intelligence.musics.constants.MESSAGE_MSG_CODE
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_APP_REFRESH
import com.snap.intelligence.musics.constants.NativeWeavesListener
import com.snap.intelligence.musics.constants.albumDetBanner
import com.snap.intelligence.musics.constants.albumDetInter
import com.snap.intelligence.musics.ui.start.HomeActivity.Companion.playerConnection
import com.snap.intelligence.musics.databinding.AlbumDetBinding
import com.snap.intelligence.musics.db.InternalDatabase
import com.snap.intelligence.musics.db.entities.AlbumWithSongs
import com.snap.intelligence.musics.extensions.currentMetadata
import com.snap.intelligence.musics.extensions.logClickEvents
import com.snap.intelligence.musics.mob.MobToStyle
import com.snap.intelligence.musics.mob.SnapMobManager
import com.snap.intelligence.musics.models.MediaMetadata
import com.snap.intelligence.musics.playback.queues.LocalAlbumRadio
import com.snap.intelligence.musics.viewmodels.AlbumViewModel
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.launch
import options
import timber.log.Timber

class AlbumDetActivity : BasicView() {

    private var albumSongs: AlbumWithSongs? = null
    private lateinit var binding: AlbumDetBinding
    private lateinit var newReleaseAlbumsAdapter: SongListAdapter
    override fun onResume() {
        super.onResume()
        if (playerConnection != null && playerConnection?.player?.currentMetadata != null){
            playerConnection?.mediaMetadata?.let {
                updatePlayerUI(it.value)
            }
        }
        showBanners()
    }
    private fun showBanners() {
        val normalBan: NativeAd? = SnapMobManager.homeStudy.showWeaveNatives(
            albumDetBanner(), this)
        if (normalBan == null) {
            if (SnapPosManager.cyIsShowingFLag) {
                LogUtils.e("albumDetBanner  ban inter is showing return------")
                return
            }
            if (isFinishing || isDestroyed) {
                LogUtils.e("albumDetBanner  ban destroy  return------")
                return
            }
            if (SnapPosManager.posIsCanShow(albumDetBanner())) {
                LogUtils.e("albumDetBanner  ban limit  return------")
                return
            }
            SnapMobManager.homeStudy.initLoadBanners(albumDetBanner(),object :
                NativeWeavesListener {
                override fun nativeIsWeavesSuc(sucFlag: Boolean) {
                    if (isFinishing || isDestroyed) {
                        return
                    }
                    if (sucFlag) {
                        LogUtils.e("albumDetBanner  ban load  suc------")
                        showBanners()
                    }
                }
            })
        } else {
            LogUtils.e("albumDetBanner  ban show  suc------")
            binding?.homeBans?.let {
                it.visibility = View.VISIBLE
                it.setStyles(MobToStyle.Builder().build())
                it.setNativeAd(normalBan)
                if(!SnapPosManager.posIsCanShow(albumDetBanner())){
                    val cts = MMKV.defaultMMKV().getInt(MUSIC_CONSTANT_APP_REFRESH, 0)
                    if (cts != 0){
                        handler.removeMessages(MESSAGE_MSG_CODE)
                        handler.sendEmptyMessageDelayed(MESSAGE_MSG_CODE,(cts * 1000).toLong())
                    }
                }
            }
        }
    }



    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg?.what) {
                MESSAGE_MSG_CODE -> {
                    showBanners()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (handler?.hasMessages(MESSAGE_MSG_CODE) == true){
            handler?.removeMessages(MESSAGE_MSG_CODE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler?.removeCallbacksAndMessages(null)
    }
    private fun updatePlayerUI(metadata: MediaMetadata?) {
        addUIPlayer(binding.bottomPlayer.artist,binding.bottomPlayer.title,binding.bottomPlayer.thumbnail,metadata)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SnapMobManager.homeStudy.getShowCyAds(albumDetInter(),this)

        binding = AlbumDetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ImmersionBar.with(this).transparentStatusBar().titleBar(binding.topCon.root).init()

        val albumViewModel = intent?.getStringExtra("albumId")?.let {
            AlbumViewModel(InternalDatabase.newInstance(this), it

            )
        }
        binding.topCon.apply {
            clearButton.setOnClickListener {
                startQueuePage()
            }
            searchBtns.setOnClickListener {
                startSearchPage()
            }
            backBtn.setOnClickListener {
                finishCurrent()
            }
        }
        binding.playBtns.setOnClickListener {
            logClickEvents("album_det_play_btn")
            albumSongs?.let {
                playerConnection?.playQueue(LocalAlbumRadio(it))
               startPlayerPage()
            }
        }
        binding.specAnm2.post {
            binding.specAnm2.visibility = View.VISIBLE
            binding.specAnm2.startShimmerAnimation()
        }
        binding.bottomPlayer.container.setOnClickListener {
            startPlayerPage()
        }
        lifecycleScope.launch {
//              database.quickPicks()
//                    .first().shuffled().take(20).forEach {
//                        Log.e("quickPicks-----",it.toString())
//                  }
//
//                val fromTimeStamp = System.currentTimeMillis() - 86400000 * 7 * 2
//
//                database.mostPlayedSongs(fromTimeStamp, limit = 15, offset = 5)
//                    .first().shuffled().take(10).forEach {
//                        Log.e("keep -----",it.toString())
//                    }

                albumViewModel?.songLists?.collect {
                    Timber.e(it.toString())
                    if (it == null) {
                        return@collect
                    }
                    if (isFinishing || isDestroyed) {
                        return@collect
                    }
                    binding.container.visibility = View.VISIBLE
                    binding.specAnm2.visibility = View.GONE
                    binding.specAnm2.stopShimmerAnimation()
                    binding.titles.text = it.firstOrNull()?.album?.name
                    binding.artists.text =  it.firstOrNull()?.artists?.joinToString(", ") { it.name }
                            Glide.with(this@AlbumDetActivity)
                                .load(it.firstOrNull()?.thumbnail)
                                .apply(options)
                                .thumbnail(
                                    Glide.with(this@AlbumDetActivity)
                                        .load(R.mipmap.bg_album_default)
                                        .apply(options)
                                )
                                .into(binding.thumbnailo)

                    // Set data to adapters
                        it?.apply {
                            newReleaseAlbumsAdapter = SongListAdapter(true,albumSongs,this@AlbumDetActivity)
                            binding.moodAndGenresRecyclerView.layoutManager = LinearLayoutManager(this@AlbumDetActivity)
                            binding.moodAndGenresRecyclerView.adapter = newReleaseAlbumsAdapter
                            newReleaseAlbumsAdapter?.submitList(this)
                        }

                }

        }
        lifecycleScope.launch {
            albumViewModel?.albumWithSongs?.collect {
                if (it == null){
                    return@collect
                }
                albumSongs = it
            }
        }
        lifecycleScope.launch {

            playerConnection?.mediaMetadata?.collect{
                updatePlayerUI(it)
            }

        }
        lifecycleScope.launch {
            playerConnection?.isPlaying?.collect{
                if (it){
                    binding.bottomPlayer.playPauseButton.setImageResource(R.mipmap.home_pause_btn)
                }else{
                    binding.bottomPlayer.playPauseButton.setImageResource(R.mipmap.play)
                }
            }
        }


    }
}
