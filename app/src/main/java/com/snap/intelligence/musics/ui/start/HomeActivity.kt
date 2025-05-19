package com.snap.intelligence.musics.ui.start

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.RatingBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ToastUtils
import com.snap.intelligence.musics.constants.MESSAGE_MSG_CODE
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_APP_REFRESH
import com.snap.intelligence.musics.constants.collectInter
import com.snap.intelligence.musics.constants.historyInter
import com.snap.intelligence.musics.constants.homeBanner
import com.snap.intelligence.musics.constants.playViewsInter
import com.snap.intelligence.musics.constants.releaseDetailInter
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.material.appbar.AppBarLayout
import com.snap.intelligence.musics.BasicView
import com.snap.intelligence.musics.ui.collect.CollectActivity
import com.snap.intelligence.musics.EvenItemDecoration
import com.snap.intelligence.musics.ui.history.HistoryActivity
import com.snap.intelligence.musics.HomePageSectionsAdapter
import com.snap.intelligence.musics.MoodAndGenresAdapter
import com.snap.intelligence.musics.ui.music.MainPlayer
import com.snap.intelligence.musics.NewReleaseAlbumsAdapter
import com.snap.intelligence.musics.NewReleaseDetailsActivity
import com.snap.intelligence.musics.R
import com.snap.intelligence.musics.beans.InitBanner
import com.snap.intelligence.musics.constants.SnapPosManager
import com.snap.intelligence.musics.constants.SnapPosManager.getInstallSys
import com.snap.intelligence.musics.constants.MMKV_HOME_BACK_NO_FINISH
import com.snap.intelligence.musics.constants.NativeWeavesListener
import com.snap.intelligence.musics.constants.StopMusicOnTaskClearKey
import com.snap.intelligence.musics.databinding.ActivityHomeBinding
import com.snap.intelligence.musics.db.InternalDatabase
import com.snap.intelligence.musics.db.MusicDatabase
import com.snap.intelligence.musics.extensions.currentMetadata
import com.snap.intelligence.musics.extensions.logClickEvents
import com.snap.intelligence.musics.ui.about.AboutView
import com.snap.intelligence.musics.local.AppBarStateChangeListener
import com.snap.intelligence.musics.local.showDialogs
import com.snap.intelligence.musics.mob.MobToStyle
import com.snap.intelligence.musics.mob.SnapMobManager
import com.snap.intelligence.musics.models.MediaMetadata
import com.snap.intelligence.musics.playback.MusicService
import com.snap.intelligence.musics.playback.MusicService.MusicBinder
import com.snap.intelligence.musics.playback.PlayerConnection
import com.snap.intelligence.musics.utils.dataStore
import com.snap.intelligence.musics.utils.get
import com.snap.intelligence.musics.viewmodels.HomeViewModel
import com.snap.intelligence.musics.work.SnapBasicInterfaces.Companion.loadBaseInfos
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

class HomeActivity : BasicView() {
    override fun onStart() {
        super.onStart()
        startService(Intent(this@HomeActivity, MusicService::class.java))
        bindService(Intent(this@HomeActivity, MusicService::class.java), serviceConnection, BIND_AUTO_CREATE)
    }
    companion object {
        var playerConnection by mutableStateOf<PlayerConnection?>(null)
        var showed = false
    }
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is MusicBinder) {
                playerConnection = PlayerConnection(this@HomeActivity, service, database, lifecycleScope)
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

        override fun onServiceDisconnected(name: ComponentName?) {
            playerConnection?.dispose()
            playerConnection = null
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            if (SnapMobManager.homeStudy.uniqueInter != null && !SnapMobManager.homeStudy.backLimit()){
                SnapMobManager.homeStudy.showCachedInter(this)
                return true
            }
            moveTaskToBack(false)
            return true
        }
        return super.onKeyDown( keyCode,event)
    }
    private lateinit var database: MusicDatabase
    private lateinit var binding: ActivityHomeBinding
    private  var homePageSectionsAdapter: HomePageSectionsAdapter?= null
    private  var newReleaseAlbumsAdapter: NewReleaseAlbumsAdapter?= null
    private  var moodAndGenresAdapter: MoodAndGenresAdapter?= null
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun loadHomeData(reModel: InitBanner?) {
        LogUtils.e("BannerEvs------")
        if (reModel == null) {
            return
        }
        if (isFinishing || isDestroyed) {
            return
        }
        if (reModel.bannerType && !isLoaded) {
            initHomeBan()
        }
    }
    private fun initHomeBan() {
        val mainAds: NativeAd? = SnapMobManager.homeStudy.showWeaveNatives(
            homeBanner(),this)
        if (mainAds == null) {
            if (SnapPosManager.cyIsShowingFLag) {
                LogUtils.e("main banner inter is showing return------")
                return
            }
            if (isFinishing || isDestroyed) {
                LogUtils.e("main banner destroy  return------")
                return
            }
            if (SnapPosManager.posIsCanShow(homeBanner())) {
                LogUtils.e("main banner limit  return------")
                return
            }
            SnapMobManager.homeStudy.initLoadBanners(homeBanner(),object :
                NativeWeavesListener {
                override fun nativeIsWeavesSuc(sucFlag: Boolean) {
                    if (isFinishing || isDestroyed) {
                        return
                    }
                    if (sucFlag) {
                        LogUtils.e("main banner load  suc------")
                        initHomeBan()
                    }
                }
            })
        } else {
            isLoaded = true
            LogUtils.e("main banner show  suc------")
            binding?.homeBans?.let {
                it.visibility = View.VISIBLE
                it.setStyles(MobToStyle.Builder().build())
                it.setNativeAd(mainAds)
                if(!SnapPosManager.posIsCanShow(homeBanner())){
                    val cts = MMKV.defaultMMKV().getInt(MUSIC_CONSTANT_APP_REFRESH, 0)
                    if (cts != 0){
                        messages.removeMessages(MESSAGE_MSG_CODE)
                        messages.sendEmptyMessageDelayed(MESSAGE_MSG_CODE,(cts * 1000).toLong())
                    }
                }
            }
        }
    }

    private val messages: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg?.what) {
                MESSAGE_MSG_CODE -> {
                    initHomeBan()
                }
            }
        }
    }
    private var isLoaded = false
    override fun onPause() {
        super.onPause()
        if (messages?.hasMessages(MESSAGE_MSG_CODE) == true){
            messages?.removeMessages(MESSAGE_MSG_CODE)
        }
//        if (binding?.music?.isAnimating){
//            binding?.music.pauseAnimation()
//        }
    }
    override fun onStop() {
        unbindService(serviceConnection)
        super.onStop()
    }
    override fun onResume() {
        super.onResume()
        try {
            if (playerConnection != null && playerConnection?.player?.currentMetadata != null){
                playerConnection?.mediaMetadata?.let {
                    updatePlayerUI(it.value)
                }
            }
            if (!MMKV.defaultMMKV().getBoolean("showSuc",false)
                && (System.currentTimeMillis() - getInstallSys() > 12 * 60 * 60 * 1000)  && showed) {
                starRate()
            }
            loadBaseInfos(this,true)
            initHomeBan()
        }catch (e : Exception){
            e.printStackTrace()
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        if (dataStore.get(StopMusicOnTaskClearKey,false)&& playerConnection?.isPlaying?.value == true && isFinishing){
            stopService(Intent(this,MusicService::class.java))
            serviceConnection?.let {
                unbindService(it)
            }
            playerConnection = null
        }
        isLoaded = false
        if (EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().unregister(this)
        }
        EventBus.getDefault().removeAllStickyEvents()
        messages?.removeCallbacksAndMessages(null)
    }
    private fun updatePlayerUI(metadata: MediaMetadata?) {
        addUIPlayer(binding.bottomPlayer.artist,binding.bottomPlayer.title,binding.bottomPlayer.thumbnail,metadata)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        database =  InternalDatabase.newInstance(this)
        val homeViewModel = HomeViewModel(this, database)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Initialize adapters
        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this)
        }
        binding.appbar.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout?, state: State?) {
                state ?: return
                when (state) {
                    State.COLLAPSED -> {
//                        binding.collapsing.setBackgroundResource(R.mipmap.bg3)
                        binding.searchEditText.visibility = View.VISIBLE
                        binding.a.visibility = View.GONE
                        binding.searchEditText2.visibility = View.GONE
//                        binding.tools.fitsSystemWindows = true

//                        binding.searchEditText2.visibility = View.VISIBLE
                    }
                    State.EXPANDED -> {
//                        binding.appbar.setBackgroundResource(R.mipmap.bg2)
                        binding.searchEditText.visibility = View.GONE
                        binding.a.visibility = View.VISIBLE
                        binding.searchEditText2.visibility = View.VISIBLE
//                        binding.tools.fitsSystemWindows = false
//                        binding.searchEditText2.visibility = View.VISIBLE
                    }

                    State.IDLE -> {}

                }
            }


        })


        binding.specAnm.post {
            binding.specAnm.visibility = View.VISIBLE
            binding.specAnm.startShimmerAnimation()
        }
        homePageSectionsAdapter = HomePageSectionsAdapter(this)
        newReleaseAlbumsAdapter = NewReleaseAlbumsAdapter(this@HomeActivity)
        moodAndGenresAdapter = MoodAndGenresAdapter(this)

        // Set up RecyclerViews
        binding.homePageSectionsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.homePageSectionsRecyclerView.adapter = homePageSectionsAdapter

        binding.newReleaseAlbumsRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false)
        binding.newReleaseAlbumsRecyclerView.adapter = newReleaseAlbumsAdapter

        binding.moodAndGenresRecyclerView.layoutManager = GridLayoutManager(this, 2)
//        binding.moodAndGenresRecyclerView?.addItemDecoration(SpaceItemDecoration( SizeUtils.dp2px( 10f)))
        binding.moodAndGenresRecyclerView?.addItemDecoration(EvenItemDecoration(SizeUtils.dp2px( 10f),2))
        binding.moodAndGenresRecyclerView.adapter = moodAndGenresAdapter
//        binding.moodAndGenresRecyclerView.isNestedScrollingEnabled = false
//        binding.newReleaseAlbumsRecyclerView.isNestedScrollingEnabled = false
//        binding.homePageSectionsRecyclerView.isNestedScrollingEnabled = false
        binding.searchEditText.setOnClickListener {
            startSearchPage()
            logClickEvents("home_to_search")
        }
        binding.searchEditText2.setOnClickListener {
            startSearchPage()
            logClickEvents("home_to_search")
        }
        binding.listButton.setOnClickListener {
            startQueuePage()
            logClickEvents("home_to_queue")

        }
        binding.history.setOnClickListener {
            logClickEvents("home_to_history")

            startInterActivity(Intent(this, HistoryActivity::class.java), historyInter(),false)
        }
        binding.fav.setOnClickListener {
            logClickEvents("home_to_collect")

            startInterActivity(Intent(this, CollectActivity::class.java), collectInter(),false)

        }
        binding.bottomPlayer.container.setOnClickListener {
            logClickEvents("home_to_player")
            startInterActivity(Intent(this, MainPlayer::class.java), playViewsInter(),false)
        }
//        binding.bottomPlayer.nextButton.setOnClickListener {
//            playerConnection?.player?.seekToNext()
//
//        }
        binding.emoScan.setOnClickListener {
            logClickEvents("home_refresh_btn")
            binding.specAnm.post {
                binding.specAnm.visibility = View.VISIBLE
                binding.specAnm.startShimmerAnimation()
            }
            homeViewModel?.refresh()
        }
        binding.newReleaseTitle.setOnClickListener {
            logClickEvents("home_to_newRelease")

            startInterActivity(Intent(this, NewReleaseDetailsActivity::class.java), releaseDetailInter(),false)
        }

//        binding.scroll.post {
//            binding.moodAndGenresRecyclerView?.parent?.apply {
//
//            }
//        }
        binding.endBtn.setOnClickListener {
            logClickEvents("home_to_about")

            startInterActivity(Intent(this, AboutView::class.java), 0,false)

        }
//        binding.bottomPlayer.playPauseButton.setOnClickListener {
//            if (playerConnection?.isPlaying?.value == true){
//                    binding.bottomPlayer.playPauseButton.setImageResource(R.drawable.play)
//            }else{
//                binding.bottomPlayer.playPauseButton.setImageResource(R.drawable.pause)
//            }
//            if (playerConnection?.player?.playbackState == Player.STATE_ENDED) {
//                playerConnection?.player?.seekTo(0, 0)
//                playerConnection?.player?.playWhenReady = true
//            } else {
//                playerConnection?.player?.togglePlayPause()
//            }
//        }

        lifecycleScope.launch {
//            loadHomePageFromMMKV()
            homeViewModel?.homePage?.collect {
                if (isFinishing || isDestroyed){
                    return@collect
                }
                binding.specAnm.visibility = View.GONE
                binding.specAnm.stopShimmerAnimation()
                if (binding.emptyLayout.isVisible){
                    binding.emptyLayout.visibility = View.GONE
                }
                Timber.e(it.toString())
                if (it == null) {
                    return@collect
                }
                // Set data to adapters
//                    withContext(Dispatchers.Main){
                it?.sections?.apply {
                    homePageSectionsAdapter?.submitList(this)
                }



                homeViewModel?.explorePage?.collect {
                    try {
                        if (isFinishing || isDestroyed){
                            return@collect
                        }
                        binding.specAnm.visibility = View.GONE
                        binding.specAnm.stopShimmerAnimation()
                        binding.newReleaseTitle.visibility = View.VISIBLE
                        binding.moodTitle.visibility = View.VISIBLE
                        binding.line1.visibility = View.VISIBLE
                        it?.newReleaseAlbums?.apply {
                            newReleaseAlbumsAdapter?.submitList(this)
                        }
                        it?.moodAndGenres?.apply {
                            moodAndGenresAdapter?.submitList(this)
                        }
                        Timber.e(it.toString())

                    }catch (e : Exception){
                        e.printStackTrace()
                    }


                }


            }
        }
//

        lifecycleScope.launch {
            try {
                homeViewModel?.isError?.collect {
                    binding.specAnm.visibility = View.GONE
                    binding.specAnm.stopShimmerAnimation()
                    if (isFinishing || isDestroyed){
                        return@collect
                    }
                    if (it){
                        binding.emptyLayout.visibility = View.VISIBLE
                    }
                }
            }catch (e : Exception){
                e.printStackTrace()
            }
        }


    }
    private fun starRate() {
        try {
            val dialog =   showDialogs(this, R.layout.dialog_rate,false, Gravity.CENTER)
            dialog.show()
            val ratingBar = dialog.findViewById<RatingBar>(R.id.rating_bar)
            ratingBar?.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                ThreadUtils.runOnUiThreadDelayed({
                    dialog.dismiss()
                    MMKV.defaultMMKV().putBoolean("showSuc",true)
                    if (rating == 5f) {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.data =
                                Uri.parse("market://details?id=" + AppUtils.getAppPackageName()) //跳转到应用市场，非Google Play市场一般情况也实现了这个接口
                            //存在手机里没安装应用市场的情况，跳转会包异常，做一个接收判断
                            if (intent.resolveActivity(packageManager) != null) { //可以接收
                                startActivity(intent)
                            } else { //没有应用市场，我们通过浏览器跳转到Google Play
                                intent.data =
                                    Uri.parse("https://play.google.com/store/apps/details?id=" + AppUtils.getAppPackageName())
                                //这里存在一个极端情况就是有些用户浏览器也没有，再判断一次
                                if (intent.resolveActivity(packageManager) == null) { //有浏览器
                                    startActivity(intent)
                                } else { //
                                    ToastUtils.showShort(
                                        "You don't have an app market installed, not even a browser!",
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },1000)

            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



}
