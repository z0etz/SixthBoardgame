package com.katja.sixthboardgame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.katja.sixthboardgame.databinding.ActivityHighscoreBinding


class HighscoreActivity : AppCompatActivity() {

    lateinit var binding: ActivityHighscoreBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHighscoreBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val highscores = listOf(
            Highscore("User1", 100),
            Highscore("User2", 90),
            Highscore("User3", 80),

        )

        val adapter = HighscoreAdapter(highscores)
        binding.recyclerViewHighscore.adapter = adapter
        binding.recyclerViewHighscore.layoutManager = LinearLayoutManager(this)



    }
}