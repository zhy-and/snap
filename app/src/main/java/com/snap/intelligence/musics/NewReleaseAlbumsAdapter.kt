package com.snap.intelligence.musics

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.snap.intelligence.musics.constants.albumDetInter
import com.zionhuang.innertube.models.AlbumItem
import com.snap.intelligence.musics.databinding.ItemNewReleaseAlbumBinding
import com.snap.intelligence.musics.extensions.logClickEvents
import com.snap.intelligence.musics.local.randomBitmap
import com.snap.intelligence.musics.ui.album.AlbumDetActivity
import com.snap.intelligence.musics.utils.joinByBullet
import options

class NewReleaseAlbumsAdapter(var homeActivity: BasicView) : RecyclerView.Adapter<NewReleaseAlbumsAdapter.NewReleaseAlbumViewHolder>() {

    private var albums = listOf<AlbumItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewReleaseAlbumViewHolder {
        val binding = ItemNewReleaseAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewReleaseAlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewReleaseAlbumViewHolder, position: Int) {
        val album = albums[position]
        holder.bind(album)
    }

    override fun getItemCount(): Int = albums.size

    fun submitList(newAlbums: List<AlbumItem>) {
        albums = newAlbums
        notifyDataSetChanged()
    }

    inner class NewReleaseAlbumViewHolder(private val binding: ItemNewReleaseAlbumBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(album: AlbumItem) {
           binding?.artist?.text = joinByBullet(album.artists?.joinToString { it.name }, album.year?.toString())
           binding?.title?.text = album.title
            binding.container?.setOnClickListener {
                if (homeActivity?.isFinishing == true || homeActivity?.isDestroyed == true){
                    return@setOnClickListener
                }
                homeActivity?.startInterActivity(Intent(homeActivity, AlbumDetActivity::class.java).apply {
                    putExtra("albumId",album.id)
                }, albumDetInter(),false)
                logClickEvents("home_release_to_album_detail")

            }


           album?.thumbnail?.let {
                   it ->
               Glide.with(itemView)
                   .load(it)
                   .apply(options)
                   .thumbnail(
                       Glide.with(itemView)
                           .load(randomBitmap())
                           .apply(options)
                   )
                   .into(binding.thumbnail)
               }
           }


        }
}
