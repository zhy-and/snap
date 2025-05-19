package com.snap.intelligence.musics

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.snap.intelligence.musics.constants.albumDetInter
import com.snap.intelligence.musics.constants.artistDetInter
import com.snap.intelligence.musics.constants.playViewsInter
import com.snap.intelligence.musics.constants.playlistDetInter
import com.zionhuang.innertube.models.AlbumItem
import com.zionhuang.innertube.models.ArtistItem
import com.zionhuang.innertube.models.PlaylistItem
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.pages.HomePage
import com.snap.intelligence.musics.ui.start.HomeActivity.Companion.playerConnection
import com.snap.intelligence.musics.databinding.ItemHomePageSectionBinding
import com.snap.intelligence.musics.extensions.logClickEvents
import com.snap.intelligence.musics.models.toMediaMetadata
import com.snap.intelligence.musics.playback.queues.YouTubeQueue
import com.snap.intelligence.musics.ui.album.AlbumDetActivity
import com.snap.intelligence.musics.ui.artist.ArtistDetActivity
import com.snap.intelligence.musics.ui.music.MainPlayer
import com.snap.intelligence.musics.ui.playlist.PlayListDetActivity
import com.snap.intelligence.musics.ui.start.HomeActivity

class HomePageSectionsAdapter(var homeActivity: HomeActivity) : RecyclerView.Adapter<HomePageSectionsAdapter.HomePageSectionViewHolder>() {

    private var sections = listOf<HomePage.Section>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePageSectionViewHolder {
        val binding = ItemHomePageSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomePageSectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomePageSectionViewHolder, position: Int) {
        val section = sections[position]
        holder.bind(section)
    }

    override fun getItemCount(): Int = sections.size

    fun submitList(newSections: List<HomePage.Section>) {
        sections = newSections
        notifyDataSetChanged()
    }

    inner class HomePageSectionViewHolder(private val binding: ItemHomePageSectionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(section: HomePage.Section) {
            binding.title.text = section.title
//            Log.e("label----",section.label.toString())
//            section.thumbnail?.let { thumbnailUrl ->
//                Glide.with(binding.root.context).asBitmap().load(thumbnailUrl).into(binding.thumbnail)
//            }

            binding.itemsRecyclerView.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
            val adapter = HomeGridAdapter(
                onItemClickListener = { item ->
                    when (item) {
                        is SongItem -> {
//                            Log.e("-------type","song")
                            playerConnection?.playQueue(YouTubeQueue.radio(item.toMediaMetadata()))
                            if (homeActivity?.isFinishing == true || homeActivity?.isDestroyed == true){
                                return@HomeGridAdapter
                            }
                            logClickEvents("home_to_player")
                            homeActivity?.startInterActivity(Intent(homeActivity, MainPlayer::class.java), playViewsInter(),false)
//                            playerConnection.playQueue(YouTubeQueue(item.endpoint ?: WatchEndpoint(videoId = item.id), item.toMediaMetadata()))
                        }
//                            playerConnection.playQueue(YouTubeQueue(item.endpoint ?: WatchEndpoint(videoId = item.id), item.toMediaMetadata()))
                        is AlbumItem ->{
//                            Log.e("-------type","album")
                            if (homeActivity?.isFinishing == true || homeActivity?.isDestroyed == true){
                                return@HomeGridAdapter
                            }
                            logClickEvents("home_to_album_det")

                            homeActivity?.startInterActivity(Intent(homeActivity, AlbumDetActivity::class.java).apply {
                                putExtra("albumId",item.id)
                            }, albumDetInter(),false)
                        }
//                            navController.navigate("album/${item.id}")
                        is ArtistItem -> {
//                            Log.e("-------type","artist")
                            if (homeActivity?.isFinishing == true || homeActivity?.isDestroyed == true){
                                return@HomeGridAdapter
                            }
                            logClickEvents("home_to_artist_det")

                            homeActivity?.startInterActivity(Intent(homeActivity, ArtistDetActivity::class.java).apply {
                                putExtra("artistId",item.id)
                            }, artistDetInter(),false)

                        }
//                            navController.navigate("artist/${item.id}")
                        is PlaylistItem -> {
//                            Log.e("-------type","PlaylistItem")

                            if (homeActivity?.isFinishing == true || homeActivity?.isDestroyed == true){
                                return@HomeGridAdapter
                            }
                            logClickEvents("home_to_playlist_det")

                            homeActivity?.startInterActivity(Intent(homeActivity, PlayListDetActivity::class.java).apply {
                                putExtra("playlistId",item.id)
                            }, playlistDetInter(),false)

                        }
//                            navController.navigate("online_playlist/${item.id}")
                    }
                },
            )
            binding.itemsRecyclerView.adapter = adapter
            adapter.setList(section.items)
        }
    }
}
