package com.snap.intelligence.musics.ui.search


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zionhuang.innertube.MainBe
import com.zionhuang.innertube.MainBe.SearchFilter.Companion.FILTER_ALBUM
import com.zionhuang.innertube.MainBe.SearchFilter.Companion.FILTER_ARTIST
import com.zionhuang.innertube.MainBe.SearchFilter.Companion.FILTER_SONG
import com.zionhuang.innertube.models.AlbumItem
import com.zionhuang.innertube.models.ArtistItem
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.models.filterExplicit
import com.snap.intelligence.musics.ui.start.HomeActivity.Companion.playerConnection
import com.snap.intelligence.musics.constants.HideExplicitKey
import com.snap.intelligence.musics.databinding.IndiaBinding
import com.snap.intelligence.musics.db.InternalDatabase
import com.snap.intelligence.musics.db.MusicDatabase
import com.snap.intelligence.musics.db.entities.SearchHistory
import com.snap.intelligence.musics.models.ItemsPage
import com.snap.intelligence.musics.models.toMediaMetadata
import com.snap.intelligence.musics.playback.queues.YouTubeQueue.Companion.radio
import com.snap.intelligence.musics.utils.dataStore
import com.snap.intelligence.musics.utils.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.graphics.toColorInt
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.blankj.utilcode.util.LogUtils
import com.google.android.gms.ads.nativead.NativeAd
import com.snap.intelligence.musics.constants.albumDetInter
import com.snap.intelligence.musics.constants.artistDetInter
import com.snap.intelligence.musics.ui.album.AlbumAdapter
import com.snap.intelligence.musics.ui.album.AlbumDetActivity
import com.snap.intelligence.musics.ui.artist.ArtistAdapter
import com.snap.intelligence.musics.ui.artist.ArtistDetActivity
import com.snap.intelligence.musics.BasicView
import com.snap.intelligence.musics.R
import com.snap.intelligence.musics.constants.SnapPosManager
import com.snap.intelligence.musics.constants.MESSAGE_MSG_CODE
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_APP_REFRESH
import com.snap.intelligence.musics.constants.NativeWeavesListener
import com.snap.intelligence.musics.constants.searchInter
import com.snap.intelligence.musics.constants.searchResultBanner
import com.snap.intelligence.musics.extensions.logClickEvents
import com.snap.intelligence.musics.ui.search.adapter.SongAdapter
import com.snap.intelligence.musics.ui.search.adapter.VideoSongAdapter
import com.snap.intelligence.musics.flow.MaxLineLinearLayoutManager
import com.snap.intelligence.musics.mob.MobToStyle
import com.snap.intelligence.musics.mob.SnapMobManager
import com.tencent.mmkv.MMKV
import com.zionhuang.innertube.MainBe.SearchFilter.Companion.FILTER_VIDEO


class MusicSearchActivity : BasicView() {
    private var lastVisibleItem: Int = 0
    private  var newHistoryList: List<SearchHistory> ?= null
    private var data: MusicDatabase?= null
    private  var songAdapter: SongAdapter?= null
    private  var albumAdapter: AlbumAdapter?= null
    private  var artistAdapter: ArtistAdapter?= null
    private  var songUniqueAdapter: SongAdapter?= null
    private  var albumUniqueAdapter: AlbumAdapter?= null
    private  var artistUniqueAdapter: ArtistAdapter?= null
    private  var videoUniqueAdapter: VideoSongAdapter?= null
    var isLoadingNextPage = false;
    var isLoadingAlbumNextPage = false;
    var isLoadingArtistNextPage = false;
    var isLoadingPlayNextPage = false;
    private  var playAdapter: SongAdapter?= null
    private val youTube = MainBe
    var key = FILTER_SONG.value
    var currentType = 0
    var currentSearchContent = ""
    val viewStateMap = mutableStateMapOf<String, ItemsPage?>()


    override fun onDestroy() {
        super.onDestroy()
        handler?.removeCallbacksAndMessages(null)
        
    }
    private lateinit var binding: IndiaBinding
    private fun setFlowRecy(){


    }
    fun emptyLayout(){
        binding.apply {
            albumsContainer.visibility = View.GONE
            emptyResult.visibility = View.VISIBLE
//            if(emptyResult.isVisible){
//                emptyResult.visibility = View.GONE
                emIv.setImageResource(R.mipmap.empty_mus)
                emTx.text = "No result"
                emoScan.visibility = View.GONE
//            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SnapMobManager.homeStudy.getShowCyAds(searchInter(),this)

        enableEdgeToEdge()
        binding = IndiaBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        InternalDatabase.newInstance(this@MusicSearchActivity).searchHistory().map { history ->


        lifecycleScope.launch {
            data =  InternalDatabase.newInstance(this@MusicSearchActivity)

        }
        binding.closeButton.setOnClickListener {
            finishCurrent()
        }
        val content =  intent?.getStringExtra("search_txt") ?: ""
        if (!TextUtils.isEmpty(content)){
            binding.searchEditText.setText(content)
            binding.search.post {
                logClickEvents("search_page_search")
                binding.search.performClick()
            }
        }
        binding.songMore.setOnClickListener {
            binding.song.performClick()
        }
        binding.artistMore.setOnClickListener {
            binding.artist.performClick()
        }
        binding.albumMore.setOnClickListener {
            binding.albums.performClick()
        }
        binding.playListMore.setOnClickListener {
            binding.video.performClick()
        }
        binding.searchEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {

                // 处理确认事件
                binding.search.performClick()
                logClickEvents("search_page_search")
                
                // 隐藏软键盘
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                true // 消费事件
            } else {
                false
            }
        }
        binding.list?.apply {
            layoutManager =  MaxLineLinearLayoutManager(this@MusicSearchActivity,2)
            songAdapter = SongAdapter() { songItem ->
                playerConnection?.playQueue(radio(songItem.toMediaMetadata()))
                startPlayerPage()

            }
            adapter = songAdapter
        }

        binding.albumList?.layoutManager =  MaxLineLinearLayoutManager(this,2)
        albumAdapter = AlbumAdapter(emptyList()) { songItem ->
            startInterActivity(Intent(this@MusicSearchActivity, AlbumDetActivity::class.java).apply {
                putExtra("albumId",songItem.id)
            }, albumDetInter(),false)
        }


        binding.albumList?.adapter = albumAdapter


        binding.playList?.layoutManager = MaxLineLinearLayoutManager(this,2)
        playAdapter =  SongAdapter() { songItem ->
            playerConnection?.playQueue(radio(songItem.toMediaMetadata()))
            startPlayerPage()

        }
        binding.playList?.adapter = playAdapter


        binding.artistList?.layoutManager = MaxLineLinearLayoutManager(this,2)
        artistAdapter = ArtistAdapter(emptyList()) { songItem ->
            startInterActivity(Intent(this@MusicSearchActivity, ArtistDetActivity::class.java).apply {
                putExtra("artistId",songItem.id)
            }, artistDetInter(),false)
        }
        binding.artistList?.adapter = artistAdapter

        binding.all.setOnClickListener {
            currentType = 0
            setVisible(binding.allContainer,binding.songContainer, binding.artistContainer, binding.albumsContainer,binding.videoContainer)
            if (TextUtils.isEmpty(binding.searchEditText.text.toString())){
                return@setOnClickListener
            }
            logClickEvents("search_page_search")
            binding.search.performClick()
        }

        binding.uniqueSongList?.apply {
            songUniqueAdapter = SongAdapter() { songItem ->
                playerConnection?.playQueue(radio(songItem.toMediaMetadata()))
                startPlayerPage()

            }

            layoutManager = LinearLayoutManager(this@MusicSearchActivity)
            adapter = songUniqueAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager
                    if (layoutManager is LinearLayoutManager) {
                         lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                    }
//                        Log.e("count","----"+totalItemCount)
//
//
//                        Log.e("lastVisibleItem","----ff"+lastVisibleItem)
//
//                        if (lastVisibleItem == totalItemCount - 1 && !isLoadingNextPage ) {
//                            if (totalItemCount > 0) {
//                                // 在 recyclerView 滚动时向列表中添加item 并调用 notifyItemInserted() 方法更新时，
//                                // 系统会给出 warning: 可能会影响RecyclerView滑动时的高度等测量
//                                // 所以将这次更新 UI 的操作，延迟到下一帧绘制。
//                                binding.uniqueSongList?.post{
//                                    songUniqueAdapter?.addLoadItem()
//                                }
//                            }
//                            // 在前面addLoadItem后，itemCount已经变化
//                            // 增加一层判断，确保用户是滑到了正在加载的地方，才加载更多
//                            val findLastVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition()
//                            if (findLastVisibleItemPosition == layoutManager.itemCount - 1) {
//                                isLoadingNextPage = true
//                                lifecycleScope.launch {
//                                    withContext(Dispatchers.IO){
//
//                                        val viewState = viewStateMap[key] ?: return@withContext
//                                        val continuation = viewState.continuation
//                                        if (continuation != null) {
//                                            val searchResult = YouTube.searchContinuation(continuation).getOrNull() ?: return@withContext
//                                            viewStateMap[key] = ItemsPage((viewState.items + searchResult.items).distinctBy { it.id }, searchResult.continuation)
//                                            viewStateMap[key]?.items?.filterIsInstance<SongItem>()?.let {
//                                                withContext(Dispatchers.Main){
//                                                    songUniqueAdapter?.removeLoadItem()
//                                                    songUniqueAdapter?.addData(it as ArrayList<SongItem>)
//                                                }
//                                                isLoadingNextPage = false
////                                                                            binding.progress.visibility = View.GONE
//                                            }
//                                            Log.e("viewStateMap2",viewStateMap[key].toString())
//                                        }else{
//                                            withContext(Dispatchers.Main){
//                                                songUniqueAdapter?.removeLoadItem()
//                                            }
//                                        }
//                                    }
//
//                                }}
//                        }
//
//                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
//                    if (newState == RecyclerView.SCROLL_STATE_IDLE && isBottom(binding.uniqueSongList) && !isLoadingNextPage) { //当前状态为停止滑动
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && lastVisibleItem + 1 >= songUniqueAdapter?.songs?.size!! && !isLoadingNextPage) { //当前状态为停止滑动
                        binding.uniqueSongList.post {
                            songUniqueAdapter?.addLoadItem()
                        }
                            lifecycleScope.launch {
                                withContext(Dispatchers.IO){
                                    val viewState = viewStateMap[key] ?: return@withContext
                                    val continuation = viewState.continuation
                                    if (continuation != null) {
                                        isLoadingNextPage = true
                                        val searchResult = MainBe.searchContinuation(continuation).getOrNull() ?: return@withContext
                                        viewStateMap[key] = ItemsPage((viewState.items + searchResult.items).distinctBy { it.id }, searchResult.continuation)
                                        viewStateMap[key]?.items?.filterIsInstance<SongItem>()?.let {
                                            withContext(Dispatchers.Main){
                                                songUniqueAdapter?.removeLoadItem()
                                                songUniqueAdapter?.addData(it as ArrayList<SongItem>)
                                            }
                                            isLoadingNextPage = false
//                                                                            binding.progress.visibility = View.GONE
                                        }
//                                        Log.e("viewStateMap2",viewStateMap[key].toString())
                                    }else{
                                        withContext(Dispatchers.Main){
                                            songUniqueAdapter?.removeLoadItem()
                                        }
                                    }
                                }

                            }
                        }
                    }

            })

        }

        binding.uniqueAlbumList?.apply {
            albumUniqueAdapter = AlbumAdapter(emptyList()) { songItem ->
                startInterActivity(Intent(this@MusicSearchActivity, ArtistDetActivity::class.java).apply {
                    putExtra("artistId",songItem.id)
                }, artistDetInter(),false)
            }

            layoutManager = LinearLayoutManager(this@MusicSearchActivity)
            adapter = albumUniqueAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE && isBottom(binding.uniqueAlbumList) && !isLoadingAlbumNextPage) { //当前状态为停止滑动
                        lifecycleScope.launch {
                            withContext(Dispatchers.IO){
                                val viewState = viewStateMap[key] ?: return@withContext
                                val continuation = viewState.continuation
                                if (continuation != null) {
                                    isLoadingAlbumNextPage = true
                                    val searchResult = MainBe.searchContinuation(continuation).getOrNull() ?: return@withContext
                                    viewStateMap[key] = ItemsPage((viewState.items + searchResult.items).distinctBy { it.id }, searchResult.continuation)
                                    viewStateMap[key]?.items?.filterIsInstance<AlbumItem>()?.let {
                                        withContext(Dispatchers.Main){
                                            albumUniqueAdapter?.addData(it)
                                        }
                                        isLoadingAlbumNextPage = false
                                    }
//                                    Log.e("album2",viewStateMap[key].toString())
                                }
                            }

                        }
                    }
                    }
//                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                    super.onScrolled(recyclerView, dx, dy)
////                    val layoutManager = recyclerView.layoutManager
////                    if (layoutManager is LinearLayoutManager) {
////                        val totalItemCount = layoutManager.itemCount
////                        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
////                        if (lastVisibleItem == totalItemCount - 1 && !isLoadingAlbumNextPage ) {
////                            if (totalItemCount > 0) {
////                                // 在 recyclerView 滚动时向列表中添加item 并调用 notifyItemInserted() 方法更新时，
////                                // 系统会给出 warning: 可能会影响RecyclerView滑动时的高度等测量
////                                // 所以将这次更新 UI 的操作，延迟到下一帧绘制。
//////                            recyclerView.post {
//////                                mAdapter.addLoadItem()
//////                            }
////                            }
////                            // 在前面addLoadItem后，itemCount已经变化
////                            // 增加一层判断，确保用户是滑到了正在加载的地方，才加载更多
////                            val findLastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
////                            if (findLastVisibleItemPosition == layoutManager.itemCount - 1) {
////                                isLoadingAlbumNextPage = true
////                                lifecycleScope.launch {
////                                    withContext(Dispatchers.IO){
////                                        val viewState = viewStateMap[key] ?: return@withContext
////                                        val continuation = viewState.continuation
////                                        if (continuation != null) {
////                                            val searchResult = YouTube.searchContinuation(continuation).getOrNull() ?: return@withContext
////                                            viewStateMap[key] = ItemsPage((viewState.items + searchResult.items).distinctBy { it.id }, searchResult.continuation)
////                                            viewStateMap[key]?.items?.filterIsInstance<AlbumItem>()?.let {
////                                                withContext(Dispatchers.Main){
////                                                    albumUniqueAdapter?.addData(it)
////                                                }
////                                                isLoadingAlbumNextPage = false
////                                            }
////                                            Log.e("album2",viewStateMap[key].toString())
////                                        }
////                                    }
////
////                                }}
////                        }
////
////                    }
//                }
            })

            binding.uniqueArtistList?.apply {
                artistUniqueAdapter = ArtistAdapter(emptyList()) { songItem ->
                    startInterActivity(Intent(this@MusicSearchActivity, ArtistDetActivity::class.java).apply {
                        putExtra("artistId",songItem.id)
                    }, artistDetInter(),false)
                }

                layoutManager = LinearLayoutManager(this@MusicSearchActivity)
                adapter = artistUniqueAdapter
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                        super.onScrolled(recyclerView, dx, dy)
//                        val layoutManager = recyclerView.layoutManager
//                        if (layoutManager is LinearLayoutManager) {
//                            val totalItemCount = layoutManager.itemCount
//                            val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
//                            if (lastVisibleItem == totalItemCount - 1 && !isLoadingArtistNextPage ) {
//                                if (totalItemCount > 0) {
//                                    // 在 recyclerView 滚动时向列表中添加item 并调用 notifyItemInserted() 方法更新时，
//                                    // 系统会给出 warning: 可能会影响RecyclerView滑动时的高度等测量
//                                    // 所以将这次更新 UI 的操作，延迟到下一帧绘制。
////                            recyclerView.post {
////                                mAdapter.addLoadItem()
////                            }
//                                }
//                                // 在前面addLoadItem后，itemCount已经变化
//                                // 增加一层判断，确保用户是滑到了正在加载的地方，才加载更多
//                                val findLastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
//                                if (findLastVisibleItemPosition == layoutManager.itemCount - 1) {
//                                    isLoadingArtistNextPage = true
//                                    lifecycleScope.launch {
//                                        withContext(Dispatchers.IO){
//                                            val viewState = viewStateMap[key] ?: return@withContext
//                                            val continuation = viewState.continuation
//                                            if (continuation != null) {
//                                                val searchResult = YouTube.searchContinuation(continuation).getOrNull() ?: return@withContext
//                                                viewStateMap[key] = ItemsPage((viewState.items + searchResult.items).distinctBy { it.id }, searchResult.continuation)
//                                                viewStateMap[key]?.items?.filterIsInstance<ArtistItem>()?.let {
//                                                    withContext(Dispatchers.Main){
//                                                        artistUniqueAdapter?.addData(it)
//                                                    }
//                                                    isLoadingArtistNextPage = false
//                                                }
//                                                Log.e("artist2",viewStateMap[key].toString())
//                                            }
//                                        }
//
//                                    }}
//                            }
//
//                        }
//                    }
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (newState == RecyclerView.SCROLL_STATE_IDLE && isBottom(binding.uniqueArtistList) && !isLoadingArtistNextPage) { //当前状态为停止滑动

                            lifecycleScope.launch {
                                        withContext(Dispatchers.IO){
                                            val viewState = viewStateMap[key] ?: return@withContext
                                            val continuation = viewState.continuation
                                            if (continuation != null) {
                                                isLoadingArtistNextPage = true
                                                val searchResult = MainBe.searchContinuation(continuation).getOrNull() ?: return@withContext
                                                viewStateMap[key] = ItemsPage((viewState.items + searchResult.items).distinctBy { it.id }, searchResult.continuation)
                                                viewStateMap[key]?.items?.filterIsInstance<ArtistItem>()?.let {
                                                    withContext(Dispatchers.Main){
                                                        artistUniqueAdapter?.addData(it)
                                                    }
                                                    isLoadingArtistNextPage = false
                                                }
//                                                Log.e("artist2",viewStateMap[key].toString())
                                            }
                                        }

                                    }
                        }
                    }

                })

            }
            binding.uniqueVideoList?.apply {
                videoUniqueAdapter =  VideoSongAdapter() { songItem ->
                    playerConnection?.playQueue(radio(songItem.toMediaMetadata()))
                    startPlayerPage()

                }

                layoutManager = LinearLayoutManager(this@MusicSearchActivity)
                adapter = videoUniqueAdapter
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                        super.onScrolled(recyclerView, dx, dy)
//                        val layoutManager = recyclerView.layoutManager
//                        if (layoutManager is LinearLayoutManager) {
//                            val totalItemCount = layoutManager.itemCount
//                            val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
//                            if (lastVisibleItem == totalItemCount - 1 && !isLoadingPlayNextPage ) {
//                                if (totalItemCount > 0) {
//                                    // 在 recyclerView 滚动时向列表中添加item 并调用 notifyItemInserted() 方法更新时，
//                                    // 系统会给出 warning: 可能会影响RecyclerView滑动时的高度等测量
//                                    // 所以将这次更新 UI 的操作，延迟到下一帧绘制。
////                            recyclerView.post {
////                                mAdapter.addLoadItem()
////                            }
//                                }
//                                // 在前面addLoadItem后，itemCount已经变化
//                                // 增加一层判断，确保用户是滑到了正在加载的地方，才加载更多
//                                val findLastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
//                                if (findLastVisibleItemPosition == layoutManager.itemCount - 1) {
//                                    isLoadingPlayNextPage = true
//                                    lifecycleScope.launch {
//                                        withContext(Dispatchers.IO){
//                                            val viewState = viewStateMap[key] ?: return@withContext
//                                            val continuation = viewState.continuation
//                                            if (continuation != null) {
//                                                val searchResult = YouTube.searchContinuation(continuation).getOrNull() ?: return@withContext
//                                                viewStateMap[key] = ItemsPage((viewState.items + searchResult.items).distinctBy { it.id }, searchResult.continuation)
//                                                viewStateMap[key]?.items?.filterIsInstance<PlaylistItem>()?.let {
//                                                    withContext(Dispatchers.Main){
//                                                        videoUniqueAdapter?.addData(it)
//                                                    }
//                                                    isLoadingPlayNextPage = false
//                                                }
//                                                Log.e("playlist2",viewStateMap[key].toString())
//                                            }
//                                        }
//
//                                    }
//                                }
//                            }
//
//                        }
//                    }
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (newState == RecyclerView.SCROLL_STATE_IDLE && isBottom(binding.uniqueVideoList) && !isLoadingPlayNextPage) { //当前状态为停止滑动
                            lifecycleScope.launch {
                                withContext(Dispatchers.IO){
                                    val viewState = viewStateMap[key] ?: return@withContext
                                    val continuation = viewState.continuation
                                    if (continuation != null) {
                                        isLoadingPlayNextPage = true
                                        val searchResult = MainBe.searchContinuation(continuation).getOrNull() ?: return@withContext
                                        viewStateMap[key] = ItemsPage((viewState.items + searchResult.items).distinctBy { it.id }, searchResult.continuation)
                                        viewStateMap[key]?.items?.filterIsInstance<SongItem>()?.let {
                                            withContext(Dispatchers.Main){
                                                videoUniqueAdapter?.addData(it as ArrayList<SongItem>)
                                            }
                                            isLoadingPlayNextPage = false
                                        }
//                                        Log.e("playlist2",viewStateMap[key].toString())
                                    }
                                }

                            }
                        }
                    }
                })

            }


        }


        binding.song.setOnClickListener {
            currentType = 1
            setVisible(binding.songContainer,binding.allContainer, binding.artistContainer, binding.albumsContainer,binding.videoContainer)
            if (TextUtils.isEmpty(binding.searchEditText.text.toString())){
                return@setOnClickListener
            }
            logClickEvents("search_page_search_songs")
            
            key = FILTER_SONG.value + binding.searchEditText.text.toString()
            lifecycleScope.launch {

                if (viewStateMap[key] == null) {
                    showAnimations(true)

                    withContext(Dispatchers.IO){

                    MainBe.search(binding.searchEditText.text.toString(), FILTER_SONG)
                        .onSuccess { result ->
                            ItemsPage(
                                result.items
                                    .distinctBy { it.id }
                                    .filterExplicit(dataStore.get(HideExplicitKey, false)),
                                result.continuation
                            )?.let {
                                if (it.items.isNullOrEmpty()){
                                    withContext(Dispatchers.Main){
                                        showAnimations(false)
                                        emptyLayout()
                                        songUniqueAdapter?.addData(it.items as ArrayList<SongItem>)
                                    }
                                    return@onSuccess
                                }else{
                                    viewStateMap[key] = it
                                }
                            }
//                            Log.e("viewStateMap1",  viewStateMap[key].toString())
                            viewStateMap[key]?.items?.filterIsInstance<SongItem>()?.let {
                                withContext(Dispatchers.Main){
                                    showAnimations(false)
                                    songUniqueAdapter?.addData(it as ArrayList<SongItem>)
                                }
                            }
                        }
                        .onFailure {
                            withContext(Dispatchers.Main){
                                showAnimations(false)
                                diconnectView()
                            }
//                            reportException(it)
                        }
                }}else{
//                    viewStateMap[key]?.items?.filterIsInstance<SongItem>().isNullOrEmpty().apply {
//                        binding.emptyResult.visibility = View.VISIBLE
//                    }
//                    viewStateMap[key]?.items?.filterIsInstance<SongItem>()?.let {
//                        withContext(Dispatchers.Main){
//                            showAnimations(false)
//                            songUniqueAdapter?.addData(it as ArrayList<SongItem>)
//                        }
//                    }
                }

            }
        }
        binding.albums.setOnClickListener {
            currentType = 2
            setVisible(binding.albumsContainer,binding.allContainer, binding.artistContainer, binding.songContainer,binding.videoContainer)
            if (TextUtils.isEmpty(binding.searchEditText.text.toString())){
                return@setOnClickListener
            }
            logClickEvents("search_page_search_albums")

            key = FILTER_ALBUM.value + binding.searchEditText.text.toString()
            lifecycleScope.launch {

                if (viewStateMap[key] == null) {
                    showAnimations(true)

                    withContext(Dispatchers.IO){

                        MainBe.search(binding.searchEditText.text.toString(), FILTER_ALBUM)
                            .onSuccess { result ->
                                ItemsPage(
                                    result.items
                                        .distinctBy { it.id }
                                        .filterExplicit(dataStore.get(HideExplicitKey, false)),
                                    result.continuation
                                )?.let {
                                    if (it.items.isNullOrEmpty()){
                                        withContext(Dispatchers.Main){
                                            showAnimations(false)
                                            emptyLayout()
                                            albumUniqueAdapter?.addData(it.items as ArrayList<AlbumItem>)
                                        }
                                        return@onSuccess
                                    }else{
                                        viewStateMap[key] = it
                                    }
                                }
//                                Log.e("album1",  viewStateMap[key].toString())
                                viewStateMap[key]?.items?.filterIsInstance<AlbumItem>()?.let {
                                    withContext(Dispatchers.Main){
                                        showAnimations(false)
                                        albumUniqueAdapter?.addData(it)

                                    }
                                }



                            }
                            .onFailure {
                                withContext(Dispatchers.Main){
                                    showAnimations(false)
                                    diconnectView()
                                }
//                                reportException(it)
                            }
                    }
                }else{
//                    viewStateMap[key]?.items?.filterIsInstance<AlbumItem>().isNullOrEmpty().apply {
//                        binding.emptyResult.visibility = View.VISIBLE
//                    }
                }

            }
        }
        binding.artist.setOnClickListener {
            currentType = 3
            setVisible(binding.artistContainer,binding.allContainer, binding.albumsContainer, binding.songContainer,binding.videoContainer)
            if (TextUtils.isEmpty(binding.searchEditText.text.toString())){
                return@setOnClickListener
            }
            logClickEvents("search_page_search_artists")

            key = FILTER_ARTIST.value + binding.searchEditText.text.toString()
            lifecycleScope.launch {
                if (viewStateMap[key] == null) {
                    showAnimations(true)

                    withContext(Dispatchers.IO){

                        MainBe.search(binding.searchEditText.text.toString(), FILTER_ARTIST)
                            .onSuccess { result ->
                                ItemsPage(
                                    result.items
                                        .distinctBy { it.id }
                                        .filterExplicit(dataStore.get(HideExplicitKey, false)),
                                    result.continuation
                                )?.let {
                                    if (it.items.isNullOrEmpty()){
                                        withContext(Dispatchers.Main){
                                            showAnimations(false)
                                            emptyLayout()
                                            artistUniqueAdapter?.addData(it.items as ArrayList<ArtistItem>)
                                        }
                                        return@onSuccess
                                    }else{
                                        viewStateMap[key] = it
                                    }
                                }
//                                Log.e("artist1",  viewStateMap[key].toString())
                                viewStateMap[key]?.items?.filterIsInstance<ArtistItem>()?.let {
                                    withContext(Dispatchers.Main){
                                        showAnimations(false)
                                        artistUniqueAdapter?.addData(it)
                                    }
                                }



                            }
                            .onFailure {
                                withContext(Dispatchers.Main){
                                    showAnimations(false)
                                    diconnectView()
                                }
//                                reportException(it)
                            }
                    }}else{
//                    viewStateMap[key]?.items?.filterIsInstance<ArtistItem>().isNullOrEmpty().apply {
//                        binding.emptyResult.visibility = View.VISIBLE
//                    }
//                    viewStateMap[key]?.items?.filterIsInstance<ArtistItem>()?.let {
//                        withContext(Dispatchers.Main){
//                            showAnimations(false)
//                            artistUniqueAdapter?.addData(it)
//                        }
//                    }
                    }

            }
        }


        binding.video.setOnClickListener {
            currentType = 4
            setVisible(binding.videoContainer,binding.allContainer, binding.albumsContainer, binding.songContainer,binding.artistContainer)
            if (TextUtils.isEmpty(binding.searchEditText.text.toString())){
                return@setOnClickListener
            }
            logClickEvents("search_page_search_videos")

            key = FILTER_VIDEO.value + binding.searchEditText.text.toString()
            lifecycleScope.launch {
                if (viewStateMap[key] == null) {
                    showAnimations(true)

                    withContext(Dispatchers.IO){

                        MainBe.search(binding.searchEditText.text.toString(), FILTER_VIDEO)
                            .onSuccess { result ->
                                ItemsPage(
                                    result.items
                                        .distinctBy { it.id }
                                        .filterExplicit(dataStore.get(HideExplicitKey, false)),
                                    result.continuation
                                )?.let {
                                    if (it.items.isNullOrEmpty()){
                                        withContext(Dispatchers.Main){
                                            showAnimations(false)
                                            emptyLayout()
                                            videoUniqueAdapter?.addData(it.items as ArrayList<SongItem>)
                                        }
                                        return@onSuccess
                                    }else{
                                        viewStateMap[key] = it
                                    }
                                }
//                                Log.e("video1",  viewStateMap[key].toString())
                                viewStateMap[key]?.items?.filterIsInstance<SongItem>()?.let {
                                    withContext(Dispatchers.Main){
                                        showAnimations(false)
                                        videoUniqueAdapter?.addData(it as ArrayList<SongItem>)

                                    }
                                }



                            }
                            .onFailure {
                                withContext(Dispatchers.Main){
                                    showAnimations(false)
                                    diconnectView()
                                }
//                                reportException(it)
                            }
                    }}
                else{
//                    viewStateMap[key]?.items?.filterIsInstance<SongItem>().isNullOrEmpty().apply {
//                        binding.emptyResult.visibility = View.VISIBLE
//                    }
//                    viewStateMap[key]?.items?.filterIsInstance<SongItem>()?.let {
//                        withContext(Dispatchers.Main){
//                            showAnimations(false)
//                            videoUniqueAdapter?.addData(it as ArrayList<SongItem>)
//
//                        }
//                    }
                    }

            }
        }

       binding.search.setOnClickListener {
           var isFirst = false
//           if (!isNetworkOnline1()){
//               diconnectView()
//               return@setOnClickListener
//           }else{
//               connect()
//           }
           var content  = ""
               if (TextUtils.isEmpty(binding.searchEditText.text.toString())){
               Toast.makeText(this, "Please input content", Toast.LENGTH_SHORT).show()
               return@setOnClickListener
           }else{
             content =  binding.searchEditText.text.toString()
               if (content == currentSearchContent){
                   return@setOnClickListener
               }
           }
           logClickEvents("search_page_search")

           insertSearchHistory()

           showAnimations(true)
            viewStateMap.clear()
            //search
           logClickEvents("search_page_search_artists")

          if (currentType != 0){
              when (currentType) {
                  1 ->{
                      binding.song.performClick()
                      return@setOnClickListener
                  }
                  2 ->{
                      binding.albums.performClick()
                      return@setOnClickListener
                  }
                  3 ->{
                      binding.artist.performClick()
                      return@setOnClickListener
                  }
                  4 ->{
                      binding.video.performClick()
                      return@setOnClickListener
                  }
              }
          }

            // 搜索关键词 "musi" 的顶级结果
            lifecycleScope.launch {
                withContext(Dispatchers.IO){
                    for (filter in listOf(
                        FILTER_SONG,
                        FILTER_ALBUM,
                        FILTER_ARTIST,
                        FILTER_VIDEO
                    )) {
                        val searchResult = youTube.search(binding.searchEditText.text.trim().toString(), filter).getOrNull()
                        searchResult ?: return@withContext
                        if(searchResult.items.isNotEmpty()){
                            currentSearchContent = binding.searchEditText.text.toString()

                            withContext(Dispatchers.Main){
                                if (currentType == 0 && !isFirst ){
                                    isFirst = true
                                    setVisible(binding.allContainer,binding.songContainer, binding.artistContainer, binding.albumsContainer,binding.videoContainer)
                                    isShow( binding.songTitle)
                                    isShow( binding.songMore)
                                    isShow( binding.albumTitle)
                                    isShow( binding.albumMore)

                                    isShow( binding.artistTitle)
                                    isShow( binding.artistMore)

                                    isShow( binding.playlistTitle)
                                    isShow( binding.playListMore)

                                    isShow( binding.line1)
                                    isShow( binding.line2)
                                    isShow( binding.line3)
                                }
                                if (filter == FILTER_SONG){
                                    if (searchResult.items.filterIsInstance<SongItem>().isEmpty()){
                                        isGone(binding.songTitle)
                                        isGone(binding.songMore)
                                        isGone(binding.list)
                                    }
                                    if(searchResult.items.filterIsInstance<SongItem>().size >= 2){
                                        songAdapter?.addData(searchResult.items.filterIsInstance<SongItem>().take(2) as ArrayList<SongItem>)
                                    }else{
                                        songAdapter?.addData(searchResult.items.filterIsInstance<SongItem>() as ArrayList<SongItem>)
                                    }

                                }else if(filter == FILTER_ALBUM){
                                   if (searchResult.items.filterIsInstance<AlbumItem>().isEmpty()){
                                       isGone(binding.albumTitle)
                                       isGone(binding.albumList)
                                       isGone(binding.albumMore)

                                   }
                                    if(searchResult.items.filterIsInstance<AlbumItem>().size >= 2){
                                        albumAdapter?.addData(searchResult.items.filterIsInstance<AlbumItem>().take(2) as ArrayList<AlbumItem>)
                                    }else{
                                        albumAdapter?.addData(searchResult.items.filterIsInstance<AlbumItem>() as ArrayList<AlbumItem>)
                                    }
                                }else if(filter == FILTER_VIDEO){
                                    if (searchResult.items.filterIsInstance<SongItem>().isEmpty()){
                                        isGone(binding.playlistTitle)
                                        isGone(binding.playList)
                                        isGone(binding.playListMore)
                                    }
                                    if(searchResult.items.filterIsInstance<SongItem>().size >= 2){
                                        playAdapter?.addData(searchResult.items.filterIsInstance<SongItem>().take(2) as ArrayList<SongItem>)
                                    }else{
                                        playAdapter?.addData(searchResult.items.filterIsInstance<SongItem>() as ArrayList<SongItem>)
                                    }
                                }else if(filter == FILTER_ARTIST){
                                    if (searchResult.items.filterIsInstance<ArtistItem>().isEmpty()){
                                        isGone(binding.artistTitle)
                                        isGone(binding.artistList)
                                        isGone(binding.artistMore)
                                    }
                                    if(searchResult.items.filterIsInstance<ArtistItem>().size >= 2){
                                        artistAdapter?.addData(searchResult.items.filterIsInstance<ArtistItem>().take(2) as ArrayList<ArtistItem>)
                                    }else{
                                        artistAdapter?.addData(searchResult.items.filterIsInstance<ArtistItem>() as ArrayList<ArtistItem>)
                                    }
                                } else {

                                }
                            }
                            }else{
                            withContext(Dispatchers.Main){

                                if (filter == FILTER_SONG){
                                    if (searchResult.items.filterIsInstance<SongItem>().isEmpty()){
//                                    isGone(binding.songTitle)
//                                    isGone(binding.list)
                                    }
                                    songAdapter?.addData(searchResult.items.filterIsInstance<SongItem>() as ArrayList<SongItem>)

                                }else if(filter == FILTER_ALBUM){
                                    if (searchResult.items.filterIsInstance<AlbumItem>().isEmpty()){
//                                    isGone(binding.albumTitle)
//                                    isGone(binding.albumList)
                                    }
                                    albumAdapter?.addData(searchResult.items.filterIsInstance<AlbumItem>())
                                }else if(filter == FILTER_VIDEO){
                                    if (searchResult.items.filterIsInstance<SongItem>().isEmpty()){
//                                    isGone(binding.playlistTitle)
//                                    isGone(binding.playList)
                                    }

                                    playAdapter?.addData(searchResult.items.filterIsInstance<SongItem>()  as ArrayList<SongItem>)
                                }else if(filter == FILTER_ARTIST){
                                    if (searchResult.items.filterIsInstance<ArtistItem>().isEmpty()){
//                                    isGone(binding.artistTitle)
//                                    isGone(binding.artistList)
                                    }
                                    artistAdapter?.addData(searchResult.items.filterIsInstance<ArtistItem>())
                                } else {

                                }

                                showAnimations(false)
                            }

                            }

                    }

                    withContext(Dispatchers.Main){

                        if (currentType == 0){
                            if (binding.allContainer.visibility == View.GONE){
                                binding.allContainer.visibility = View.VISIBLE
                            }
                        }

                       showAnimations(false)
                    }
                }
            }
        }
    }

    private fun diconnectView() {
        binding.apply {
            emptyResult.visibility = View.VISIBLE
            emIv.setImageResource(R.mipmap.on_network)
            emTx.text = "There may be some network problems.\n" +
                    "You can try clicking the button to refresh."
            emoScan.visibility = View.VISIBLE
            emoScan.setOnClickListener {
//                Log.e("refresh","refresh")
                logClickEvents("search_page_refresh_search")
                binding.search.performClick()
            }
            allContainer.visibility = View.GONE
            songContainer.visibility = View.GONE
            artistContainer.visibility = View.GONE
            albumsContainer.visibility = View.GONE
            videoContainer.visibility = View.GONE
        }
    }

    private fun isShow(songTitle: View) {
        if (songTitle.isGone){
            songTitle.visibility = View.VISIBLE
        }
    }

    private fun isGone(songTitle: View) {
        if (songTitle.isVisible){
            songTitle.visibility = View.GONE
        }
    }

    private fun insertSearchHistory() {
        if (binding.searchEditText.text.toString().isEmpty()){
            return
        }
            data?.query {
                insert(SearchHistory(query = binding.searchEditText.text.toString()))
            }
    }

    private fun showAnimations(isShow: Boolean) {
        binding.specAnm.apply {
            if (isShow){
                visibility = View.VISIBLE
                startShimmerAnimation()
            }else{
                visibility = View.GONE
                stopShimmerAnimation()
            }
        }
    }

    private fun setVisible(one: View,song: View, album: View, artist: View,view : View) {
        one.visibility = View.VISIBLE
        song.visibility = View.GONE
        album.visibility = View.GONE
        artist.visibility = View.GONE
        view.visibility = View.GONE
        if (binding.emptyResult.isVisible){
            binding.emptyResult.visibility = View.GONE
        }
        when(currentType){
            0 ->{
                binding.all.setBackgroundResource(R.drawable.select_search)
                binding.artist.setBackgroundResource(R.drawable.unselect_search)
                binding.albums.setBackgroundResource(R.drawable.unselect_search)
                binding.video.setBackgroundResource(R.drawable.unselect_search)
                binding.song.setBackgroundResource(R.drawable.unselect_search)
                binding.all.setTextColor("#FFFFFF".toColorInt())
                binding.artist.setTextColor("#8C8C8C".toColorInt())
                binding.albums.setTextColor("#8C8C8C".toColorInt())
                binding.video.setTextColor("#8C8C8C".toColorInt())
                binding.song.setTextColor("#8C8C8C".toColorInt())
            }
            1 ->{
                binding.song.setBackgroundResource(R.drawable.select_search)
                binding.artist.setBackgroundResource(R.drawable.unselect_search)
                binding.albums.setBackgroundResource(R.drawable.unselect_search)
                binding.video.setBackgroundResource(R.drawable.unselect_search)
                binding.all.setBackgroundResource(R.drawable.unselect_search)
                binding.song.setTextColor("#FFFFFF".toColorInt())
                binding.artist.setTextColor("#8C8C8C".toColorInt())
                binding.albums.setTextColor("#8C8C8C".toColorInt())
                binding.video.setTextColor("#8C8C8C".toColorInt())
                binding.all.setTextColor("#8C8C8C".toColorInt())
            }
            2 ->{
                binding.albums.setBackgroundResource(R.drawable.select_search)
                binding.artist.setBackgroundResource(R.drawable.unselect_search)
                binding.all.setBackgroundResource(R.drawable.unselect_search)
                binding.video.setBackgroundResource(R.drawable.unselect_search)
                binding.song.setBackgroundResource(R.drawable.unselect_search)
                binding.albums.setTextColor("#FFFFFF".toColorInt())
                binding.artist.setTextColor("#8C8C8C".toColorInt())
                binding.all.setTextColor("#8C8C8C".toColorInt())
                binding.video.setTextColor("#8C8C8C".toColorInt())
                binding.song.setTextColor("#8C8C8C".toColorInt())
            }
            3 ->{
                binding.artist.setBackgroundResource(R.drawable.select_search)
                binding.all.setBackgroundResource(R.drawable.unselect_search)
                binding.albums.setBackgroundResource(R.drawable.unselect_search)
                binding.video.setBackgroundResource(R.drawable.unselect_search)
                binding.song.setBackgroundResource(R.drawable.unselect_search)
                binding.artist.setTextColor("#FFFFFF".toColorInt())
                binding.all.setTextColor("#8C8C8C".toColorInt())
                binding.albums.setTextColor("#8C8C8C".toColorInt())
                binding.video.setTextColor("#8C8C8C".toColorInt())
                binding.song.setTextColor("#8C8C8C".toColorInt())
            }
            4 ->{
                binding.video.setBackgroundResource(R.drawable.select_search)
                binding.artist.setBackgroundResource(R.drawable.unselect_search)
                binding.albums.setBackgroundResource(R.drawable.unselect_search)
                binding.all.setBackgroundResource(R.drawable.unselect_search)
                binding.song.setBackgroundResource(R.drawable.unselect_search)
                binding.video.setTextColor("#FFFFFF".toColorInt())
                binding.albums.setTextColor("#8C8C8C".toColorInt())
                binding.artist.setTextColor("#8C8C8C".toColorInt())
                binding.all.setTextColor("#8C8C8C".toColorInt())
                binding.song.setTextColor("#8C8C8C".toColorInt())
            }
        }
    }
    companion object{
        fun isBottom(recyclerView: RecyclerView) : Boolean {
            if (recyclerView == null)
                return false

            if (recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange()
                && !recyclerView.canScrollVertically(1)
            )
                return true

            return false;
        }
    }
    private fun showBanners() {
        val normalBan: NativeAd? = SnapMobManager.homeStudy.showWeaveNatives(
            searchResultBanner(), this)
        if (normalBan == null) {
            if (SnapPosManager.cyIsShowingFLag) {
                LogUtils.e("searchResultBanner  ban inter is showing return------")
                return
            }
            if (isFinishing || isDestroyed) {
                LogUtils.e("searchResultBanner  ban destroy  return------")
                return
            }
            if (SnapPosManager.posIsCanShow(searchResultBanner())) {
                LogUtils.e("searchResultBanner  ban limit  return------")
                return
            }
            SnapMobManager.homeStudy.initLoadBanners(searchResultBanner(),object :
                NativeWeavesListener {
                override fun nativeIsWeavesSuc(sucFlag: Boolean) {
                    if (isFinishing || isDestroyed) {
                        return
                    }
                    if (sucFlag) {
                        LogUtils.e("searchResultBanner  ban load  suc------")
                        showBanners()
                    }
                }
            })
        } else {
            LogUtils.e("searchResultBanner  ban show  suc------")
            binding?.homeBans?.let {
                it.visibility = View.VISIBLE
                it.setStyles(MobToStyle.Builder().build())
                it.setNativeAd(normalBan)
                if(!SnapPosManager.posIsCanShow(searchResultBanner())){
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

  

    override fun onResume() {
        super.onResume()
        showBanners()
    }

}