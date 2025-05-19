package com.snap.intelligence.musics.local.frag

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tencent.mmkv.MMKV
import com.snap.intelligence.musics.R
import com.snap.intelligence.musics.local.DataKeeper.loadSongs
import com.snap.intelligence.musics.local.Song


class CollectFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var adapter: LocalSongsFragment.SongAdapter? = null
    private var empty_view: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_collect_list, container, false)
        recyclerView = view.findViewById(R.id.recycler_view)
        empty_view = view.findViewById(R.id.empty_layout)
        setupRecyclerView()
        return view
    }

    override fun onResume() {
        super.onResume()
        if (MMKV.defaultMMKV().getBoolean("hasPer",false)){
            loadData()
        }else{
            empty_view?.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView() {
        recyclerView?.setLayoutManager(LinearLayoutManager(requireContext()))
        adapter = LocalSongsFragment.SongAdapter()
        adapter?.setNormalType(1)
        recyclerView?.setAdapter(adapter)
    }

    private fun loadData() {
        try {
            context?.let {
                val favsongs  = ArrayList<Song>()
                loadSongs(Song()).forEach {
                    if (it.isCollect){
                        favsongs.add(it)
                    }
                }
                if (favsongs.isEmpty()){
                    empty_view?.visibility = View.VISIBLE
                    adapter?.add(favsongs)
                    return@let
                }
                if (  empty_view?.visibility == View.VISIBLE){
                    empty_view?.visibility = View.GONE
                }
                adapter?.add(favsongs)
            }
        }catch (e : Exception){
            e.printStackTrace()
        }

        }
    }





