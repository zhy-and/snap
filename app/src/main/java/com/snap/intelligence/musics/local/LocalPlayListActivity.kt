package com.snap.intelligence.musics.local

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Timeline
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SizeUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.snap.intelligence.musics.BasicView
import com.snap.intelligence.musics.R
import com.snap.intelligence.musics.constants.NativeWeavesListener
import com.snap.intelligence.musics.constants.SnapPosManager
import com.snap.intelligence.musics.constants.queueInter
import com.snap.intelligence.musics.constants.queueListBanner
import com.snap.intelligence.musics.databinding.ActivityQueueBinding
import com.snap.intelligence.musics.extensions.currentMetadata
import com.snap.intelligence.musics.extensions.mediaItems
import com.snap.intelligence.musics.extensions.metadata
import com.snap.intelligence.musics.extensions.togglePlayPause
import com.snap.intelligence.musics.local.LocalHomeActivity.Companion.playerConnection
import com.snap.intelligence.musics.mob.MobToStyle
import com.snap.intelligence.musics.mob.SnapMobManager
import com.snap.intelligence.musics.models.MediaMetadata
import kotlinx.coroutines.launch

class LocalPlayListActivity : BasicView() {
    override fun onResume() {
        super.onResume()
        if (playerConnection != null && playerConnection?.player?.currentMetadata != null){
            playerConnection?.mediaMetadata?.let {
                updatePlayerUI(it.value)
            }
        }
        showBanners()
    }
    private lateinit var binding: ActivityQueueBinding
    private lateinit var newReleaseAlbumsAdapter: ItemsAdapter
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
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SnapMobManager.homeStudy.getShowCyAds(queueInter(),this)
        binding = ActivityQueueBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.titles.apply {
            lists.visibility = View.GONE
            closeButton.setOnClickListener {
                finish()
            }
        }
        newReleaseAlbumsAdapter = ItemsAdapter()

        binding.bottomPlayer.container.setOnClickListener {
            startActivity(Intent(this, LocalMusicPlayerActivity::class.java))
        }
        binding.moodAndGenresRecyclerView.layoutManager = LinearLayoutManager(this@LocalPlayListActivity)
        binding.moodAndGenresRecyclerView.adapter = newReleaseAlbumsAdapter
        lifecycleScope.launch {
            playerConnection?.queueWindows?.value?.let {
                if (it.isNullOrEmpty()){
                    return@launch
                }
//                Log.e("------",""+it.size)
                try {
                    val arrayList = ArrayList<Timeline.Window>()
                    it.forEach { one ->
                        arrayList.add(one)
                    }
                    newReleaseAlbumsAdapter?.addData(arrayList)
                }catch (e : Exception){
                    e.printStackTrace()
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


    }
    inner class ItemsAdapter() : RecyclerView.Adapter<ItemsAdapter.ViewHolder>() {
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
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_local_queue, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            item.mediaItem.metadata?.let {
                holder.itemTitleTextView.text = it.title
                holder.subtitle.text = item.mediaItem.metadata?.artists?.joinToString { it.name }
                Glide.with(holder.itemView.context).asBitmap().load(posBitmap(position))
                    .apply(RequestOptions().transform(RoundedCorners(SizeUtils.dp2px(6f))))
                    .into(holder.ivgs)
            }
            holder.delete.setOnClickListener {
                if (items?.size == 1){
                    ToastUtils.getDefaultMaker().setGravity(Gravity.CENTER,0,0).show("Only one song left, please add more!")
                    return@setOnClickListener
                }
                playerConnection?.player?.mediaItems?.forEachIndexed { index, mediaItem ->
                    if (mediaItem.mediaMetadata.title == item.mediaItem.metadata?.title) {
                        playerConnection?.player?.removeMediaItem(index)
                    }
                    }
                items.remove(item)
                notifyDataSetChanged()
            }

            holder.itemView.setOnClickListener {
                if (position == playerConnection?.currentWindowIndex?.value) {
                    playerConnection?.player?.togglePlayPause()
                } else {
                    playerConnection?.player?.seekToDefaultPosition(item.firstPeriodIndex)
                    playerConnection?.player?.playWhenReady = true
                }
                startActivity(Intent(this@LocalPlayListActivity, LocalMusicPlayerActivity::class.java))
            }

        }

        override fun getItemCount(): Int = items.size

    }

}
