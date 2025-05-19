package com.snap.intelligence.musics.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.MainBe
import com.zionhuang.innertube.models.PlaylistItem
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.utils.completed
import com.snap.intelligence.musics.utils.reportException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class OnlinePlaylistViewModel  constructor(
  playlistid : String,
) : ViewModel() {

    val playlist = MutableStateFlow<PlaylistItem?>(null)
    val playlistSongs = MutableStateFlow<List<SongItem>>(emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            MainBe.playlist(playlistid).completed()
                .onSuccess { playlistPage ->
                    playlist.value = playlistPage.playlist
                    playlistSongs.value = playlistPage.songs
                }.onFailure {
                    reportException(it)
                }
        }
    }
}
