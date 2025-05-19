package com.snap.intelligence.musics

import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zionhuang.innertube.models.SongItem
import com.snap.intelligence.musics.ui.start.HomeActivity.Companion.playerConnection
import com.snap.intelligence.musics.databinding.ItemOnSongBinding
import com.snap.intelligence.musics.extensions.togglePlayPause
import com.snap.intelligence.musics.local.randomBitmap
import com.snap.intelligence.musics.models.toMediaMetadata
import com.snap.intelligence.musics.playback.queues.YouTubeQueue
import options
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.ToastUtils
import com.snap.intelligence.musics.constants.playViewsInter
import com.snap.intelligence.musics.db.InternalDatabase
import com.snap.intelligence.musics.db.MusicDatabase
import com.snap.intelligence.musics.db.entities.AlbumWithSongs
import com.snap.intelligence.musics.db.entities.SongEntity
import com.snap.intelligence.musics.extensions.toMediaItem
import com.snap.intelligence.musics.local.showDialogs
import com.snap.intelligence.musics.playback.queues.LocalAlbumRadio
import com.snap.intelligence.musics.playback.queues.YouTubeQueue.Companion.radio
import com.snap.intelligence.musics.ui.music.MainPlayer
import com.zionhuang.innertube.models.WatchEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SongListAdapter(
    var isGone: Boolean = false,
    var index: AlbumWithSongs? = null,
    var homeActivity : BasicView
) : RecyclerView.Adapter<SongListAdapter.NewReleaseAlbumViewHolder>() {

    private var albums = listOf<SongItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewReleaseAlbumViewHolder {
        val binding = ItemOnSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewReleaseAlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewReleaseAlbumViewHolder, position: Int) {
        val album = albums[position]
        holder.bind(album,position)
    }

    override fun getItemCount(): Int = albums.size

    fun submitList(newAlbums: List<SongItem>) {
        albums = newAlbums
        notifyDataSetChanged()
    }

    inner class NewReleaseAlbumViewHolder(private val binding: ItemOnSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(album: SongItem, position: Int) {
            if (isGone){
                binding.thumbnail.visibility  = View.GONE
            }else{
                if (binding.thumbnail.isGone){
                    binding.thumbnail.visibility  = View.VISIBLE
                }
                Glide.with(itemView)
                    .load(album?.thumbnail)
                    .apply(options)
                    .thumbnail(
                        Glide.with(itemView)
                            .load(randomBitmap())
                            .apply(options)
                    )
                    .into(binding.thumbnail)
            }
           binding?.title?.text = album.title
           binding?.title2?.text = album.artists.firstOrNull()?.name

            binding?.ivgs?.setOnClickListener {
                try {
                    showDialogs(binding.ivgs.context,R.layout.dialog_menu,true, Gravity.BOTTOM).apply {
                        val text =   findViewById<TextView>(R.id.titles4)
                        var database : MusicDatabase ?= null
                        App.instance?.let {
                            database = InternalDatabase.newInstance(it)
                            database?.transaction {
                                lifecycleScope.launch {
                                    song(album.id).first().let {
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
                            playerConnection?.playQueue(radio(album.toMediaMetadata()))
                            if (homeActivity?.isFinishing == true || homeActivity?.isDestroyed == true){
                                return@setOnClickListener
                            }
                            homeActivity?.startInterActivity(Intent(homeActivity, MainPlayer::class.java), playViewsInter(),false)
                        }
                        findViewById<View>(R.id.play_next)?.setOnClickListener {
                            if (isShowing){
                                dismiss()
                            }
                            playerConnection?.playNext(album.toMediaItem())
                        }
                        findViewById<View>(R.id.add_queue)?.setOnClickListener {
                            if (isShowing){
                                dismiss()
                            }
                            playerConnection?.addToQueue(album.toMediaItem())
                        }
                        findViewById<View>(R.id.add_fav)?.setOnClickListener {
                            if (isShowing){
                                dismiss()
                            }
                            database?.transaction {
                                lifecycleScope.launch {
                                    song(album.id).first().let {
                                            its ->
//                                        Log.e("test",it.toString())
                                        if (its == null){
                                            withContext(Dispatchers.IO){
                                                insert(album.toMediaMetadata(), SongEntity::toggleLike)
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
                                ToastUtils.showShort("Removed from favourite")
                            }else{
                                ToastUtils.showShort("Added to favourite")
                            }

                        }
                    }
                }catch (e : Exception){
                    e.printStackTrace()
                }
            }
            binding?.all?.setOnClickListener {
                if (album.id == playerConnection?.mediaMetadata?.value?.id) {
                    playerConnection?.player?.togglePlayPause()
                } else {
                    if (index == null){
                        playerConnection?.playQueue(
                            YouTubeQueue(album.endpoint?: WatchEndpoint(videoId = album.id),album.toMediaMetadata())
                        )
                    }else{
                        playerConnection?.playQueue(
                            LocalAlbumRadio(index!!,position))
                    }

                }
                if (homeActivity?.isFinishing == true || homeActivity?.isDestroyed == true){
                    return@setOnClickListener
                }
                homeActivity?.startInterActivity(Intent(homeActivity, MainPlayer::class.java), playViewsInter(),false)
            }

        }}
}
