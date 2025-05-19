package com.snap.intelligence.musics.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.MainBe
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.innertube.models.filterExplicit
import com.snap.intelligence.musics.constants.HideExplicitKey
import com.snap.intelligence.musics.models.ItemsPage
import com.snap.intelligence.musics.utils.dataStore
import com.snap.intelligence.musics.utils.get
import com.snap.intelligence.musics.utils.reportException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class ArtistItemsViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    browseId : String,
    params : String
) : ViewModel() {

    val title = MutableStateFlow("")
    val itemsPage = MutableStateFlow<ItemsPage?>(null)

    init {
        viewModelScope.launch {
            MainBe.artistItems(
                BrowseEndpoint(
                    browseId = browseId,
                    params = params
                )
            ).onSuccess { artistItemsPage ->
                title.value = artistItemsPage.title
                itemsPage.value = ItemsPage(
                    items = artistItemsPage.items.distinctBy { it.id },
                    continuation = artistItemsPage.continuation
                )
            }.onFailure {
                reportException(it)
            }
        }
    }

    fun loadMore() {
        viewModelScope.launch {
            val oldItemsPage = itemsPage.value ?: return@launch
            val continuation = oldItemsPage.continuation ?: return@launch
            MainBe.artistItemsContinuation(continuation)
                .onSuccess { artistItemsContinuationPage ->
                    itemsPage.update {
                        ItemsPage(
                            items = (oldItemsPage.items + artistItemsContinuationPage.items)
                                .distinctBy { it.id }
                                .filterExplicit(context.dataStore.get(HideExplicitKey, false)),
                            continuation = artistItemsContinuationPage.continuation
                        )
                    }
                }.onFailure {
                    reportException(it)
                }
        }
    }
}
