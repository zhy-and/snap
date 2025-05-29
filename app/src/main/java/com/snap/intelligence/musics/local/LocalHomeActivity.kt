package com.snap.intelligence.musics.local

import com.snap.intelligence.musics.local.frag.AlbumsFragment
import com.snap.intelligence.musics.local.frag.ArtistsFragment
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.StyleSpan
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_MUSIC
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ThreadUtils
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayoutMediator
import com.tencent.mmkv.MMKV
import com.snap.intelligence.musics.BasicView
import com.snap.intelligence.musics.R
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_CURRENT_PAGE
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_IS_M_USER
import com.snap.intelligence.musics.constants.StopMusicOnTaskClearKey
import com.snap.intelligence.musics.constants.queueInter
import com.snap.intelligence.musics.databinding.ActivityLocalIndexBinding
import com.snap.intelligence.musics.db.InternalDatabase
import com.snap.intelligence.musics.db.MusicDatabase
import com.snap.intelligence.musics.extensions.currentMetadata
import com.snap.intelligence.musics.local.DataKeeper.loadSongs
import com.snap.intelligence.musics.local.frag.CollectFragment
import com.snap.intelligence.musics.local.frag.LocalSongsFragment
import com.snap.intelligence.musics.models.MediaMetadata
import com.snap.intelligence.musics.playback.MusicService
import com.snap.intelligence.musics.playback.MusicService.MusicBinder
import com.snap.intelligence.musics.playback.PlayerConnection
import com.snap.intelligence.musics.ui.about.AboutView
import com.snap.intelligence.musics.ui.queue.QueueActivity
import com.snap.intelligence.musics.ui.start.HomeActivity
import com.snap.intelligence.musics.utils.dataStore
import com.snap.intelligence.musics.utils.get
import com.snap.intelligence.musics.work.SnapBasicInterfaces
import kotlinx.coroutines.launch

class LocalHomeActivity : BasicView() {
    override fun onStart() {
        super.onStart()
        startService(Intent(this@LocalHomeActivity, MusicService::class.java))
        bindService(Intent(this@LocalHomeActivity, MusicService::class.java), serviceConnection, BIND_AUTO_CREATE)
    }
    companion object {
        var playerConnection by mutableStateOf<PlayerConnection?>(null)
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


    private  var home: LocalSongsFragment?= null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is MusicBinder) {
                playerConnection = PlayerConnection(this@LocalHomeActivity, service, database, lifecycleScope)
                lifecycleScope.launch {
                    playerConnection?.mediaMetadata?.collect{
                        if (MMKV.defaultMMKV().getBoolean("hasPer",false)){
                            updatePlayerUI(it)
                        }
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
                if (isReload){
                    updateUI()
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
            moveTaskToBack(false)
            return true
        }
        return super.onKeyDown( keyCode,event)
    }
    private lateinit var database: MusicDatabase
    private lateinit var binding: ActivityLocalIndexBinding
    override fun onDestroy() {
        super.onDestroy()
        if (dataStore.get(StopMusicOnTaskClearKey, false) && playerConnection?.isPlaying?.value == true && isFinishing) {
            stopService(Intent(this, MusicService::class.java))
            unbindService(serviceConnection)
            playerConnection = null
        }
    }
    override fun onStop() {
        unbindService(serviceConnection)
        super.onStop()
    }
    override fun onResume() {
        super.onResume()
        try {
            if (playerConnection != null && playerConnection?.player?.currentMetadata != null && MMKV.defaultMMKV().getBoolean("hasPer",false)){
                playerConnection?.mediaMetadata?.let {
                    updatePlayerUI(it.value)
                }
            }
            //
            if (MMKV.defaultMMKV().getBoolean(MUSIC_CONSTANT_IS_M_USER,false)){
                binding.progressBar.visibility = View.VISIBLE
                ThreadUtils.runOnUiThreadDelayed({
                    try {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }catch (e : Exception){
                        e.printStackTrace()
                    }

                },2000)
            }
            SnapBasicInterfaces.pageAEventsUpload()
        }catch (e : Exception){
            e.printStackTrace()
        }

    }

    private fun updatePlayerUI(metadata: MediaMetadata?) {
        if (!TextUtils.isEmpty(metadata?.title)){
            binding.bottomPlayer.title.text = metadata?.title
        }
        if (!TextUtils.isEmpty(metadata?.artists?.joinToString(", ") { it.name })){
            binding.bottomPlayer.artist.text = metadata?.artists?.joinToString(", ") { it.name }
        }
        metadata?.thumbnailUrl?.let { thumbnailUrl ->
            Glide.with(binding.root.context).asBitmap().load(thumbnailUrl).into(binding.bottomPlayer.thumbnail)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocalIndexBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MMKV.defaultMMKV().putString(MUSIC_CONSTANT_CURRENT_PAGE,"local")
        database =  InternalDatabase.newInstance(this)
        setupViewPager()
        setupTabLayout()
        // Initialize adapters
        // Set up RecyclerViews
        binding.bottomPlayer.container.setOnClickListener {
            if (!MMKV.defaultMMKV().getBoolean("hasPer",false)){
                home?.outClick()
                return@setOnClickListener
            }
            startActivity(Intent(this, LocalMusicPlayerActivity::class.java))
        }
//        binding.bottomPlayer.nextButton.setOnClickListener {
//            playerConnection?.player?.seekToNext()
//        }
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
    }


    private fun setupViewPager() {
        val adapter = ViewPagerAdapter(this)
         home = LocalSongsFragment()
        home?.let {
            adapter.addFragment(it, "Playlist")
        }
        adapter.addFragment(ArtistsFragment(), "Artist")
        adapter.addFragment(AlbumsFragment(), "Album")
        adapter.addFragment(CollectFragment(), "Favorite")
        binding.viewPager.setAdapter(adapter)
    }
    var sizes = 4
    private fun setupTabLayout() {
        TabLayoutMediator(
            binding.tabLayout, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.setText((binding.viewPager.getAdapter() as ViewPagerAdapter).getPageTitle(position))
            handlerTabLayoutBold(tab,position == 0)
        }.attach()
        initFirstTab()
//        binding.viewPager.registerOnPageChangeCallback(object  : ViewPager2.OnPageChangeCallback() {
//            override fun onPageSelected(position: Int) {
//                super.onPageSelected(position)
//                Log.e("pos","----"+position)
//                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position))
////                for (i in 0..3){
////                    if (i == position){
////                        handlerTabLayoutBold( ,true)
////                    }else{
////                        handlerTabLayoutBold( binding.tabLayout.getTabAt(i),false)
////                    }
////
////                }
//
//            }
//
//
//        })
        binding.lists.setOnClickListener {
            if (!MMKV.defaultMMKV().getBoolean("hasPer",false)){
                home?.outClick()
                return@setOnClickListener
            }
            startInterActivity(Intent(this, LocalPlayListActivity::class.java), queueInter(),false)
        }
        binding.closeButton.setOnClickListener {
            startActivity(Intent(this, AboutView::class.java))
        }
        handlerTabLayoutBold(binding.tabLayout)

        // 设置可滚动Tab
//        binding.tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE)
    }

    private fun initFirstTab() {
        val tab: TabLayout.Tab = binding?.tabLayout?.getTabAt(0) ?: return
        val selectTab = tab.text.toString().trim { it <= ' ' }

        val spannableString = SpannableString(selectTab)
        val styleSpan = StyleSpan(Typeface.BOLD)
        spannableString.setSpan(styleSpan, 0, selectTab.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)

        tab.setText(spannableString)
    }

    var isReload = false
    fun updateUI() {
        try {
            if (playerConnection == null){
                isReload = true
                return
            }
            val data =  loadSongs(Song())
            if (data.isNotEmpty()){
                addLocalMusic(data)
            }
            playerConnection?.mediaMetadata?.let {
                updatePlayerUI(it.value)
            }
        }catch (e : Exception){
            e.printStackTrace()
        }

    }
    /** 动态处理选中状态为加粗 否则不加粗*/
    fun handlerTabLayoutBold(tab: TabLayout) {

        tab.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab?.text == null) {
                    return
                }

                val selectTab = tab.text.toString().trim { it <= ' ' }

                val spannableString: SpannableString = SpannableString(selectTab)
                val styleSpan: StyleSpan = StyleSpan(Typeface.BOLD)
                spannableString.setSpan(
                    styleSpan,
                    0,
                    selectTab.length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )

                tab.setText(spannableString)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                if (tab?.text == null) {
                    return
                }

                val selectTab = tab.text.toString().trim { it <= ' ' }

                val spannableString: SpannableString = SpannableString(selectTab)
                val styleSpan: StyleSpan = StyleSpan(Typeface.NORMAL)
                spannableString.setSpan(
                    styleSpan,
                    0,
                    selectTab.length,
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE
                )

                tab.setText(spannableString)
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })


    }

    /** 修改单独某一个的粗细*/
    fun handlerTabLayoutBold(tab: TabLayout.Tab?, isBold: Boolean) {
        tab?.view?.forEach { view ->
            if (view is TextView) {
                if (isBold) {
                    view.typeface = Typeface.DEFAULT_BOLD
                    view.textSize = SizeUtils.sp2px(18f).toFloat()
                } else {
                    view.typeface = Typeface.DEFAULT
                    view.textSize = SizeUtils.sp2px(16f).toFloat()
                }
            }
        }
    }
    fun animation() {
        try {
            val dialog =  showDialogs(this,R.layout.dialog_anis)
            binding?.root?.postDelayed({
                if (dialog.isShowing && !isFinishing && !isDestroyed){
                    dialog.dismiss()
                }
            },2000)
        }catch (e : Exception){
            e.printStackTrace()
        }

    }

    fun denPermission() {
        startActivity(Intent(this, LocalMusicPlayerActivity::class.java))
    }

    inner  class ViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        private val fragments: MutableList<Fragment> = ArrayList<Fragment>()
        private val titles: MutableList<String> = ArrayList()

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        fun getPageTitle(position: Int): String {
            return titles[position]
        }

        override fun getItemCount(): Int {
            return fragments.size
        }

        override fun createFragment(position: Int): Fragment {
            return fragments[position]
        }
    }

}
