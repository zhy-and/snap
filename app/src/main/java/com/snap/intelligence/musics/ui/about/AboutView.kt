package com.snap.intelligence.musics.ui.about

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.snap.intelligence.musics.BasicView
import com.snap.intelligence.musics.databinding.ActivityAboutsBinding

class AboutView : BasicView() {

    private lateinit var binding: ActivityAboutsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.pp.setOnClickListener {
            startActivity(Intent(this, BroView::class.java))
            finish()
        }
        binding.titles.apply {
            lists.visibility = View.GONE
            nowPlayingText.text = "About"
            closeButton.setOnClickListener {
                finish()
            }
        }



    }

}
