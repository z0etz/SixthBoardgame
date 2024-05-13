package com.katja.sixthboardgame

import javax.security.auth.callback.Callback

class GameViewModel {

    val gameDao = GameDao()
    var game: Game = Game(UserDao(),mutableListOf("1", "2", "3"))
    fun loadGame(currentId: String?, opponentId: String?, gameId: String): Game {
        gameDao.fetchGameById(currentId, opponentId, gameId) { game: Game ->

            this.game = game
        }

        return this.game

        /*//TODO: Load existing game from Firebase, change return below to return that game
        return Game(listOf("1","2"))

         */
    }

    fun createNewGame(playerIdsList: List<String>): Game {
        return Game(UserDao(), playerIdsList)
    }


    fun getGameById(
        currentId: String?,
        opponentId: String?,
        gameId: String?,
        callback: (Game) -> Unit
    ) {
        gameDao.fetchGameById(currentId, opponentId, gameId) { game: Game ->

            callback(game)
        }
    }

    fun saveGame(game: Game) {

        gameDao.addGame(game)
    }

    fun loadGame2(currentId: String? , opponentId: String?): Game {
        var gameList = mutableListOf<Game>()
        gameDao.fetchGamesAgainstOpponent(currentId, opponentId) {

            gameList = it
        }


        return gameList.get(0)
    }
}