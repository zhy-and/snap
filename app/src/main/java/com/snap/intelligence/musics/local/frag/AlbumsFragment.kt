package com.snap.intelligence.musics.local.frag

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.tencent.mmkv.MMKV
import com.snap.intelligence.musics.R
import com.snap.intelligence.musics.constants.albumDetInter
import com.snap.intelligence.musics.constants.queueInter
import com.snap.intelligence.musics.local.Album
import com.snap.intelligence.musics.local.DataKeeper.initCreateData
import com.snap.intelligence.musics.local.DataKeeper.loadSongs
import com.snap.intelligence.musics.local.LocalHomeActivity
import com.snap.intelligence.musics.local.LocalPlayListActivity
import com.snap.intelligence.musics.local.frag.LocalSongsFragment.SongAdapter2
import com.snap.intelligence.musics.local.MusicScanner
import com.snap.intelligence.musics.local.Song
import com.snap.intelligence.musics.local.SongListActivity
import com.snap.intelligence.musics.local.SpaceItemDecoration
import com.snap.intelligence.musics.local.posBitmap


class AlbumsFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var adapter: AlbumAdapter? = null
    private var recommend_list: RecyclerView? = null
    private var empty_view: View? = null
    private var recommend_view: View? = null
    private var horAdapter: SongAdapter2? = null
    private var em_iv: ImageView? = null
    private var em_tx: TextView? = null
    private var scan: ImageView? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_album_list, container, false)


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recycler_view)
        recommend_list = view.findViewById(R.id.recommend_list)
        empty_view = view.findViewById(R.id.empty_layout)
        scan = view.findViewById(R.id.emo_scan)
        recommend_view = view.findViewById(R.id.recommend_view)
        em_iv = view.findViewById(R.id.em_iv)
        em_tx = view.findViewById(R.id.em_tx)
        setupRecyclerView()
        initView()
        if (!MMKV.defaultMMKV().getBoolean("hasPer",false)){
            empty_view?.visibility = View.VISIBLE
            recommend_view?.visibility = View.GONE
            recyclerView?.visibility = View.GONE
        }else{
            loadAlbums(false)
        }
    }

    private fun initView() {
        scan?.setOnClickListener {
            activity?.apply {
                if (isFinishing || isDestroyed){
                    return@apply
                }
                var ctx = this as LocalHomeActivity
                ctx.updateUI()
                ctx.animation()
            }
            loadAlbums(true)
        }
    }
    override fun onResume() {
        super.onResume()
        if (empty_view?.visibility == View.VISIBLE &&  MMKV.defaultMMKV().getBoolean("hasPer",false)){
            val songs = MusicScanner.getLocalAlbums(requireContext()) as ArrayList<Album>
            if (songs.isNullOrEmpty()){
                empty_view?.visibility = View.VISIBLE
                recommend_view?.visibility = View.GONE
                recyclerView?.visibility = View.GONE
                em_iv?.setImageResource(R.mipmap.no_music_logo)
                em_tx?.text = "No music found in this device!"
                return
            }
            changeUI(songs)
        }
    }
    private fun setupRecyclerView() {
        recyclerView?.setLayoutManager(GridLayoutManager(getContext(),2))
//        recyclerView?.addItemDecoration( GridSpaceItemDecoration(2,
//            SizeUtils.dp2px( 20f),
//            SizeUtils.dp2px( 16f))
//        );
        recyclerView?.addItemDecoration(SpaceItemDecoration( SizeUtils.dp2px( 20f)))
        adapter = AlbumAdapter()
        recyclerView?.setAdapter(adapter)
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
                                ctx.updateUI()
                                ctx.animation()
                            }
                        }
                        MMKV.defaultMMKV().putBoolean("hasPer",true)
                        val songs = MusicScanner.getLocalAlbums(requireContext()) as ArrayList<Album>
                        if (songs.isNullOrEmpty()){
                            empty_view?.visibility = View.VISIBLE
                            recommend_view?.visibility = View.GONE
                            recyclerView?.visibility = View.GONE
                            em_iv?.setImageResource(R.mipmap.no_music_logo)
                            em_tx?.text = "No music found in this device!"
                            return@request
                        }
                        changeUI(songs,b)
                    }
                }
        }catch (e : Exception){
            e.printStackTrace()
        }

    }

    private fun changeUI(songs: ArrayList<Album>,isCheck : Boolean = false) {
        try {
            empty_view?.visibility = View.GONE
            recyclerView?.visibility = View.VISIBLE
            adapter?.add(songs)
            loadSongs(Song()).let { songs ->
                if (songs.size == 0){
                    val items = MusicScanner.getLocalSongs(requireContext()) as ArrayList<Song>
                    initCreateData(items)
                    if (items.size >= 2){
                        recommend_view?.visibility = View.VISIBLE
                        recommend_list?.setLayoutManager(
                            LinearLayoutManager(getContext(),
                                LinearLayoutManager.HORIZONTAL,false)
                        )
                        horAdapter = SongAdapter2()
                        recommend_list?.setAdapter(horAdapter)
                        horAdapter?.add(items.take(2) as ArrayList<Song>)
                    }
                }else{
                    if (songs.size >= 2){
                        recommend_view?.visibility = View.VISIBLE
                        recommend_list?.setLayoutManager(
                            LinearLayoutManager(getContext(),
                                LinearLayoutManager.HORIZONTAL,false)
                        )
                        horAdapter = SongAdapter2()
                        recommend_list?.setAdapter(horAdapter)
                        horAdapter?.add(songs.take(2) as ArrayList<Song>)
                    }
                }
                if (isCheck){
                    activity?.apply {
                        if (isFinishing || isDestroyed){
                            return@apply
                        }
                        val ctx = this as LocalHomeActivity
                        ctx.updateUI()
                    }
                }

            }

        }catch (e : Exception){
            e.printStackTrace()
        }

    }

    inner class AlbumAdapter : RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {
        var items: ArrayList<Album> = arrayListOf()
        fun add( datas: ArrayList<Album>){
            items = datas
            notifyDataSetChanged()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvTitle: TextView = itemView.findViewById<TextView>(R.id.title)
            val tvArtist: TextView = itemView.findViewById<TextView>(R.id.artist)
            val img: ImageView = itemView.findViewById(R.id.thumbnail)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(com.snap.intelligence.musics.R.layout.item_local_album, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val song = items[position]
            holder.apply {
                tvTitle.text = song.artist
                tvArtist.text = song.name
                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    song.albumid.toLong()
                )
                val options: RequestOptions = RequestOptions()
                    .placeholder(posBitmap(position))
                    .error(posBitmap(position))
                    .transform(RoundedCorners(SizeUtils.dp2px(8f)))
                Glide.with(itemView)
                    .load(albumArtUri)
//                    .load(posBitmap(position))
                    .apply(options)
                    .thumbnail(
                        Glide.with(itemView)
                            .load(posBitmap(position))
                            .apply(options)
                    )

                    .into(img)
            }


            holder.itemView.setOnClickListener {
                if (activity == null || activity?.isDestroyed == true|| activity?.isFinishing == true){
                    return@setOnClickListener
                }
                val ctx = activity as? LocalHomeActivity
                ctx?.apply {
                    startInterActivity(Intent(this, SongListActivity::class.java).apply {
                        putExtra("type", position)
                        putExtra("album_id", song.albumid.toString())
                        putExtra("title", song.name)
                        putExtra("art_name", song.artist)
                    }, albumDetInter(),false)
                }
            }

        }


        override fun getItemCount(): Int = items.size

    }



}