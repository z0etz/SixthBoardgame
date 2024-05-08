package com.katja.sixthboardgame


class GameViewModel(private val userDao: UserDao) {

    fun loadGame(gameId: String, playerIdsList: List<String>): Game {
        //TODO: Load existing game from Firebase, change return below to return that game
        return Game(userDao, gameId, playerIdsList)
    }

    fun createNewGame(playerIdsList: List<String>): Game {
        return Game(userDao, playerIdsList)
    }
}

