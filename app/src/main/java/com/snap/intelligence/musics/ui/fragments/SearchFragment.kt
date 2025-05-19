//package com.snap.intelligence.musics.ui.fragments
//
//import android.os.Bundle
//import android.text.Editable
//import android.text.TextWatcher
//import android.util.Log
//import android.view.KeyEvent
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.view.inputmethod.EditorInfo
//import android.widget.EditText
//import android.widget.ImageButton
//import android.widget.TextView
//import android.widget.TextView.OnEditorActionListener
//import android.widget.Toast
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.asLiveData
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.android.material.chip.Chip
//import com.google.android.material.chip.ChipGroup
//import com.google.android.material.floatingactionbutton.FloatingActionButton
//import com.zionhuang.innertube.YouTube
//import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_ALBUM
//import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_ARTIST
//import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_COMMUNITY_PLAYLIST
//import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_FEATURED_PLAYLIST
//import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_SONG
//import com.zionhuang.innertube.YouTube.SearchFilter.Companion.FILTER_VIDEO
//import com.zionhuang.innertube.models.AlbumItem
//import com.zionhuang.innertube.models.ArtistItem
//import com.zionhuang.innertube.models.PlaylistItem
//import com.zionhuang.innertube.models.SongItem
//import com.zionhuang.innertube.pages.SearchSummaryPage
//import com.snap.intelligence.musics.R
//import com.snap.intelligence.musics.db.entities.Album
//import com.snap.intelligence.musics.db.entities.Artist
//import com.snap.intelligence.musics.db.entities.LocalItem
//import com.snap.intelligence.musics.db.entities.Playlist
//import com.snap.intelligence.musics.db.entities.Song
//import com.snap.intelligence.musics.extensions.togglePlayPause
//import com.snap.intelligence.musics.models.toMediaMetadata
//import com.snap.intelligence.musics.playback.queues.YouTubeQueue.Companion.radio
////import com.snap.intelligence.musics.ui.fragments.adapters.LocalSearchAdapter
//import com.snap.intelligence.musics.ui.fragments.adapters.OnlineSearchAdapter
//import com.snap.intelligence.musics.viewmodels.LocalFilter
//import com.snap.intelligence.musics.viewmodels.LocalSearchViewModel
//import com.snap.intelligence.musics.viewmodels.OnlineSearchViewModel
//import com.snap.intelligence.musics.viewmodels.OnlineSearchViewModel.Companion.query
//import dagger.hilt.android.AndroidEntryPoint
//
//@AndroidEntryPoint
//class SearchFragment : BaseFragment() {
//    private var searchEditText: EditText? = null
//    private var clearButton: ImageButton? = null
//    private var localFilterChipGroup: ChipGroup? = null
//    private var onlineFilterChipGroup: ChipGroup? = null
//    private var searchResultsRecyclerView: RecyclerView? = null
//    private var emptyView: View? = null
//
//    private var localSearchViewModel: LocalSearchViewModel? = null
//    private var onlineSearchViewModel: OnlineSearchViewModel? = null
//
//    private var isLocalSearch = true // 默认为本地搜索
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_search, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//
//        // 初始化视图
//        searchEditText = view.findViewById(R.id.search_edit_text)
//        clearButton = view.findViewById(R.id.clear_button)
//        val backButton = view.findViewById<ImageButton>(R.id.back_button)
////        localFilterChipGroup = view.findViewById(R.id.local_filter_chip_group)
////        onlineFilterChipGroup = view.findViewById(R.id.online_filter_chip_group)
//        searchResultsRecyclerView = view.findViewById(R.id.search_results_recycler_view)
//        emptyView = view.findViewById(R.id.empty_view)
//        val searchTypeFab = view.findViewById<FloatingActionButton>(R.id.search_type_fab)
//
//
//        // 设置RecyclerView
//        searchResultsRecyclerView?.setLayoutManager(LinearLayoutManager(requireContext()))
//
//
//        // 初始化ViewModel
//        localSearchViewModel = ViewModelProvider(this).get(
//            LocalSearchViewModel::class.java
//        )
//
//
//        // 设置搜索框监听
//        searchEditText?.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
//
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                clearButton?.setVisibility(if (s.length > 0) View.VISIBLE else View.GONE)
//                if (isLocalSearch) {
//                    localSearchViewModel!!.query.value = s.toString()
//                }
//            }
//
//            override fun afterTextChanged(s: Editable) {}
//        })
//
//
//        searchEditText?.setOnEditorActionListener(OnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->
//            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                var querys= searchEditText?.getText().toString()
//                if (!querys.isEmpty()) {
//                    // 如果是在线搜索，需要创建新的OnlineSearchViewModel
//                    query = querys
//                    onlineSearchViewModel = ViewModelProvider(
//                        this)
//                        .get(OnlineSearchViewModel::class.java)
//                    // 更新UI显示在线搜索结果
//
//                    updateOnlineSearchResults()
//                }
//                return@OnEditorActionListener true
//            }
//            false
//        })
//
//
//        // 清除按钮点击事件
//        clearButton?.setOnClickListener(View.OnClickListener { v: View? ->
//            searchEditText?.setText("")
//            clearButton?.setVisibility(View.GONE)
//        })
//
//
//        // 返回按钮点击事件
//        backButton.setOnClickListener { v: View? -> requireActivity().onBackPressed() }
//
//
//        // 搜索类型切换按钮点击事件
//        searchTypeFab.setOnClickListener { v: View? ->
////            if (isLocalSearch) {
////                showOnlineSearchUI()
////                searchTypeFab.setImageResource(R.drawable.library_music)
////                Toast.makeText(requireContext(), "local search", Toast.LENGTH_SHORT)
////                //                searchTypeFab.setContentDescription(getString(R.string.switch_to_local_search));
////            } else {
//                showLocalSearchUI()
//                searchTypeFab.setImageResource(R.drawable.search)
//                Toast.makeText(requireContext(), "online search", Toast.LENGTH_SHORT)
//
//                //                searchTypeFab.setContentDescription(getString(R.string.switch_to_online_search));
////            }
//        }
//
//
//        // 设置本地搜索过滤器
////        setupLocalFilterChips()
//
//
//        // 设置在线搜索过滤器
////        setupOnlineFilterChips()
//
//
//        // 观察本地搜索结果
//        // 注意：Kotlin的Flow需要通过asLiveData()转换为LiveData才能在Java中观察
//        localSearchViewModel?.result?.asLiveData()?.observe(viewLifecycleOwner) { results ->
//            //todo
////                updateLocalSearchResults(results)
//        }
//
//
//        // 默认显示本地搜索UI
//        showLocalSearchUI()
//    }
//
//    private fun setupLocalFilterChips() {
//        // 添加本地搜索过滤器选项
//        val filterNames = arrayOf(
//            getString(R.string.filter_all),
//            getString(R.string.filter_songs),
//            getString(R.string.filter_albums),
//            getString(R.string.filter_artists),
//            getString(R.string.filter_playlists)
//        )
//
//        val filters = arrayOf(
//            LocalFilter.ALL,
//            LocalFilter.SONG,
//            LocalFilter.ALBUM,
//            LocalFilter.ARTIST,
//            LocalFilter.PLAYLIST
//        )
//
//        for (i in filterNames.indices) {
//            val chip = Chip(requireContext())
//            chip.text = filterNames[i]
//            chip.isCheckable = true
//            chip.isClickable = true
//
//            val filter = filters[i]
//            chip.setOnClickListener { v: View? ->
//                localSearchViewModel!!.filter.value = filter
//            }
//
//            if (filter == LocalFilter.ALL) {
//                chip.isChecked = true
//            }
//
////            localFilterChipGroup!!.addView(chip)
//        }
//    }
//
//    private fun setupOnlineFilterChips() {
//        // 添加在线搜索过滤器选项
//        val filterNames = arrayOf(
//            getString(R.string.filter_all),
//            getString(R.string.filter_songs),
//            getString(R.string.filter_videos),
//            getString(R.string.filter_albums),
//            getString(R.string.filter_artists),
//            getString(R.string.filter_community_playlists),
//            getString(R.string.filter_featured_playlists)
//        )
//
//        val filters = arrayOf<YouTube.SearchFilter?>(
//            null,
//            FILTER_SONG,
//            FILTER_VIDEO,
//            FILTER_ALBUM,
//            FILTER_ARTIST,
//            FILTER_COMMUNITY_PLAYLIST,
//            FILTER_FEATURED_PLAYLIST
//        )
//
//        for (i in filterNames.indices) {
//            val chip = Chip(requireContext())
//            chip.text = filterNames[i]
//            chip.isCheckable = true
//            chip.isClickable = true
//
//            val filter: YouTube.SearchFilter? = filters[i]
//            chip.setOnClickListener { v: View? ->
//                if (onlineSearchViewModel != null) {
//                    onlineSearchViewModel!!.filter.value = filter
//                }
//            }
//
//            if (i == 0) { // 默认选中"全部"
//                chip.isChecked = true
//            }
//
////            onlineFilterChipGroup!!.addView(chip)
//        }
//    }
//
//    private fun updateLocalSearchResults(result: LocalSearchResult) {
//        // 更新本地搜索结果UI
//        if (result.query.isEmpty() || result.map.isEmpty()) {
//            searchResultsRecyclerView!!.visibility = View.GONE
//            emptyView!!.visibility = if (result.query.isEmpty()) View.GONE else View.VISIBLE
//        } else {
//            searchResultsRecyclerView!!.visibility = View.VISIBLE
//            emptyView!!.visibility = View.GONE
//
//
//            // 使用LocalSearchAdapter显示搜索结果
////            if (searchResultsRecyclerView!!.adapter == null) {
////                val adapter = LocalSearchAdapter(
////                    playerConnection,
////                    object : LocalSearchAdapter.OnItemClickListener {
////                        override fun onFilterClick(filter: LocalFilter) {
////                            localSearchViewModel!!.filter.value = filter
////                        }
////
////                        override fun onSongClick(song: Song) {
////                            // 播放歌曲
////                            if (playerConnection != null) {
////                                if (song.id === playerConnection.mediaMetadata.value?.id) {
////                                    playerConnection.player.togglePlayPause()
////                                } else {
////                                    // 创建播放队列并播放
//////                                List<Song> songs = result.getMap().getOrDefault(LocalFilter.SONG, Collections.emptyList())
//////                                        .stream()
//////                                        .filter(Song.class::isInstance)
//////                                        .map(Song.class::cast)
//////                                        .map(MediaItemExtKt.toMediaItem(song))
//////                                        .collect(Collectors.toList());
//////
//////                                playerConnection.playQueue(new ListQueue(
//////                                        context.getString(R.string.queue_searched_songs),
//////                                        songs,
//////                                        songs.indexOf(songs.stream().filter(song -> song.getMediaId().equals(item.getId())).findFirst().orElse(null))
//////                                ));
//////
//////                                playerConnection.playQueue(new ListQueue(song.getTitle(),song));
////                                }
////                            }
////                        }
////
////                        override fun onSongMenuClick(song: Song, view: View) {
////                            // TODO: 显示歌曲菜单
////                        }
////
////                        override fun onAlbumClick(album: Album) {
////                            // 导航到专辑详情页
//////                        mainActivityJava.getSupportFragmentManager().beginTransaction()
//////                                .replace(R.id.fragment_container, AlbumFragment.newInstance(album.getId()))
//////                                .addToBackStack(null)
//////                                .commit();
////                        }
////
////                        override fun onArtistClick(artist: Artist) {
////                            // 导航到艺术家详情页
//////                        mainActivityJava.getSupportFragmentManager().beginTransaction()
//////                                .replace(R.id.fragment_container, ArtistFragment.newInstance(artist.getId()))
//////                                .addToBackStack(null)
//////                                .commit();
////                        }
////
////                        override fun onPlaylistClick(playlist: Playlist) {
////                            // 导航到播放列表详情页
//////                        mainActivityJava.getSupportFragmentManager().beginTransaction()
//////                                .replace(R.id.fragment_container, PlaylistFragment.newInstance(playlist.getId()))
//////                                .addToBackStack(null)
//////                                .commit();
////                        }
////                    })
////                searchResultsRecyclerView!!.adapter = adapter
////            }
//
//
//            // 更新适配器数据
////            (searchResultsRecyclerView!!.adapter as LocalSearchAdapter).submitList(
////                result.map,
////                result.filter
////            )
//        }
//    }
//
//    private fun updateOnlineSearchResults() {
//        Log.e("result",query.toString())
//        // 更新在线搜索结果UI
//        if (onlineSearchViewModel != null) {
//            // 观察在线搜索结果
//            onlineSearchViewModel!!.filter.asLiveData().observe(viewLifecycleOwner) { filter ->
//                // 当过滤器变化时更新UI
//                if (searchResultsRecyclerView!!.adapter is OnlineSearchAdapter && onlineSearchViewModel!!.summaryPage != null) {
//                    (searchResultsRecyclerView!!.adapter as OnlineSearchAdapter).submitList(
//                        onlineSearchViewModel!!.summaryPage!!.summaries, filter
//                    )
//                }
//            }
//
//
//            // 观察搜索摘要页面 - 使用LiveData包装器观察MutableState
//            MutableLiveData<SearchSummaryPage?>(onlineSearchViewModel!!.summaryPage).observe(
//                viewLifecycleOwner
//            ) { summaryPage: SearchSummaryPage? ->
//                Log.e("1111",summaryPage.toString())
//                if (summaryPage == null) {
//                    // 显示加载中
//                    searchResultsRecyclerView!!.visibility = View.GONE
//                    emptyView!!.visibility = View.GONE
//                } else if (summaryPage.summaries.isEmpty()) {
//                    // 显示空结果
//                    searchResultsRecyclerView!!.visibility = View.GONE
//                    emptyView!!.visibility = View.VISIBLE
//                } else {
//                    // 显示搜索结果
//                    searchResultsRecyclerView!!.visibility = View.VISIBLE
//                    emptyView!!.visibility = View.GONE
//
//
//                    // 使用OnlineSearchAdapter显示搜索结果
//                    if (searchResultsRecyclerView!!.adapter !is OnlineSearchAdapter) {
//                        val adapter = OnlineSearchAdapter(
//                            playerConnection,
//                            object : OnlineSearchAdapter.OnItemClickListener {
//                                override fun onFilterClick(filter: YouTube.SearchFilter) {
//                                    onlineSearchViewModel!!.filter.value = filter
//                                }
//
//                                override fun onSongClick(song: SongItem) {
//                                    // 播放歌曲
//                                    if (playerConnection != null) {
//                                        if (song.id == playerConnection.mediaMetadata.value?.id) {
//                                            playerConnection.player.togglePlayPause()
//                                        } else {
//                                            // 创建播放队列并播放
//                                            playerConnection.playQueue(
//                                                radio(
//                                                    song.toMediaMetadata()
//                                                )
//                                            )
//                                        }
//                                    }
//                                }
//
//                                override fun onSongMenuClick(
//                                    song: SongItem,
//                                    view: View
//                                ) {
//                                    // 显示歌曲菜单
//                                    // 使用YouTubeSongMenu显示歌曲菜单
////                                    YouTubeSongMenuHelper(requireContext(), song, view).show()
//                                }
//
//                                override fun onAlbumClick(album: AlbumItem) {
//                                    // 导航到专辑详情页
//                                    // 导航到专辑详情页
//                                    // 使用专辑ID创建AlbumFragment实例
////                                mainActivityJava.getSupportFragmentManager().beginTransaction()
////                                        .replace(R.id.fragment_container, AlbumFragment.newInstance(album.getBrowseId()))
////                                        .addToBackStack(null)
////                                        .commit();
//                                }
//
//                                override fun onArtistClick(artist: ArtistItem) {
//                                    // 导航到艺术家详情页
//                                    // 导航到艺术家详情页
//                                    // 使用艺术家ID创建ArtistFragment实例
////                                mainActivityJava.getSupportFragmentManager().beginTransaction()
////                                        .replace(R.id.fragment_container, ArtistFragment.newInstance(artist.getBrowseId()))
////                                        .addToBackStack(null)
////                                        .commit();
//                                }
//
//                                override fun onPlaylistClick(playlist: PlaylistItem) {
//                                    // 导航到播放列表详情页
//                                    // 导航到播放列表详情页
//                                    // 使用播放列表ID创建OnlinePlaylistFragment实例
////     todo                           mainActivityJava.getSupportFragmentManager().beginTransaction()
////                                        .replace(R.id.fragment_container, OnlinePlaylistFragment.newInstance(playlist.getBrowseId()))
////                                        .addToBackStack(null)
////                                        .commit();
//                                }
//                            })
//                        searchResultsRecyclerView!!.adapter = adapter
//                    }
//
//
//                    // 更新适配器数据
//                    (searchResultsRecyclerView!!.adapter as OnlineSearchAdapter).submitList(
//                        summaryPage.summaries, onlineSearchViewModel!!.filter.value
//                    )
//                }
//            }
//        }
//    }
//
//    private fun showLocalSearchUI() {
//        isLocalSearch = true
////        localFilterChipGroup!!.visibility = View.VISIBLE
////        onlineFilterChipGroup!!.visibility = View.GONE
//    }
//
//    private fun showOnlineSearchUI() {
//        isLocalSearch = false
////        localFilterChipGroup!!.visibility = View.GONE
////        onlineFilterChipGroup!!.visibility = View.VISIBLE
//    }
//
//    // 内部类，用于表示本地搜索结果
//    private class LocalSearchResult(
//        val query: String,
//        val filter: LocalFilter,
//        val map: Map<LocalFilter, List<LocalItem>>
//    )
//
//}