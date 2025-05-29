package com.snap.intelligence.musics.ui.search


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.snap.intelligence.musics.db.InternalDatabase
import com.snap.intelligence.musics.db.MusicDatabase
import com.snap.intelligence.musics.db.entities.SearchHistory
import kotlinx.coroutines.launch
import com.blankj.utilcode.util.ToastUtils
import com.snap.intelligence.musics.constants.searchRecordInter
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.gms.ads.nativead.NativeAd
import com.snap.intelligence.musics.BasicView
import com.snap.intelligence.musics.constants.SnapPosManager
import com.snap.intelligence.musics.constants.MESSAGE_MSG_CODE
import com.snap.intelligence.musics.constants.MUSIC_CONSTANT_APP_REFRESH
import com.snap.intelligence.musics.constants.NativeWeavesListener
import com.snap.intelligence.musics.constants.searchHistoryBanner
import com.snap.intelligence.musics.databinding.SearchHistoryBinding
import com.snap.intelligence.musics.extensions.logClickEvents
import com.snap.intelligence.musics.flow.FlowAdapter
import com.snap.intelligence.musics.flow.FlowItem
import com.snap.intelligence.musics.mob.MobToStyle
import com.snap.intelligence.musics.mob.SnapMobManager
import com.tencent.mmkv.MMKV


class MusicSearchHistoryActivity : BasicView() {
    private  var newHistoryList: List<SearchHistory> ?= null
    private var data: MusicDatabase?= null

    override fun onDestroy() {
        super.onDestroy()
        handler?.removeCallbacksAndMessages(null)
    }
    private lateinit var binding: SearchHistoryBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SnapMobManager.homeStudy.getShowCyAds(searchRecordInter(),this)

        enableEdgeToEdge()
        binding = SearchHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.search.setOnClickListener {
            searchAction()
        }
        binding.closeButton.setOnClickListener {
            finishCurrent()
        }
        binding.searchEditText.requestFocus()
        binding.searchEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {

                // 处理确认事件
                searchAction()
                // 隐藏软键盘
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                true // 消费事件
            } else {
                false
            }
        }
//        InternalDatabase.newInstance(this@MusicSearchActivity).searchHistory().map { history ->
        lifecycleScope.launch {
            try {
                data =  InternalDatabase.newInstance(this@MusicSearchHistoryActivity)
                data?.searchHistory()?.collect{
//                Log.e("history----", it.toString())
                    val list = ArrayList<FlowItem>()
                    newHistoryList = it
                    it.forEach { ios->
                        list.add(FlowItem(ios.query))
                    }
// 配置Flexbox布局
                    val layoutManager =  FlexboxLayoutManager(this@MusicSearchHistoryActivity)
                    layoutManager.setFlexDirection(FlexDirection.ROW)
                    layoutManager.setJustifyContent(JustifyContent.FLEX_START)
                    layoutManager.maxLine = 3
                    binding.flowList.setLayoutManager(layoutManager)

                    val adapter =  FlowAdapter(list)
                    adapter.setOnItemClickListener(object :FlowAdapter.OnItemClickListener{
                        override fun onItemClick(item: FlowItem?) {
                            startInterActivity(Intent(this@MusicSearchHistoryActivity, MusicSearchActivity::class.java).apply {
                                putExtra("search_txt",item?.text)
                            }, searchRecordInter(),true)

                        }

                        override fun onDeleteClick(position: Int) {
                            newHistoryList?.let {
                                if (it.isNullOrEmpty()){
                                    return
                                }
                            }
                            newHistoryList?.forEach {
                                if (it.query == adapter.data.getOrNull(position)?.text){
                                    data?.query {
                                        delete(it)
                                    }
                                }
                            }
                            adapter.removeItem(position)

                        }

                    })

                    binding.flowList.setAdapter(adapter)

                }
            }catch (e : Exception){
                e.printStackTrace()
            }

        }

    }

    private fun searchAction() {
        if (TextUtils.isEmpty(binding.searchEditText.text.toString())) {
            ToastUtils.showShort("Please input content")
            return
        }
        logClickEvents("search_enter_to_search")
        startInterActivity(Intent(this@MusicSearchHistoryActivity, MusicSearchActivity::class.java).apply {
            putExtra("search_txt",binding.searchEditText.text.toString())
        }, searchRecordInter(),true)
    }
    private fun showBanners() {
        val normalBan: NativeAd? = SnapMobManager.homeStudy.showWeaveNatives(
            searchHistoryBanner(), this)
        if (normalBan == null) {
            if (SnapPosManager.cyIsShowingFLag) {
                LogUtils.e("searchHistoryBanner  ban inter is showing return------")
                return
            }
            if (isFinishing || isDestroyed) {
                LogUtils.e("searchHistoryBanner  ban destroy  return------")
                return
            }
            if (SnapPosManager.posIsCanShow(searchHistoryBanner())) {
                LogUtils.e("searchHistoryBanner  ban limit  return------")
                return
            }
            SnapMobManager.homeStudy.initLoadBanners(searchHistoryBanner(),object :
                NativeWeavesListener {
                override fun nativeIsWeavesSuc(sucFlag: Boolean) {
                    if (isFinishing || isDestroyed) {
                        return
                    }
                    if (sucFlag) {
                        LogUtils.e("searchHistoryBanner  ban load  suc------")
                        showBanners()
                    }
                }
            })
        } else {
            LogUtils.e("searchHistoryBanner  ban show  suc------")
            binding?.homeBans?.let {
                it.visibility = View.VISIBLE
                it.setStyles(MobToStyle.Builder().build())
                it.setNativeAd(normalBan)
                if(!SnapPosManager.posIsCanShow(searchHistoryBanner())){
                    val cts = MMKV.defaultMMKV().getInt(MUSIC_CONSTANT_APP_REFRESH, 0)
                    if (cts != 0){
                        handler.removeMessages(MESSAGE_MSG_CODE)
                        handler.sendEmptyMessageDelayed(MESSAGE_MSG_CODE,(cts * 1000).toLong())
                    }
                }
            }
        }
    }



    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg?.what) {
                MESSAGE_MSG_CODE -> {
                    showBanners()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (handler?.hasMessages(MESSAGE_MSG_CODE) == true){
            handler?.removeMessages(MESSAGE_MSG_CODE)
        }
    }



    override fun onResume() {
        super.onResume()
        showBanners()
    }


}