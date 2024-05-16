package com.katja.sixthboardgame

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.katja.sixthboardgame.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    lateinit var binding: ActivityWelcomeBinding
    lateinit var adapter: WelcomeAdapterCurrentGamesList
    private val ongoingGamesData = mutableListOf<String>()
    lateinit var firestore: FirebaseFirestore
    lateinit var currentUser: FirebaseUser

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

        firestore = FirebaseFirestore.getInstance()
        currentUser = FirebaseAuth.getInstance().currentUser!!

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
        val gamesData = mutableListOf<String>()

        // Fetch games data from Firestore
        firestore.collection("games")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val senderId = document.getString("senderId")
                    val receiverId = document.getString("receiverId")

                    // Check if the current user is either the sender or receiver
                    if (senderId == currentUser.uid || receiverId == currentUser.uid) {
                        // Add game ID or relevant data to the list
                        val gameId = document.id
                        gamesData.add(gameId)


                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnSuccessListener {
                Log.i("Success", "Success getting ongoing games data")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting ongoing games data: ", exception)
            }

        return gamesData
    }

    companion object {
        const val NEW_GAME_REQUEST = 1
    }
}
