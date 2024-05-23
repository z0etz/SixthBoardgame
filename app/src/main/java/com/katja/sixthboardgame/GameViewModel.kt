package com.katja.sixthboardgame

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import javax.security.auth.callback.Callback

class GameViewModel {

    val gameDao = GameDao()
    val userDao = UserDao()


    fun loadGameById(gameId: String, callback: (Game?) -> Unit) {
        gameDao.fetchGameById(gameId) { fetchedGame ->
            callback(fetchedGame)
        }
    }

    fun endGame(gameId: String, winnerId: String, loserId: String) {
        if (winnerId != "Unknown") {
            println("$winnerId won")
            userDao.updateUserScoreById(winnerId, 1) { success ->
                if (success) {
                    println("Score incremented successfully.")
                    userDao.fetchUserScoreById(winnerId) { score ->
                        if (score != null) {
                            println("Winner's new total score is $score")
                        } else {
                            println("Failed to fetch new score")
                        }
                    }
                } else {
                    println("Failed to increment score.")
                }
            }
        }
        if (loserId != "Unknown") {
            println("$loserId lost")
            userDao.updateUserScoreById(loserId, -1) { success ->
                if (success) {
                    println("Score decremented successfully.")
                    userDao.fetchUserScoreById(loserId) { score ->
                        if (score != null) {
                            println("Loser's new total score is $score")
                        } else {
                            println("Failed to fetch new score")
                        }
                    }
                } else {
                    println("Failed to decrement score.")
                }
            }
        }

        println("Attempting to delete game with ID: $gameId")
        gameDao.removeGameFromFirebase(gameId, object : GameDeletionCallback {
            override fun onGameDeleted(success: Boolean) {
                if (success) {
                   // refreshGameList()
                } else {
                    println("Failed to delete the game from Firebase.")
                }
            }
        })
    }
}