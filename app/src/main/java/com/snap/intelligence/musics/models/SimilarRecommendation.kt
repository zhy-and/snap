package com.snap.intelligence.musics.models

import com.zionhuang.innertube.models.YTItem
import com.snap.intelligence.musics.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)
