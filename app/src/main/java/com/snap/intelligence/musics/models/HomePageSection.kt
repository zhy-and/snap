package com.snap.intelligence.musics.models

import com.zionhuang.innertube.models.YTItem

data class HomePageSection(
    val title: String,
    val label: String? = null,
    val thumbnail: String? = null,
    val endpoint: YTItem? = null,
    val items: List<YTItem>
)
