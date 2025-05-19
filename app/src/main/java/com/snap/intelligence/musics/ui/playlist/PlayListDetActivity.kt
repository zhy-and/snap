package com.snap.intelligence.musics.ui.playlist

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
import com.snap.intelligence.musics.constants.playListDetBanner
import com.snap.intelligence.musics.constants.playlistDetInter
import com.snap.intelligence.musics.ui.start.HomeActivity.Companion.playerConnection
import com.snap.intelligence.musics.databinding.AlbumDetBinding
import com.snap.intelligence.musics.extensions.currentMetadata
import com.snap.intelligence.musics.mob.MobToStyle
import com.snap.intelligence.musics.mob.SnapMobManager
import com.snap.intelligence.musics.models.MediaMetadata
import com.snap.intelligence.musics.playback.queues.YouTubeQueue
import com.snap.intelligence.musics.viewmodels.OnlinePlaylistViewModel
import com.tencent.mmkv.MMKV
import com.zionhuang.innertube.models.WatchEndpoint
import kotlinx.coroutines.launch
import options
import timber.log.Timber

class PlayListDetActivity : BasicView() {

    private var playList: WatchEndpoint?= null
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

    private fun updatePlayerUI(metadata: MediaMetadata?) {
        addUIPlayer(binding.bottomPlayer.artist,binding.bottomPlayer.title,binding.bottomPlayer.thumbnail,metadata)
    }

    private fun showBanners() {
        val normalBan: NativeAd? = SnapMobManager.homeStudy.showWeaveNatives(
            playListDetBanner(), this)
        if (normalBan == null) {
            if (SnapPosManager.cyIsShowingFLag) {
                LogUtils.e("playListDetBanner  ban inter is showing return------")
                return
            }
            if (isFinishing || isDestroyed) {
                LogUtils.e("playListDetBanner  ban destroy  return------")
                return
            }
            if (SnapPosManager.posIsCanShow(playListDetBanner())) {
                LogUtils.e("playListDetBanner  ban limit  return------")
                return
            }
            SnapMobManager.homeStudy.initLoadBanners(playListDetBanner(),object :
                NativeWeavesListener {
                override fun nativeIsWeavesSuc(sucFlag: Boolean) {
                    if (isFinishing || isDestroyed) {
                        return
                    }
                    if (sucFlag) {
                        LogUtils.e("playListDetBanner  ban load  suc------")
                        showBanners()
                    }
                }
            })
        } else {
            LogUtils.e("playListDetBanner  ban show  suc------")
            binding?.homeBans?.let {
                it.visibility = View.VISIBLE
                it.setStyles(MobToStyle.Builder().build())
                it.setNativeAd(normalBan)
                if(!SnapPosManager.posIsCanShow(playListDetBanner())){
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SnapMobManager.homeStudy.getShowCyAds(playlistDetInter(),this)

        binding = AlbumDetBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ImmersionBar.with(this).transparentStatusBar().titleBar(binding.topCon.root).init()
//        binding.playBtns.visibility = View.GONE
        val albumViewModel = intent?.getStringExtra("playlistId")?.let {
            OnlinePlaylistViewModel(it)
        }
        binding.specAnm2.post {
            binding.specAnm2.visibility = View.VISIBLE
            binding.specAnm2.startShimmerAnimation()
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
            playList?.let {
                playerConnection?.playQueue(YouTubeQueue(it))
                startPlayerPage()
            }
        }
        binding.bottomPlayer.container.setOnClickListener {
            startPlayerPage()
        }
        lifecycleScope.launch {

//            withContext(Dispatchers.IO){
                albumViewModel?.playlist?.collect {
                    Timber.e(it.toString())
                    if (it == null) {
                        return@collect
                    }

                    binding.specAnm2.visibility = View.GONE
                    binding.specAnm2.stopShimmerAnimation()
                    // Set data to adapters
//                    withContext(Dispatchers.Main){
                        it?.apply {
                            it.shuffleEndpoint?.let { a->
                                playList = a
//                                binding.playBtns.visibility = View.VISIBLE
                            }
                            binding.titles.text = it.title
                            binding.artists.text = it.songCountText+" "
                                Glide.with(this@PlayListDetActivity)
                                    .load(it.thumbnail)
                                    .apply(options)
                                    .thumbnail(
                                        Glide.with(this@PlayListDetActivity)
                                            .load(R.mipmap.bg_album_default)
                                            .apply(options)
                                    )
                                    .into(binding.thumbnailo)

                            albumViewModel?.playlistSongs?.collect {
                                Timber.e(it.toString())
                                if (it == null) {
                                    return@collect
                                }
                                // Set data to adapters
//                    withContext(Dispatchers.Main){
                                it?.apply {

                                    newReleaseAlbumsAdapter.submitList(it)
                                }
//                    }

                            }

                        }
//                    }

                }

//            }





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

//            }
        newReleaseAlbumsAdapter = SongListAdapter(homeActivity = this@PlayListDetActivity)

        binding.moodAndGenresRecyclerView.layoutManager = LinearLayoutManager(this@PlayListDetActivity)
        binding.moodAndGenresRecyclerView.adapter = newReleaseAlbumsAdapter
        }
    
    
    
}
