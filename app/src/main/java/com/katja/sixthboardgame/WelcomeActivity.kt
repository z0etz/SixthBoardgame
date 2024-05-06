package com.katja.sixthboardgame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val ongoingGamesData: List<String> = getOngoingGamesData()
        val adapter = WelcomeAdapterCurrentGamesList(this, getOngoingGamesData)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewOngoingGames)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun getOngoingGamesData(): List<String> {
        return listOf("Game 1", "Game 2", "Game 3")
    }
}