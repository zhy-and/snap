package com.snap.intelligence.musics.ui.collect

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.snap.intelligence.musics.BasicView
import com.snap.intelligence.musics.EvenItemDecoration
import com.snap.intelligence.musics.MoodAndGenresAdapter
import com.snap.intelligence.musics.NewReleaseAlbumsAdapter
import com.snap.intelligence.musics.R
import com.snap.intelligence.musics.ui.start.HomeActivity.Companion.playerConnection
import com.snap.intelligence.musics.constants.SongSortType
import com.snap.intelligence.musics.databinding.ActivityQueueBinding
import com.snap.intelligence.musics.db.InternalDatabase
import com.snap.intelligence.musics.db.MusicDatabase
import com.snap.intelligence.musics.db.entities.Song
import com.snap.intelligence.musics.extensions.currentMetadata
import com.snap.intelligence.musics.extensions.toMediaItem
import com.snap.intelligence.musics.extensions.togglePlayPause
import com.snap.intelligence.musics.local.posBitmap
import com.snap.intelligence.musics.models.MediaMetadata
import com.snap.intelligence.musics.playback.queues.ListQueue
import com.snap.intelligence.musics.utils.joinByBullet
import com.snap.intelligence.musics.utils.makeTimeString
import com.snap.intelligence.musics.viewmodels.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import options

import com.snap.intelligence.musics.mob.MobToStyle
import com.snap.intelligence.musics.mob.SnapMobManager
import com.snap.intelligence.musics.constants.NativeWeavesListener
import com.tencent.mmkv.MMKV
import com.snap.intelligence.musics.constants.SnapPosManager
import com.snap.intelligence.musics.constants.MESSAGE_MSG_CODE
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_APP_REFRESH
import com.blankj.utilcode.util.LogUtils
import com.google.android.gms.ads.nativead.NativeAd
import com.snap.intelligence.musics.constants.collectBanner
import com.snap.intelligence.musics.constants.collectInter

class CollectActivity : BasicView() {

    private var data: MusicDatabase ?= null
    private lateinit var binding: ActivityQueueBinding
    private  var favAdapter: ItemsAdapter?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SnapMobManager.homeStudy.getShowCyAds(collectInter(),this)

        binding = ActivityQueueBinding.inflate(layoutInflater)
        setContentView(binding.root)
       data = InternalDatabase.newInstance(this@CollectActivity)
        binding.titles.apply {
            lists.setImageResource(R.mipmap.search_title_btn)
            nowPlayingText.text = "Favourite"

            closeButton.setOnClickListener {
                finishCurrent()
            }
            lists.setOnClickListener {
                startSearchPage(true)
            }

        }
        binding.ivs.setImageResource(R.mipmap.collect_empty)
        binding.tvs.text = "No songs in favourite."


        lifecycleScope.launch {

            playerConnection?.mediaMetadata?.collect{
                updatePlayerUI(it)
            }

        }
        binding.bottomPlayer.container.setOnClickListener {
            startPlayerPage(true)
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
        refreshCollectItem()

    }


    private fun refreshCollectItem() {
        lifecycleScope.launch {
            if (data?.likedSongs(SongSortType.CREATE_DATE, true) == null) {
                initEmptyLayout()
                return@launch
            }
            data?.likedSongs(SongSortType.CREATE_DATE, true)?.collect {
                if (it.isNullOrEmpty()) {
                    initEmptyLayout()
                    return@collect
                }
                if (binding.emptyLayout.isVisible){
                    binding.emptyLayout.visibility = View.GONE
                }
                binding.listLayout.visibility = View.VISIBLE
                favAdapter = ItemsAdapter(it)
                binding.moodAndGenresRecyclerView.layoutManager =
                    LinearLayoutManager(this@CollectActivity)
                binding.moodAndGenresRecyclerView.adapter = favAdapter
            }

        }
    }

    private  var newReleaseAlbumsAdapter: NewReleaseAlbumsAdapter?= null
    private  var moodAndGenresAdapter: MoodAndGenresAdapter?= null
    private fun initEmptyLayout() {
        val datas = data ?: return
        binding.emptyLayout.visibility = View.VISIBLE
        binding.listLayout.visibility = View.GONE
        HomeViewModel(
            this@CollectActivity,
            datas
        ).apply {
            binding.specAnm.post {
                binding.specAnm.visibility = View.VISIBLE
                binding.specAnm.startShimmerAnimation()
            }
            lifecycleScope.launch {
                explorePage?.collect {
                    if (isFinishing || isDestroyed) {
                        return@collect
                    }
                    binding.specAnm.visibility = View.GONE
                    binding.specAnm.stopShimmerAnimation()
                    if (newReleaseAlbumsAdapter == null){
                        newReleaseAlbumsAdapter = NewReleaseAlbumsAdapter(this@CollectActivity)
                        binding.newReleaseAlbumsRecyclerView.layoutManager = LinearLayoutManager(
                            this@CollectActivity,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        binding.newReleaseAlbumsRecyclerView.adapter = newReleaseAlbumsAdapter
                    }
                    if (moodAndGenresAdapter == null){
                        moodAndGenresAdapter = MoodAndGenresAdapter(this@CollectActivity)
                        binding.homeMoodAndGenresRecyclerView.layoutManager =
                            GridLayoutManager(this@CollectActivity, 2)

                        binding.homeMoodAndGenresRecyclerView?.addItemDecoration(EvenItemDecoration(SizeUtils.dp2px( 10f),2))

                        binding.homeMoodAndGenresRecyclerView.adapter = moodAndGenresAdapter
                    }
                    withContext(Dispatchers.Main) {
                        it?.newReleaseAlbums?.apply {
                            newReleaseAlbumsAdapter?.submitList(this)
                        }
                        it?.moodAndGenres?.apply {
                            moodAndGenresAdapter?.submitList(this)
                        }
                    }
                }
            }
        }
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

    private fun updatePlayerUI(metadata: MediaMetadata?) {
        addUIPlayer(binding.bottomPlayer.artist,binding.bottomPlayer.title,binding.bottomPlayer.thumbnail,metadata)
    }
    inner class ItemsAdapter(var items: List<Song>) : RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {


        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val itemTitleTextView: TextView = itemView.findViewById(R.id.title)
            val subtitle: TextView = itemView.findViewById(R.id.title2)
            val ivgs: ImageView = itemView.findViewById(R.id.thumbnail)
            val collect: ImageView = itemView.findViewById(R.id.ivgs)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_on_song, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            item?.apply {
                holder.itemTitleTextView.text = title
                holder.subtitle.text = joinByBullet(
                    artists.joinToString { it.name },
                    makeTimeString(song.duration * 1000L)
                )
                Glide.with(holder.itemView)
                    .load(thumbnailUrl)
                    .apply(options)
                    .thumbnail(
                        Glide.with(holder.itemView)
                            .load(posBitmap(position))
                            .apply(options)
                    )
                    .into(holder.ivgs)
            }
            holder.collect.setImageResource(R.mipmap.icon_collect)

            holder.itemView.setOnClickListener {
                if (item.song.id == playerConnection?.mediaMetadata?.value?.id) {
                    playerConnection?.player?.togglePlayPause()
                } else {
                    playerConnection?.playQueue(ListQueue(title = "Favourite",items.map { it.toMediaItem() }, startIndex = position))
                }
                startPlayerPage(true)

            }

        }

        override fun getItemCount(): Int = items.size

    }

    private fun showBanners() {
        val normalBan: NativeAd? = SnapMobManager.homeStudy.showWeaveNatives(
            collectBanner(), this)
        if (normalBan == null) {
            if (SnapPosManager.cyIsShowingFLag) {
                LogUtils.e("collectBanner  ban inter is showing return------")
                return
            }
            if (isFinishing || isDestroyed) {
                LogUtils.e("collectBanner  ban destroy  return------")
                return
            }
            if (SnapPosManager.posIsCanShow(collectBanner())) {
                LogUtils.e("collectBanner  ban limit  return------")
                return
            }
            SnapMobManager.homeStudy.initLoadBanners(collectBanner(),object :
                NativeWeavesListener {
                override fun nativeIsWeavesSuc(sucFlag: Boolean) {
                    if (isFinishing || isDestroyed) {
                        return
                    }
                    if (sucFlag) {
                        LogUtils.e("collectBanner  ban load  suc------")
                        showBanners()
                    }
                }
            })
        } else {
            LogUtils.e("collectBanner  ban show  suc------")
            binding?.homeBans?.let {
                it.visibility = View.VISIBLE
                it.setStyles(MobToStyle.Builder().build())
                it.setNativeAd(normalBan)
                if(!SnapPosManager.posIsCanShow(collectBanner())){
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


}
