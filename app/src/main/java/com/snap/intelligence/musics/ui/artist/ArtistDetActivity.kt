package com.snap.intelligence.musics.ui.artist


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import artOptions1
import artOptions2
import artOptions3
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.snap.intelligence.musics.constants.albumDetInter
import com.snap.intelligence.musics.constants.artistDetInter
import com.snap.intelligence.musics.constants.playlistDetInter
import com.snap.intelligence.musics.constants.projectDetInter
import com.gyf.immersionbar.ImmersionBar
import com.snap.intelligence.musics.App
import com.snap.intelligence.musics.BasicView
import com.snap.intelligence.musics.ui.playlist.PlayListDetActivity
import com.snap.intelligence.musics.R
import com.snap.intelligence.musics.constants.SnapPosManager
import com.snap.intelligence.musics.constants.MESSAGE_MSG_CODE
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_APP_REFRESH
import com.snap.intelligence.musics.constants.NativeWeavesListener
import com.snap.intelligence.musics.constants.artistDetBanner
import com.snap.intelligence.musics.ui.start.HomeActivity.Companion.playerConnection
import com.snap.intelligence.musics.databinding.ArtistListBinding
import com.snap.intelligence.musics.db.InternalDatabase
import com.snap.intelligence.musics.db.MusicDatabase
import com.snap.intelligence.musics.db.entities.SongEntity
import com.snap.intelligence.musics.extensions.currentMetadata
import com.snap.intelligence.musics.extensions.logClickEvents
import com.snap.intelligence.musics.extensions.toMediaItem
import com.snap.intelligence.musics.local.CornerTransform
import com.snap.intelligence.musics.local.randomBitmap
import com.snap.intelligence.musics.local.showDialogs
import com.snap.intelligence.musics.mob.MobToStyle
import com.snap.intelligence.musics.mob.SnapMobManager
import com.snap.intelligence.musics.models.MediaMetadata
import com.snap.intelligence.musics.models.toMediaMetadata
import com.snap.intelligence.musics.playback.queues.YouTubeQueue
import com.snap.intelligence.musics.playback.queues.YouTubeQueue.Companion.radio
import com.snap.intelligence.musics.ui.album.AlbumDetActivity
import com.snap.intelligence.musics.utils.joinByBullet
import com.snap.intelligence.musics.viewmodels.ArtistViewModel
import com.tencent.mmkv.MMKV
import com.zionhuang.innertube.models.*
import com.zionhuang.innertube.pages.ArtistSection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import roundOptions2


class ArtistDetActivity : BasicView() {

    private var queue: WatchEndpoint? = null
    private lateinit var binding: ArtistListBinding
    private lateinit var newReleaseAlbumsAdapter: SectionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SnapMobManager.homeStudy.getShowCyAds(artistDetInter(),this)

        binding = ArtistListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ImmersionBar.with(this).transparentStatusBar().titleBar(binding.tops.root).init()
        val albumViewModel = intent?.getStringExtra("artistId")?.let {
            ArtistViewModel(this,InternalDatabase.newInstance(this), it

            )
        }
        binding.tops.apply {
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
        newReleaseAlbumsAdapter =  SectionsAdapter(emptyList())
        binding.specAnm.post {
            binding.specAnm.visibility = View.VISIBLE
            binding.specAnm.startShimmerAnimation()
        }
        binding.playBtns.setOnClickListener {
            logClickEvents("artist_det_play_btn")

            queue?.let {
                startPlayerPage()
                playerConnection?.playQueue(YouTubeQueue(it))
            }
        }
        binding.bottomPlayer.container.setOnClickListener {
            startPlayerPage()
        }
        binding.homePageSectionsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.homePageSectionsRecyclerView.adapter = newReleaseAlbumsAdapter
        lifecycleScope.launch {
//            withContext(Dispatchers.IO){
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
            val cornerTransform = CornerTransform(this@ArtistDetActivity, SizeUtils.dp2px(45f).toFloat())
            cornerTransform.setNeedCorner(false, false, true, true)
            val options: RequestOptions = RequestOptions()
                .placeholder(randomBitmap())
                .error(randomBitmap())
                .transform(cornerTransform)

            albumViewModel?.artistPage?.collect {
                if (isFinishing || isDestroyed) {
                    return@collect
                }
                binding.specAnm.visibility = View.GONE
                binding.specAnm.stopShimmerAnimation()
                binding.titles.text = it?.artist?.title

                Glide.with(this@ArtistDetActivity)
//                .load(posBitmap(queryType))
                    .load(it?.artist?.thumbnail)
                    .apply(options)
                    .thumbnail(
                        Glide.with(this@ArtistDetActivity)
                            .load(randomBitmap())
                            .apply(options)
                    )
                    .into(binding.thumbnailss)

//                Glide.with(this@ArtistDetActivity)
//                    .load(it?.artist?.thumbnail)
//                    .apply(roundOptions)
//                    .thumbnail(
//                        Glide.with(this@ArtistDetActivity)
//                            .load(randomBitmap())
//                            .apply(roundOptions)
//                    )
//                    .into(binding.thumbnailTx)

//                it?.artist?.thumbnail?.let {
//                    Glide.with(this@ArtistDetActivity).asBitmap().load(it).into(binding.thumbnail)
//                }
                it?.sections?.let { ots ->
                    newReleaseAlbumsAdapter?.addData(ots)
                }
                it?.artist?.shuffleEndpoint?.let {
                    queue = it
                }
            }


//            // 观察 ViewModel 数据
//            albumViewModel?.librarySongs.observe(this) { artistPage ->
//                if (artistPage != null) {
//                    // 更新 UI
//                    supportActionBar?.title = artistPage.artist.title
//                    // 其他 UI 更新逻辑...
//                }
//            }
//
//            albumViewModel?.librarySongs.observe(this) { songs ->
//                adapter.submitList(songs)
//                progressBar.isVisible = false
//            }
//
//
//            albumViewModel?.songLists?.collect {
//                    Timber.e(it.toString())
//                    if (it == null) {
//                        return@collect
//                    }
//                    // Set data to adapters
//                    withContext(Dispatchers.Main){
//                        it?.apply {
//                            newReleaseAlbumsAdapter?.submitList(this)
//                        }
//                    }
//
//                }
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
    }
            // 设置艺术家页面部分

        inner class SectionsAdapter(private var sections: List<ArtistSection>) : RecyclerView.Adapter<SectionsAdapter.ViewHolder>() {

            inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                val sectionTitleTextView: TextView = itemView.findViewById(R.id.section_title)
                val constraint: View = itemView.findViewById(R.id.constraint)
                val sectionItemsRecyclerView: RecyclerView = itemView.findViewById(R.id.section_items_recycler_view)
                val lines: View = itemView.findViewById(R.id.lines)
                val mores: View = itemView.findViewById(R.id.more)
            }
            fun addData(songs: List<ArtistSection>){
                this.sections = songs
                notifyDataSetChanged()
            }
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_section, parent, false)
                return ViewHolder(view)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                if (position == sections.size -1){
                    holder.lines.visibility = View.GONE
                }
                val section = sections[position]
                holder.sectionTitleTextView.text = section.title
                section.moreEndpoint?.let{
                    holder.mores.visibility = View.VISIBLE
                }
                holder.constraint.setOnClickListener {
                    section.moreEndpoint?.let {
                        startInterActivity(Intent(this@ArtistDetActivity, ArtistItemActivity::class.java).apply {
                            putExtra("browseId",it.browseId)
                            putExtra("params",it.params)
                        }, projectDetInter(),false)
                        logClickEvents("art_det_to_item_detail")

//                        navController.navigate("artist/${viewModel.artistId}/items?browseId=${it.browseId}?params=${it.params}")
                    }
                }
                val isVertical = (section.items.firstOrNull() as? SongItem)?.album != null
                if (isVertical){
                    holder.mores.visibility = View.GONE
                }
                holder.sectionItemsRecyclerView.layoutManager = if (isVertical) {
                    LinearLayoutManager(this@ArtistDetActivity, LinearLayoutManager.VERTICAL, false)
                } else {
                    LinearLayoutManager(this@ArtistDetActivity, LinearLayoutManager.HORIZONTAL, false)
                }
                holder.sectionItemsRecyclerView.adapter = SectionItemsAdapter(section.items,isVertical)
            }

            override fun getItemCount(): Int = sections.size
        }

        inner class SectionItemsAdapter(private val items: List<YTItem>, var isVertical: Boolean) : RecyclerView.Adapter<SectionItemsAdapter.ViewHolder>() {

            inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                val title: TextView? = itemView.findViewById(R.id.title)
                val title2: TextView? = itemView.findViewById(R.id.title2)
                val artist: TextView? = itemView.findViewById(R.id.artist)
                val ivgs: ImageView? = itemView.findViewById(R.id.ivgs)
                val thumbnail: ImageView? = itemView.findViewById(R.id.thumbnail)
                val thumbnail2: ImageView? = itemView.findViewById(R.id.thumbnail2)
                val thumbnail3: ImageView? = itemView.findViewById(R.id.thumbnail3)
                val artPlay: ImageView? = itemView.findViewById(R.id.play_icon)
                val videoPlay: ImageView? = itemView.findViewById(R.id.play_icon2)

            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val layoutId = if (isVertical) R.layout.item_art_on_song else R.layout.item_art_new_release_album
                val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
                return ViewHolder(view)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                val item = items[position]

                when (item) {
                    is SongItem -> {
                        if (item.album == null){
                            holder.thumbnail2?.let {
                                holder.thumbnail2.visibility = View.VISIBLE
                                holder.thumbnail?.visibility = View.GONE
                                holder.videoPlay?.let {
                                    its ->
                                    if(its.isGone){
                                        its.visibility = View.VISIBLE
                                    }
                                }
                                holder.artPlay?.let {
                                        arts ->
                                    if(arts.isVisible){
                                        arts.visibility = View.GONE
                                    }
                                }
                                Glide.with(holder.itemView)
                                    .asBitmap()
                                    .load(item.thumbnail)
                                    .apply(artOptions3)
                                    .into(it)
                            }
                        }else{
                            holder.thumbnail?.let {
                                Glide.with(holder.itemView)
                                    .asBitmap()
                                    .load(item.thumbnail)
                                    .apply(artOptions1)
                                    .into(it)
                            }
                        }
                        holder?.title?.text = item.title
                        holder?.artist?.text = joinByBullet(item.artists?.joinToString { it.name })
                    }
                    is AlbumItem -> {
                        holder.thumbnail?.let {
                            Glide.with(holder.itemView)
                                .asBitmap()

                                .load(item.thumbnail)
                                .apply(artOptions2)

//                                .thumbnail(
//                                    Glide.with(holder.itemView)
//                                        .load(randomBitmap())
//                                        .apply(options)
//                                )
                                .into(it)
                        }
                        holder?.title?.text = item.title
                        if (!TextUtils.isEmpty(joinByBullet(item.artists?.joinToString { it.name }))){
                            holder?.artist?.text = joinByBullet(item.artists?.joinToString { it.name })
                            holder.artist?.visibility = View.VISIBLE
                        }else{
                            holder.artist?.visibility = View.GONE
                        }
                    }
                    is ArtistItem -> {
                        holder.thumbnail3?.let {
                            holder.thumbnail?.visibility = View.GONE
                            holder.thumbnail3?.visibility = View.VISIBLE
                            holder.thumbnail2?.visibility = View.GONE
                            Glide.with(holder.itemView)
                                .asBitmap()
                                .load(item.thumbnail)
                                .apply(roundOptions2)
//                                .thumbnail(
//                                    Glide.with(holder.itemView)
//                                        .load(randomBitmap())
//                                        .apply(roundOptions)
//                                )
                                .into(it)
                        }
                        holder?.title?.text = item.title
                        holder.title?.gravity = Gravity.CENTER
                        holder.artist?.visibility = View.GONE
                    }
                    is PlaylistItem -> {

                        holder.thumbnail?.let {
//                            Glide.with(holder.itemView)
//                                .asBitmap()
//                                .load(item.thumbnail)
//                                .into(object : CustomTarget<Bitmap?>() {
//
//                                    override fun onResourceReady(
//                                        resource: Bitmap,
//                                        transition: Transition<in Bitmap?>?
//                                    ) {
//                                        Glide.with(holder.itemView)
//                                .load(resource)
//                                .apply(artOptions2)
//                                .into(it)
//                                    }
//
//                                    override fun onLoadCleared(@Nullable placeholder: Drawable?) {
//
//                                    }
//                                })
                            Glide.with(holder.itemView)
                                .asBitmap()
                                .load(item.thumbnail)
                                .apply(artOptions2)
                                .into(it)
                        }
                        holder?.title?.text = item.title
                        holder.artist?.visibility = View.GONE
                    }
                    else -> {
//                        Log.e("other",item.toString())
                    }
                }
                holder.itemView.setOnClickListener {
                    when (item) {
                        is SongItem -> {
                            playerConnection?.playQueue(radio(item.toMediaMetadata()))
                           startPlayerPage()
                        }
                        is AlbumItem ->   {
                            startInterActivity(Intent(this@ArtistDetActivity, AlbumDetActivity::class.java).apply {
                                putExtra("albumId",item.id)
                            }, albumDetInter(),false)
                        }
                        is ArtistItem -> {
                            startInterActivity(Intent(this@ArtistDetActivity, ArtistDetActivity::class.java).apply {
                                putExtra("artistId",item.id)
                            }, artistDetInter(),false)
                        }
                        is PlaylistItem -> {
                            startInterActivity(Intent(this@ArtistDetActivity, PlayListDetActivity::class.java).apply {
                                putExtra("playlistId",item.id)
                            }, playlistDetInter(),false)
                        }
                    }
                }
//                holder.itemView.setOnLongClickListener {
//                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
//                    menuState.show {
//                        when (item) {
//                            is SongItem -> YouTubeSongMenu(
//                                song = item,
//                                navController = navController,
//                                onDismiss = menuState::dismiss
//                            )
//                            is AlbumItem -> YouTubeAlbumMenu(
//                                albumItem = item,
//                                navController = navController,
//                                onDismiss = menuState::dismiss
//                            )
//                            is ArtistItem -> YouTubeArtistMenu(
//                                artist = item,
//                                onDismiss = menuState::dismiss
//                            )
//                            is PlaylistItem -> YouTubePlaylistMenu(
//                                playlist = item,
//                                coroutineScope = coroutineScope,
//                                onDismiss = menuState::dismiss
//                            )
//                        }
//                    }
//                    true
//                }

                holder.ivgs?.setOnClickListener{
                    val data = item as? SongItem ?: return@setOnClickListener
                    try {
                        showDialogs(holder.itemView.context, R.layout.dialog_menu,true, Gravity.BOTTOM).apply {
                            val text =   findViewById<TextView>(R.id.titles4)
                            var database : MusicDatabase?= null
                            App.instance?.let {
                                database = InternalDatabase.newInstance(it)
                                database?.transaction {
                                    lifecycleScope.launch {
                                        song(item.id).first().let {
                                                its ->
                                            if (its == null){

                                                return@let
                                            }
                                            its?.apply {
                                                if (song.liked){
                                                    text?.text = "Remove from favourite"
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                            findViewById<View>(R.id.play_now)?.setOnClickListener {
                                if (isShowing){
                                    dismiss()
                                }
                                playerConnection?.playQueue(radio(data.toMediaMetadata()))
                                startPlayerPage()
                            }
                            findViewById<View>(R.id.play_next)?.setOnClickListener {
                                if (isShowing){
                                    dismiss()
                                }
                                playerConnection?.playNext(data.toMediaItem())
                            }
                            findViewById<View>(R.id.add_queue)?.setOnClickListener {
                                if (isShowing){
                                    dismiss()
                                }
                                playerConnection?.addToQueue(data.toMediaItem())
                            }
                            findViewById<View>(R.id.add_fav)?.setOnClickListener {
                                if (isShowing){
                                    dismiss()
                                }
                                database?.transaction {
                                    lifecycleScope.launch {
                                        song(item.id).first().let {
                                                its ->
//                                            Log.e("test",it.toString())
                                            if (its == null){
                                                withContext(Dispatchers.IO){
                                                    insert(item.toMediaMetadata(), SongEntity::toggleLike)
                                                }
                                                return@let
                                            }
                                            its?.apply {
                                                withContext(Dispatchers.IO){
                                                    update(song.toggleLike())
                                                }
                                            }
                                        }
                                    }

                                }
                                if (text?.text.toString().contains("Remove")){
                                    ToastUtils.getDefaultMaker().setGravity(Gravity.CENTER,0,0).show("Removed from favourite")
                                }else{
                                    ToastUtils.getDefaultMaker().setGravity(Gravity.CENTER,0,0).show("Added to favourite")
                                }


                            }
                        }

                    }catch (e : Exception){
                        e.printStackTrace()
                    }
                }

            }

            override fun getItemCount(): Int = items.size
        }
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
            artistDetBanner(), this)
        if (normalBan == null) {
            if (SnapPosManager.cyIsShowingFLag) {
                LogUtils.e("artistDetBanner  ban inter is showing return------")
                return
            }
            if (isFinishing || isDestroyed) {
                LogUtils.e("artistDetBanner  ban destroy  return------")
                return
            }
            if (SnapPosManager.posIsCanShow(artistDetBanner())) {
                LogUtils.e("artistDetBanner  ban limit  return------")
                return
            }
            SnapMobManager.homeStudy.initLoadBanners(artistDetBanner(),object :
                NativeWeavesListener {
                override fun nativeIsWeavesSuc(sucFlag: Boolean) {
                    if (isFinishing || isDestroyed) {
                        return
                    }
                    if (sucFlag) {
                        LogUtils.e("artistDetBanner  ban load  suc------")
                        showBanners()
                    }
                }
            })
        } else {
            LogUtils.e("artistDetBanner  ban show  suc------")
            binding?.homeBans?.let {
                it.visibility = View.VISIBLE
                it.setStyles(MobToStyle.Builder().build())
                it.setNativeAd(normalBan)
                if(!SnapPosManager.posIsCanShow(artistDetBanner())){
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
}


