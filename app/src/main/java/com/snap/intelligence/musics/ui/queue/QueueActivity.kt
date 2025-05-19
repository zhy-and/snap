package com.snap.intelligence.musics.ui.queue

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Timeline
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.google.android.gms.ads.nativead.NativeAd
import com.snap.intelligence.musics.BasicView
import com.snap.intelligence.musics.EvenItemDecoration
import com.snap.intelligence.musics.MoodAndGenresAdapter
import com.snap.intelligence.musics.NewReleaseAlbumsAdapter
import com.snap.intelligence.musics.R
import com.snap.intelligence.musics.constants.SnapPosManager
import com.snap.intelligence.musics.constants.MESSAGE_MSG_CODE
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_APP_REFRESH
import com.snap.intelligence.musics.constants.NativeWeavesListener
import com.snap.intelligence.musics.constants.queueInter
import com.snap.intelligence.musics.constants.queueListBanner
import com.snap.intelligence.musics.ui.start.HomeActivity.Companion.playerConnection
import com.snap.intelligence.musics.databinding.ActivityQueueBinding
import com.snap.intelligence.musics.db.InternalDatabase
import com.snap.intelligence.musics.extensions.currentMetadata
import com.snap.intelligence.musics.extensions.getQueueWindows
import com.snap.intelligence.musics.extensions.metadata
import com.snap.intelligence.musics.extensions.togglePlayPause
import com.snap.intelligence.musics.local.posBitmap
import com.snap.intelligence.musics.local.showDialogs
import com.snap.intelligence.musics.mob.MobToStyle
import com.snap.intelligence.musics.mob.SnapMobManager
import com.snap.intelligence.musics.models.MediaMetadata
import com.snap.intelligence.musics.viewmodels.HomeViewModel
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import options
import timber.log.Timber

class QueueActivity : BasicView() {


    override fun onResume() {
        super.onResume()
        if (playerConnection != null && playerConnection?.player?.currentMetadata != null){
            playerConnection?.mediaMetadata?.let {
                updatePlayerUI(it.value)
            }
        }
        showBanners()
        
    }

    private var songAdapter: ItemsAdapter?= null
    private lateinit var binding: ActivityQueueBinding
    private  var newReleaseAlbumsAdapter: NewReleaseAlbumsAdapter?= null
    private  var moodAndGenresAdapter: MoodAndGenresAdapter?= null

    private fun updatePlayerUI(metadata: MediaMetadata?) {
        addUIPlayer(binding.bottomPlayer.artist,binding.bottomPlayer.title,binding.bottomPlayer.thumbnail,metadata)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SnapMobManager.homeStudy.getShowCyAds(queueInter(),this)

        binding = ActivityQueueBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.titles.apply {
            lists.setImageResource(R.mipmap.search_title_btn)
            closeButton.setOnClickListener {
                finish()
            }
            lists.setOnClickListener {
                startSearchPage()
            }
        }
        songAdapter = ItemsAdapter()
        binding.titles.apply {
            lists.setImageResource(R.mipmap.search_title_btn)
            nowPlayingText.text = "Queue"
            closeButton.setOnClickListener {
                finishCurrent()
            }
            lists.setOnClickListener {
                startSearchPage()
            }

        }

        binding.bottomPlayer.container.setOnClickListener {
            startPlayerPage()
        }
        binding.moodAndGenresRecyclerView.layoutManager = LinearLayoutManager(this@QueueActivity)
        binding.moodAndGenresRecyclerView.adapter = songAdapter
        lifecycleScope.launch {
            if (playerConnection?.queueWindows?.value.isNullOrEmpty()){
                initEmptyLayout()
                return@launch
            }
            playerConnection?.queueWindows?.value?.let {
                if (it.isNullOrEmpty()){
                    return@launch
                }

                try {
                    val arrayList = ArrayList<Timeline.Window>()
                    it.forEach { one ->
                        arrayList.add(one)
                    }
                    if (arrayList.isNullOrEmpty()){
                        initEmptyLayout()
                        return@let
                    }
                    binding.listLayout.visibility = View.VISIBLE
                    binding.emptyLayout.visibility = View.GONE
                    songAdapter?.addData(arrayList)
                }catch (e : Exception){
                    e.printStackTrace()
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

    private fun initEmptyLayout() {
        binding.emptyLayout.visibility = View.VISIBLE
        binding.listLayout.visibility = View.GONE
        HomeViewModel(
            this@QueueActivity,
            InternalDatabase.newInstance(this@QueueActivity)
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
    //                                    binding.specAnm.visibility = View.GONE
    //                                    binding.specAnm.stopShimmerAnimation()

                    binding.specAnm.visibility = View.GONE
                    binding.specAnm.stopShimmerAnimation()
                    newReleaseAlbumsAdapter = NewReleaseAlbumsAdapter(this@QueueActivity)
                    moodAndGenresAdapter = MoodAndGenresAdapter(this@QueueActivity)
                    binding.newReleaseAlbumsRecyclerView.layoutManager = LinearLayoutManager(
                        this@QueueActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    binding.newReleaseAlbumsRecyclerView.adapter = newReleaseAlbumsAdapter

                    binding.homeMoodAndGenresRecyclerView.layoutManager =
                        GridLayoutManager(this@QueueActivity, 2)
                    binding.homeMoodAndGenresRecyclerView?.addItemDecoration(EvenItemDecoration(SizeUtils.dp2px( 10f),2))

                    binding.homeMoodAndGenresRecyclerView.adapter = moodAndGenresAdapter
                    binding.homeMoodAndGenresRecyclerView.isNestedScrollingEnabled = false
                    withContext(Dispatchers.Main) {
                        it?.newReleaseAlbums?.apply {
                            newReleaseAlbumsAdapter?.submitList(this)
                        }
                        it?.moodAndGenres?.apply {
                            moodAndGenresAdapter?.submitList(this)
                        }
                    }
                    Timber.e(it.toString())
    //                        }
                }
            }
        }
    }

    inner class ItemsAdapter : RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {
        var items: ArrayList<Timeline.Window> = ArrayList()

        fun  addData(data: ArrayList<Timeline.Window>){
            items.clear()
            items.addAll(data)
            notifyDataSetChanged()
        }
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
            item.mediaItem.metadata?.let {
                holder.itemTitleTextView.text = it.title
                holder.subtitle.text = item.mediaItem.metadata?.artists?.joinToString { it.name }
                Glide.with(holder.itemView)
                    .load(it.thumbnailUrl)
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
                    showDialogs(this@QueueActivity, R.layout.dialog_yt).apply {
                        findViewById<View>(R.id.cancel_button)?.setOnClickListener {
                            if (isShowing && !isDestroyed && !isFinishing){
                                dismiss()
                            }
                        }
                        findViewById<TextView>(R.id.titles)?.text = "Are you sure you want to delete this song from queue?"
                        findViewById<View>(R.id.ok_btn)?.setOnClickListener {
                            if (isShowing && !isDestroyed && !isFinishing){
                                dismiss()
                            }
                            if (items?.size == 1){
                                ToastUtils.getDefaultMaker().setGravity(Gravity.CENTER,0,0).show("Only one song left, please add more!")
                                return@setOnClickListener
                            }
//                        playerConnection?.player?.mediaItems?.forEachIndexed { index, mediaItem ->
//                            if (mediaItem.mediaMetadata.title == item.mediaItem.metadata?.title) {
//                                Log.e("-----f","fff"+item.firstPeriodIndex)
//                                playerConnection?.player?.removeMediaItem(index)
//                                items.remove(item)
//                                notifyDataSetChanged()
//                            }
//                        }
//                        items.sortedBy {
//                            -it.firstPeriodIndex
//                        }.forEach {
//                            its ->
                            playerConnection?.player?.removeMediaItem(item.firstPeriodIndex)
//                        }
                            try {
                                val arrayList = ArrayList<Timeline.Window>()
                                playerConnection?.player?.getQueueWindows()?.forEach { one ->
                                    arrayList.add(one)
                                }
                                addData(arrayList)
                            }catch (e : Exception){
                                e.printStackTrace()
                            }

                        }
                    }

                }catch (e : Exception){
                    e.printStackTrace()
                }

            }

            holder.itemView.setOnClickListener {


                if (position == playerConnection?.currentWindowIndex?.value) {
                    playerConnection?.player?.togglePlayPause()
                } else {


                    playerConnection?.player?.seekToDefaultPosition(items[position].firstPeriodIndex)
                    playerConnection?.player?.playWhenReady = true
                }
               startPlayerPage()
            }

        }

        override fun getItemCount(): Int = items.size

    }

    private fun showBanners() {
        val normalBan: NativeAd? = SnapMobManager.homeStudy.showWeaveNatives(
            queueListBanner(), this)
        if (normalBan == null) {
            if (SnapPosManager.cyIsShowingFLag) {
                LogUtils.e("queueListBanner  ban inter is showing return------")
                return
            }
            if (isFinishing || isDestroyed) {
                LogUtils.e("queueListBanner  ban destroy  return------")
                return
            }
            if (SnapPosManager.posIsCanShow(queueListBanner())) {
                LogUtils.e("queueListBanner  ban limit  return------")
                return
            }
            SnapMobManager.homeStudy.initLoadBanners(queueListBanner(),object :
                NativeWeavesListener {
                override fun nativeIsWeavesSuc(sucFlag: Boolean) {
                    if (isFinishing || isDestroyed) {
                        return
                    }
                    if (sucFlag) {
                        LogUtils.e("queueListBanner  ban load  suc------")
                        showBanners()
                    }
                }
            })
        } else {
            LogUtils.e("queueListBanner  ban show  suc------")
            binding?.homeBans?.let {
                it.visibility = View.VISIBLE
                it.setStyles(MobToStyle.Builder().build())
                it.setNativeAd(normalBan)
                if(!SnapPosManager.posIsCanShow(queueListBanner())){
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
