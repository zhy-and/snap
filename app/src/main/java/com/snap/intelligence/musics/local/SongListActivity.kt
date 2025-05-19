
package com.snap.intelligence.musics.local
import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SizeUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.tencent.mmkv.MMKV
import com.snap.intelligence.musics.BasicView
import com.snap.intelligence.musics.constants.MESSAGE_MSG_CODE
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_APP_REFRESH
import com.snap.intelligence.musics.constants.NativeWeavesListener
import com.snap.intelligence.musics.constants.SnapPosManager
import com.snap.intelligence.musics.constants.albumDetBanner
import com.snap.intelligence.musics.constants.albumDetInter
import com.snap.intelligence.musics.databinding.ActivityLAlbumListBinding
import com.snap.intelligence.musics.extensions.currentMetadata
import com.snap.intelligence.musics.local.LocalHomeActivity.Companion.playerConnection
import com.snap.intelligence.musics.local.frag.LocalSongsFragment
import com.snap.intelligence.musics.mob.MobToStyle
import com.snap.intelligence.musics.mob.SnapMobManager
import com.snap.intelligence.musics.models.MediaMetadata
import kotlinx.coroutines.launch


class SongListActivity : BasicView() {
    private lateinit var binding: ActivityLAlbumListBinding

    private var adapter: LocalSongsFragment.SongAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SnapMobManager.homeStudy.getShowCyAds(albumDetInter(),this)
        binding = ActivityLAlbumListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.bottomPlayer.container.setOnClickListener {
            startActivity(Intent(this, LocalMusicPlayerActivity::class.java))
        }
        // 获取传递参数并加载数据‌:ml-citation{ref="5,6" data="citationList"}
        val queryType = intent.getIntExtra("type",0)
        val queryId = intent.getStringExtra("album_id") ?: return
        loadSongs( queryId)
        // 设置标题栏‌:ml-citation{ref="1,7" data="citationList"}
        val title = intent.getStringExtra("title")
        val art_name = intent.getStringExtra("art_name")
        binding.title.text = title
        binding.artist.text = art_name
        binding.playAlbum.setOnClickListener {
           if (adapter?.items.isNullOrEmpty()){
               return@setOnClickListener
           }
            // 处理歌曲点击事件‌:ml-citation{ref="7,8" data="citationList"}
            val intent = Intent(
            this,
                LocalMusicPlayerActivity::class.java
            )
            startActivity(intent)
            adapter?.items?.let {
                playLocalMusic(it,0)
            }
        }
        binding.closeButton.setOnClickListener {
            finish()
        }

        try {
            val albumArtUri = ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),
                queryId.toLong()
            )
            val cornerTransform = CornerTransform(this, SizeUtils.dp2px(52f).toFloat())
            cornerTransform.setNeedCorner(false, false, true, false)
            val options: RequestOptions = RequestOptions()
                .placeholder(posBitmap(queryType))
                .error(posBitmap(queryType))
                .transform(cornerTransform)
            Glide.with(this)
//                .load(posBitmap(queryType))
                .load(albumArtUri)
                .apply(options)
                .thumbnail(
                    Glide.with(this)
                        .load(posBitmap(queryType))
                        .apply(options)
                )
                .into(binding.album)

            lifecycleScope.launch {
                playerConnection?.isPlaying?.collect{
                    if (it){
                        binding.bottomPlayer.playPauseButton.setImageResource(com.snap.intelligence.musics.R.mipmap.home_pause_btn)
                    }else{
                        binding.bottomPlayer.playPauseButton.setImageResource(com.snap.intelligence.musics.R.mipmap.play)
                    }
                }
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
    }


    private fun setupRecyclerView() {
        binding.moodAndGenresRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter =
            LocalSongsFragment.SongAdapter() // 传递点击监听器‌:ml-citation{ref="1,7" data="citationList"}
        binding.moodAndGenresRecyclerView.adapter = adapter
    }

    private fun loadSongs(id: String?) {
        try {
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            var selection: String? = null
            var selectionArgs: Array<String?>? = null

            // 构建查询条件‌:ml-citation{ref="5,6" data="citationList"}
//        if ("album" == type) {
            selection = MediaStore.Audio.Media.ALBUM_ID + "=?"
            selectionArgs = arrayOf(id)
//        }
//        else if ("artist" == type) {
//            selection = MediaStore.Audio.Media.ARTIST_KEY + "=?"
//            selectionArgs = arrayOf(id)
//        }

            contentResolver.query(
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
//                Log.e("------",",,"+songs.size)
                adapter?.add(songs)
            }
        }catch (e : Exception){
            e.printStackTrace()
        }

    }
    override fun onResume() {
        super.onResume()
        if (playerConnection != null && playerConnection?.player?.currentMetadata != null && MMKV.defaultMMKV().getBoolean("hasPer",false)){
            playerConnection?.mediaMetadata?.let {
                updatePlayerUI(it.value)
            }
        }
        showBanners()
    }

    private fun showBanners() {
        val normalBan: NativeAd? = SnapMobManager.homeStudy.showWeaveNatives(
            albumDetBanner(), this)
        if (normalBan == null) {
            if (SnapPosManager.cyIsShowingFLag) {
                LogUtils.e("albumDetBanner  ban inter is showing return------")
                return
            }
            if (isFinishing || isDestroyed) {
                LogUtils.e("albumDetBanner  ban destroy  return------")
                return
            }
            if (SnapPosManager.posIsCanShow(albumDetBanner())) {
                LogUtils.e("albumDetBanner  ban limit  return------")
                return
            }
            SnapMobManager.homeStudy.initLoadBanners(albumDetBanner(),object :
                NativeWeavesListener {
                override fun nativeIsWeavesSuc(sucFlag: Boolean) {
                    if (isFinishing || isDestroyed) {
                        return
                    }
                    if (sucFlag) {
                        LogUtils.e("albumDetBanner  ban load  suc------")
                        showBanners()
                    }
                }
            })
        } else {
            LogUtils.e("albumDetBanner  ban show  suc------")
            binding?.homeBans?.let {
                it.visibility = View.VISIBLE
                it.setStyles(MobToStyle.Builder().build())
                it.setNativeAd(normalBan)
            }
        }
    }

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

}