package com.snap.intelligence.musics.ui.music
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDialog
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Player.REPEAT_MODE_OFF
import com.blankj.utilcode.util.LogUtils
import com.bumptech.glide.Glide
import com.google.android.gms.ads.nativead.NativeAd
import com.snap.intelligence.musics.constants.collectInter
import com.snap.intelligence.musics.App.Companion.isNextPlay
import com.snap.intelligence.musics.BasicView
import com.snap.intelligence.musics.R
import com.snap.intelligence.musics.constants.SnapPosManager
import com.snap.intelligence.musics.constants.MESSAGE_MSG_CODE
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_APP_REFRESH
import com.snap.intelligence.musics.constants.NativeWeavesListener
import com.snap.intelligence.musics.constants.playViewBanner
import com.snap.intelligence.musics.constants.playViewsInter
import com.snap.intelligence.musics.ui.start.HomeActivity.Companion.playerConnection
import com.snap.intelligence.musics.databinding.ActLoPlayerBinding
import com.snap.intelligence.musics.db.InternalDatabase
import com.snap.intelligence.musics.extensions.collectLatest
import com.snap.intelligence.musics.extensions.logClickEvents
import com.snap.intelligence.musics.extensions.mediaItems
import com.snap.intelligence.musics.extensions.toggleRepeatMode
import com.snap.intelligence.musics.local.randomBitmap
import com.snap.intelligence.musics.mob.MobToStyle
import com.snap.intelligence.musics.mob.SnapMobManager
import com.snap.intelligence.musics.ui.collect.CollectActivity
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import roundOptions
import java.util.concurrent.TimeUnit
import kotlin.random.Random


class MainPlayer : BasicView() {
    private var dialog: AppCompatDialog?= null
    private var zsAnis: ObjectAnimator?= null
    override fun onDestroy() {
        super.onDestroy()
        updateProgressRunnable?.let {
            handler.removeCallbacks(it)
        }
            zsAnis?.cancel();
            zsAnis?.removeAllListeners();
        adHandler?.removeCallbacksAndMessages(null)
        
    }
private lateinit var binding: ActLoPlayerBinding


    var isAnimating = false




    private fun resetToOriginal() {
        isAnimating = false
        zsAnis?.cancel()
        // 快速平滑复位动画
        ObjectAnimator.ofFloat(binding.anis, "rotation", binding.anis.getRotation(), 0f)
            .setDuration(300)
            .start();
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        try {
            lifecycleScope.launch {
                playerConnection?.currentSong?.collectLatest(scope) {
                        value ->
                    updateLikeButton(                       value?.song?.liked
                            == true)
                }
            }
        }catch (e : Exception){
            e.printStackTrace()
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SnapMobManager.homeStudy.getShowCyAds(playViewsInter(),this)
        enableEdgeToEdge()
        binding = ActLoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            setupRotationAnimation(binding.anis)
            setupRotationAnimation()
            if (playerConnection?.isPlaying?.value == true && playerConnection?.player?.playbackState == Player.STATE_READY){
                startAnimation()
            }
            lifecycleScope.launch {
                // 更新收藏状态
                playerConnection?.currentSong?.collectLatest(scope){
                    isCollect = it?.song?.liked ?: false
//                    Log.e("status ----",""+it?.song?.liked)
                    updateLikeButton(isCollect)
                }
            }

            binding.titles.apply {
                closeButton.setImageResource(R.mipmap.backs)
                nowPlayingText.visibility = View.GONE
                searchEditText2.visibility = View.VISIBLE
                closeButton.setOnClickListener {
                    finishCurrent()
                }
                searchEditText2.setOnClickListener {
                    logClickEvents("player_to_search")

                    startSearchPage()
                }
                searchEditText.setOnClickListener {
                    logClickEvents("player_to_search")

                    startSearchPage()
                }
                lists.setOnClickListener {
                    logClickEvents("player_to_queue")

                    startQueuePage()
                }
            }
            observePlayerState()
            setupListeners()
        }catch (e : Exception){
            e.printStackTrace()
        }

    }
    var isCollect = false
    var data : String?= null

        private val handler = Handler(Looper.getMainLooper())
        private var updateProgressRunnable: Runnable? = null


        private fun setupListeners() {

            var database = InternalDatabase.newInstance(this@MainPlayer)
           binding.addFav?.setOnClickListener {
               try {
                   logClickEvents("player_collect_btn")

                   if (playerConnection == null || binding.title.text.contains(getString(R.string.app_name))){
                       startInterActivity(Intent(this, CollectActivity::class.java), collectInter(),false)
                       return@setOnClickListener
                   }
                   database.transaction {
                           lifecycleScope.launch {
                               playerConnection?.currentSong?.first().let {
                                   it?.song?.apply {
                                       withContext(Dispatchers.IO){
                                           update(toggleLike())
                                       }
                                       updateLikeButton(!it.song.liked)
                                   }
                               }
                           }

                   }
               }catch (e : Exception){
                   e.printStackTrace()
               }


//               database.transaction {
//                   lifecycleScope.launch {
//
////                       playerConnection?.currentSong?.collect{
//
//                           database.song(playerConnection?.mediaMetadata?.value?.id).collect {
//                                   its ->
//                               val test = its?.song?.liked ?: false
//                               Log.e("old","----"+test)
//                               if (its == null){
//                                   withContext(Dispatchers.IO){
//                                       database.insert(playerConnection?.mediaMetadata?.value!!, SongEntity::toggleLike)
//                                   }
//                                   return@collect
//                               }
//
//                               its?.apply {
//                                   withContext(Dispatchers.IO){
//                                       database.update(song.toggleLike())
//                                   }
//                               }
//                           }
//
//                       }
//               }


//               lifecycleScope.launch {
//                   playerConnection?.currentSong?.collectLatest(scope) {
//                       value ->
//                       updateLikeButton(                       value?.song?.liked
//                               == true)
//                   }
//               }

           }

            binding.seekBar?.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser && playerConnection != null) {
                        updatePositionText(progress.toLong())
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    updateProgressRunnable?.let {
                        handler.removeCallbacks(it)
                    }
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    if (playerConnection != null) {
                        playerConnection?.player?.seekTo(seekBar.progress.toLong())
                        startProgressUpdate()
                    }
                }
            })

            binding.playPauseButton?.setOnClickListener { v: View? ->
                if (playerConnection == null || binding.title.text.contains(getString(R.string.app_name))){
                    startQueuePage()
                    logClickEvents("player_empty_click_play_btn")

                    return@setOnClickListener
                }
                logClickEvents("player_play_btn")

                if (playerConnection != null) {
                    if (playerConnection?.player?.playbackState == Player.STATE_ENDED) {
                        playerConnection?.player?.seekTo(0)
                        playerConnection?.player?.playWhenReady = true
                    } else {
                        playerConnection?.player?.playWhenReady =
                            !playerConnection?.player?.playWhenReady!!
                    }
                }
            }

            binding.previousButton?.setOnClickListener { v: View? ->
                try {
                    if (playerConnection == null || binding.title.text.contains(getString(R.string.app_name))){
                        startQueuePage()
                        return@setOnClickListener
                    }
                    if (playerConnection?.player?.repeatMode == REPEAT_MODE_OFF && !isNextPlay){
                        randomPlay()
                        return@setOnClickListener
                    }
                    if (playerConnection != null) {
                        playerConnection?.seekToPrevious()
                    }
                    logClickEvents("player_previous_btn")
                }catch (e : Exception){
                    e.printStackTrace()
                }


            }

            binding.nextButton?.setOnClickListener { v: View? ->
                try {
                    if (playerConnection == null || binding.title.text.contains(getString(R.string.app_name))){
                        startQueuePage()
                        return@setOnClickListener
                    }
                    if (playerConnection?.player?.repeatMode == REPEAT_MODE_OFF && !isNextPlay){
                        randomPlay()
                        return@setOnClickListener
                    }
                    if (playerConnection != null) {
                        playerConnection?.seekToNext()
                    }
                    logClickEvents("player_next_btn")
                }catch (e : Exception){
                    e.printStackTrace()
                }


            }

            binding.repeatButton?.setOnClickListener { v: View? ->
                if (playerConnection == null || binding.title.text.contains(getString(R.string.app_name))){
                    startQueuePage()
                    return@setOnClickListener
                }
                if (playerConnection != null) {
                    playerConnection?.player?.toggleRepeatMode()
                }
                logClickEvents("player_repeat_btn")

            }


        }
    private var scope = CoroutineScope(Dispatchers.Main) + Job()

        private fun observePlayerState() {
            if (playerConnection == null) return

            // 监听播放状态变化
            playerConnection?.player?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
//                    Log.e("onPlaybackStateChanged","---"+playbackState)
                    updatePlayPauseButton()
                    updateProgress()
                }

                override fun onEvents(player: Player, events: Player.Events) {
                    super.onEvents(player, events)
                    if (events.containsAny(Player.EVENT_PLAYBACK_STATE_CHANGED, Player.EVENT_PLAY_WHEN_READY_CHANGED)) {
                        val isBufferingOrReady = player.playbackState == Player.STATE_BUFFERING
                        if (isBufferingOrReady) {
                            try {
                                if (binding.anims.isInvisible){
                                    binding.anims.visibility = View.VISIBLE
                                    binding.anims.playAnimation()
                                }

                            }catch (e : Exception){
                                e.printStackTrace()
                            }
                        }else if(player.playbackState == Player.STATE_READY){
                            if (binding.anims.isVisible){
                                binding.anims.visibility = View.INVISIBLE
                                binding.anims.cancelAnimation()
                            }
                        }

//
                }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
//                    togglePlayback()

                    updatePlayPauseButton()
                    if (isPlaying && playerConnection?.isPlaying?.value == true) {
                        startAnimation()
//                        binding?.anims?.setMinAndMaxFrame(100,100)
                        startProgressUpdate()
                    } else {
                        pauseAnimation()
//                        binding?.anims?.setMinAndMaxFrame(0,0)
                        updateProgressRunnable?.let {
                            handler.removeCallbacks(it)
                        }
                    }
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    updateRepeatButton(repeatMode)
                }

                override fun onIsLoadingChanged(isLoading: Boolean) {
                    super.onIsLoadingChanged(isLoading)
//                    if (isLoading){
//                        try {
//                            binding.anims.visibility = View.VISIBLE
//                            binding.anims.playAnimation()
//                        }catch (e : Exception){
//                            e.printStackTrace()
//                        }
////
//                    }else{
//                        binding.anims.visibility = View.INVISIBLE
//                        binding.anims.cancelAnimation()
//                    }
                }
                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {

                }

                override fun onMediaMetadataChanged(metadata: MediaMetadata) {
                    binding.positionText.text = "0:00"
                    binding.seekBar.progress = 0
//                    resetAnimation()
//                    togglePlayback()
                    updateMediaInfo()
                }
            })

            // 初始化UI状态
            updatePlayPauseButton()
            updateRepeatButton(playerConnection?.player?.repeatMode!!)
            updateMediaInfo()
            updateProgress()
            startProgressUpdate()
        }

        private fun updatePlayPauseButton() {
            if (playerConnection == null   || isFinishing || isDestroyed) return
            val isPlaying = playerConnection?.player?.isPlaying ?: return
            binding.playPauseButton?.setImageResource(if (isPlaying) R.mipmap.pause_player else R.mipmap.play_btn_local)
        }

    private fun updateRepeatButton(repeatMode: Int) {
        if ( isFinishing || isDestroyed) return
        when (repeatMode) {
            Player.REPEAT_MODE_OFF -> {
//                playerConnection?.player?.shuffleModeEnabled = true
                binding. repeatButton?.setImageResource(R.mipmap.mode_random)
            }

            Player.REPEAT_MODE_ONE -> {
//                playerConnection?.player?.shuffleModeEnabled = false
                binding. repeatButton?.setImageResource(R.mipmap.mode_single)
            }

            Player.REPEAT_MODE_ALL -> {
//                playerConnection?.player?.shuffleModeEnabled = false
                binding. repeatButton?.setImageResource(R.mipmap.mode_list_loop)
            }
        }
    }


        private fun updateMediaInfo() {
            try {
                if (playerConnection == null || isFinishing || isDestroyed) return

                playerConnection?.mediaMetadata?.value?.let {
                        binding.title?.text = it.title
                    binding.artist?.text = it?.artists?.joinToString(", ") { it.name }
                    Glide.with(this@MainPlayer)
                        .load(it.thumbnailUrl)
                        .apply(roundOptions)
                        .thumbnail(
                            Glide.with(this@MainPlayer)
                                .load(randomBitmap())
                                .apply(roundOptions)
                        )
                        .into(binding.thumbnail)
                    // 加载专辑封面
//                    if (it.thumbnailUrl != null) {
//                        Glide.with(this)
//                            .load(it.thumbnailUrl) //                        .placeholder(R.drawable.ic_album)
//                            .placeholder(randomBitmap())
//                            .apply(RequestOptions.bitmapTransform(CropCircleTransformation())) // 设置圆形图片
//                            .into(binding.thumbnail)
//
//                    } else {
//                        Glide.with(this)
//                            .load(randomBitmap()) //                        .placeholder(R.drawable.ic_album)
//                            .apply(RequestOptions.bitmapTransform(CropCircleTransformation())) // 设置圆形图片
//                            .into(binding.thumbnail)
//                    }

                }
            }catch (e : Exception){
                e.printStackTrace()
            }


        }

        private fun updateLikeButton(isLiked: Boolean) {
            if (playerConnection == null   || isFinishing || isDestroyed) return
            binding.addFav?.setImageResource(if (isLiked) R.mipmap.collect_playe else R.mipmap.uncollect)
        }

        private fun updateProgress() {
            try {
                if (playerConnection == null || isFinishing || isDestroyed) return

                val duration = playerConnection?.player?.duration ?: return
                val position = playerConnection?.player?.currentPosition ?: return
                if (duration > 0) {
                    binding. seekBar?.max = duration.toInt()
                    binding.seekBar?.progress = position.toInt()
                    updateDurationText(duration)
                    updatePositionText(position)
                }
            }catch (e : Exception){
                e.printStackTrace()
            }
        }

        private fun startProgressUpdate() {
            if (playerConnection == null   || isFinishing || isDestroyed) return
            updateProgressRunnable?.let {
                handler.removeCallbacks(it)
            }
            updateProgressRunnable = object : Runnable {
                override fun run() {
                    updateProgress()
                    handler.postDelayed(this, 1000)
                }
            }
            updateProgressRunnable?.let {
                handler.post(it)
            }
        }

        private fun updatePositionText(position: Long) {
            binding.positionText?.text = formatDuration(position)
        }

        private fun updateDurationText(duration: Long) {
            binding.durationText?.text = formatDuration(duration)
        }

        private fun formatDuration(duration: Long): String {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
            val seconds =
                TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(minutes)
            return String.format("%d:%02d", minutes, seconds)
        }

    override fun onPause() {
        super.onPause()
        updateProgressRunnable?.let {
            handler.removeCallbacks(it)
        }
        rotationAnimator?.pause()
        if (adHandler?.hasMessages(MESSAGE_MSG_CODE) == true){
            adHandler?.removeMessages(MESSAGE_MSG_CODE)
        }
    }


    override fun onResume() {
        super.onResume()
        if (playerConnection != null && playerConnection?.player?.isPlaying == true) {
            startProgressUpdate()
            if (rotationAnimator?.isPaused == true){
                rotationAnimator?.resume()
            }
        }
        showBanners()
    }
    fun randomPlay(){
        try {

                var flag = 0
                playerConnection?.player?.mediaItems?.apply {
                    forEachIndexed() {  index,its ->
                        if (its?.mediaMetadata?.title == playerConnection?.player?.currentMediaItem?.mediaMetadata?.title){
                            flag = index
                        }
                    }
                    var index =  Random.nextInt(this.size)
                    if (index == flag){
                        index =  Random.nextInt(this.size)
                    }
                    playerConnection?.player?.seekToDefaultPosition(index)
                    playerConnection?.player?.playWhenReady = true

                }
        }catch (e : Exception){
            e.printStackTrace()
        }
    }
    // 初始化旋转动画
    private fun setupRotationAnimation() {
        rotationAnimator = ObjectAnimator.ofFloat(binding.allImg, "rotation", 0f, 360f)
        rotationAnimator?.setDuration(10000) // 10秒转一圈
        rotationAnimator?.setInterpolator(LinearInterpolator())
        rotationAnimator?.setRepeatCount(ValueAnimator.INFINITE)
    }


    var lastSYs = 0L
    // 播放控制方法
    fun togglePlayback() {
        var currentSYs = System.currentTimeMillis()
        if (currentSYs - lastSYs > 500){
            lastSYs = currentSYs
            if (playerConnection?.isPlaying?.value == true) {
                startAnimation()
            } else {
                pauseAnimation()
            }
        }

    }

    // 初始化视图时调用
    private fun setupRotationAnimation(targetView: View) {
        // 设置顶部中心为轴
        targetView.post {
            targetView.pivotX = targetView.width / 2f
            targetView.pivotY = 0f // 顶部轴心
        }

        // 初始化进入页面的右→左动画（只执行一次）
        zsAnis = ObjectAnimator.ofFloat(
            targetView, "rotation", -20f, 0f // 从0度到左倾-30度
        )
        zsAnis?.setDuration(500)
        zsAnis?.interpolator = AccelerateDecelerateInterpolator()
//        zsAnis?.start()
    }


    // 开始旋转（从当前角度继续）
    private fun startAnimation() {
        isAnimating = true
        zsAnis?.start()
        if (rotationAnimator?.isPaused() == true) {
            rotationAnimator?.resume()
        } else {
            rotationAnimator?.start()
        }
    }

    // 暂停旋转（保留当前角度）
    private fun pauseAnimation() {
        if (isAnimating) {
            resetToOriginal()
        }
        rotationAnimator?.pause()
        ObjectAnimator.ofFloat(binding.anis, "rotation", binding.anis.getRotation(), -20f)
            .setDuration(300)
            .start();
//        if (!binding.anims.isAnimating){
//            binding.anims.repeatMode = LottieDrawable.REVERSE
//            binding.anims.playAnimation()
//        }else{
//            binding.anims.setMinAndMaxFrame(0,0)
//            binding.anims.playAnimation()
//        }
    }

    // 重置转盘位置
    fun resetAnimation() {
        rotationAnimator?.cancel()
        binding.allImg.rotation = 0f
    }
    private var rotationAnimator: ObjectAnimator?= null

    private fun showBanners() {
        val normalBan: NativeAd? = SnapMobManager.homeStudy.showWeaveNatives(
            playViewBanner(), this)
        if (normalBan == null) {
            if (SnapPosManager.cyIsShowingFLag) {
                LogUtils.e("playViewBanner  ban inter is showing return------")
                return
            }
            if (isFinishing || isDestroyed) {
                LogUtils.e("playViewBanner  ban destroy  return------")
                return
            }
            if (SnapPosManager.posIsCanShow(playViewBanner())) {
                LogUtils.e("playViewBanner  ban limit  return------")
                return
            }
            SnapMobManager.homeStudy.initLoadBanners(playViewBanner(),object :
                NativeWeavesListener {
                override fun nativeIsWeavesSuc(sucFlag: Boolean) {
                    if (isFinishing || isDestroyed) {
                        return
                    }
                    if (sucFlag) {
                        LogUtils.e("playViewBanner  ban load  suc------")
                        showBanners()
                    }
                }
            })
        } else {
            LogUtils.e("playViewBanner  ban show  suc------")
            binding?.homeBans?.let {
                it.visibility = View.VISIBLE
                it.setStyles(MobToStyle.Builder().build())
                it.setNativeAd(normalBan)
                if(!SnapPosManager.posIsCanShow(playViewBanner())){
                    val cts = MMKV.defaultMMKV().getInt(MUSIC_CONSTANT_APP_REFRESH, 0)
                    if (cts != 0){
                        adHandler.removeMessages(MESSAGE_MSG_CODE)
                        adHandler.sendEmptyMessageDelayed(MESSAGE_MSG_CODE,(cts * 1000).toLong())
                    }
                }
            }
        }
    }



    private val adHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg?.what) {
                MESSAGE_MSG_CODE -> {
                    showBanners()
                }
            }
        }
    }


}