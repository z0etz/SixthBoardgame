package com.katja.sixthboardgame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.katja.sixthboardgame.databinding.ActivityHighscoreBinding


class HighscoreActivity : AppCompatActivity() {

    lateinit var binding: ActivityHighscoreBinding
    private val userDao = UserDao()
    private lateinit var leaderboardAdapter: LeaderboardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHighscoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        leaderboardAdapter = LeaderboardAdapter(emptyList())
        binding.recyclerViewHighscore.adapter = leaderboardAdapter
        binding.recyclerViewHighscore.layoutManager = LinearLayoutManager(this)

        fetchAndDisplayLeaderboard()
    }

    private fun fetchAndDisplayLeaderboard() {
        userDao.fetchLeaderboard { leaderboard ->
            leaderboardAdapter.updateHighscores(leaderboard)
        }
    }
}