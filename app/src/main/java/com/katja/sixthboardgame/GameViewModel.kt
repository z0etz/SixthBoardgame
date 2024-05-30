package com.katja.sixthboardgame

class GameViewModel {

    private val gameDao = GameDao()
    private val userDao = UserDao()


    fun loadGameById(gameId: String, callback: (Game?) -> Unit) {
        gameDao.fetchGameById(gameId) { fetchedGame ->
            callback(fetchedGame)
        }
    }

    fun endGame(gameId: String, winnerId: String, loserId: String ) {
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
       gameDao.removeGameFromFirebase(gameId)
    }
}