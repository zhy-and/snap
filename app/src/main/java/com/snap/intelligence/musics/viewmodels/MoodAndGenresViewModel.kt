package com.snap.intelligence.musics.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.MainBe
import com.zionhuang.innertube.pages.MoodAndGenres
import com.snap.intelligence.musics.utils.reportException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class MoodAndGenresViewModel @Inject constructor() : ViewModel() {
    val moodAndGenres = MutableStateFlow<List<MoodAndGenres>?>(null)

    init {
        viewModelScope.launch {
            MainBe.moodAndGenres().onSuccess {
                moodAndGenres.value = it
            }.onFailure {
                reportException(it)
            }
        }
    }
}
