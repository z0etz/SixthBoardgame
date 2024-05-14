package com.katja.sixthboardgame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.katja.sixthboardgame.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    lateinit var binding: ActivityWelcomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val ongoingGamesData: List<String> = getOngoingGamesData()
        var adapter = WelcomeAdapterCurrentGamesList(this, ongoingGamesData) { gameId ->
            openGame(gameId)
        }

        // Set the adapter to the RecyclerView
        binding.recyclerViewOngoingGames.adapter = adapter
        binding.recyclerViewOngoingGames.layoutManager = LinearLayoutManager(this)

        binding.textButtonNewGame.setOnClickListener{
            val intent = Intent(this, StartGameActivity::class.java)
            startActivity(intent)
        }

        binding.textButtonScoreboard.setOnClickListener{
            val intent = Intent(this, HighscoreActivity::class.java)
            startActivity(intent)
        }

        binding.textButtonInstructions.setOnClickListener{
            val intent = Intent(this, GameInstructionActivity::class.java)
            startActivity(intent)
        }

        binding.textButtonProfile.setOnClickListener{
            val intent = Intent(this, PlayerProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun openGame(gameId: String) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("GAME_ID", gameId)
        startActivity(intent)
    }

    private fun getOngoingGamesData(): List<String> {
        return listOf("Game 1", "Game 2", "Game 3")
    }
}
