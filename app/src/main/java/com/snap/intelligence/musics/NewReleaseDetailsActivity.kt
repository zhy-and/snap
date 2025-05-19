package com.snap.intelligence.musics

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.google.android.gms.ads.nativead.NativeAd
import com.snap.intelligence.musics.constants.albumDetInter
import com.gyf.immersionbar.ImmersionBar
import com.snap.intelligence.musics.constants.SnapPosManager
import com.snap.intelligence.musics.constants.MESSAGE_MSG_CODE
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_APP_REFRESH
import com.snap.intelligence.musics.constants.NativeWeavesListener
import com.snap.intelligence.musics.constants.homeReleaseDetailBanner
import com.snap.intelligence.musics.constants.releaseDetailInter
import com.snap.intelligence.musics.ui.start.HomeActivity.Companion.playerConnection
import com.zionhuang.innertube.models.AlbumItem
import com.snap.intelligence.musics.databinding.NewReleaseDetailBinding
import com.snap.intelligence.musics.db.InternalDatabase
import com.snap.intelligence.musics.extensions.currentMetadata
import com.snap.intelligence.musics.local.randomBitmap
import com.snap.intelligence.musics.mob.MobToStyle
import com.snap.intelligence.musics.mob.SnapMobManager
import com.snap.intelligence.musics.models.MediaMetadata
import com.snap.intelligence.musics.ui.album.AlbumDetActivity
import com.snap.intelligence.musics.utils.joinByBullet
import com.snap.intelligence.musics.viewmodels.NewReleaseViewModel
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.launch
import options
import timber.log.Timber

class NewReleaseDetailsActivity : BasicView() {

    private lateinit var binding: NewReleaseDetailBinding
    private lateinit var newReleaseAlbumsAdapter: ItemsAdapter
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SnapMobManager.homeStudy.getShowCyAds(releaseDetailInter(),this)

        binding = NewReleaseDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ImmersionBar.with(this).transparentStatusBar().titleBar(binding.tops.root).init()
        val albumViewModel =
            NewReleaseViewModel(this,InternalDatabase.newInstance(this)

            )
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
        binding.bottomPlayer.container.setOnClickListener {
            startPlayerPage()
        }
        binding.specAnm2.post {
            binding.specAnm2.visibility = View.VISIBLE
            binding.specAnm2.startShimmerAnimation()
        }
        newReleaseAlbumsAdapter = ItemsAdapter(emptyList())
        binding.moodAndGenresRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.moodAndGenresRecyclerView?.addItemDecoration(EvenItemDecoration(SizeUtils.dp2px( 1f),2))
        binding.moodAndGenresRecyclerView.adapter = newReleaseAlbumsAdapter
        lifecycleScope.launch {

                albumViewModel?.newReleaseAlbums?.collect {
                    Timber.e(it.toString())
                    if (isFinishing || isDestroyed){
                        return@collect
                    }
                    binding.specAnm2.visibility = View.GONE
                    binding.specAnm2.stopShimmerAnimation()
                    it?.let {
                        its ->
                        newReleaseAlbumsAdapter?.addData(its)
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
    inner class ItemsAdapter( var items: List<AlbumItem>) : RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val itemTitleTextView: TextView = itemView.findViewById(R.id.title)
            val artist: TextView = itemView.findViewById(R.id.artist)
            val ivgs: ImageView = itemView.findViewById(R.id.thumbnail)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_on_album, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
          holder.itemTitleTextView.text = item.title
          holder.artist.text =  joinByBullet(item.artists?.joinToString { it.name })

            Glide.with(this@NewReleaseDetailsActivity)
                .load(item?.thumbnail)
                .apply(options)
                .thumbnail(
                    Glide.with(this@NewReleaseDetailsActivity)
                        .load(randomBitmap())
                        .apply(options)
                )
                .into(holder.ivgs)

            holder.itemView.setOnClickListener {
                startInterActivity(Intent(this@NewReleaseDetailsActivity, AlbumDetActivity::class.java).apply {
                    putExtra("albumId",item.id)
                }, albumDetInter(),false)
            }

        }

        override fun getItemCount(): Int = items.size
        fun addData(its: List<AlbumItem>) {
            items = its
            notifyDataSetChanged()
        }
    }
    private fun showBanners() {
        val normalBan: NativeAd? = SnapMobManager.homeStudy.showWeaveNatives(
            homeReleaseDetailBanner(), this)
        if (normalBan == null) {
            if (SnapPosManager.cyIsShowingFLag) {
                LogUtils.e("homeReleaseDetailBanner  ban inter is showing return------")
                return
            }
            if (isFinishing || isDestroyed) {
                LogUtils.e("homeReleaseDetailBanner  ban destroy  return------")
                return
            }
            if (SnapPosManager.posIsCanShow(homeReleaseDetailBanner())) {
                LogUtils.e("homeReleaseDetailBanner  ban limit  return------")
                return
            }
            SnapMobManager.homeStudy.initLoadBanners(homeReleaseDetailBanner(),object :
                NativeWeavesListener {
                override fun nativeIsWeavesSuc(sucFlag: Boolean) {
                    if (isFinishing || isDestroyed) {
                        return
                    }
                    if (sucFlag) {
                        LogUtils.e("homeReleaseDetailBanner  ban load  suc------")
                        showBanners()
                    }
                }
            })
        } else {
            LogUtils.e("homeReleaseDetailBanner  ban show  suc------")
            binding?.homeBans?.let {
                it.visibility = View.VISIBLE
                it.setStyles(MobToStyle.Builder().build())
                it.setNativeAd(normalBan)
                if(!SnapPosManager.posIsCanShow(homeReleaseDetailBanner())){
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
