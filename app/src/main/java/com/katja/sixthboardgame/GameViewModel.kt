package com.katja.sixthboardgame

import javax.security.auth.callback.Callback

class GameViewModel {

    val gameDao = GameDao()
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

        gameDao.fetchAllUserGmes(currentId) {

            gameList = it
        }


        return gameList
    }


    fun getGameById(gameId: String?): Game{

        var game = Game(UserDao(), listOf("1", "2"))

        gameDao.fetchGameById(gameId) { fetchedGame ->

            game = fetchedGame
        }

        //TODO: Load existing game from Firebase, change return below to return that game

        return game
    }

    fun saveGame(game: Game) {

        gameDao.addGame(game)
    }
}