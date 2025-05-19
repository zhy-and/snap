package com.snap.intelligence.musics.playback.queues

import androidx.media3.common.MediaItem
import com.snap.intelligence.musics.models.MediaMetadata

object EmptyQueue : Queue {
    override val preloadItem: MediaMetadata? = null
    override suspend fun getInitialStatus() = Queue.Status(null, emptyList(), -1)
    override fun hasNextPage() = false
    override suspend fun nextPage() = emptyList<MediaItem>()
}