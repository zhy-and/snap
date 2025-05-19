package com.snap.intelligence.musics.local.frag

import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.tencent.mmkv.MMKV
import com.snap.intelligence.musics.R
import com.snap.intelligence.musics.local.Artist
import com.snap.intelligence.musics.local.DataKeeper
import com.snap.intelligence.musics.local.DataKeeper.initCreateData
import com.snap.intelligence.musics.local.LocalHomeActivity
import com.snap.intelligence.musics.local.MusicScanner
import com.snap.intelligence.musics.local.Song
import com.snap.intelligence.musics.local.posBitmap
import jp.wasabeef.glide.transformations.CropCircleTransformation


class ArtistsFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var songRecy: RecyclerView? = null
    private var adapter: ArtistAdapter? = null
    private var songAdapter: LocalSongsFragment.SongAdapter? = null
    private var empty_view: View? = null
    private var scan: ImageView? = null
    private var lines: View? = null
    private var em_iv: ImageView? = null
    private var em_tx: TextView? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_local_artist, container, false)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recycler_view)
        songRecy = view.findViewById(R.id.song_list)
        empty_view = view.findViewById(R.id.empty_layout)
        scan = view.findViewById(R.id.emo_scan)
        lines = view.findViewById(R.id.lines)
        em_iv = view.findViewById(R.id.em_iv)
        em_tx = view.findViewById(R.id.em_tx)
        scan?.setOnClickListener {

            loadAlbums(true)
        }
        setupRecyclerView()
        if (!MMKV.defaultMMKV().getBoolean("hasPer",false)){
            empty_view?.visibility = View.VISIBLE
        }else{
            loadAlbums(false)
        }
    }

    override fun onResume() {
        super.onResume()
        if (empty_view?.visibility == View.VISIBLE &&  MMKV.defaultMMKV().getBoolean("hasPer",false)){
            val songs = MusicScanner.getLocalArtists(requireContext()) as ArrayList<Artist>
            if (songs.isNullOrEmpty()){
                empty_view?.visibility = View.VISIBLE
                recyclerView?.visibility = View.GONE
                lines?.visibility = View.GONE
                em_iv?.setImageResource(R.mipmap.no_music_logo)
                em_tx?.text = "No music found in this device!"
                return
            }
            empty_view?.visibility = View.GONE
            recyclerView?.visibility = View.VISIBLE
            songRecy?.visibility = View.VISIBLE
            lines?.visibility = View.VISIBLE
            adapter?.add(songs)
            loadSong(false)
        }
    }

    private fun setupRecyclerView() {
        recyclerView?.setLayoutManager(LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false))
        adapter = ArtistAdapter()
        recyclerView?.setAdapter(adapter)

        songRecy?.setLayoutManager(LinearLayoutManager(getContext()))
        songAdapter = LocalSongsFragment.SongAdapter()
        songRecy?.setAdapter(songAdapter)
    }

    private fun loadAlbums(b: Boolean) {
        try {
            XXPermissions.with(this).permission(Permission.READ_MEDIA_AUDIO)
                .request { permissions, allGranted ->
                    if (allGranted) {
//                    if (adapter?.items?.isNotEmpty() == true){
//                        return@request
//                    }

                        if (b){
                            activity?.apply {
                                if (isFinishing || isDestroyed){
                                    return@apply
                                }
                                val ctx = this as LocalHomeActivity
                                ctx.animation()
                            }
                        }
                        MMKV.defaultMMKV().putBoolean("hasPer",true)
                        val songs = MusicScanner.getLocalArtists(requireContext()) as ArrayList<Artist>
                        if (songs.isNullOrEmpty()){
                            empty_view?.visibility = View.VISIBLE
                            recyclerView?.visibility = View.GONE
                            lines?.visibility = View.GONE
                            em_iv?.setImageResource(R.mipmap.no_music_logo)
                            em_tx?.text = "No music found in this device!"
                            return@request
                        }
                        empty_view?.visibility = View.GONE
                        recyclerView?.visibility = View.VISIBLE
                        songRecy?.visibility = View.VISIBLE
                        lines?.visibility = View.VISIBLE
                        adapter?.add(songs)
                        loadSong(b)
                    }
                }
        }catch (e : Exception){
            e.printStackTrace()
        }

    }

    private fun loadSong(b: Boolean) {
        try {
            DataKeeper.loadSongs(Song()).let { songs ->
                if (songs.size == 0){
                    val songs = MusicScanner.getLocalSongs(requireContext()) as ArrayList<Song>
                    initCreateData(songs)
                    if (b){
                        activity?.apply {
                            if (isFinishing || isDestroyed){
                                return@apply
                            }
                            val ctx = this as LocalHomeActivity
                            ctx.updateUI()
                        }
                    }
                }
            }

        }catch (e : Exception){
            e.printStackTrace()
        }

    }


    inner class ArtistAdapter : RecyclerView.Adapter<ArtistAdapter.ViewHolder>() {
        var items: ArrayList<Artist> = arrayListOf()
        fun add( datas: ArrayList<Artist>){
            try {
                items = datas
                if (items.size > 0){
                    items?.forEachIndexed() { index,it ->
                        if (index == 0){
                            it.isSelect = true
                        }else{
                            it.isSelect = false
                        }
                    }
                    loadSongs(items.firstOrNull()?.key)
                }
                notifyDataSetChanged()
            }catch (e : Exception){
                e.printStackTrace()
            }

        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//            val tvTitle: TextView = itemView.findViewById<TextView>(R.id.title)
            val tvArtist: TextView = itemView.findViewById<TextView>(R.id.artist)
//            val tvDuration: TextView = itemView.findViewById<TextView>(R.id.count)
            val img: ImageView = itemView.findViewById(R.id.thumbnail)
            val select_bg: ImageView = itemView.findViewById(R.id.select_bg)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_local_artist, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val song = items[position]
            holder.apply {
                if (song.isSelect){
                    select_bg.visibility = View.VISIBLE
                }else{
                    select_bg.visibility = View.INVISIBLE
                }
                tvArtist.text = song.name
//                tvArtist.text = song.albumCount.toString()
//                tvDuration.text = "gong --"+song.songCount

                Glide.with(holder.itemView.context).asBitmap()
                    .apply(RequestOptions.bitmapTransform(CropCircleTransformation())) // 设置圆形图片
                    .load(posBitmap(position)).into(holder.img)
            }

            holder.itemView.setOnClickListener {
                items?.forEachIndexed() { index,it ->
                    if (index == position){
                        it.isSelect = true
                    }else{
                        it.isSelect = false
                    }
                }
               notifyDataSetChanged()
                loadSongs(song.key)
            }

        }
        private fun loadSongs(id: String?) {
            try {
                Thread{
                    val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    var selection: String? = null
                    var selectionArgs: Array<String?>? = null

                    // 构建查询条件‌:ml-citation{ref="5,6" data="citationList"}
                    selection = MediaStore.Audio.Media.ARTIST_KEY + "=?"
                    selectionArgs = arrayOf(id)

                    context?.contentResolver?.query(
                        uri,
                        arrayOf<String>(
                            MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.DURATION,
                            MediaStore.Audio.Media.DATA,
                            MediaStore.Audio.Media.ALBUM,
                            MediaStore.Audio.Media.ALBUM_ID,
                        ),
                        selection,
                        selectionArgs,
                        MediaStore.Audio.Media.TITLE + " ASC"
                    ).use { cursor ->
                        val songs = ArrayList<Song>()
                        if (cursor != null) {
                            val idIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                            val titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                            val artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                            val durationIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                            val pathIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

                            while (cursor.moveToNext()) {
                                songs.add(
                                    Song(
                                        cursor.getLong(idIndex),
                                        cursor.getString(titleIndex),
                                        cursor.getString(artistIndex),
                                        cursor.getLong(durationIndex),
                                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString(),
                                        cursor.getLong(idIndex),
                                        cursor.getString(pathIndex),
                                        cursor.getString(albumColumn),
                                        cursor.getLong(albumIdColumn)
                                    )
                                )
                            }
                        }
                        activity?.runOnUiThread {
                            songAdapter?.add(songs)
                        }
                    }
                }.start()
            }catch (e : Exception){
                e.printStackTrace()
            }


        }


        override fun getItemCount(): Int = items.size

    }



}