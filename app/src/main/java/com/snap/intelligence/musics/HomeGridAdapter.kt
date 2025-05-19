package com.snap.intelligence.musics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import artOptions3
import com.bumptech.glide.Glide
import com.zionhuang.innertube.models.AlbumItem
import com.zionhuang.innertube.models.ArtistItem
import com.zionhuang.innertube.models.PlaylistItem
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.models.YTItem
import com.snap.intelligence.musics.local.randomBitmap
import com.snap.intelligence.musics.utils.joinByBullet
import com.snap.intelligence.musics.utils.makeTimeString
import options

class HomeGridAdapter(
    private val onItemClickListener: (YTItem) -> Unit
) : RecyclerView.Adapter<HomeGridAdapter.ViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_new_release_album, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val album = its[position]
        holder.bind(album,onItemClickListener)
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val images: ImageView = itemView.findViewById(R.id.thumbnail)
        private val thumbnail2: ImageView = itemView.findViewById(R.id.thumbnail2)
        private val videoPlay: ImageView = itemView.findViewById(R.id.play_icon2)
        private val title: TextView = itemView.findViewById(R.id.title)
        private val subTitle: TextView = itemView.findViewById(R.id.artist)
        private val view: View = itemView.findViewById(R.id.container)

        fun bind(item: YTItem, onItemClickListener: (YTItem) -> Unit) {
            item?.apply {
                thumbnail?.let {
                    Glide.with(itemView)
                        .load(it)
                        .apply(options)
                        .thumbnail(
                            Glide.with(itemView)
                                .load(randomBitmap())
                                .apply(options)
                        )
                        .into(images)
                }
            }
            title.text = item?.title

            val subtitle = when (item) {
                is SongItem -> {
                    if (item.album == null){
                        thumbnail2?.let {
                            thumbnail2.visibility = View.VISIBLE
                            images?.visibility = View.GONE
                           videoPlay?.let {
                                    its ->
                                if(its.isGone){
                                    its.visibility = View.VISIBLE
                                }
                            }
                            Glide.with(itemView)
                                .asBitmap()
                                .load(item.thumbnail)
                                .apply(artOptions3)
                                .into(it)
                        }
                    }
                    joinByBullet(item.artists.joinToString { it.name }, makeTimeString(item.duration?.times(1000L)))
                }
                is AlbumItem -> joinByBullet(item.artists?.joinToString { it.name }, item.year?.toString())
                is ArtistItem -> null
                is PlaylistItem -> joinByBullet(item.author?.name, item.songCountText)
            }
            if (subtitle != null) {
                subTitle.text = subtitle
            }
            view.setOnClickListener { onItemClickListener(item) }
        }
    }

    private var its = listOf<YTItem>()





    override fun getItemCount(): Int = its.size
    fun setList(items: List<YTItem>) {
        items?.let {
            its =  items
            notifyDataSetChanged()
        }

    }


}
