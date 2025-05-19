package com.snap.intelligence.musics.ui.history

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
import com.google.android.gms.ads.nativead.NativeAd
import com.snap.intelligence.musics.BasicView
import com.snap.intelligence.musics.EvenItemDecoration
import com.snap.intelligence.musics.MoodAndGenresAdapter
import com.snap.intelligence.musics.NewReleaseAlbumsAdapter
import com.snap.intelligence.musics.R
import com.snap.intelligence.musics.ui.start.HomeActivity.Companion.playerConnection
import com.snap.intelligence.musics.databinding.ActivityQueueBinding
import com.snap.intelligence.musics.db.InternalDatabase
import com.snap.intelligence.musics.db.MusicDatabase
import com.snap.intelligence.musics.db.entities.EventWithSong
import com.snap.intelligence.musics.extensions.currentMetadata
import com.snap.intelligence.musics.extensions.togglePlayPause
import com.snap.intelligence.musics.local.posBitmap
import com.snap.intelligence.musics.local.showDialogs
import com.snap.intelligence.musics.models.MediaMetadata
import com.snap.intelligence.musics.models.toMediaMetadata
import com.snap.intelligence.musics.playback.queues.YouTubeQueue
import com.snap.intelligence.musics.utils.joinByBullet
import com.snap.intelligence.musics.viewmodels.HistoryViewModel
import com.snap.intelligence.musics.viewmodels.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import options

import com.snap.intelligence.musics.constants.historyBanner
import com.snap.intelligence.musics.mob.MobToStyle
import com.snap.intelligence.musics.mob.SnapMobManager
import com.snap.intelligence.musics.constants.NativeWeavesListener
import com.tencent.mmkv.MMKV
import com.snap.intelligence.musics.constants.SnapPosManager
import com.snap.intelligence.musics.constants.MESSAGE_MSG_CODE
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_APP_REFRESH
import com.blankj.utilcode.util.LogUtils
import com.snap.intelligence.musics.constants.historyInter

class HistoryActivity : BasicView() {

    private var data: MusicDatabase ?= null
    private lateinit var binding: ActivityQueueBinding
    private  var historyAdapter: ItemsAdapter?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SnapMobManager.homeStudy.getShowCyAds(historyInter(),this)

        binding = ActivityQueueBinding.inflate(layoutInflater)
        setContentView(binding.root)
         data = InternalDatabase.newInstance(this@HistoryActivity)
        binding.titles.apply {
            lists.setImageResource(R.mipmap.search_title_btn)
            nowPlayingText.text = "History"
            closeButton.setOnClickListener {
                finishCurrent()
            }
            lists.setOnClickListener {
                startSearchPage(true)
            }
        }
        binding.bottomPlayer.container.setOnClickListener {
            startPlayerPage(true)
        }
        binding.tvs.text = "No songs in the history."
        lifecycleScope.launch {
            val viewModel =
                HistoryViewModel(data!!
                )
            viewModel.events.collect {
                if ( it.values.isNullOrEmpty()){
                    initEmptyLayout()
                    return@collect
                }
                val list = ArrayList<EventWithSong>()
                it.values?.forEach { its ->
                    list.addAll(its)
                }
                if (binding.emptyLayout.isVisible){
                    binding.emptyLayout.visibility = View.GONE
                }
                historyAdapter = ItemsAdapter(list)
                binding.moodAndGenresRecyclerView.layoutManager = LinearLayoutManager(this@HistoryActivity)
                binding.moodAndGenresRecyclerView.adapter = historyAdapter
            }
        }
        }

    private  var newReleaseAlbumsAdapter: NewReleaseAlbumsAdapter?= null
    private  var moodAndGenresAdapter: MoodAndGenresAdapter?= null
    private fun initEmptyLayout() {
        val datas = data ?: return
        binding.emptyLayout.visibility = View.VISIBLE
        HomeViewModel(
            this@HistoryActivity,
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
                        newReleaseAlbumsAdapter = NewReleaseAlbumsAdapter(this@HistoryActivity)
                        binding.newReleaseAlbumsRecyclerView.layoutManager = LinearLayoutManager(
                            this@HistoryActivity,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        binding.newReleaseAlbumsRecyclerView.adapter = newReleaseAlbumsAdapter
                    }
                    if (moodAndGenresAdapter == null){
                        moodAndGenresAdapter = MoodAndGenresAdapter(this@HistoryActivity)
                        binding.homeMoodAndGenresRecyclerView.layoutManager =
                            GridLayoutManager(this@HistoryActivity, 2)

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
    inner class ItemsAdapter(var items: ArrayList<EventWithSong>) : RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val itemTitleTextView: TextView = itemView.findViewById(R.id.title)
            val subtitle: TextView = itemView.findViewById(R.id.title2)
            val ivgs: ImageView = itemView.findViewById(R.id.thumbnail)
            val delete: ImageView = itemView.findViewById(R.id.ivgs)

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_on_song, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.delete.setImageResource(R.mipmap.delete)
            val item = items[position]
            item?.song?.apply {
                holder.itemTitleTextView.text = title
//                holder.subtitle.text = joinByBullet(
//                    artists.joinToString { it.name },
//                    makeTimeString(song.duration * 1000L)
//                )
                                holder.subtitle.text = joinByBullet(
                    artists.joinToString { it.name }
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
            holder.delete.setOnClickListener {
                try {
                    showDialogs(this@HistoryActivity, R.layout.dialog_yt).apply {
                        findViewById<View>(R.id.cancel_button)?.setOnClickListener {
                            if (isShowing && !isDestroyed && !isFinishing){
                                dismiss()
                            }
                        }
                        findViewById<TextView>(R.id.titles)?.text = "Are you sure you want to delete this song from history?"
                        findViewById<View>(R.id.ok_btn)?.setOnClickListener {
                            if (isShowing && !isDestroyed && !isFinishing){
                                dismiss()
                            }
                            items.remove(item)
                            notifyDataSetChanged()
                            Thread{
                                data?.delete(item.event)
                            }.start()
                        }
                    }

                }catch (e : Exception){
                    e.printStackTrace()
                }

            }

            holder.itemView.setOnClickListener {
                if (item.song.id == playerConnection?.mediaMetadata?.value?.id) {
                    playerConnection?.player?.togglePlayPause()
                } else {
                    playerConnection?.playQueue(
                        YouTubeQueue.radio(item.song.toMediaMetadata())
                    )
                }
                startPlayerPage(true)
            }

        }

        override fun getItemCount(): Int = items.size

    }

    private fun showBanners() {
        val normalBan: NativeAd? = SnapMobManager.homeStudy.showWeaveNatives(
            historyBanner(), this)
        if (normalBan == null) {
            if (SnapPosManager.cyIsShowingFLag) {
                LogUtils.e("historyBanner  ban inter is showing return------")
                return
            }
            if (isFinishing || isDestroyed) {
                LogUtils.e("historyBanner  ban destroy  return------")
                return
            }
            if (SnapPosManager.posIsCanShow(historyBanner())) {
                LogUtils.e("historyBanner  ban limit  return------")
                return
            }
            SnapMobManager.homeStudy.initLoadBanners(historyBanner(),object :
                NativeWeavesListener {
                override fun nativeIsWeavesSuc(sucFlag: Boolean) {
                    if (isFinishing || isDestroyed) {
                        return
                    }
                    if (sucFlag) {
                        LogUtils.e("historyBanner  ban load  suc------")
                        showBanners()
                    }
                }
            })
        } else {
            LogUtils.e("historyBanner  ban show  suc------")
            binding?.homeBans?.let {
                it.visibility = View.VISIBLE
                it.setStyles(MobToStyle.Builder().build())
                it.setNativeAd(normalBan)
                if(!SnapPosManager.posIsCanShow(historyBanner())){
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
