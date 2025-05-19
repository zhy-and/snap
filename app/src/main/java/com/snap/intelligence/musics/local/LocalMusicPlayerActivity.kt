package com.snap.intelligence.musics.local
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.enableEdgeToEdge
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_MUSIC
import androidx.media3.common.Player
import androidx.media3.common.Player.REPEAT_MODE_OFF
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.tencent.mmkv.MMKV
import com.zionhuang.innertube.MainBe
import com.snap.intelligence.musics.BasicView
import com.snap.intelligence.musics.R
import com.snap.intelligence.musics.constants.queueInter
import com.snap.intelligence.musics.databinding.ActLoPlayerBinding
import com.snap.intelligence.musics.extensions.mediaItems
import com.snap.intelligence.musics.extensions.toggleRepeatMode
import com.snap.intelligence.musics.local.DataKeeper.initCreateData
import com.snap.intelligence.musics.local.DataKeeper.loadSongs
import com.snap.intelligence.musics.local.LocalHomeActivity.Companion.playerConnection
import jp.wasabeef.glide.transformations.CropCircleTransformation
import java.util.concurrent.TimeUnit
import kotlin.random.Random


class LocalMusicPlayerActivity : BasicView() {
    private var rotationAnimator: ObjectAnimator?= null
    private val youTube = MainBe
    private var zsAnis: ObjectAnimator?= null
    var isAnimating = false

    // 初始化视图时调用
    private fun setupRotationAnimation(targetView: View) {
        // 设置顶部中心为轴
        targetView.post {
            targetView.pivotX = targetView.width / 2f
            targetView.pivotY = 0f // 顶部轴心
        }

        // 初始化进入页面的右→左动画（只执行一次）
        zsAnis = ObjectAnimator.ofFloat(
            targetView, "rotation", -30f, 0f // 从0度到左倾-30度
        )
        zsAnis?.setDuration(500)
        zsAnis?.interpolator = AccelerateDecelerateInterpolator()
//        zsAnis?.start()
    }


    private fun resetToOriginal() {
        isAnimating = false
        zsAnis?.cancel()
        // 快速平滑复位动画
        ObjectAnimator.ofFloat(binding.anis, "rotation", binding.anis.getRotation(), 0f)
            .setDuration(300)
            .start();
    }

   private var songs = mutableListOf<MediaItem>()
    override fun onDestroy() {
        super.onDestroy()
        updateProgressRunnable?.let {
            handler.removeCallbacks(it)
        }
        zsAnis?.cancel();
        zsAnis?.removeAllListeners();
    }
private lateinit var binding: ActLoPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActLoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        try {
            setupRotationAnimation(binding.anis)
            setupRotationAnimation()
            if (playerConnection?.isPlaying?.value == true){
                startAnimation()
            }
            binding.titles.lists.setOnClickListener {
                if (!MMKV.defaultMMKV().getBoolean("hasPer",false)){
                    requestData()
                    return@setOnClickListener
                }
                startInterActivity(Intent(this, LocalPlayListActivity::class.java), queueInter(),false)
            }
            binding.titles.closeButton.setImageResource(R.mipmap.backs)
            binding.titles.nowPlayingText.setTextColor(Color.parseColor("#000000"))
            binding.titles.closeButton.setOnClickListener {
                finish()
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


           binding.addFav?.setOnClickListener { v: View? ->
               if (!MMKV.defaultMMKV().getBoolean("hasPer",false)){
                   requestData()
                   return@setOnClickListener
               }
//               playerConnection?.toggleLike()
//               updateLikeButton(!isCollect)
               updateCollectImages()

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
                if (!MMKV.defaultMMKV().getBoolean("hasPer",false)){
                    requestData()
                    return@setOnClickListener
                }
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
                if (!MMKV.defaultMMKV().getBoolean("hasPer",false)){
                    requestData()
                    return@setOnClickListener
                }
                try {
                    if (playerConnection?.player?.repeatMode == REPEAT_MODE_OFF){
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
                        return@setOnClickListener
                    }
                }catch (e : Exception){
                    e.printStackTrace()
                }

                if (playerConnection != null) {
                    playerConnection?.seekToPrevious()
                }
            }

            binding.nextButton?.setOnClickListener { v: View? ->
                if (!MMKV.defaultMMKV().getBoolean("hasPer",false)){
                    requestData()
                    return@setOnClickListener
                }
                try {
                    if (playerConnection?.player?.repeatMode == REPEAT_MODE_OFF){
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
                        return@setOnClickListener
                    }
                }catch (e : Exception){
                    e.printStackTrace()
                }


                if (playerConnection != null) {
                    playerConnection?.seekToNext()
                }
            }

            binding.repeatButton?.setOnClickListener { v: View? ->
                if (!MMKV.defaultMMKV().getBoolean("hasPer",false)){
                    requestData()
                    return@setOnClickListener
                }
                if (playerConnection != null) {
                    playerConnection?.player?.toggleRepeatMode()
                }
            }


        }

    private fun requestData() {
        try {
            XXPermissions.with(this).permission(Permission.READ_MEDIA_AUDIO)
                .request { permissions, allGranted ->
                    if (allGranted) {
                        MMKV.defaultMMKV().putBoolean("hasPer",true)
                        val dialog =  showDialogs(this,R.layout.dialog_anis)
                        binding?.root?.postDelayed({
                            if (dialog.isShowing && !isFinishing && !isDestroyed){
                                dialog.dismiss()
                            }
                        },2000)
                        val songs = MusicScanner.getLocalSongs(this) as ArrayList<Song>
                        if (songs.isNullOrEmpty()){
                            return@request
                        }
                        initCreateData(songs)
                        val data =  loadSongs(Song())
                        if (data.isNotEmpty()){
                            addLocalMusic(data)
                        }
                        finish()
                    }
                }
        }catch (e : Exception){
            e.printStackTrace()
        }

    }

    private fun addLocalMusic(localMusic: ArrayList<Song>) {
        val mediaItems = localMusic.map { music ->
            MediaItem.Builder()
                .setMediaId(music.id.toString())
                .setUri(Uri.parse(music.uri).buildUpon().appendPath(music.uriId.toString()).build())
                .setCustomCacheKey(music.id.toString())
                .setTag(music.toMediaMetadata())
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(music.title)
                        .setSubtitle(music.artist)
                        .setArtist(music.artist)
//                        .setArtworkUri(music.albumArtUri.uri)
//                        .setAlbumTitle(music.album)
                        .setMediaType(MEDIA_TYPE_MUSIC)
                        .build()
                )
                .build()

        }
        if (playerConnection?.queueWindows != null && playerConnection?.queueWindows?.value?.isEmpty() == true){
            playerConnection?.addToQueue(mediaItems)
        }

    }

    private fun observePlayerState() {
        try {
            if (playerConnection == null ||  isFinishing || isDestroyed) return
            checkCollect()
            // 监听播放状态变化
            playerConnection?.player?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    updateProgress()
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
//                    Log.e("---onIsPlayingChanged",""+isPlaying)
//                    togglePlayback()
                    updatePlayPauseButton()
                    if (isPlaying) {
                        startAnimation()
                        startProgressUpdate()
                    } else {
                        pauseAnimation()
                        updateProgressRunnable?.let {
                            handler.removeCallbacks(it)
                        }
                    }
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    updateRepeatButton(repeatMode)
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                }

                override fun onMediaMetadataChanged(metadata: MediaMetadata) {
//                    Log.e("---onIsPlayingChanged","onMediaMetadataChanged")
                    checkCollect()
                    updateMediaInfo()
//                    resetAnimation()
//                    togglePlayback()
                }
            })

            // 初始化UI状态
            updatePlayPauseButton()
            updateRepeatButton(playerConnection?.player?.repeatMode!!)
            updateMediaInfo()
            updateProgress()
            startProgressUpdate()
        }catch (e : Exception){
            e.printStackTrace()
        }

        }

    private fun checkCollect() {
        try {
            if (isFinishing || isDestroyed){
                return
            }
            var isCan = false
            loadSongs(Song()).forEach {
                if (it.isCollect && it.title == playerConnection?.player?.currentMediaItem?.mediaMetadata?.title){
                    isCan = true
                    return@forEach
                }
            }
            currentCollect = isCan
            updateLikeButton(isCan)
        }catch (e : Exception){
            e.printStackTrace()
        }

    }

    var currentCollect = false

    private fun updateCollectImages() {
        if (isFinishing || isDestroyed){
            return
        }
        currentCollect = !currentCollect
        updateLikeButton(currentCollect)
       val data =  loadSongs(Song())
           data.forEach {
            if (it.title == playerConnection?.player?.currentMediaItem?.mediaMetadata?.title){
                it.isCollect = currentCollect
                return@forEach
            }
        }
        initCreateData(data)
    }

    private fun updatePlayPauseButton() {
            if (playerConnection == null   || isFinishing || isDestroyed) return
            val isPlaying = playerConnection?.player?.isPlaying ?: return
            //        playPauseButton.setIma😎geResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
            binding.playPauseButton?.setImageResource(if (isPlaying) R.mipmap.pause_player else R.mipmap.play_btn_local)
        }

        private fun updateRepeatButton(repeatMode: Int) {
            if ( isFinishing || isDestroyed) return
            when (repeatMode) {
                Player.REPEAT_MODE_OFF -> {
                    binding. repeatButton?.setImageResource(R.mipmap.mode_random)
                }

                Player.REPEAT_MODE_ONE -> {
                    binding. repeatButton?.setImageResource(R.mipmap.mode_single)
                }

                Player.REPEAT_MODE_ALL -> {
                    binding. repeatButton?.setImageResource(R.mipmap.mode_list_loop)
                }
            }
        }



        private fun updateMediaInfo() {
            try {
                if (playerConnection == null || isFinishing || isDestroyed) return

                playerConnection?.mediaMetadata?.value?.let {
                    binding. title?.text = it.title
                    binding.titles.nowPlayingText?.text = it.title
                    binding.artist?.text = it?.artists?.joinToString(", ") { it.name }

                    // 加载专辑封面
                    if (it.thumbnailUrl != null) {
                        Glide.with(this)
                            .load(it.thumbnailUrl) //
                            .placeholder(randomBitmap())
                            .error(randomBitmap())
                            .apply(RequestOptions.bitmapTransform(CropCircleTransformation())) // 设置圆形图片
                            .into(binding.thumbnail)
                    } else {
                        Glide.with(this)
                            .load(randomBitmap()) //
                            .placeholder(randomBitmap())
                            .error(randomBitmap())
                            .apply(RequestOptions.bitmapTransform(CropCircleTransformation())) // 设置圆形图片
                            .into(binding.thumbnail)
                    }


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
            try {
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
            }catch (e : Exception){
                e.printStackTrace()
            }

        }

        private fun updatePositionText(position: Long) {
            if (playerConnection == null   || isFinishing || isDestroyed) return
            binding.positionText?.text = formatDuration(position)
        }

        private fun updateDurationText(duration: Long) {
            if (playerConnection == null   || isFinishing || isDestroyed) return
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
        }


        override fun onResume() {
            super.onResume()
            if (playerConnection != null && playerConnection?.player?.isPlaying == true) {
                startProgressUpdate()
                if (rotationAnimator?.isPaused == true){
                    rotationAnimator?.resume()
                }
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
    }

    // 重置转盘位置
    fun resetAnimation() {
        rotationAnimator?.cancel()
        binding.allImg.rotation = 0f
    }





}