package com.snap.intelligence.musics.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.MainBe
import com.zionhuang.innertube.models.AlbumItem
import com.zionhuang.innertube.models.SongItem
import com.snap.intelligence.musics.db.MusicDatabase
import com.snap.intelligence.musics.utils.reportException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class AlbumViewModel @Inject constructor(
    database: MusicDatabase,
    albumsId: String,
) : ViewModel() {
    val albumId = albumsId
    val albumWithSongs = database.albumWithSongs(albumId)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val otherVersions = MutableStateFlow<List<AlbumItem>>(emptyList())
    val songLists = MutableStateFlow<List<SongItem>>(emptyList())

    init {
        viewModelScope.launch {
            val album = database.album(albumId).first()
            MainBe.album(albumId).onSuccess {
                if (album == null || album.album.songCount == 0) {
                    database.transaction {
                        if (album == null) insert(it)
                        else update(album.album, it)
                    }
                }
                songLists.value = it.songs
                otherVersions.value = it.otherVersions
            }.onFailure {
                reportException(it)
                if (it.message?.contains("NOT_FOUND") == true) {
                    // This album no longer exists in YouTube Music
                    database.query {
                        album?.album?.let(::delete)
                    }
                }
            }
        }
    }
}
