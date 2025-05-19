package com.snap.intelligence.musics.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.MainBe
import com.zionhuang.innertube.pages.BrowseResult
import com.snap.intelligence.musics.constants.HideExplicitKey
import com.snap.intelligence.musics.utils.dataStore
import com.snap.intelligence.musics.utils.get
import com.snap.intelligence.musics.utils.reportException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class YouTubeBrowseViewModel @Inject constructor(
    @ApplicationContext context: Context,
    browseId : String,
    params : String,
) : ViewModel() {

    val result = MutableStateFlow<BrowseResult?>(null)

    init {
        viewModelScope.launch {
            MainBe.browse(browseId, params).onSuccess {
                result.value = it.filterExplicit(context.dataStore.get(HideExplicitKey, false))
            }.onFailure {
                reportException(it)
            }
        }
    }
}
