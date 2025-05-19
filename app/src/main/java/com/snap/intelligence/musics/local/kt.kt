package com.snap.intelligence.musics.local

import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_MUSIC
import com.snap.intelligence.musics.R
import com.snap.intelligence.musics.local.LocalHomeActivity.Companion.playerConnection
import com.snap.intelligence.musics.playback.queues.ListQueue

fun randomBitmap(): Int {
   val type = (1..3).random()
     if (type == 1){
         return R.mipmap.m_icon_one
     }else if (type == 2){
         return R.mipmap.m_icon_tw
     }else{
         return R.mipmap.m_icon_thr
     }
}

fun posBitmap(type : Int): Int {
    return if (type % 3 == 0){
        R.mipmap.m_icon_one
    }else if (type % 3 == 1){
        R.mipmap.m_icon_tw
    }else if (type % 3 == 2){
        R.mipmap.m_icon_thr
    }else{
        R.mipmap.m_icon_one
    }
}


fun playLocalMusic(localMusic: ArrayList<Song>, position: Int) {
    try {
        val mediaItems = localMusic.map { music ->

            MediaItem.Builder()
                .setMediaId(music.id.toString())
                .setUri(Uri.parse(music.uri).buildUpon().appendPath(music.uriId.toString()).build())
                .setCustomCacheKey(music.id.toString())
                .setTag(music.toMediaMetadata())
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setTitle(music.title)
                        .setSubtitle(music.artist)
                        .setArtist(music.artist)
//                        .setArtworkUri(music.albumArtUri.uri)
//                        .setAlbumTitle(music.album)
                        .setMediaType(MEDIA_TYPE_MUSIC)
                        .build()
                )
                .build()

        }
        for (i in mediaItems){
//            Log.e("local_media",i.mediaMetadata.title.toString())
        }
        val queue = ListQueue(title = "Local Music",mediaItems,  startIndex = position)
        playerConnection?.playQueue(queue)
    }catch (e : Exception){
        e.printStackTrace()
    }


}
