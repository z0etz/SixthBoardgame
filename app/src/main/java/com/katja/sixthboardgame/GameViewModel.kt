package com.katja.sixthboardgame

import Game
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import javax.security.auth.callback.Callback

class GameViewModel {

    val gameDao = GameDao()
    val userDao = UserDao()
    var game: Game = Game(UserDao(),mutableListOf("1", "2", "3"))

    fun createNewGame(playerIdsList: List<String>): Game {
        val game = Game(UserDao(), playerIdsList)
        gameDao.addGame(game)

        return game
    }

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
    
        fun loadGame(playerIds: List<String>): Game {

        //TODO: Load existing game from Firebase, change return below to return that game
        return Game(UserDao(), listOf("1","2"))

    }

    fun loadGameById(gameId: String, callback: (Game?) -> Unit) {
        gameDao.fetchGameById(gameId) { fetchedGame ->
            callback(fetchedGame)
        }
    }

    fun saveGame(game: Game) {

        gameDao.addGame(game)
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