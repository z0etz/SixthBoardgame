package com.katja.sixthboardgame

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import javax.security.auth.callback.Callback

class GameViewModel {

    val gameDao = GameDao()
    val userDao = UserDao()

    fun loadUserOpponentGames(currentId: String?, opponentId: String?): MutableList<Game> {
        var gameList = mutableListOf<Game>()

        gameDao.fetchGamesAgainstOpponent(currentId, opponentId) {

            gameList = it
        }


        return gameList
    }

    fun loadAllUserGames(currentId: String?): MutableList<Game> {
        var gameList = mutableListOf<Game>()

        gameDao.fetchAllCurrentUserGames(currentId) {

            gameList = it
        }


        return gameList
    }


//    fun getGameById(gameId: String?): Game{
//
//        var game = Game(UserDao(), listOf("1", "2"))
//
//        gameDao.fetchGameById(gameId) { fetchedGame ->
//
//            game = fetchedGame
//        }
//
//        return game
//    }
    
//        fun loadGame(playerIds: List<String>): Game {
//
//        //TODO: Remove once p vs. p works
//        return Game(UserDao(), listOf("1","2"))
//
//    }

    fun loadGameById(gameId: String, callback: (Game?) -> Unit) {
        gameDao.fetchGameById(gameId) { fetchedGame ->
            callback(fetchedGame)
        }
    }

    fun endGame(winnerId: String, loserId: String ) {
        if (winnerId != "Unknown") {
            println("$winnerId won")
            userDao.updateUserScoreById(winnerId, 1) { success ->
                if (success) {
                    println("Score incremented successfully.")
                    userDao.fetchUserScoreById(winnerId) { score ->
                        if (score != null) {
                            println("Winners new total score is $score")
                        } else {
                            println("Failed to fetch new score")
                        }
                    }
                } else {
                    println("Failed to decrement score.")
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
                            println("Losers new total score is $score")
                        } else {
                            println("Failed to fetch new score")
                        }
                    }
                } else {
                    println("Failed to decrement score.")
                }
            }
        }

        //TODO: Make sure the the finished game does not show up in current games lists anymore
    }
}