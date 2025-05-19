package com.snap.intelligence.musics.ui.about

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.snap.intelligence.musics.databinding.ActivityPrivacysBinding

class BroView : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacysBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacysBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.webs.loadUrl("https://waad.velvymusicplay.com/privacyPolicy.html")
        binding.titles.apply {
            lists.visibility = View.GONE
            nowPlayingText.text = "Privacy"
            closeButton.setOnClickListener {
                finish()
            }
        }

    }

}
