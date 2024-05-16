package com.katja.sixthboardgame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.katja.sixthboardgame.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    lateinit var binding: ActivityWelcomeBinding
    lateinit var adapter: WelcomeAdapterCurrentGamesList
    private val ongoingGamesData = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = WelcomeAdapterCurrentGamesList(this, ongoingGamesData) { gameId ->
            openGame(gameId)
        }

        binding.recyclerViewOngoingGames.adapter = adapter
        binding.recyclerViewOngoingGames.layoutManager = LinearLayoutManager(this)

        binding.textButtonNewGame.setOnClickListener{
            val intent = Intent(this, StartGameActivity::class.java)
            startActivityForResult(intent, NEW_GAME_REQUEST)
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

        // Load ongoing games data initially
        loadOngoingGamesData()
    }

    private fun openGame(gameId: String) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("GAME_ID", gameId)
        startActivity(intent)
    }

    private fun loadOngoingGamesData() {
        ongoingGamesData.addAll(getOngoingGamesData())
        adapter.notifyDataSetChanged()
    }

    // Called after starting a new game to update ongoing games data
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == NEW_GAME_REQUEST && resultCode == RESULT_OK) {
            // Reload ongoing games data
            ongoingGamesData.clear()
            ongoingGamesData.addAll(getOngoingGamesData())
            adapter.notifyDataSetChanged()
        }
    }

    private fun getOngoingGamesData(): List<String> {
        return listOf("Game 1", "Game 2", "Game 3")
    }

    companion object {
        const val NEW_GAME_REQUEST = 1
    }

}
