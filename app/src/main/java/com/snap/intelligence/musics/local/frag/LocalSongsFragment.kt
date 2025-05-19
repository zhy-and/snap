package com.snap.intelligence.musics.local.frag

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.tencent.mmkv.MMKV
import com.snap.intelligence.musics.R
import com.snap.intelligence.musics.local.DataKeeper.initCreateData
import com.snap.intelligence.musics.local.DataKeeper.loadSongs
import com.snap.intelligence.musics.local.LocalHomeActivity
import com.snap.intelligence.musics.local.LocalMusicPlayerActivity
import com.snap.intelligence.musics.local.MusicScanner
import com.snap.intelligence.musics.local.Song
import com.snap.intelligence.musics.local.playLocalMusic
import com.snap.intelligence.musics.local.posBitmap
import java.util.concurrent.TimeUnit


class LocalSongsFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var recommend_list: RecyclerView? = null
    private var empty_view: View? = null
    private var all: View? = null
    private var text1: View? = null
    private var scan: ImageView? = null
    private var em_iv: ImageView? = null
    private var em_tx: TextView? = null

    private var adapter: SongAdapter? = null
    private var horAdapter: SongAdapter2? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_music_list, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recycler_view)
        recommend_list = view.findViewById(R.id.recommend_list)
        empty_view = view.findViewById(R.id.empty_layout)
        all = view.findViewById(R.id.all)
        text1 = view.findViewById(R.id.text1)
        scan = view.findViewById(R.id.emo_scan)
        em_iv = view.findViewById(R.id.em_iv)
        em_tx = view.findViewById(R.id.em_tx)
        setupRecyclerView()
        initView()
        if (!MMKV.defaultMMKV().getBoolean("hasPer",false)){
            empty_view?.visibility = View.VISIBLE
            all?.visibility = View.GONE
        }else{
            loadData(false)
        }
    }
    fun outClick(){
        loadData(true,true)
    }
    private fun initView() {
        scan?.setOnClickListener {

            loadData(true)
        }
    }

    override fun onResume() {
        super.onResume()
        if (empty_view?.visibility == View.VISIBLE &&  MMKV.defaultMMKV().getBoolean("hasPer",false)){
            loadSongs(Song()).let { songs ->
                if (songs.size == 0){
                    val items = MusicScanner.getLocalSongs(requireContext()) as ArrayList<Song>
                    initCreateData(items)
                    if (items.size >= 2){
                        recommend_list?.setLayoutManager(LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false))
                        horAdapter = SongAdapter2()
                        recommend_list?.setAdapter(horAdapter)
                        horAdapter?.add(items.take(2) as ArrayList<Song>)
                    }
                }else{
                    if (songs.size >= 2){
                        recommend_list?.setLayoutManager(LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false))
                        horAdapter = SongAdapter2()
                        recommend_list?.setAdapter(horAdapter)
                        horAdapter?.add(songs.take(2) as ArrayList<Song>)
                    }
                }
                empty_view?.visibility = View.GONE
                all?.visibility = View.VISIBLE
                adapter?.add(songs)
            }

        }
    }

    private fun setupRecyclerView() {
        recyclerView?.setLayoutManager(LinearLayoutManager(getContext()))
        adapter = SongAdapter()
        recyclerView?.setAdapter(adapter)


    }

    private fun loadData(b: Boolean,isHome : Boolean = false) {
        try {
            XXPermissions.with(this).permission(Permission.READ_MEDIA_AUDIO)
                .request(object : OnPermissionCallback{
                    override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {
                        if (allGranted) {
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
                            val songs = MusicScanner.getLocalSongs(requireContext()) as ArrayList<Song>
                            if (songs.isNullOrEmpty()){
                                all?.visibility = View.GONE
                                empty_view?.visibility = View.VISIBLE
                                em_iv?.setImageResource(R.mipmap.no_music_logo)
                                em_tx?.text = "No music found in this device!"
                                return
                            }

                            empty_view?.visibility = View.GONE
                            all?.visibility = View.VISIBLE
                            loadSongs(Song())?.apply {
                                if (isNullOrEmpty()){
                                    return@apply
                                }
                                forEach {its ->
                                    for (i in songs){
                                        if (its.albumId == i.albumId && its.title == i.title){
                                            i.isCollect = its.isCollect
                                        }

                                    }

                                }
                            }
                            initCreateData(songs)
                            adapter?.add(songs)

                            if (songs.size >= 2){
                                recommend_list?.setLayoutManager(LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false))
                                horAdapter = SongAdapter2()
                                recommend_list?.setAdapter(horAdapter)
                                horAdapter?.add(songs.take(2) as ArrayList<Song>)
                            }else{
                                text1?.visibility = View.GONE
                                recommend_list?.visibility = View.GONE
                            }
//                    if (b){
                            activity?.apply {
                                if (isFinishing || isDestroyed){
                                    return@apply
                                }
                                val ctx = this as LocalHomeActivity
                                ctx.updateUI()
//                        }
                            }
                        }

                    }
                    override fun onDenied(permissions: MutableList<String>, allGranted: Boolean) {
//                        Log.e("permission","---sd-s")
                        if (isHome){
                            activity?.apply {
                                if (isFinishing || isDestroyed){
                                    return@apply
                                }
                                val ctx = this as LocalHomeActivity
                                ctx.denPermission()
                            }
                        }
                    }
                })
        }catch (e: Exception){
            e.printStackTrace()
        }



    }





     class SongAdapter : RecyclerView.Adapter<SongAdapter.ViewHolder>() {
        var items: ArrayList<Song> = arrayListOf()
         var norTy = 0
        fun add( datas: ArrayList<Song>){
            items = datas
            notifyDataSetChanged()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvTitle: TextView = itemView.findViewById<TextView>(R.id.title)
             val tvArtist: TextView = itemView.findViewById<TextView>(R.id.title2)
             val tvDuration: TextView = itemView.findViewById<TextView>(R.id.subtitle)
             val ivgs: ImageView = itemView.findViewById<ImageView>(R.id.ivgs)
             val thumbnail: ImageView = itemView.findViewById<ImageView>(R.id.thumbnail)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_local_song, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val song = items[position]
            holder.apply {
                tvTitle.text = song.title
                tvArtist.text = song.artist
                tvDuration.text = formatDuration(song.duration)
                if (norTy == 1){
                    holder.ivgs.setImageResource(R.mipmap.icon_collect)
                }else{
                    holder.ivgs.setImageResource(R.mipmap.play_btn)
                }
            }
                Glide.with(holder.itemView.context).asBitmap().load(posBitmap(position))
                    .apply(RequestOptions().transform(RoundedCorners(SizeUtils.dp2px(6f))))
                    .into(holder.thumbnail)
            holder.itemView.setOnClickListener {
                // 处理歌曲点击事件‌:ml-citation{ref="7,8" data="citationList"}
                val intent = Intent(
                    holder.itemView.context,
                    LocalMusicPlayerActivity::class.java
                )
               holder.itemView.context.startActivity(intent)
                playLocalMusic(items,position)
            }

        }



         private fun formatDuration(milliseconds: Long): String {
            return java.lang.String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60
            )
        }

        override fun getItemCount(): Int = items.size
         fun setNormalType(i: Int) {
             norTy = i
         }
     }

    class SongAdapter2 : RecyclerView.Adapter<SongAdapter2.ViewHolder>() {
        var items: ArrayList<Song> = arrayListOf()
        fun add( datas: ArrayList<Song>){
            items = datas
            notifyDataSetChanged()
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvTitle: TextView = itemView.findViewById<TextView>(R.id.title)
            val tvArtist: TextView = itemView.findViewById<TextView>(R.id.title2)
            val ivgs: ImageView = itemView.findViewById<ImageView>(R.id.ivgs)
            val thumbnail: ImageView = itemView.findViewById<ImageView>(R.id.thumbnail)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recommend_song, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val song = items[position]
            holder.apply {
                tvTitle.text = song.title
                tvArtist.text = song.artist
            }
            Glide.with(holder.itemView.context).asBitmap().load(posBitmap(position))
                .apply(RequestOptions().transform(RoundedCorners(SizeUtils.dp2px(6f))))
                .into(holder.thumbnail)
            holder.itemView.setOnClickListener {


                // 处理歌曲点击事件‌:ml-citation{ref="7,8" data="citationList"}
                val intent = Intent(
                    holder.itemView.context,
                    LocalMusicPlayerActivity::class.java
                )
                holder.itemView.context.startActivity(intent)
                playLocalMusic(items,position)
            }
        }

        override fun getItemCount(): Int = items.size


    }


}