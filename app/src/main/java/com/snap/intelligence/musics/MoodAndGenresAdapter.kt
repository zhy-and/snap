package com.snap.intelligence.musics

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.snap.intelligence.musics.constants.homeMoodDetInter
import com.snap.intelligence.musics.constants.playlistDetInter
import com.zionhuang.innertube.pages.MoodAndGenres
import com.snap.intelligence.musics.databinding.ItemMoodAndGenresBinding
import com.snap.intelligence.musics.extensions.logClickEvents

class MoodAndGenresAdapter(var homeActivity: BasicView) : RecyclerView.Adapter<MoodAndGenresAdapter.MoodAndGenresViewHolder>() {

    private var moodAndGenres = listOf<MoodAndGenres.Item>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodAndGenresViewHolder {
        val binding = ItemMoodAndGenresBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MoodAndGenresViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoodAndGenresViewHolder, position: Int) {
        val moodAndGenre = moodAndGenres[position]
        holder.bind(moodAndGenre,position)
    }

    override fun getItemCount(): Int = moodAndGenres.size

    fun submitList(newMoodAndGenres: List<MoodAndGenres.Item>) {
        moodAndGenres = newMoodAndGenres
        notifyDataSetChanged()
    }

    inner class MoodAndGenresViewHolder(private val binding: ItemMoodAndGenresBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(moodAndGenre: MoodAndGenres.Item, position: Int) {
            when(position%8){
                0 ->{
                    binding.ivs.setImageResource(R.mipmap.one)
                }
                1 ->{
                    binding.ivs.setImageResource(R.mipmap.tws)
                }
                2 ->{
                    binding.ivs.setImageResource(R.mipmap.thrs)
                }
                3 ->{
                    binding.ivs.setImageResource(R.mipmap.fors)
                }
                4 ->{
                    binding.ivs.setImageResource(R.mipmap.fvs)
                }
                5 ->{
                    binding.ivs.setImageResource(R.mipmap.sxs)
                }
                6 ->{
                    binding.ivs.setImageResource(R.mipmap.svs)
                }
                7 ->{
                    binding.ivs.setImageResource(R.mipmap.egs)
                }

            }
            binding.button.text = moodAndGenre.title
            binding.container.setOnClickListener {
                if (homeActivity?.isFinishing == true || homeActivity?.isDestroyed == true){
                    return@setOnClickListener
                }
                homeActivity?.startInterActivity(Intent(homeActivity, MoodDetailsActivity::class.java).apply {
                    moodAndGenre?.endpoint?.let {
                        putExtra("browseId", it.browseId)
                        putExtra("params", it.params)
                    }
                }, homeMoodDetInter(),false)
                logClickEvents("home_to_style_detail")

            }

        }
    }
}
